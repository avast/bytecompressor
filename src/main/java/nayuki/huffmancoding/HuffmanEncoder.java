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

import java.io.IOException;
import java.util.List;


public final class HuffmanEncoder {
	
	private BitOutputStream output;
	
	// Must be initialized before calling write().
	// The code tree can be changed after each symbol encoded, as long as the encoder and decoder have the same code tree at the same time.
	public CodeTree codeTree;
	
	
	
	public HuffmanEncoder(BitOutputStream out) {
		if (out == null)
			throw new NullPointerException("Argument is null");
		output = out;
	}
	
	
	
	public void write(int symbol) throws IOException {
		if (codeTree == null)
			throw new NullPointerException("Code tree is null");
		
		List<Integer> bits = codeTree.getCode(symbol);
		for (int b : bits)
			output.write(b);
	}
	
}
