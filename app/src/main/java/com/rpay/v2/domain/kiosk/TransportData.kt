package com.rpay.v2.domain.kiosk

import java.io.IOException

/** A byte link to the kiosk PC: the Q3's USB slave serial port, or a TCP socket.  */
interface TransportData {
    fun description(): String?

    /** Opens the link, or waits until a peer connects (TCP). Throws if the port can't be opened.  */
    @Throws(IOException::class)
    fun open()

    /** Reads up to buffer.length bytes. Returns 0 if nothing arrived within timeoutMs. Throws when the link is lost.  */
    @Throws(IOException::class)
    fun read(buffer: ByteArray, timeoutMs: Int): Int

    @Throws(IOException::class)
    fun write(data: ByteArray)

    fun close()
}