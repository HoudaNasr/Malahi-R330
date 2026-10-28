package com.rpay.v2.core.utils


import java.io.ByteArrayOutputStream

/**
 * One frame of the WizarPOS PAYwizard ECR link.
 *
 * STX | VERSION | CTRL(type, 0x00, sequence high, sequence low) |
 * LEN(high, low) | CONTENT | ETX | BCC
 *
 * BCC is the XOR of bytes from VERSION through ETX.
 */
class Frame(
    type: Int,
    sequence: Int,
    content: ByteArray?
) {
    val type: Int = type and 0xFF
    val sequence: Int = sequence and 0xFFFF
    val content: ByteArray = content ?: byteArrayOf()

    fun encode(): ByteArray {
        require(content.size <= 0xFFFF) { "Frame content too long" }

        val output = ByteArrayOutputStream(content.size + FRAME_OVERHEAD)
        output.write(STX.toInt())
        output.write(VERSION.toInt())
        output.write(type)
        output.write(0x00)
        output.write(sequence ushr 8)
        output.write(sequence and 0xFF)
        output.write(content.size ushr 8)
        output.write(content.size and 0xFF)
        output.write(content)
        output.write(ETX.toInt())

        val frameWithoutBcc = output.toByteArray()
        return frameWithoutBcc + bcc(
            data = frameWithoutBcc,
            offset = 1,
            length = frameWithoutBcc.size - 1
        )
    }

    companion object {
        const val STX: Byte = 0x02
        const val ETX: Byte = 0x03
        const val VERSION: Byte = 0x02

        const val DATA_FROM_CASHIER = 0x01
        const val DATA_FROM_POS = 0x02
        const val ACK_FROM_CASHIER = 0x03
        const val HANDSHAKE_REQUEST = 0xF1
        const val HANDSHAKE_RESPONSE = 0xF2
        const val CANCEL_REQUEST = 0xC1
        const val CANCEL_RESPONSE = 0xC2

        private const val FRAME_OVERHEAD = 10

        fun bcc(data: ByteArray, offset: Int, length: Int): Byte {
            var checksum: Byte = 0
            for (index in offset until offset + length) {
                checksum = (checksum.toInt() xor data[index].toInt()).toByte()
            }
            return checksum
        }

        fun typeName(type: Int): String =
            when (type) {
                DATA_FROM_CASHIER -> "request"
                DATA_FROM_POS -> "response"
                ACK_FROM_CASHIER -> "ack"
                HANDSHAKE_REQUEST -> "handshake"
                HANDSHAKE_RESPONSE -> "handshake-reply"
                CANCEL_REQUEST -> "cancel"
                CANCEL_RESPONSE -> "cancel-reply"
                else -> "0x%02X".format(type)
            }
    }
}