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

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import rs.igram.kiribi.io.*;
import rs.igram.kiribi.store.*;

/**
 * 
 *
 * @author Michael Sargent
 */
public class EncryptedStoreDelegateTest {
	static final SecureRandom random;

 	static {
 	//	Security.setProperty("crypto.policy", "unlimited");
		try{
			random = SecureRandom.getInstance("SHA1PRNG", "SUN"); 
		}catch(Exception e){
			throw new RuntimeException("Could not initialize secure random",e);
		}
	}
	
	static void random(byte[] bytes) {
		random.nextBytes(bytes);
	}

	
   @TempDir Path root;
   String scheme = "a";
   
   @Test
   public void testPreexisting() throws IOException {
   	   assertTrue(Files.isDirectory(root));
   	   
   	   var key = key();
   	   
   	   var delegate = new EncryptedStoreDelegate(key, root, scheme); 
   	   var store = new ObjectStore(delegate, null);  	   
   	   assertFalse(delegate.preexisting());
   	   
   	   delegate = new EncryptedStoreDelegate(key, root, scheme); 
   	   store = new ObjectStore(delegate, null);  
   	   assertTrue(store.preexisting());
   }
	
   @Test
   public void testExists() throws IOException {
   	   var key = key();
   	   
   	   var delegate = new EncryptedStoreDelegate(key, root, scheme); 
   	   var store = new ObjectStore(delegate, null); 
   	   var name = "b";
   	   
   	   assertFalse(store.exists(name));
   	   
   	   var path = dir(root, scheme).resolve(name);
   	   Files.createFile(path);
   	   
   	   assertTrue(store.exists(name));
   }
	
   @Test
   public void testRemove() throws IOException {
   	   byte[] key = key();
   	   
   	   var delegate = new EncryptedStoreDelegate(key, root, scheme); 
   	   var store = new ObjectStore(delegate, null); 
   	   var name = "c";
   	   
   	   assertFalse(store.exists(name));
   	   
   	   var path = dir(root, scheme).resolve(name);
   	   Files.createFile(path);
   	   
   	   assertTrue(store.exists(name));
   	   store.remove(name);
   	   assertFalse(store.exists(name));
   }

   @Test
   public void testIO() throws IOException {
   	   var key = key();
   	   
   	   var delegate = new EncryptedStoreDelegate(key, root, scheme); 
   	   var store = new ObjectStore(delegate, null); 
   	   var name = "foo";
   	   
   	   var foo = new Foo();
   	   store.put(name, foo);
   	   // WTF??? shouldn'' need to cast
   	   var test = (Foo)store.get(name, Foo::new);
   	   
   	   assertEquals(foo, test);
   	   
   	   store = new ObjectStore(delegate, Foo::new); 
   	   
   	   foo = new Foo();
   	   store.put(name, foo);
   	   // WTF??? shouldn'' need to cast
   	   test = (Foo)store.get(name);
   	   
   	   assertEquals(foo, test);
   }
   
   static Path dir(Path root, String... schema) {
   	   var d = root;
		for(String s : schema) d = root.resolve(s);
		return d;
   }
   
   static byte[] key() {
   	   var key = new byte[16];
   	   random(key);
   	   return key;
   }
}