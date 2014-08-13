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
import java.util.Arrays;


public final class AdaptiveHuffmanCompress {
	
	public static void main(String[] args) throws IOException {
		// Show what command line arguments to use
		if (args.length == 0) {
			System.err.println("Usage: java AdaptiveHuffmanCompress InputFile OutputFile");
			System.exit(1);
			return;
		}
		
		// Otherwise, compress
		File inputFile = new File(args[0]);
		File outputFile = new File(args[1]);
		
		InputStream in = new BufferedInputStream(new FileInputStream(inputFile));
		BitOutputStream out = new BitOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
		try {
			compress(in, out);
		} finally {
			out.close();
			in.close();
		}
	}
	
	
	public static void compress(InputStream in, BitOutputStream out) throws IOException {
		int[] initFreqs = new int[257];
		Arrays.fill(initFreqs, 1);
		
		FrequencyTable freqTable = new FrequencyTable(initFreqs);
		HuffmanEncoder enc = new HuffmanEncoder(out);
		enc.codeTree = freqTable.buildCodeTree();  // We don't need to make a canonical code since we don't transmit the code tree
		int count = 0;
		while (true) {
			int b = in.read();
			if (b == -1)
				break;
			enc.write(b);
			
			freqTable.increment(b);
			count++;
			if (count < 262144 && isPowerOf2(count) || count % 262144 == 0)  // Update code tree
				enc.codeTree = freqTable.buildCodeTree();
			if (count % 262144 == 0)  // Reset frequency table
				freqTable = new FrequencyTable(initFreqs);
		}
		enc.write(256);  // EOF
	}
	
	
	public static boolean isPowerOf2(int x) {
		return x > 0 && (x & -x) == x;
	}

    public static OutputStream compressionOutputStream(OutputStream delegate){
        final BitOutputStream out = new BitOutputStream(delegate);
        final HuffmanEncoder enc = new HuffmanEncoder(out);

        return new OutputStream() {
            final int[] initFreqs = new int[257];
            FrequencyTable freqTable;

            {
                Arrays.fill(initFreqs, 1);
                freqTable = new FrequencyTable(initFreqs);
                enc.codeTree = freqTable.buildCodeTree();  // We don't need to make a canonical code since we don't transmit the code tree
            }

            int count = 0;

            public void write(int b) throws IOException {
                enc.write(b);
                freqTable.increment(b);
                count += 1;
                if (count < 262144 && AdaptiveHuffmanCompress.isPowerOf2(count) || count % 262144 == 0)
                    enc.codeTree = freqTable.buildCodeTree();
                if (count % 262144 == 0)
                    freqTable = new FrequencyTable(initFreqs);
            }

            public void close() throws IOException {
                enc.write(256);  // EOF
                out.close();
            }
        };
    }
	
}
