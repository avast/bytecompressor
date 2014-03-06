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

package com.avast.jsnappy

import com.avast.{ByteBufferBackedOutputStream, ByteBufferBackedInputStream, Compressor}
import java.nio.ByteBuffer
import de.jarnbjo.jsnappy._

/**
 * Created with IntelliJ IDEA.
 * User: karry
 * Date: 6.5.13
 * Time: 21:15
 */
object SnappyCompressor extends SnappyCompressor(
  de.jarnbjo.jsnappy.SnappyCompressor.DEFAULT_EFFORT,
  SnzOutputStream.DEFAULT_BUFFER_SIZE
  ){

  val DEFAULT_EFFORT = de.jarnbjo.jsnappy.SnappyCompressor.DEFAULT_EFFORT
  val DEFAULT_BUFFER_SIZE = SnzOutputStream.DEFAULT_BUFFER_SIZE
}

/**
 *
 * @param effort 	 Sets the compression effort used by this stream from 1 (fastest, less
 * compression) to 100 (slowest, best compression). If the effort is
 * changed after the stream has been written to, the new effort will take
 * effect on the next packet processed internally and may so affect
 * data already written to the stream.
 *
 * Default effort is 1
 *
 * @param compressBufferSize buffer size must be a power of 2 between 2**0 and 2**29
 */
class SnappyCompressor(val effort:Int = 1, val compressBufferSize:Int = 1024) extends Compressor{

  def decompress(compressedIn: ByteBuffer): ByteBuffer = {
    val sis: SnzInputStream = new SnzInputStream(new ByteBufferBackedInputStream(compressedIn));
    val bufferSize = math.max(1024, compressedIn.remaining() * 2 )
    val out = new ByteBufferBackedOutputStream(bufferSize)
    pipe(sis, out, bufferSize);
    sis.close();

    out.asByteBuffer()
  }

  def compress(rawIn: ByteBuffer): ByteBuffer = {

    val bufferSize = math.max(1024, rawIn.remaining() * 2 )
    val out = new ByteBufferBackedOutputStream(bufferSize)
    val cos = new SnzOutputStream(out, compressBufferSize)
    cos.setCompressionEffort(effort)
    
    pipe(rawIn, cos);
    cos.close()

    out.asByteBuffer()
  }
}
