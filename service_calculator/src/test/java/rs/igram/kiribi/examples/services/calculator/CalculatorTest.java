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
 
package rs.igram.kiribi.examples.services.calculator;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import rs.igram.kiribi.crypto.*;
import rs.igram.kiribi.io.*;
import rs.igram.kiribi.net.*;
import rs.igram.kiribi.net.natt.*;
import rs.igram.kiribi.service.*;

/**
 * 
 *
 * @author Michael Sargent
 */
class CalculatorTest {
	static final PrivateKey KEY1 = Key.generateKeyPair().getPrivate();
	static final PrivateKey KEY2 = Key.generateKeyPair().getPrivate();
	
	static final int PORT1 = 7700;
	static final int PORT2 = 7701;
	static final int PORT3 = 7702;
	
	static final InetAddress LOCAL_HOST;
	static {
		try {
			LOCAL_HOST = InetAddress.getByName("127.0.0.1");
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	SocketAddress SA1 = new InetSocketAddress(LOCAL_HOST, NATTServer.SERVER_PORT);
	SocketAddress SA2 = new InetSocketAddress(LOCAL_HOST, NATTServer.SERVER_PORT);
	SocketAddress SA3 = new InetSocketAddress(LOCAL_HOST, PORT3);
	
	static final ServiceId ID = ServiceId.parse(1l);
	static final byte CODE = 0x01;
	
	static final String BOB = "Bob";
	static final String ALICE = "Alice";
	
	NetworkExecutor executor;	
	NATTServer server;
   	   	
	ServiceAdmin admin1;
	ServiceAdmin admin2;
	
	EntityManager mgr1;
	EntityManager mgr2;
	
	Entity bob;
	Entity alice;
	
   @Test
   public void testRestrictedConnect() throws IOException, InterruptedException, Exception {
   	   setup();
   	   configureEntities(Scope.RESTRICTED);
   	   ServiceAddress address = admin1.address(ID);
   	   CalculatorSession client = new CalculatorSession(Scope.RESTRICTED, address);
   	   
   	   client.connect(admin2);
   	   
   	   // add
   	   double result = client.add(1.0, 1.0, 5);
   	   assertEquals(2.0, result);
   	   
   	   // subtract
   	   result = client.subtract(1.0, 1.0, 5);
   	   assertEquals(0.0, result);
   	   
   	   // multiply
   	   result = client.multiply(2.0, 3.0, 5);
   	   assertEquals(6.0, result);
   	   
   	   // divide
   	   result = client.divide(6.0, 2.0, 5);
   	   assertEquals(3.0, result);
   	   
   	   // divide by zero
   	   String actual = null;
   	   try {
   	   	   result = client.divide(6.0, 0.0, 5);
   	   } catch(ServiceException e) {
   	   	   actual = e.getCause().getMessage();
   	   }
   	   System.out.println("FUCK: " + actual);
   	   assertEquals("java.lang.IllegalArgumentException: Cannot divide by zero", actual);
   	   
   	   shutdown();
   }
 	
	void setup() throws Exception {
		System.out.println("INET: " + LOCAL_HOST);
		executor = new NetworkExecutor();
		NetworkMonitor.monitor(executor);
		server = new NATTServer();
		server.start(LOCAL_HOST, NATTServer.SERVER_PORT);
		admin1 = new ServiceAdmin(KEY1, PORT1, SA1);
		admin2 = new ServiceAdmin(KEY2, PORT2, SA2);
		
		mgr1 = admin1.entityManager(new ArrayList<Entity>());
		mgr2 = admin2.entityManager(new ArrayList<Entity>());
	}
	
	void shutdown() throws Exception {
		admin1.shutdown();
		admin2.shutdown();
		server.shutdown();
	}
	
	void configureEntities(Scope scope) throws Exception {
		CountDownLatch latch = new CountDownLatch(2);
		bob = new Entity(true, address(KEY2).toString(), BOB);
		alice = new Entity(true, address(KEY1).toString(), ALICE);
		
		ServiceAddress address = admin1.address(ID); 
		CalculatorService service = new CalculatorService(address, scope);
		admin1.activate(service);
		
		Set<Descriptor> granted = Set.of(service.getDescriptor());
		bob.setGranted(granted);
		
		mgr1.setOnExchange(e -> latch.countDown());
		mgr1.add(bob);
		Thread.sleep(3000);
		
		mgr2.setOnExchange(e -> latch.countDown());
		mgr2.add(alice);
		Thread.sleep(3000);
		
		latch.await();
	}
	
	static Address address(PrivateKey key) {
		Key.Private privateKey = (Key.Private)key;
		return privateKey.address();
	}
}