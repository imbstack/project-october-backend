package org.octob.codecs

import java.nio.ByteBuffer

import com.twitter.cassie.codecs.Codec

object DoubleCodec extends Codec[Double] {
    private val length = 8

    def encode(v: Double) = {
        val b = ByteBuffer.allocate(length)
        b.putDouble(v)
        b.rewind
        b
    }

    def decode(buf: ByteBuffer) = {
        require(buf.remaining == length)
        buf.duplicate.getDouble
    }
}
