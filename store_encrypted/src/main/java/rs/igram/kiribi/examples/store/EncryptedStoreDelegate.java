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
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static java.nio.file.StandardOpenOption.*;
import static javax.crypto.Cipher.*;

import rs.igram.kiribi.io.VarInput;
import rs.igram.kiribi.io.VarInputStream;
import rs.igram.kiribi.io.VarOutput;
import rs.igram.kiribi.io.VarOutputStream;
import rs.igram.kiribi.store.*;

import static rs.igram.kiribi.io.ByteUtils.crop;

/**
 * 
 *
 * @author Michael Sargent
 */
public class EncryptedStoreDelegate extends StoreDelegate {
	private static final int AES_KEY_SIZE = 16; 
	
	private byte[] key;
	private AESCipher encrypter;
	private AESCipher decrypter;
	
	public EncryptedStoreDelegate(byte[] key, Path root, String... schema) throws IOException {
		super(root, schema);
		
		this.key = key;
		
		encrypter = new AESCipher();
		decrypter = new AESCipher();
	}

	@Override
	protected VarInputStream in(Path path) throws IOException {
		var b = Files.readAllBytes(path);
        return new VarInputStream(decrypt(b));
    }
    
    @Override
	protected void out(byte[] b, Path path) throws IOException {
		try(OutputStream out = Files.newOutputStream(path, CREATE, TRUNCATE_EXISTING, WRITE)){
        	out.write(encrypt(b));
        }
	}
	
	protected byte[] encrypt(byte[] b) throws IOException {
		if (encrypter == null) throw new IllegalStateException("Key exchange not complete");
		synchronized(encrypter) {
			try{
				encrypter.init(key);
				var M = b.length;
				var N = encrypter.getBlockSize();
				var buf = new byte[((M / N) * N) + (2 * N)];
				System.arraycopy(b, 0, buf, 0, M);
				return encrypter.encrypt(buf);
			}catch(Exception e){
				e.printStackTrace();
				throw new IOException(e);
			}
		}
	}
	
	protected byte[] decrypt(byte[] b) throws IOException {
		if (decrypter == null) throw new IllegalStateException("Key exchange not complete");
		synchronized(decrypter) {
			try{
				decrypter.init(key);
				return decrypter.decrypt(b);
			}catch(Exception e){
				e.printStackTrace();
				throw new IOException(e);
			}
		}
	}

	static final class AESCipher {
		private static final String AES = "AES";
		private static final String ALGORITHM = "AES/GCM/NoPadding";
		private static final int LEN_TAG = 128;
		private static final int LEN_NONCE = 12;
		private final SecureRandom secureRandom = new SecureRandom();
		private final javax.crypto.Cipher cipher;
		private SecretKeySpec spec;

		AESCipher() {
			try{
				cipher = javax.crypto.Cipher.getInstance(ALGORITHM, "SunJCE");
			}catch(Exception e){
				throw new RuntimeException("Cipher not available: "+ALGORITHM);
			}
		}
		
		void init(byte[] key) {
			var sk = crop(key, AES_KEY_SIZE);
			spec = new SecretKeySpec(sk, AES);
		}
		
		int getBlockSize() {
			return cipher.getBlockSize();
		}
		
		byte[] encrypt(byte[] b) throws GeneralSecurityException {		
			try{
				var nonce = new byte[LEN_NONCE];
				secureRandom.nextBytes(nonce);
				cipher.init(ENCRYPT_MODE, spec, new GCMParameterSpec(LEN_TAG, nonce));
				var encrypted = cipher.doFinal(b);

				var byteBuffer = ByteBuffer.allocate(LEN_NONCE + encrypted.length);
				byteBuffer.put(nonce);
				byteBuffer.put(encrypted);

				return byteBuffer.array();
			}catch(Exception e){
				throw new GeneralSecurityException("Encryption failed", e);
			}
		}

		byte[] decrypt(byte[] b) throws GeneralSecurityException {		
			try {
				var byteBuffer = ByteBuffer.wrap(b);
				var nonce = new byte[LEN_NONCE];
				byteBuffer.get(nonce);
				var encrypted = new byte[b.length - LEN_NONCE];
				byteBuffer.get(encrypted);
				cipher.init(DECRYPT_MODE, spec, new GCMParameterSpec(LEN_TAG, nonce));
				var decrypted = cipher.doFinal(encrypted);
				return decrypted;
			} catch (Exception e) {
				throw new GeneralSecurityException("Decryption failed", e);
			}
		}

		void reset() {
			throw new UnsupportedOperationException("Cipher reset not supported");
		}
	}
}
