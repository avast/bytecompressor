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

import com.google.protobuf.ByteString;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

public class ByteBufferBackedOutputStream extends OutputStream {
    protected static final int MIN_CAPACITY = 1024;
    protected final int initialBuffCapacity;
    private final boolean allocateDirect;

    protected ByteBuffer lastBuff;
    protected final List<ByteBuffer> buffers = new LinkedList<ByteBuffer>();
    protected int written = 0;

    public ByteBufferBackedOutputStream(int initialBuffCapacity){
        this(initialBuffCapacity, false);
    }
    public ByteBufferBackedOutputStream(int initialBuffCapacity, boolean allocateDirect){
        this.initialBuffCapacity = initialBuffCapacity;
        this.allocateDirect = allocateDirect;
        lastBuff = allocateBuff(Math.max(MIN_CAPACITY, initialBuffCapacity));
    }

    protected ByteBuffer allocateBuff(int capacity){
        if (allocateDirect)
            return ByteBuffer.allocateDirect(capacity);
        return ByteBuffer.allocate(capacity);
    }

    protected void allocateNextBuff(){
        lastBuff.flip();
        buffers.add(lastBuff);
        lastBuff = allocateBuff( Math.max(MIN_CAPACITY, initialBuffCapacity) );
    }

    @Override
    public synchronized void write(int b) throws IOException {
        if (lastBuff.remaining() == 0)
            allocateNextBuff();
        lastBuff.put((byte)b);
        written+=1;
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        int offset = off;
        int lenght = len;

        while (lenght > 0){
            if (lastBuff.remaining() == 0)
              allocateNextBuff();

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
            ByteBuffer result = allocateBuff( written );
            buffers.add(lastBuff);
            for (ByteBuffer b:buffers){
                result.put(b);
                if (allocateDirect && (b instanceof sun.nio.ch.DirectBuffer))
                    ((sun.nio.ch.DirectBuffer)b).cleaner().clean();
            }
            result.flip();
            return result;
        }
    }

    public ByteString asByteString(){
        return ByteString.copyFrom(asByteBuffer());
    }
}
