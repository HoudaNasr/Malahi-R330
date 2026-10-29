package com.rpay.v2.features.kiosk.presentation

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.rpay.v2.core.base.BaseViewModel
import com.rpay.v2.core.utils.FileLogger
import com.rpay.v2.core.utils.enums.TransactionTypeLookup
import com.rpay.v2.data.repository.AppPreferencesRepository
import com.rpay.v2.domain.machine.OutOfServiceReason
import com.rpay.v2.domain.transactions.HandlePaymentApprovalUseCase
import com.rpay.v2.features.pay.domain.PaymentData
import com.rpay.v2.core.payment.RefactoredNearPayManager
import com.rpay.v2.core.utils.Frame
import com.rpay.v2.core.utils.FrameParser
import com.rpay.v2.domain.config.UiConfigProvider
import com.rpay.v2.domain.kiosk.TransportData
import com.rpay.v2.domain.model.QrDiscountType
import com.rpay.v2.features.pay.domain.PaymentResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.min

@HiltViewModel
class KioskScreenViewModel @Inject constructor(
    private val context: Context,
    private val prefsRepo: AppPreferencesRepository,
    private val handlePaymentApprovalUseCase: HandlePaymentApprovalUseCase,
    private val nearPayManager: RefactoredNearPayManager,
    private val uiConfigProvider: UiConfigProvider,
    private val transport: TransportData,
) : BaseViewModel<KioskScreenContract.State, KioskScreenContract.Intent>(
    KioskScreenContract.State()
) {
    private var TAG: String = "KioskScreenViewModel"
    private val _uiEffects = MutableSharedFlow<KioskScreenContract.UiEffect>()
    val uiEffects = _uiEffects.asSharedFlow()

    private val _incomingData = MutableSharedFlow<ByteArray>(
        extraBufferCapacity = 16
    )
    private val _usbConnected = MutableStateFlow(false)
    val usbConnected: StateFlow<Boolean> = _usbConnected.asStateFlow()

    data class IncomingPaymentRequest(
        val reference: String,
        val amountMinor: Long,
        val currencyCode: String,
        val transactionType: String,
        val sequence: Int
    )

    /**
     * Initializes the ViewModel.
     *
     * - Starts MDB hardware initialization
     * - Begins observing hardware state changes
     * - Loads persisted system configuration and initial UI state
     *
     * This block is executed once when the ViewModel is created.
     */
    init {
        observeNearPayEvents()
        viewModelScope.launch {
            loadPosAvailability()
        }
        viewModelScope.launch {
            loadInitialState()
        }
        viewModelScope.launch(Dispatchers.IO) {
            listen()
        }
    }

    /**
     * Handles all UI intents coming from the presentation layer.
     */
    override fun handleIntent(intent: KioskScreenContract.Intent) {
        when (intent) {
            is KioskScreenContract.Intent.Init -> {
                viewModelScope.launch { loadInitialState() }
            }


            is KioskScreenContract.Intent.LogoLongPressed -> {
                _state.value = _state.value.copy(
                    showPasswordDialog = true, passwordError = null
                )
            }

            is KioskScreenContract.Intent.PasswordDialogDismissed -> {
                _state.value = _state.value.copy(
                    showPasswordDialog = false, passwordError = null
                )
            }

            is KioskScreenContract.Intent.PasswordSubmitted -> {
                validatePassword(intent.value)
            }

            is KioskScreenContract.Intent.PaymentApproved -> {
                _state.update {
                    it.copy(
                        isProcessing = false,
                    )
                }
                onPaymentApproved(
                    intent.data!!, intent.transactionTypeLookup
                )
            }

            is KioskScreenContract.Intent.PaymentFailed -> {
                _state.update {
                    it.copy(
                        isProcessing = false,
                    )
                }
                onPaymentFailed(intent.value.reason)
            }

            is KioskScreenContract.Intent.TidReceived -> {
                updateDeviceTid(intent.tid)
            }

            is KioskScreenContract.Intent.ClearDialog -> {
                clearDialog()
            }

            KioskScreenContract.Intent.SessionTimeOut -> {
            }
        }
    }

    /**
     * Loads persisted device information and determines initial UI state.
     */
    private suspend fun loadInitialState() {
        val versionName = prefsRepo.getVersionName()
        val terminalSN = prefsRepo.getTerminalSn()
        val tid = prefsRepo.getDeviceTid()
        _state.update {
            it.copy(
                appVersion = versionName,
                deviceSn = terminalSN,
                deviceTid = tid,
                uiScale = uiConfigProvider.uiMode
            )
        }
        if (tid.isBlank()) {
            refreshTidFromNearPay()
        }
    }

    /**
     * Loads the pos availabilty state (out of service or out of subscription).
     */
    private suspend fun loadPosAvailability() {
        combine(
            prefsRepo.observableSystemOutService(),
            prefsRepo.isSubscriptionValid(),
            usbConnected
        ) { isSystemOutService, isSubscriptionValid, isUsbConnected ->
            when {
                isSystemOutService -> OutOfServiceReason.MAINTENANCE
                !isSubscriptionValid -> OutOfServiceReason.SUBSCRIPTION_EXPIRED
                !isUsbConnected -> OutOfServiceReason.NOT_CONNECTED
                else -> null
            }
        }.collect { reason ->
            _state.update {
                it.copy(
                    outOfServiceReason = reason
                )
            }
        }
    }

    private fun observeNearPayEvents() {

        viewModelScope.launch {

            nearPayManager.paymentEvents.collect { paymentResult ->

                when (paymentResult) {

                    is PaymentResult.Success -> {

                        if (paymentResult.data?.transactionType == TransactionTypeLookup.Purchase) {

                            FileLogger.log(
                                context, "NearPay", "PaymentResult.Success"
                            )

                            onIntent(
                                KioskScreenContract.Intent.PaymentApproved(
                                    paymentResult.data, TransactionTypeLookup.Purchase
                                )
                            )

                        } else {

//                            onIntent(
//                                KioskScreenContract.Intent.onGetReversalApprovedIntent(
//                                    paymentData = paymentResult.data!!,
//                                    transactionType = paymentResult.data.transactionType
//                                )
//                            )
                        }
                    }

                    is PaymentResult.Failed -> {

                        if (paymentResult.transactionType == TransactionTypeLookup.Purchase) {

                            onIntent(
                                KioskScreenContract.Intent.PaymentFailed(
                                    paymentResult
                                )
                            )

                        } else {

//                            onIntent(
//                                KioskScreenContract.Intent.onGetReversalFailedIntent(
//                                    paymentResult.reason
//                                )
//                            )
                        }
                    }
                }
            }
        }
    }


    fun getNearPayTid() {
        nearPayManager.getTid { result ->
            result?.let {
                onIntent(
                    KioskScreenContract.Intent.TidReceived(it)
                )
            }
        }
    }


    /**
     * Clears any active dialog from the UI by resetting the dialog state.
     */
    fun clearDialog() {
        _state.update {
            it.copy(dialogState = KioskScreenContract.DialogState.None)
        }
    }

    /**
     * Emits a one-time toast message to the UI.
     */
    private fun emitToast(toastMessage: String) {
        viewModelScope.launch {
            _uiEffects.emit(
                KioskScreenContract.UiEffect.ShowToast(
                    toastMessage
                )
            )
        }
    }

    /**
     * Requests the device TID from NearPay.
     *
     * Emits a one-time UI effect that instructs the UI layer
     * to fetch the Terminal ID from the payment provider.
     */
    private suspend fun refreshTidFromNearPay() {
        _uiEffects.emit(
            KioskScreenContract.UiEffect.getTid
        )
    }


    /**
     * Updates the device TID both in local storage and UI state.
     * Called when a new TID is received from the payment provider.
     */
    private fun updateDeviceTid(tid: String) {
        viewModelScope.launch {
            prefsRepo.setDeviceTid(tid)
            _state.update {
                it.copy(
                    deviceTid = tid
                )
            }
        }
    }



    /**
     * Validates admin password and navigates to settings on success.
     */
    private fun validatePassword(enteredPass: String) {
        viewModelScope.launch {
            val stored = prefsRepo.getDevicePassword()

            if (enteredPass == stored) {
                _state.update { it.copy(showPasswordDialog = false) }
                _uiEffects.emit(KioskScreenContract.UiEffect.NavigateToSettings)
            } else {
                _state.update {
                    it.copy(passwordError = "Incorrect password")
                }
            }
        }
    }


    /**
     * Runs continuously until stopListening() is called.
     * Reopens the transport after read/open errors using a bounded backoff.
     */
    private val listenMutex = Mutex()
    @Volatile private var running = false

    fun stopListening() {
        running = false
        log("Stop requested; closing USB transport to unblock read")
        transport.close()
    }

    private fun createFrameParser(): FrameParser = FrameParser(object : FrameParser.Log {
        override fun line(message: String) {
            // Optional: forward to your app logger.
        }
    })


    /** Results of finished payments by TransIndexCode: resent on a repeated Purchase, answered on QueryTransaction. */
    private val completedPayments = object : LinkedHashMap<String, JSONObject>() {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, JSONObject>?) = size > 100
    }

    /** The listen loop and the NearPay callbacks both write; one frame at a time. */
    private val writeMutex = Mutex()

    // ───────────────────────────── listen ─────────────────────────────

    suspend fun listen() = listenMutex.withLock {
        if (running) return@withLock
        running = true

        var retryDelayMs = INITIAL_RETRY_MS

        try {
            while (currentCoroutineContext().isActive && running) {
                var handshakeCompleted = false

                try {
                    _usbConnected.value = false
                    log("Opening USB serial transport: ${transport.description()}")

                    transport.open()
                    log("USB serial port opened; waiting for PAYwizard handshake")

                    val parser = createFrameParser()
                    val buffer = ByteArray(READ_BUFFER_SIZE)

                    while (currentCoroutineContext().isActive && running) {
                        val byteCount = transport.read(buffer, READ_TIMEOUT_MS)

                        // A timeout means no data arrived yet; keep listening.
                        if (byteCount <= 0) continue

                        val frames = parser.feed(buffer, 0, minOf(byteCount, buffer.size))

                        for (frame in frames) {
                            log(
                                "Received frame: type=${Frame.typeName(frame.type)}, " +
                                        "sequence=${frame.sequence}, contentLength=${frame.content.size}"
                            )

                            if (frame.type == Frame.HANDSHAKE_REQUEST) {
                                log("Sending PAYwizard handshake response")
                                sendFrame(Frame(Frame.HANDSHAKE_RESPONSE, frame.sequence + 1, frame.content))

                                handshakeCompleted = true
                                _usbConnected.value = true
                                retryDelayMs = INITIAL_RETRY_MS
                                log("PAYwizard handshake completed")
                            } else {
                                processFrame(frame)
                            }
                        }
                    }
                } catch (cancelled: CancellationException) {
                    log("Listening cancelled")
                    throw cancelled
                } catch (error: Exception) {
                    _usbConnected.value = false
                    log("USB/PAYwizard connection error: ${error.message}")
                } finally {
                    _usbConnected.value = false
                    log("Closing USB transport (handshakeCompleted=$handshakeCompleted)")
                    transport.close()
                }

                if (running && currentCoroutineContext().isActive) {
                    log("Retrying USB connection in ${retryDelayMs}ms")
                    delay(retryDelayMs)
                    retryDelayMs = (retryDelayMs * 2).coerceAtMost(MAX_RETRY_MS)
                }
            }
        } finally {
            running = false
            _usbConnected.value = false
            log("Listener stopped; closing USB transport")
            transport.close()
        }
    }

    // ───────────────────────────── frame routing ─────────────────────────────

    /** Every frame except the handshake. Every request gets exactly one reply, so the kiosk never times out. */
    private suspend fun processFrame(frame: Frame) {
        when (frame.type) {
            Frame.DATA_FROM_CASHIER -> handleRequest(frame)
            Frame.CANCEL_REQUEST -> handleCancel(frame)
            Frame.ACK_FROM_CASHIER -> log("ACK from kiosk (seq=${frame.sequence})")
            else -> log("Ignored frame type ${Frame.typeName(frame.type)}")
        }
    }

    private suspend fun handleRequest(frame: Frame) {
        val json = try {
            if (frame.content.isEmpty()) JSONObject() else JSONObject(String(frame.content, UTF8))
        } catch (e: org.json.JSONException) {
            log("Bad JSON from kiosk: ${e.message}")
            reply(frame, base(null, null).fail("SYSTEM_ERROR", "INVALID_REQUEST", "Request is not valid JSON"))
            return
        }
        log("Request JSON (seq=${frame.sequence}): $json")

        val type = json.getStringOrNull("TransType")
        when {
            type.equals("GetPosInfo", ignoreCase = true) -> handleGetPosInfo(frame)
            type.equals("Purchase", ignoreCase = true) ||
                    type.equals("Sale", ignoreCase = true) -> handlePurchase(frame, json, type!!)
            type.equals("QueryTransaction", ignoreCase = true) -> handleQuery(frame, json)
            else -> {
                log("Unsupported request: $type")
                reply(
                    frame,
                    base(type, json.getStringOrNull("TransIndexCode"))
                        .fail("SYSTEM_ERROR", "UNSUPPORTED_TRANS_TYPE", "Transaction type not supported: $type")
                )
            }
        }
    }

    // ───────────────────────────── GetPosInfo ─────────────────────────────

    private suspend fun handleGetPosInfo(frame: Frame) {
        val s = _state.value
        val inTransaction = s.isProcessing && s.lastPaymentRequest != null
        val sn = s.deviceSn.ifBlank { prefsRepo.getTerminalSn() }
        val tid = s.deviceTid.ifBlank { prefsRepo.getDeviceTid() }
        val body = base("GetPosInfo", null).apply {
            put("TransResult", true)
            put("RespCode", "00")
            put("RespDesc", "OK")
            put("PosStatus", if (inTransaction) "InTransaction" else "Idle")
            put("PaymentAppInstalled", true)
            put("PaymentConfigured", nearPayManager.isInitialized())
            put("PosNetworkAvailable", isNetworkAvailable())
            put("HasMSR", true)
            // The kiosk service reads TID as the device id; SN is shown by newer service builds.
            if (sn.isNotBlank()) put("TID", tid)
            if (sn.isNotBlank()) put("SN", sn)
            put("TestMode", false)
        }
        reply(frame, body)
    }

    // ───────────────────────────── Purchase ─────────────────────────────

    private suspend fun handlePurchase(frame: Frame, json: JSONObject, type: String) {
        val reference = json.getStringOrNull("TransIndexCode")
        if (reference == null) {
            reply(frame, base(type, null).fail("SYSTEM_ERROR", "MISSING_TRANS_INDEX_CODE", "TransIndexCode is required"))
            return
        }

        // Same reference already finished: resend the stored result, never charge twice.
        completedPayments[reference]?.let { stored ->
            log("Repeated payment $reference: resending stored result (not charged twice)")
            reply(frame, stored)
            return
        }

        val current = _state.value.lastPaymentRequest
        if (_state.value.isProcessing && current != null) {
            if (current.reference == reference) {
                log("Payment $reference is already in progress; result will be sent when it finishes")
                return
            }
            reply(
                frame,
                base(type, reference).fail("SYSTEM_ERROR", "HW_ERR_TERMINAL_BUSY", "Terminal is busy with payment ${current.reference}")
            )
            return
        }

        val amountText = json.getStringOrNull("TransAmount")
        val currency = json.getStringOrNull("CurrencyCode")
        val amountMinor = amountText
            ?.takeIf { it.matches(Regex("[0-9]{1,12}")) }
            ?.toLongOrNull()
            ?.takeIf { it > 0 }
        if (amountMinor == null || currency == null) {
            reply(
                frame,
                base(type, reference).fail(
                    "SYSTEM_ERROR", "INVALID_AMOUNT",
                    "TransAmount must be a positive whole number of minor units and CurrencyCode is required"
                )
            )
            return
        }

        val paymentRequest = IncomingPaymentRequest(
            reference = reference,
            amountMinor = amountMinor,
            currencyCode = currency,
            transactionType = type,
            sequence = frame.sequence
        )
        _state.update { it.copy(lastPaymentRequest = paymentRequest, isProcessing = true) }
        log("Starting NearPay payment: $paymentRequest")

        try {
            val receiptWaitingDuration = prefsRepo.getPaymentWaitDuration().toLong()
            withContext(Dispatchers.Main) {
                nearPayManager.makePayment(
                    amount = paymentRequest.amountMinor,
                    finishTimeOut = receiptWaitingDuration
                )
                _state.update {
                    it.copy(isProcessing = true)
                }
            }
        } catch (e: Exception) {
            _state.update {
                it.copy(isProcessing = false)
            }
            log("Could not start NearPay payment: ${e.message}")
            finishPayment(paymentRequest, buildFailedResponse(paymentRequest, "Could not start the payment: ${e.message}"))
        }
    }

    // ───────────────────────────── QueryTransaction ─────────────────────────────

    private suspend fun handleQuery(frame: Frame, json: JSONObject) {
        val reference = json.getStringOrNull("OriTransIndexCode")
        val stored = reference?.let { completedPayments[it] }
        val current = _state.value.lastPaymentRequest

        val body = when {
            stored != null ->
                JSONObject(stored.toString()).put("TransType", "QueryTransaction")

            reference != null && _state.value.isProcessing && current?.reference == reference ->
                // Still running: "outcome not known yet" keeps the payment open on the kiosk.
                base("QueryTransaction", reference)
                    .fail("SYSTEM_ERROR", "ERROR_TIMEOUT", "Payment still in progress on the terminal")

            else ->
                base("QueryTransaction", reference)
                    .fail("TERMINAL_ERROR", "HW_ERR_TXN_NOT_FOUND", "No payment with this reference on the terminal")
        }
        log("Query $reference -> ${body.optString("UnifiedCode")}")
        reply(frame, body)
    }

    // ───────────────────────────── Cancel ─────────────────────────────

    /** NearPay can't be stopped safely from here, so answer "not cancelled"; the payment result still follows. */
    private suspend fun handleCancel(frame: Frame) {
        val current = _state.value.lastPaymentRequest
        log(
            if (current == null) "Cancel request: no payment in progress"
            else "Cancel request for ${current.reference}: cannot be cancelled now"
        )
        val body = JSONObject().put("cancelled", false)
        sendFrame(Frame(Frame.CANCEL_RESPONSE, frame.sequence + 1, body.toString().toByteArray(UTF8)))
    }

    // ───────────────────────────── NearPay results ─────────────────────────────

    fun onPaymentApproved(paymentData: PaymentData, transactionType: TransactionTypeLookup) {
        val request = state.value.lastPaymentRequest ?: run {
            log("onPaymentApproved with no kiosk request waiting")
            return
        }
        val receipt = paymentData.transactionData?.receipts?.firstOrNull()
        val transactionUuid = receipt?.transaction_uuid
        val authCode = receipt?.approval_code?.value

        _state.update { it.copy(transactionId = transactionUuid ?: it.transactionId , isProcessing = false) }
        log("onPaymentApproved ref=${request.reference} uuid=$transactionUuid auth=$authCode")

        viewModelScope.launch {
            val response = base(request.transactionType, request.reference).apply {
                put("TransAmount", request.amountMinor.toString())
                put("CurrencyCode", request.currencyCode)
                put("TransResult", true)
                put("RespCode", "00")
                put("RespDesc", "Approved")
                put("UnifiedCategory", "APPROVED")
                put("UnifiedCode", "APPROVED")
                transactionUuid?.let { put("TransID", it) }
                authCode?.let { put("AuthCode", it) }
            }
            finishPayment(request, response)
        }
        viewModelScope.launch {
            val transactionUseCaseResult = handlePaymentApprovalUseCase(
                paymentData = paymentData,
                giftCount = null,
                slotCode = null,
                transactionType = transactionType,
                discountType = QrDiscountType.None,
                onTidUpdated = { tid -> updateDeviceTid(tid) }
            )

            handleTransactionResult(
                result = transactionUseCaseResult,
                emitToast = ::emitToast
            )
        }

    }

    fun onPaymentFailed(message: String?) {
        val request = state.value.lastPaymentRequest ?: run {
            log("onPaymentFailed with no kiosk request waiting")
            return
        }
        _state.update { it.copy(errorMessage = message , isProcessing = false) }
        log("onPaymentFailed ref=${request.reference}: $message")

        viewModelScope.launch {
            val response = when (message) {
                "declined", "rejected" -> base(request.transactionType, request.reference).apply {
                    put("TransAmount", request.amountMinor.toString())
                    put("CurrencyCode", request.currencyCode)
                    put("RespCode", "05")
                }.fail("HARD_DECLINE", "DECLINED", message)

                else -> buildFailedResponse(request, message ?: "Payment failed")
            }
            finishPayment(request, response)
        }
    }

    /**
     * Handles the result of a transaction submission.
     *
     * Logs the outcome and notifies the UI layer with a user-facing message,
     * whether the transaction succeeds or fails.
     *
     * @param result Result returned from the transaction use case
     * @param context Context used for logging
     * @param emitToast Callback used to emit toast messages to the UI
     */
    private suspend fun handleTransactionResult(
        result: Result<UUID>,
        emitToast: suspend (String) -> Unit
    ) {
        result
            .onSuccess { transactionId ->
                emitToast("Transaction successfully sent to backend: $transactionId")
            }
            .onFailure { throwable ->
                val errorMessage = throwable.message ?: "Transaction failed"
                emitToast("Transaction Failure: $errorMessage")
            }
    }

    private fun buildFailedResponse(request: IncomingPaymentRequest, reason: String): JSONObject =
        base(request.transactionType, request.reference).apply {
            put("TransAmount", request.amountMinor.toString())
            put("CurrencyCode", request.currencyCode)
        }.fail("SYSTEM_ERROR", "PAYMENT_APP_ERROR", reason)

    /** Store the result (for repeats and QueryTransaction), free the terminal, send the reply. */
    private suspend fun finishPayment(request: IncomingPaymentRequest, response: JSONObject) {
        completedPayments[request.reference] = response
        _state.update { it.copy(isProcessing = false, lastPaymentRequest = null) }
        try {
            sendFrame(
                Frame(Frame.DATA_FROM_POS, request.sequence + 1, response.toString().toByteArray(UTF8))
            )
            log("Payment result sent for ${request.reference}: $response")
        } catch (e: Exception) {
            log("Result for ${request.reference} not sent now (${e.message}); kiosk will get it via QueryTransaction")
        }
    }

    // ───────────────────────────── helpers ─────────────────────────────

    private suspend fun reply(request: Frame, body: JSONObject) {
        log("Reply (seq=${request.sequence + 1}): $body")
        sendFrame(Frame(Frame.DATA_FROM_POS, request.sequence + 1, body.toString().toByteArray(UTF8)))
    }

    private suspend fun sendFrame(frame: Frame) = withContext(Dispatchers.IO) {
        writeMutex.withLock { transport.write(frame.encode()) }
    }

    /** TransType, TransIndexCode, TransDate, TransTime — the fields every reply carries. */
    private fun base(type: String?, reference: String?): JSONObject = JSONObject().apply {
        type?.let { put("TransType", it) }
        reference?.let { put("TransIndexCode", it) }
        val now = Date()
        put("TransDate", SimpleDateFormat("yyyyMMdd", Locale.US).format(now))
        put("TransTime", SimpleDateFormat("HHmmss", Locale.US).format(now))
    }

    private fun JSONObject.fail(category: String, code: String, description: String): JSONObject = apply {
        put("TransResult", false)
        if (!has("RespCode")) put("RespCode", code)
        put("RespDesc", description)
        put("UnifiedCategory", category)
        put("UnifiedCode", code)
    }

    /** Case-insensitive key lookup, blank → null (same as the demo). */
    private fun JSONObject.getStringOrNull(name: String): String? {
        val keys = keys()
        while (keys.hasNext()) {
            val key = keys.next()
            if (key.equals(name, ignoreCase = true)) {
                val value = opt(key)
                if (value == null || value == JSONObject.NULL) return null
                return value.toString().trim().takeIf(String::isNotEmpty)
            }
        }
        return null
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService(android.net.ConnectivityManager::class.java) ?: return false
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return caps.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }



