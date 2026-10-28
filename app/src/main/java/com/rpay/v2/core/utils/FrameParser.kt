package com.rpay.v2.core.utils

class FrameParser(
    private val log: Log? = null
) {
    fun interface Log {
        fun line(message: String)
    }

    private var buffer = ByteArray(INITIAL_BUFFER_SIZE)
    private var length = 0

    fun reset() {
        length = 0
    }

    fun feed(data: ByteArray, offset: Int = 0, count: Int = data.size - offset): List<Frame> {
        require(offset >= 0 && count >= 0 && offset + count <= data.size) {
            "Invalid offset/count for input data"
        }

        ensureCapacity(length + count)
        System.arraycopy(data, offset, buffer, length, count)
        length += count

        val frames = mutableListOf<Frame>()
        var position = 0

        while (true) {
            while (position < length && buffer[position] != Frame.STX.toByte()) {
                position++
            }

            if (length - position < HEADER_SIZE) break

            val contentLength =
                ((buffer[position + 6].toInt() and 0xFF) shl 8) or
                        (buffer[position + 7].toInt() and 0xFF)

            val totalLength = HEADER_SIZE + contentLength + TRAILER_SIZE
            if (length - position < totalLength) break

            val endIndex = position + HEADER_SIZE + contentLength
            val checksumIndex = position + totalLength - 1

            val valid =
                buffer[position + 1] == Frame.VERSION.toByte() &&
                        buffer[endIndex] == Frame.ETX.toByte() &&
                        Frame.bcc(buffer, position + 1, totalLength - 2) ==
                        buffer[checksumIndex]

            if (!valid) {
                log?.line("Dropped a malformed frame ($contentLength bytes)")
                // Resume searching one byte after this frame's STX.
                position++
                continue
            }

            val content = buffer.copyOfRange(
                position + HEADER_SIZE,
                position + HEADER_SIZE + contentLength
            )

            val type = buffer[position + 2].toInt() and 0xFF
            val sequence =
                ((buffer[position + 4].toInt() and 0xFF) shl 8) or
                        (buffer[position + 5].toInt() and 0xFF)

            frames += Frame(type, sequence, content)
            position += totalLength
        }

        val remainingLength = length - position
        if (remainingLength > 0) {
            System.arraycopy(buffer, position, buffer, 0, remainingLength)
        }
        length = remainingLength

        if (length > MAX_BUFFER_SIZE) {
            // Too much data without a complete frame; discard it and restart.
            length = 0
        }

        return frames
    }

    private fun ensureCapacity(requiredSize: Int) {
        if (requiredSize <= buffer.size) return

        val newSize = maxOf(requiredSize, buffer.size * 2)
        buffer = buffer.copyOf(newSize)
    }

    private companion object {
        const val INITIAL_BUFFER_SIZE = 4096
        const val HEADER_SIZE = 8
        const val TRAILER_SIZE = 2
        const val MAX_BUFFER_SIZE = 70_000
    }
}