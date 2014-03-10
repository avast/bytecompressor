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

package com.avast.gzip

import java.nio.ByteBuffer
import java.util.zip._
import com.avast.{ByteBufferBackedInputStream, ByteBufferBackedOutputStream, Compressor}

/**
 *
 */
class GzipCompressor extends Compressor{

  def decompress(compressedIn: ByteBuffer): ByteBuffer = {
    val bufferSize = math.max(1024, compressedIn.remaining() * 2 )
    val gzipIn = new GZIPInputStream(new ByteBufferBackedInputStream(compressedIn))
    val bufOut = new ByteBufferBackedOutputStream(bufferSize)
    pipe(gzipIn,bufOut, bufferSize)
    gzipIn.close()
    bufOut.asByteBuffer()
  }

  def compress(rawIn: ByteBuffer): ByteBuffer = {
    val bufferSize = math.max(1024, rawIn.remaining() * 2 )
    val bufOut = new ByteBufferBackedOutputStream(bufferSize)
    val gzipOs =  new GZIPOutputStream(bufOut)

    if (rawIn.hasArray()){
      gzipOs.write(rawIn.array(), rawIn.position(), rawIn.remaining())
      rawIn.position(rawIn.limit())
    }else{
      val buff = new Array[Byte](rawIn.remaining())
      rawIn.get(buff)
      gzipOs.write(buff)
    }

    gzipOs.finish()
    gzipOs.close()

    bufOut.asByteBuffer()
  }
}
