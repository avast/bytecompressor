/*
 *  Copyright 2013 Lukas Karas, Avast a.s. <karas@avast.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.avast.zlib

import com.avast.{ByteBufferBackedOutputStream, Compressor}
import java.nio.ByteBuffer
import java.util.zip._

/**
 *
 * @param level compression level (0-9)
 * @param strategy compression strategy
 */
class ZlibCompressor(level:Int = Deflater.DEFAULT_COMPRESSION, strategy:Int = Deflater.DEFAULT_STRATEGY) extends Compressor{

  def decompress(compressedIn: ByteBuffer): ByteBuffer = {
    val inflater = new Inflater()
    val bufferSize = math.max(1024, compressedIn.remaining() * 2 )

    if (compressedIn.hasArray()){
      inflater.setInput(compressedIn.array(), compressedIn.position(), compressedIn.remaining())
      compressedIn.position(compressedIn.limit())
    }else{
      val buff = new Array[Byte](compressedIn.remaining())
      compressedIn.get(buff)
      inflater.setInput(buff)
    }

    val out = new ByteBufferBackedOutputStream(bufferSize)
    val iout = new InflaterOutputStream(out, inflater, bufferSize)
    iout.finish()
    iout.close()
    return out.asByteBuffer()
  }

  def compress(rawIn: ByteBuffer): ByteBuffer = {

    val deflater = new Deflater();
    deflater.setLevel(level)
    deflater.setStrategy(strategy)

    val bufferSize = math.max(1024, rawIn.remaining() * 2 )

    if (rawIn.hasArray()){
      deflater.setInput(rawIn.array(), rawIn.position(), rawIn.remaining());
      rawIn.position(rawIn.limit())
    }else{
      val buff = new Array[Byte](rawIn.remaining())
      rawIn.get(buff)
      deflater.setInput(buff)
    }

    val out = new ByteBufferBackedOutputStream(bufferSize)
    val dout = new DeflaterOutputStream(out,deflater,bufferSize)
    dout.finish()
    dout.close()
    return out.asByteBuffer()
  }
}
