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


public final class AdaptiveHuffmanDecompress {
	
	public static void main(String[] args) throws IOException {
		// Show what command line arguments to use
		if (args.length == 0) {
			System.err.println("Usage: java AdaptiveHuffmanDecompress InputFile OutputFile");
			System.exit(1);
			return;
		}
		
		// Otherwise, decompress
		File inputFile = new File(args[0]);
		File outputFile = new File(args[1]);
		
		BitInputStream in = new BitInputStream(new BufferedInputStream(new FileInputStream(inputFile)));
		OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile));
		try {
			decompress(in, out);
		} finally {
			out.close();
			in.close();
		}
	}
	
	
	public static void decompress(BitInputStream in, OutputStream out) throws IOException {
		int[] initFreqs = new int[257];
		Arrays.fill(initFreqs, 1);
		
		FrequencyTable freqTable = new FrequencyTable(initFreqs);
		HuffmanDecoder dec = new HuffmanDecoder(in);
		dec.codeTree = freqTable.buildCodeTree();
		int count = 0;
		while (true) {
			int symbol = dec.read();
			if (symbol == 256)  // EOF symbol
				break;
			out.write(symbol);
			
			freqTable.increment(symbol);
			count++;
			if (count < 262144 && isPowerOf2(count) || count % 262144 == 0)  // Update code tree
				dec.codeTree = freqTable.buildCodeTree();
			if (count % 262144 == 0)  // Reset frequency table
				freqTable = new FrequencyTable(initFreqs);
		}
	}

    public static InputStream decompressInputStream(final InputStream delegate){
        final BitInputStream in = new BitInputStream(delegate);

        return new InputStream(){
            int[] initFreqs = new int[257];
            int count = 0;
            final HuffmanDecoder dec = new HuffmanDecoder(in);
            FrequencyTable freqTable;
            boolean endReached = false;

            {
                Arrays.fill(initFreqs, 1);
                freqTable = new FrequencyTable(initFreqs);
                dec.codeTree = freqTable.buildCodeTree();
            }

            @Override
            public int read() throws IOException {
                if (endReached)
                    return -1;

                int symbol = dec.read();
			    if (symbol == 256){  // EOF symbol
                    endReached = true;
                    return -1;
                }

                freqTable.increment(symbol);
                count++;
                if (count < 262144 && isPowerOf2(count) || count % 262144 == 0)  // Update code tree
                    dec.codeTree = freqTable.buildCodeTree();
                if (count % 262144 == 0)  // Reset frequency table
                    freqTable = new FrequencyTable(initFreqs);

                return symbol;
            }

            @Override
            public void close() throws IOException {
                delegate.close();
            }
        };
    }
	
	
	private static boolean isPowerOf2(int x) {
		return x > 0 && (x & -x) == x;
	}
	
}
