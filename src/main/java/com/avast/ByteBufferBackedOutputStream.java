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

package com.avast;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

public class ByteBufferBackedOutputStream extends OutputStream {
    protected static final int MIN_CAPACITY = 1024;
    protected final int initialBuffCapacity;

    protected ByteBuffer lastBuff;
    protected final List<ByteBuffer> buffers = new LinkedList<ByteBuffer>();
    protected int written = 0;

    public ByteBufferBackedOutputStream(int initialBuffCapacity){
        this.initialBuffCapacity = initialBuffCapacity;
        lastBuff = ByteBuffer.allocate( Math.max(MIN_CAPACITY, initialBuffCapacity) );
    }

    protected void allocateBuff(){
        lastBuff.flip();
        buffers.add(lastBuff);
        lastBuff = ByteBuffer.allocate( Math.max(MIN_CAPACITY, initialBuffCapacity) );
    }

    @Override
    public synchronized void write(int b) throws IOException {
        if (lastBuff.remaining() == 0)
            allocateBuff();
        lastBuff.put((byte)b);
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        int offset = off;
        int lenght = len;

        while (lenght > 0){
            if (lastBuff.remaining() == 0)
              allocateBuff();

            int toWrite = Math.min( lastBuff.remaining(), lenght);
            lastBuff.put(b, offset, toWrite);
            offset += toWrite;
            lenght -= toWrite;
            written += toWrite;
        }
    }

    public ByteBuffer asByteBuffer(){
        lastBuff.flip();
        if (buffers.isEmpty()){
          return lastBuff;
        }else{
            // concat all chunks into single ByteBuffer
            ByteBuffer result = ByteBuffer.allocate( written );
            buffers.add(lastBuff);
            for (ByteBuffer b:buffers){
                result.put(b);
            }
            result.flip();
            return result;
        }
    }
}
