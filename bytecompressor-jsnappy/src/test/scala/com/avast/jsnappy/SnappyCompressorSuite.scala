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

import org.scalatest.FlatSpec
import com.google.protobuf.ByteString
import java.nio.ByteBuffer

/**
 * Created with IntelliJ IDEA.
 * User: karry
 * Date: 6.5.13
 * Time: 22:40
 */
class SnappyCompressorSuite extends FlatSpec{

  it must "compress and decompress data" in {

    val bs = ByteString.copyFromUtf8("Some testing raw data... Lorem ipsum dolor sit amet, consectetuer adipiscing elit.")
    val buff = ByteBuffer.allocate(bs.size())
    bs.copyTo(buff)

    (3 to 27).foreach(exp => {
      (10 to 100 by 10).foreach(effort => {
        buff.position(0)

        val compressor = new SnappyCompressor(effort, 2 << exp)
        val compressed = compressor.compress(buff)
        val decompressed = compressor.decompress(compressed)

        buff.position(0)
        compressed.position(0)
        //println((2 << exp) + ", "+effort+": " + buff.remaining() + " => " + compressed.remaining())

        assert(compressed != buff)
        expect(decompressed)(buff)
      })
    })
  }
}
