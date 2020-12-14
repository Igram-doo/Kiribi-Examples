/* 
 * MIT License
 * 
 * Copyright (c) 2020 Igram, d.o.o.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
 
package rs.igram.kiribi.examples.store;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import rs.igram.kiribi.io.Encodable;
import rs.igram.kiribi.io.VarInput;
import rs.igram.kiribi.io.VarOutput;

/**
 * 
 *
 * @author Michael Sargent
 */
public class Foo implements Encodable {
	static final SecureRandom random = new SecureRandom();
	
	static void random(byte[] bytes) {
		random.nextBytes(bytes);
	}

	static int random(int bound) {
		return random.nextInt(bound);
	}

	static long random() {
		return random.nextLong();
	}
	
	private long l;
   	private byte[] b;
   	   
   	public Foo() {
   		l = random();
   	   	int L = 10 + random(10);
   	   	b = new byte[L];
   	   	random(b);
   	}
   	   
   	public Foo(VarInput in) throws IOException {
   		l = in.readLong();
   	   	b = in.readBytes();
   	}
   	   
   	@Override
   	public void write(VarOutput out) throws IOException {
   	   	out.writeLong(l);
   	   	out.writeBytes(b);
   	}
   	   
   	@Override
   	public int hashCode() {return (int)l;}
   	
   	@Override
   	public boolean equals(Object o) {
   	   	if(o == null || !(o instanceof Foo)) return false;
   	   	Foo t = (Foo)o;
   	   	return l == t.l && Arrays.equals(b, t.b);
   	}
}