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

package com.avast.huffman

import org.scalatest.FlatSpec
import com.google.protobuf.ByteString
import java.nio.ByteBuffer

/**
 * Created with IntelliJ IDEA.
 * User: karry
 * Date: 6.5.13
 * Time: 22:40
 */
class HuffmanCompressorSuite extends FlatSpec{

  it must "compress and decompress data with adaptive huffman" in {

    val bs = ByteString.copyFromUtf8("Some testing raw data... Lorem ipsum dolor sit amet, consectetuer adipiscing elit.")
    val buff = ByteBuffer.allocate(bs.size())
    bs.copyTo(buff)
    buff.flip()

    val compressed = AdaptiveHuffmanCompressor.compress(buff)
    val decompressed = AdaptiveHuffmanCompressor.decompress(compressed)

    buff.position(0)
    compressed.position(0)
    //println((2 << exp) + ", "+effort+": " + buff.remaining() + " => " + compressed.remaining())

    assert(compressed != buff)
    expect(decompressed)(buff)
  }

  it must "compress and decompresss data with huffman" in {
    val bs = ByteString.copyFromUtf8("Some testing raw data... Lorem ipsum dolor sit amet, consectetuer adipiscing elit.")
    val buff = ByteBuffer.allocate(bs.size())
    bs.copyTo(buff)
    buff.flip()

    val huffmanCodeGenerator = new HuffmanSaveCodeGenerator()
    huffmanCodeGenerator.update(buff)
    buff.flip()

    val compressor = new HuffmanCompressor( huffmanCodeGenerator.frequencies )
    val compressed = compressor.compress(buff)
    val decompressed = compressor.decompress(compressed)

    buff.position(0)
    compressed.position(0)
    //println((2 << exp) + ", "+effort+": " + buff.remaining() + " => " + compressed.remaining())

    assert(compressed != buff)
    expect(decompressed)(buff)

  }
}
