/**
 *
 *  Copyright © 2013 Lukas Karas, Avast a.s. <karas@avast.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of
 *  this software and associated documentation files (the "Software"), to deal in
 *  the Software without restriction, including without limitation the rights to
 *  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 *  the Software, and to permit persons to whom the Software is furnished to do so,
 *  subject to the following conditions:
 *
 *  * The above copyright notice and this permission notice shall be included in
 *    all copies or substantial portions of the Software.
 *
 *  * The Software is provided "as is", without warranty of any kind, express or
 *    implied, including but not limited to the warranties of merchantability,
 *    fitness for a particular purpose and noninfringement. In no event shall the
 *    authors or copyright holders be liable for any claim, damages or other
 *    liability, whether in an action of contract, tort or otherwise, arising from,
 *    out of or in connection with the Software or the use or other dealings in the
 *    Software.
 */

package com.avast.huffman

import com.avast.{ByteBufferBackedOutputStream, ByteBufferBackedInputStream, Compressor}
import java.nio.ByteBuffer
import nayuki.huffmancoding._
import com.google.protobuf.ByteString


// Huffman compressor with predefined byte statistic from huge sample of http urls.
object HttpUriHuffmanCompressor extends HuffmanCompressor(
  Array(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    1, 2, 1, 157, 4, 1417, 2707, 2, 3, 3, 4, 368, 80, 7428, 143759, 8223,
    5472, 7331, 5814, 3657, 4063, 3889, 3896, 2876, 2923, 1952, 2186, 9, 1, 3518, 1, 909,
    10, 691, 333, 496, 475, 342, 654, 256, 182, 521, 168, 149, 225, 426, 361, 257,
    314, 228, 401, 502, 405, 442, 216, 270, 162, 245, 147, 18, 1, 19, 9, 1572,
    1, 100398, 35478, 131317, 27211, 109743, 27357, 39927, 32030, 63352, 3536, 21484, 51645, 129491, 59668, 197803,
    32500, 1527, 56168, 64763, 64597, 34783, 9963, 20478, 5530, 25181, 5995, 5, 2, 5, 20, 1,
    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    1)){

}

object AdaptiveHuffmanCompressor extends Compressor{

  def decompress(compressedIn: ByteBuffer): ByteBuffer = {
    val is = new ByteBufferBackedInputStream(compressedIn);
    val os = new ByteBufferBackedOutputStream(math.max(1024, compressedIn.remaining() * 2))

    AdaptiveHuffmanDecompress.decompress(new BitInputStream( is ),  os )

    os.asByteBuffer()
  }

  def compress(rawIn: ByteBuffer): ByteBuffer = {
    val is = new ByteBufferBackedInputStream(rawIn);
    val os = new ByteBufferBackedOutputStream(math.max(1024, rawIn.remaining() * 2))

    val bos = new BitOutputStream( os )
    AdaptiveHuffmanCompress.compress(is, bos)
    bos.close()

    os.asByteBuffer()
  }
}

class HuffmanCompressor(val frequencies: Array[Int]) extends Compressor{

  val codeTree = {
    val freqTable = new FrequencyTable(frequencies)
    val code = freqTable.buildCodeTree();
    val canonCode = new CanonicalCode(code, 257);
    canonCode.toCodeTree();  // Replace code tree with canonical one. For each symbol, the code value may change but the code length stays the same.
  }

  def decompress(compressedIn: ByteBuffer): ByteBuffer = {
    val is = new ByteBufferBackedInputStream(compressedIn);
    val os = new ByteBufferBackedOutputStream(math.max(1024, compressedIn.remaining() * 2))

    HuffmanDecompress.decompress(codeTree, new BitInputStream( is ),  os)

    os.asByteBuffer()
  }

  def compress(rawIn: ByteBuffer): ByteBuffer = {
    val is = new ByteBufferBackedInputStream(rawIn);
    val os = new ByteBufferBackedOutputStream(math.max(1024, rawIn.remaining() * 2))

    val bos = new BitOutputStream( os )
    HuffmanCompress.compress(codeTree, is, bos)
    bos.close()

    os.asByteBuffer()
  }
}

// Helper class for generating frequencies table from data sample
class HuffmanSaveCodeGenerator{

  // set all frequencies to 1, it is not effective, but save when we can decode byte that wasn't presented in sample data
  val frequencies = {
    val arr = new Array[Int](257)
    (0 to 256).foreach(index => { arr.update(index,1) })
    arr
  }

  def update(symbol: Byte) : Array[Int] = {
    val index: Int = symbol.toInt & 0x000000ff
    var freq = frequencies(index)
    if (freq == Integer.MAX_VALUE)
      throw new RuntimeException("Arithmetic overflow")
    frequencies.update(index, freq + 1)
    frequencies
  }

  def update(sample: ByteBuffer): Array[Int] = {
    /*
    val bs = ByteString.copyFrom(sample)
    println(bs.toStringUtf8)
    sample.flip()
    */

    while (sample.hasRemaining()){
      val symbol = sample.get()
      update(symbol)
    }
    frequencies
  }

  def toScalaCode(): String = {
    /*
    val freq: Array[Int] = Array(0, 1, 2, 9, 0, 1, 2, 9, 0, 1, 2, 9, 0,
      1, 2, 9, 0, 1, 2, 9, 0, 1, 2, 9, 0, 1, 2, 9, 0, 1, 2, 9, 10)
    */

    val sb = new StringBuilder()
    sb.append("\tval freq: Array[Int] = Array(")
    var index = 0
    frequencies.foreach(value => {
      sb.append(value)
      if (index < frequencies.size -1)
        sb.append(", ")
      if (index % 16 == 15)
        sb.append("\n\t\t")
      index += 1
    })
    sb.append(")")
    sb.toString()
  }
}
