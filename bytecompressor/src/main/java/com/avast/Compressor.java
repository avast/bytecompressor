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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

abstract public class Compressor {

    public void pipe(InputStream is, OutputStream os, int bufferSize) throws IOException {
        Pipe.apply(is, os, bufferSize);
    }

    public void pipe(InputStream is, OutputStream os) throws IOException {
        Pipe.apply(is, os);
    }
    public void pipe(ByteBuffer in, OutputStream os) throws IOException {
        Pipe.apply(in, os);
    }

    public ByteBuffer decompress(ByteBuffer compressedIn) throws IOException {
        int bufferSize = Math.max(1024, compressedIn.remaining() * 2);
        InputStream is = decompressionInputStream(new ByteBufferBackedInputStream(compressedIn));
        ByteBufferBackedOutputStream bufOut = new ByteBufferBackedOutputStream(bufferSize);
        pipe(is,bufOut, bufferSize);
        is.close();
        return bufOut.asByteBuffer();
    }

    public ByteBuffer compress(ByteBuffer rawIn) throws IOException {
        ByteBufferBackedOutputStream buffer = new ByteBufferBackedOutputStream(Math.max(1024, rawIn.remaining() * 2));
        OutputStream os = compressionOutputStream(buffer);

        pipe(rawIn, os);

        os.close();
        return buffer.asByteBuffer();
    }

    public abstract OutputStream compressionOutputStream(OutputStream delegate);

    public abstract InputStream decompressionInputStream(InputStream delegate);

}
