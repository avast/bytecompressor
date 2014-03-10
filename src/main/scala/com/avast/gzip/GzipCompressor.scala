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
import java.io.{InputStream, OutputStream}

/**
 *
 */
class GzipCompressor extends Compressor{

  override def decompressionInputStream(delegate: InputStream): InputStream = new GZIPInputStream(delegate)

  override def compressionOutputStream(delegate: OutputStream): OutputStream = new GZIPOutputStream(delegate)
}