//    private suspend fun processFrame(frame: Frame) {
//        log("ProcessFrame")
//        if (frame.type != Frame.DATA_FROM_CASHIER) return
//
//        try {
//            val json = JSONObject(String(frame.content, UTF8))
//            val type = json.getStringOrNull("TransType") ?: return
//
//            if (!type.equals("Purchase", ignoreCase = true) && !type.equals(
//                    "Sale",
//                    ignoreCase = true
//                )
//            ) {
//                log("not purchase and not sale")
//                return
//            }
//
//            val reference = json.getStringOrNull("TransIndexCode") ?: return
//            val amountText = json.getStringOrNull("TransAmount") ?: return
//            val currency = json.getStringOrNull("CurrencyCode") ?: return
//
//            // The demo protocol specifies a positive whole number of minor units.
//            if (!amountText.matches(Regex("[0-9]{1,12}"))) return
//            val amountMinor = amountText.toLongOrNull()?.takeIf { it > 0 } ?: return
//            val receiptWaitingDuration = prefsRepo.getPaymentWaitDuration().toLong()
//            val paymentRequest = IncomingPaymentRequest(
//                reference = reference,
//                amountMinor = amountMinor,
//                currencyCode = currency,
//                transactionType = type,
//                sequence = frame.sequence
//            )
//            log("paymentRequest is: ${paymentRequest.toString()}")
//            _state.update {
//                it.copy(
//                    lastPaymentRequest = paymentRequest
//                )
//            }
//            log(paymentRequest.toString())
//            _state.update { it.copy(
//                isProcessing = true
//            ) }
//            nearPayManager.makePayment(
//                amount = paymentRequest.amountMinor,
//                finishTimeOut = receiptWaitingDuration
//            )
//        } catch (exception: org.json.JSONException) {
//            _state.update { it.copy(
//                isProcessing = false
//            ) }
//            log(exception.message ?: "null JSONException")
//            // Bad JSON frame: ignore/log it and keep listening.
//        }
//    }

