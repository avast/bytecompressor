package com.avast;

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
}
