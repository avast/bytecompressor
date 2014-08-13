/**
 *
 *  Copyright © 2013 Nayuki Minase
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

package nayuki.huffmancoding;

import java.io.*;


// Uses static Huffman coding to compress an input file to an output file. Use HuffmanDecompress to decompress.
// Uses 257 symbols - 256 for byte values and 1 for EOF. The compressed file format contains the code length of each symbol under a canonical code, followed by the Huffman-coded data.
public final class HuffmanCompress {
	
	public static void main(String[] args) throws IOException {
		// Show what command line arguments to use
		if (args.length == 0) {
			System.err.println("Usage: java HuffmanCompress InputFile OutputFile");
			System.exit(1);
			return;
		}
		
		// Otherwise, compress
		File inputFile = new File(args[0]);
		File outputFile = new File(args[1]);
		
		// Read input file once to compute symbol frequencies
		// The resulting generated code is optimal for static Huffman coding and also canonical
		FrequencyTable freq = getFrequencies(inputFile);
		freq.increment(256);  // EOF symbol gets a frequency of 1
		CodeTree code = freq.buildCodeTree();
		CanonicalCode canonCode = new CanonicalCode(code, 257);
		code = canonCode.toCodeTree();  // Replace code tree with canonical one. For each symbol, the code value may change but the code length stays the same.
		
		// Read input file again, compress with Huffman coding, and write output file
		InputStream in = new BufferedInputStream(new FileInputStream(inputFile));
		BitOutputStream out = new BitOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
		try {
			writeCode(out, canonCode);
			compress(code, in, out);
		} finally {
			out.close();
			in.close();
		}
	}
	
	
	private static FrequencyTable getFrequencies(File file) throws IOException {
		FrequencyTable freq = new FrequencyTable(new int[257]);
		InputStream input = new BufferedInputStream(new FileInputStream(file));
		try {
			while (true) {
				int b = input.read();
				if (b == -1)
					break;
				freq.increment(b);
			}
		} finally {
			input.close();
		}
		return freq;
	}
	
	
	static void writeCode(BitOutputStream out, CanonicalCode canonCode) throws IOException {
		for (int i = 0; i < canonCode.getSymbolLimit(); i++) {
			int val = canonCode.getCodeLength(i);
			// For this file format, we only support codes up to 255 bits long
			if (val >= 256)
				throw new RuntimeException("The code for a symbol is too long");
			
			// Write value as 8 bits in big endian
			for (int j = 7; j >= 0; j--)
				out.write((val >>> j) & 1);
		}
	}
	
	
	public static void compress(CodeTree code, InputStream in, BitOutputStream out) throws IOException {
		HuffmanEncoder enc = new HuffmanEncoder(out);
		enc.codeTree = code;
		while (true) {
			int b = in.read();
			if (b == -1)
				break;
			enc.write(b);
		}
		enc.write(256);  // EOF
	}

    public static OutputStream compressionOutputStream(CodeTree code, final OutputStream delegate){
        final BitOutputStream out = new BitOutputStream(delegate);
        final HuffmanEncoder enc = new HuffmanEncoder(out);
		enc.codeTree = code;

        return new OutputStream(){
            boolean closed = false;

            @Override
            public void write(int b) throws IOException {
                enc.write(b);
            }

            @Override
            public void close() throws IOException {
                enc.write(256);  // EOF
                out.close();
            }
        };
    }
	
}
