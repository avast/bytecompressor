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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


// Decompresses an input file that was compressed with HuffmanCompress, to an output file.
public final class HuffmanDecompress {
	
	public static void main(String[] args) throws IOException {
		// Show what command line arguments to use
		if (args.length == 0) {
			System.err.println("Usage: java HuffmanDecompress InputFile OutputFile");
			System.exit(1);
			return;
		}
		
		// Otherwise, decompress
		File inputFile = new File(args[0]);
		File outputFile = new File(args[1]);
		
		BitInputStream in = new BitInputStream(new BufferedInputStream(new FileInputStream(inputFile)));
		OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile));
		try {
			CanonicalCode canonCode = readCode(in);
			CodeTree code = canonCode.toCodeTree();
			decompress(code, in, out);
		} finally {
			out.close();
			in.close();
		}
	}
	
	
	static CanonicalCode readCode(BitInputStream in) throws IOException {
		int[] codeLengths = new int[257];
		for (int i = 0; i < codeLengths.length; i++) {
			// For this file format, we read 8 bits in big endian
			int val = 0;
			for (int j = 0; j < 8; j++) 
				val = val << 1 | in.readNoEof();
			codeLengths[i] = val;
		}
		return new CanonicalCode(codeLengths);
	}
	
	
	public static void decompress(CodeTree code, BitInputStream in, OutputStream out) throws IOException {
		HuffmanDecoder dec = new HuffmanDecoder(in);
		dec.codeTree = code;
		while (true) {
			int symbol = dec.read();
			if (symbol == 256)  // EOF symbol
				break;
			out.write(symbol);
		}
	}
	
}
