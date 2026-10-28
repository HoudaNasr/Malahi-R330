package com.rpay.v2.data.kiosk

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbAccessory
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.util.Log
import com.rpay.v2.domain.kiosk.TransportData
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.TimeUnit

/**
 * USB cable link to the kiosk PC on the R330, using Android Open Accessory (AOA) mode.
 *
 * The R330 SDK has no serial-port API, so the cable works the other way round:
 * the kiosk PC is the USB host, switches the R330 into accessory mode (USB ID 18D1:2D00)
 * and announces itself as manufacturer "RPay", model "R330 Link" (res/xml/accessory_filter.xml).
 * The accessory is then a file: bytes from the PC come in on an input stream, replies go out on an output stream.
 * Standard Android, so it works with USB debugging off.
 *
 * Same contract as the Q3's CloudposSerialTransportData, so the PAYwizard code in the ViewModel is unchanged:
 * open() / read(buffer, timeoutMs) returning 0 on timeout / write() / close().
 *
 * Logs under "UsbAccessoryTransport". Filter with: adb logcat -s UsbAccessoryTransport
 */
class UsbAccessoryTransportData(
    context: Context
) : TransportData {

    private val appContext = context.applicationContext
    private val usbManager = appContext.getSystemService(Context.USB_SERVICE) as UsbManager?

    private val lock = Any()

    @Volatile
    private var fileDescriptor: ParcelFileDescriptor? = null

    @Volatile
    private var output: FileOutputStream? = null

    @Volatile
    private var readerThread: Thread? = null

    /** Chunks read from the accessory by the reader thread; read() takes them with a timeout. */
    private val received: BlockingQueue<ByteArray> = ArrayBlockingQueue(QUEUE_CAPACITY)

    /** Part of the last chunk that didn't fit in the caller's buffer. */
    private var pending: ByteArray? = null
    private var pendingPos = 0

    private var lastPermissionRequestAt = 0L
    private var openCount = 0

    @Volatile
    private var totalBytesIn = 0L

    @Volatile
    private var totalBytesOut = 0L

    override fun description(): String = "USB accessory"

    // ─────────────────────────── open ───────────────────────────

    @Throws(IOException::class)
    override fun open() {
        synchronized(lock) {
            openLocked()
        }
    }

    private fun openLocked() {
        openCount++
        Log.i(TAG, "open #$openCount requested on thread '${Thread.currentThread().name}'")

        // Close our own previous handle, if any, before opening again.
        if (fileDescriptor != null) {
            Log.w(TAG, "open #$openCount: accessory still open, closing it first")
            closeLocked("before open #$openCount")
        }

        val manager = usbManager ?: run {
            Log.e(TAG, "open #$openCount failed: no UsbManager on this device")
            throw IOException("USB service not available")
        }

        val accessory = findKioskAccessory(manager) ?: run {
            Log.d(TAG, "open #$openCount: no kiosk accessory yet (PC hasn't switched the cable to accessory mode)")
            throw IOException("USB accessory: waiting for the kiosk PC to connect over the cable")
        }
        Log.i(TAG, "found accessory: ${accessory.manufacturer} / ${accessory.model} / version=${accessory.version}")

        if (!manager.hasPermission(accessory)) {
            requestPermission(manager, accessory)
            throw IOException("USB accessory: allow the app to use the kiosk PC on the terminal screen")
        }

        val descriptor = manager.openAccessory(accessory) ?: run {
            Log.e(TAG, "open #$openCount failed: openAccessory returned null (another app using it?)")
            throw IOException("USB accessory could not be opened (is another app using it?)")
        }

        received.clear()
        pending = null
        pendingPos = 0
        fileDescriptor = descriptor
        output = FileOutputStream(descriptor.fileDescriptor)
        startReader(FileInputStream(descriptor.fileDescriptor))

        Log.i(TAG, "open #$openCount OK: link ready")
    }

    private fun findKioskAccessory(manager: UsbManager): UsbAccessory? {
        val accessories = manager.accessoryList ?: return null
        accessories.forEach {
            Log.d(TAG, "accessory present: manufacturer=${it.manufacturer} model=${it.model}")
        }
        return accessories.firstOrNull {
            it.manufacturer == MANUFACTURER && it.model == MODEL
        }
    }

    private fun requestPermission(manager: UsbManager, accessory: UsbAccessory) {
        val now = SystemClock.elapsedRealtime()
        if (now - lastPermissionRequestAt < PERMISSION_RETRY_MS) {
            Log.d(TAG, "permission still pending; not asking again yet")
            return
        }
        lastPermissionRequestAt = now

        val intent = Intent(ACTION_USB_PERMISSION).setPackage(appContext.packageName)
        // UsbManager adds extras to this intent, so it must be mutable on Android 12+.
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
        manager.requestPermission(
            accessory,
            PendingIntent.getBroadcast(appContext, 0, intent, flags)
        )
        Log.i(TAG, "asked the user for USB accessory permission")
    }

    /**
     * The accessory stream has no read timeout: read() blocks until bytes arrive or the cable goes.
     * A background thread does the blocking reads and queues each chunk, so read(buffer, timeoutMs)
     * can return 0 on timeout like the CloudPOS serial port does.
     */
    private fun startReader(input: FileInputStream) {
        val thread = Thread({
            val buffer = ByteArray(READ_CHUNK_SIZE)
            try {
                while (!Thread.currentThread().isInterrupted) {
                    val count = input.read(buffer)
                    if (count < 0) {
                        Log.w(TAG, "reader: end of stream (cable unplugged or PC closed the accessory)")
                        break
                    }
                    if (count > 0) received.put(buffer.copyOf(count))
                }
            } catch (e: IOException) {
                Log.w(TAG, "reader: stream ended: ${e.message}")
            } catch (_: InterruptedException) {
                Log.d(TAG, "reader: interrupted")
            } finally {
                received.offer(END_OF_STREAM)
            }
        }, "usb-accessory-reader")
        thread.isDaemon = true
        readerThread = thread
        thread.start()
    }

    // ─────────────────────────── read ───────────────────────────

    @Throws(IOException::class)
    override fun read(buffer: ByteArray, timeoutMs: Int): Int {
        if (fileDescriptor == null) {
            Log.w(TAG, "read() called but accessory is closed")
            throw IOException("USB accessory closed")
        }

        val chunk: ByteArray = pending ?: run {
            val next = try {
                received.poll(timeoutMs.toLong(), TimeUnit.MILLISECONDS)
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
                return 0
            } ?: return 0                                   // nothing arrived within timeoutMs

            if (next === END_OF_STREAM) {
                Log.w(TAG, "read: link ended (in=$totalBytesIn out=$totalBytesOut)")
                throw IOException("USB accessory disconnected")
            }
            pending = next
            pendingPos = 0
            next
        }

        val count = minOf(buffer.size, chunk.size - pendingPos)
        System.arraycopy(chunk, pendingPos, buffer, 0, count)
        pendingPos += count
        if (pendingPos >= chunk.size) pending = null

        totalBytesIn += count
        Log.d(TAG, "RX $count bytes (total in=$totalBytesIn): ${hex(buffer, count)}")
        return count
    }

    // ─────────────────────────── write ───────────────────────────

    @Throws(IOException::class)
    override fun write(data: ByteArray) {
        val stream = output ?: run {
            Log.w(TAG, "write() called but accessory is closed (${data.size} bytes dropped)")
            throw IOException("USB accessory closed")
        }
        try {
            stream.write(data)
            stream.flush()
            totalBytesOut += data.size
            Log.d(TAG, "TX ${data.size} bytes (total out=$totalBytesOut): ${hex(data, data.size)}")
        } catch (e: IOException) {
            Log.e(TAG, "TX ${data.size} bytes FAILED: ${e.message}", e)
            throw IOException("USB accessory write failed: ${e.message}", e)
        }
    }

    // ─────────────────────────── close ───────────────────────────

    override fun close() {
        synchronized(lock) {
            closeLocked("close()")
        }
    }

    private fun closeLocked(reason: String) {
        val descriptor = fileDescriptor
        fileDescriptor = null
        output = null
        if (descriptor == null) {
            Log.d(TAG, "$reason: nothing to close")
            return
        }
        Log.i(TAG, "$reason: closing accessory (in=$totalBytesIn out=$totalBytesOut)")
        try {
            descriptor.close()                 // also unblocks the reader thread
        } catch (e: IOException) {
            Log.w(TAG, "$reason: close failed: ${e.message}")
        }
        readerThread?.interrupt()
        readerThread = null
    }

    // ─────────────────────────── helpers ───────────────────────────

    private fun hex(bytes: ByteArray, length: Int): String {
        val n = minOf(length, HEX_LOG_LIMIT)
        val sb = StringBuilder(n * 3)
        for (i in 0 until n) {
            if (i > 0) sb.append(' ')
            sb.append(String.format("%02X", bytes[i].toInt() and 0xFF))
        }
        if (length > n) sb.append(" … (+${length - n})")
        return sb.toString()
    }

    companion object {
        private const val TAG = "UsbAccessoryTransport"

        /** Must match res/xml/accessory_filter.xml and what the kiosk service announces. */
        const val MANUFACTURER = "RPay"
        const val MODEL = "R330 Link"

        const val ACTION_USB_PERMISSION = "com.rpay.v2.USB_ACCESSORY_PERMISSION"

        /** Accessory reads must ask for at least 16 KB or some Android versions drop data. */
        private const val READ_CHUNK_SIZE = 16 * 1024
        private const val QUEUE_CAPACITY = 256
        private const val PERMISSION_RETRY_MS = 30_000L
        private const val HEX_LOG_LIMIT = 64

        private val END_OF_STREAM = ByteArray(0)
    }
}
