
ByteCompressor
==============

	Scala abstractions for some compression algorithms.

	Beside abstraction layer, this project provide some useful utils for data processing, 
	like Pipe, ByteBufferBackedInputStream and ByteBufferBackedOutputStream.

	
	License
	-------

	Copyright 2013 Lukas Karas, Avast a.s. <karas@avast.com>

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.


ByteCompressor: Huffman Coding
==============================

	Avast fork of https://github.com/nayuki/Huffman-Coding , it ads bytecompressor layer compatibility.

	This project is an open-source reference implementation of Huffman coding in Java.
	The code is intended to be used for study, and as a solid basis for modification
	and extension. As such, it is optimized for clear logic and low complexity,
	not speed/memory/performance.


	Description
	-----------

	An overview of the code is given here: [http://nayuki.eigenstate.org/page/huffman-coding-java](http://nayuki.eigenstate.org/page/huffman-coding-java)


	License
	-------

	(MIT License)

	Copyright © 2013 Nayuki Minase

	Permission is hereby granted, free of charge, to any person obtaining a copy of
	this software and associated documentation files (the "Software"), to deal in
	the Software without restriction, including without limitation the rights to
	use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
	the Software, and to permit persons to whom the Software is furnished to do so,
	subject to the following conditions:

	* The above copyright notice and this permission notice shall be included in
	all copies or substantial portions of the Software.

	* The Software is provided "as is", without warranty of any kind, express or
	implied, including but not limited to the warranties of merchantability,
	fitness for a particular purpose and noninfringement. In no event shall the
	authors or copyright holders be liable for any claim, damages or other
	liability, whether in an action of contract, tort or otherwise, arising from,
	out of or in connection with the Software or the use or other dealings in the
	Software.

ByteCompressor: jSnappy
=======================

	Java impmenentation of compression algorithm Snappy from Google. 
	This is a Avast a.s. fork of http://code.google.com/p/jsnappy/ , 
	it adds bytecompressor layer compatibility.

	
	License
	-------

	(Apache 2.0 License)

	Copyright 2011 Tor-Einar Jarnbjo

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.

ByteCompressor: ZLIB
=======================

	Bytecompressor abstraction for Zlib and Gzip compression algorithm from standard Java package java.util.zip.

	Both the zlib and gzip formats use the same compressed data format internally, but have different headers
	and trailers around the compressed data.
