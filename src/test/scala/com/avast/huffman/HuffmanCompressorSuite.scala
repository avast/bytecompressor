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
import com.avast.{Compressor, ByteBufferBackedInputStream, Pipe, ByteBufferBackedOutputStream}

class HuffmanCompressorSuite extends FlatSpec{

  def testOutputStream(compressor: Compressor, rawIn: ByteBuffer):ByteBuffer = {
    val cBuff = new ByteBufferBackedOutputStream(1024)
    val cos = compressor.compressionOutputStream(cBuff)
    Pipe.apply(rawIn, cos)
    cos.close()
    rawIn.position(0)
    cBuff.asByteBuffer()
  }

  def testInputStream(compressor: Compressor, compressed: ByteBuffer):ByteBuffer = {
    val dis = compressor.decompressionInputStream(new ByteBufferBackedInputStream(compressed))
    val dBuff = new ByteBufferBackedOutputStream(1024)
    Pipe.apply(dis, dBuff)
    dis.close()
    compressed.position(0)
    dBuff.asByteBuffer()
  }

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

    // test streams
    val cBuff = testOutputStream( AdaptiveHuffmanCompressor, buff )
    val dBuff = testInputStream(AdaptiveHuffmanCompressor, compressed)

    assert(compressed == cBuff)
    expect(dBuff)(buff)
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

    // test streams
    val cBuff = testOutputStream( compressor, buff )
    val dBuff = testInputStream( compressor, compressed)

    assert(compressed == cBuff)
    expect(dBuff)(buff)
  }
}
