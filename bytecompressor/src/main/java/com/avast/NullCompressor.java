package com.avast;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Test compressor that don't modify data.
 *
 * Warning: this compressor uses slice method on buffers for fastest use,
 * content will be shared and later modifications can cause problems!
 */
public class NullCompressor extends Compressor {

    @Override
    public ByteBuffer decompress(ByteBuffer compressedIn) {
        return compressedIn.slice();
    }

    @Override
    public ByteBuffer compress(ByteBuffer rawIn) {
        return rawIn.slice();
    }

    @Override
    public OutputStream compressionOutputStream(final OutputStream delegate) {
        return new OutputStream(){

            @Override
            public void write(int b) throws IOException {
                delegate.write(b);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                delegate.write(b, off, len);
            }

            @Override
            public void flush() throws IOException {
                delegate.flush();
            }

            @Override
            public void close() throws IOException {
                delegate.close();
            }
        };
    }

    @Override
    public InputStream decompressionInputStream(final InputStream delegate) {
        return new InputStream() {
            @Override
            public int read() throws IOException {
               return delegate.read();
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                return delegate.read(b, off, len);
            }

            @Override
            public long skip(long n) throws IOException {
                return delegate.skip(n);
            }

            @Override
            public int available() throws IOException {
                return delegate.available();
            }

            @Override
            public void close() throws IOException {
                delegate.close();
            }

            @Override
            public synchronized void mark(int readlimit) {
                delegate.mark(readlimit);
            }

            @Override
            public synchronized void reset() throws IOException {
                delegate.reset();
            }

            @Override
            public boolean markSupported() {
                return delegate.markSupported();
            }
        };
    }
}
