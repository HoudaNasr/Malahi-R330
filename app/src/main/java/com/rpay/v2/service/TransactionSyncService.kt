// TransactionSyncService.kt
package com.rpay.v2.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.rpay.v2.data.datasource.local.TransactionDao
import com.rpay.v2.data.datasource.local.TransactionRepoDataBase
import com.rpay.v2.data.model.entity.TransactionEntity
import com.rpay.v2.data.request.TransactionRequest
import com.rpay.v2.R
import com.rpay.v2.core.utils.FileLogger
import com.rpay.v2.presentation.home.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.flow.first

import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val CHANNEL_ID = "transaction_sync_channel"
private const val NOTIF_ID = 42
private const val TAG = "TransactionSyncSvc"

@AndroidEntryPoint
class TransactionSyncService : Service() {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private val handler = Handler(Looper.getMainLooper())

    @Inject
    lateinit var dao: TransactionDao

    @Inject
    @ApplicationContext
    lateinit var context: Context

    @Inject
    lateinit var repo: TransactionRepoDataBase

    @SuppressLint("ForegroundServiceType")
    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIF_ID, buildNotification(0, 0))
        scope.launch { syncLoop() }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand() → START_STICKY")
        return START_STICKY
    }

    private suspend fun syncLoop() {
        while (isActive) {
            val pending: List<TransactionEntity> =
                dao.getAll().first().sortedBy { it.date }
            val total = pending.size
            var done = 0
            for (tx in pending) {
                Log.d(TAG, "Processing tx ${tx.id}")
                toast("Processing tx ${tx.id}")

                val ok = process(tx)
                if (ok) {
                    dao.delete(tx)
                    done++
                    toast("Synced tx ${tx.id}")
                } else {
                    toast("Failed tx ${tx.id}")
                }

                withContext(Dispatchers.Main) {
                    (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                        .notify(NOTIF_ID, buildNotification(done, total))
                }
                delay(100)
            }

            Log.d(TAG, "Iteration done: $done/$total. Sleeping 2m.")
            toast("Iteration done: $done/$total")
            delay(TimeUnit.MINUTES.toMillis(2))
        }
    }

    private fun process(item: TransactionEntity): Boolean = runBlocking {
        try {
            val request = TransactionRequest(
                amount = item.amount,
                transactionId = item.transactionId,
                transactionTypeLookup = item.transactionType,
                terminalSN = item.terminalSN,
                date = item.date,
                giftCount = item.giftCount,
                lat = item.latitude,
                lng = item.longitude,

                // 🔽 NEW fields carried to API
                slotCode = item.slotCode,
                bankCard = item.bankCard,
                quantity = item.quantity
            )
            val resp = repo.createTransaction(
                request
            )
            val ok = resp?.isSuccessful == true
            ok
        } catch (e: Exception) {
            false
        }
    }

    private fun buildNotification(progress: Int, max: Int) = run {
        ensureChannel()
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Syncing Transactions")
            .setContentText("Processed $progress / $max")
            .setSmallIcon(R.drawable.shape_msg)
            .setContentIntent(pendingIntent)
            .setProgress(max, progress, false)
            .setOngoing(true)
            .build()
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Transaction Sync", NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Shows sync progress" }
            (getSystemService(NotificationManager::class.java))
                .createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        Log.d(TAG, "onDestroy()")
        toast("Sync Service destroyed")
    }

    override fun onBind(intent: Intent?) = null

    private fun toast(msg: String) {
        handler.post { Toast.makeText(this, msg, Toast.LENGTH_SHORT).show() }
    }

    companion object {
        fun startService(context: Context) {
            val intent = Intent(context, TransactionSyncService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