//    private fun JSONObject.getStringOrNull(name: String): String? {
//        val value = opt(name)
//        if (value == null || value == JSONObject.NULL) return null
//        return value.toString().trim().takeIf(String::isNotEmpty)
//    }

    private suspend fun sendApprovedResponse(
        request: IncomingPaymentRequest,
        nearPayTransactionId: String? = null,
        authCode: String? = null
    ) {
        log("sendApprovedResponse")
        val response = JSONObject().apply {
            put("TransType", request.transactionType)
            put("TransIndexCode", request.reference)
            put("TransAmount", request.amountMinor.toString())
            put("CurrencyCode", request.currencyCode)
            put("TransResult", true)
            put("RespCode", "00")
            put("RespDesc", "Approved")
            put("UnifiedCategory", "APPROVED")
            put("UnifiedCode", "APPROVED")
            nearPayTransactionId?.let { put("TransID", it) }
            authCode?.let { put("AuthCode", it) }
        }
        log("sendApprovedResponse ${request.toString()}")

        sendPaymentResponse(request, response)
    }

    private suspend fun sendDeclinedResponse(
        request: IncomingPaymentRequest,
        reason: String
    ) {
        val response = JSONObject().apply {
            put("TransType", request.transactionType)
            put("TransIndexCode", request.reference)
            put("TransAmount", request.amountMinor.toString())
            put("CurrencyCode", request.currencyCode)
            put("TransResult", false)
            put("RespCode", "05")
            put("RespDesc", reason)
            put("UnifiedCategory", "HARD_DECLINE")
            put("UnifiedCode", "DECLINED")
        }

        sendPaymentResponse(request, response)
    }

    private suspend fun sendPaymentResponse(
        request: IncomingPaymentRequest,
        response: JSONObject
    ) = withContext(Dispatchers.IO) {
        val responseFrame = Frame(
            type = Frame.DATA_FROM_POS,
            sequence = (request.sequence + 1) and 0xFFFF,
            content = response.toString().toByteArray(Charsets.UTF_8)
        )

        transport.write(responseFrame.encode())
    }

    private companion object {
        val UTF8: Charset = Charsets.UTF_8

        const val READ_BUFFER_SIZE = 1024
        const val READ_TIMEOUT_MS = 200
        const val INITIAL_RETRY_MS = 500L
        const val MAX_RETRY_MS = 5_000L
    }

    override fun onCleared() {
        stopListening()
        super.onCleared()
    }

    private fun log(message: String) {
        Log.d(TAG, "Log: $message")
        FileLogger.log(context = context , TAG , message)
    }

}
