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
import java.io.{InputStream, OutputStream}

/**
 *
 * @param level compression level (0-9)
 * @param strategy compression strategy
 */
class ZlibCompressor(level:Int = Deflater.DEFAULT_COMPRESSION, strategy:Int = Deflater.DEFAULT_STRATEGY) extends Compressor{

  override def decompressionInputStream(delegate: InputStream): InputStream = {
    val inflater = new Inflater()
    new InflaterInputStream(delegate, inflater)
  }

  override def compressionOutputStream(delegate: OutputStream): OutputStream = {
    val deflater = new Deflater();
    deflater.setLevel(level)
    deflater.setStrategy(strategy)
    new DeflaterOutputStream(delegate,deflater)
  }
}
