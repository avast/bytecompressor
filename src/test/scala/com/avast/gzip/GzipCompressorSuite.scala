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

import org.scalatest.FlatSpec
import com.google.protobuf.ByteString
import java.nio.ByteBuffer

class GzipCompressorSuite extends FlatSpec{

  it must "compress and decompress data" in {

    val bs = ByteString.copyFromUtf8("Some testing raw data... Lorem ipsum dolor sit amet, consectetuer adipiscing elit.")
    val buff = ByteBuffer.allocate(bs.size())
    bs.copyTo(buff)

    buff.position(0)

    val compressor = GzipCompressor
    val compressed = compressor.compress(buff)
    val decompressed = compressor.decompress(compressed)

    buff.position(0)
    compressed.position(0)
    //println((2 << exp) + ", "+effort+": " + buff.remaining() + " => " + compressed.remaining())

    assert(compressed != buff)
    expect(decompressed)(buff)

  }
}
