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

package com.avast

import java.nio.ByteBuffer
import java.io.{InputStream, OutputStream}
import scala._
import scala.collection.mutable.MutableList

object Pipe{
  def apply(is: InputStream , os: OutputStream) = {
    val buffer = new Array[Byte](65536);
    var r = 0;
    while ( {r = is.read(buffer); r} >= 0  ) {
      os.write(buffer, 0, r);
    }
    os.flush();
  }
}

abstract class Compressor {

  def pipe(is: InputStream , os: OutputStream) = {
    Pipe(is, os)
  }

  def decompress(compressedIn:ByteBuffer): ByteBuffer

  def compress(rawIn:ByteBuffer): ByteBuffer
}

class ByteBufferBackedInputStream(val buff: ByteBuffer ) extends InputStream{

  def read(): Int = {
    this.synchronized[Int] {
      if (!buff.hasRemaining()) {
        return -1
      }
      return buff.get().toInt & 0x000000ff // default conversion from byt to integer expand highest bit, returned integer can be negative...
    }
  }

  override def read( bytes: Array[Byte], off:Int, _len:Int):Int = {
    this.synchronized[Int] {
      val rem = buff.remaining();
      if (rem == 0)
        return -1
      val len = scala.math.min(_len, rem)
      buff.get(bytes, off, len)
      len
    }
  }
}

class ByteBufferBackedOutputStream(val initialBuffCapacity: Int) extends OutputStream{

  var lastBuff: ByteBuffer = ByteBuffer.allocate( math.max(1024, initialBuffCapacity) )
  var buffers = new MutableList[ByteBuffer]
  var written = 0

  protected def allocateBuff() = {
    lastBuff.flip()
    buffers += lastBuff
    lastBuff = ByteBuffer.allocate( math.max(1024, initialBuffCapacity) )
  }

  def write(b:Int) = {
    this.synchronized[ByteBuffer] {
      if (lastBuff.remaining() == 0)
        allocateBuff()
      lastBuff.put(b.asInstanceOf[Byte]);
    }
  }

  override def write(bytes: Array[Byte], off:Int, len:Int) = {
    var offset = off
    var lenght = len
    this.synchronized[ByteBuffer] {
      while (lenght > 0){
        if (lastBuff.remaining() == 0)
          allocateBuff()

        val toWrite = math.min( lastBuff.remaining(), lenght)
        lastBuff.put(bytes, offset, toWrite);
        offset += toWrite
        lenght -= toWrite
        written += toWrite
      }
      lastBuff
    }
  }

  def asByteBuffer(): ByteBuffer = {
    if (buffers.isEmpty){
      lastBuff.flip()
      lastBuff
    }else{
      var result: ByteBuffer = ByteBuffer.allocate( written )
      buffers += lastBuff
      buffers.foreach(b => {
        result.put(b)
      })
      result.flip()
      result
    }
  }
}
