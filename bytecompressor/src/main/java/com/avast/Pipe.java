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

public class Pipe{

    public static void apply(InputStream is, OutputStream os) throws IOException {
        apply(is,os, 65536);
    }

    public static void apply(InputStream is, OutputStream os, int bufferSize) throws IOException {
        if (bufferSize <= 0)
            throw new IllegalArgumentException("Buffer size can not be less or equal zero.");

        byte[] buffer = new byte[bufferSize];
        int r = 0;
        while ( (r = is.read(buffer)) >= 0  ) {
            os.write(buffer, 0, r);
        }
        os.flush();
    }

    /**
     * Try to process pipe without source copying.
     *
     * @param in
     * @param os
     * @throws IOException
     */
    public static void apply(ByteBuffer in, OutputStream os) throws IOException {
        if (in.hasArray()){
            byte[] inArray = in.array();
            os.write(inArray, in.position(), in.remaining());
            in.position(in.limit()); // move to end (limit)
            os.flush();
        }else{
            // fallback with buffer copy
            apply(new ByteBufferBackedInputStream(in), os, Math.min(65536, Math.max(1, in.remaining())));
        }
    }

}