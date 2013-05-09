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


// Huffman compressor with predefined byte statistic from huge sample of http urls.
object UriHuffmanCompressor extends HuffmanCompressor(
  Array(585393, 1209, 99037, 935, 195827, 1349, 1441, 3456, 5588, 3700, 204363, 12710, 15167, 6172, 5788, 8135,
  104168, 9331, 102819, 3478, 4189, 6614, 3010, 3574, 12723, 2608, 99510, 1875, 1271, 1923, 1248, 1221,
  99373, 995, 195993, 1939, 1219, 1980, 3688, 1205, 196185, 1965, 985, 1075, 1547, 8814, 145773, 9217,
  207173, 205358, 301555, 195466, 197644, 198913, 201919, 200145, 195646, 197633, 4447, 613, 1256, 5868, 1557, 2694,
  2488, 3457, 101735, 5147, 6409, 7398, 7188, 12750, 7015, 10058, 17098, 19499, 10036, 6103, 9040, 103662,
  9409, 5895, 3869, 101650, 5943, 100283, 3284, 3821, 2913, 2786, 2077, 894, 1193, 1505, 1204, 2468,
  3798, 296635, 242582, 330926, 225316, 304690, 224463, 40303, 33239, 64647, 3989, 23550, 58504, 130715, 61392, 199041,
  33585, 2532, 57203, 65307, 65436, 36119, 11342, 21571, 7129, 25775, 7593, 1224, 589, 1425, 1502, 589,
  3462, 2146, 3270, 1931, 3284, 2113, 3213, 1376, 3408, 1645, 3098, 1857, 4100, 1763, 3896, 1742,
  3386, 1713, 3355, 2250, 3380, 2123, 3471, 1869, 3162, 1928, 3233, 1337, 3309, 1805, 3037, 1219,
  3324, 1509, 3430, 2206, 2930, 1504, 3760, 1863, 3780, 1746, 3481, 1732, 4458, 6807, 6060, 1989,
  3671, 2217, 3222, 2186, 2868, 1304, 3858, 1630, 6806, 1586, 3189, 1903, 3727, 2191, 3273, 2058,
  3524, 2078, 3260, 1324, 3070, 1513, 3453, 2395, 3430, 2059, 3717, 1880, 4452, 2422, 3393, 99697,
  3942, 3038, 3157, 1583, 3479, 1326, 3122, 6905, 17469, 14756, 16105, 15245, 16293, 15207, 15457, 1756,
  3187, 1625, 3407, 1958, 3980, 1549, 3443, 1907, 3724, 1791, 3987, 1831, 3669, 2541, 3693, 2265,
  3925, 99911, 3595, 1630, 3584, 1890, 3030, 1659, 3703, 1641, 3369, 1734, 3292, 1961, 3635, 1923,
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
