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
 
package rs.igram.kiribi.examples.services.helloworld;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;

import rs.igram.kiribi.service.Message;
import rs.igram.kiribi.service.Scope;
import rs.igram.kiribi.service.Service;
import rs.igram.kiribi.service.ServiceAddress;
import rs.igram.kiribi.service.ServiceException;
import rs.igram.kiribi.service.Session;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * 
 *
 * @author Michael Sargent
 */
public class HelloWorldSession extends Session {
	private static final byte CODE = 0x01;
	
	HelloWorldSession(Service service) {
		super(service);
	}
		
	HelloWorldSession(Scope scope, ServiceAddress address) {
		super(scope, address);
	}
		
	protected void configure() {
		handle(CODE, this::respond);
	}
		
	// ---- requests ----
	public final String request(long timeout) throws ServiceException {
		try{
			return request().get(timeout, SECONDS);
		}catch(Exception e){
			throw new ServiceException(e);
		}
	}
		
	private Future<String> request() throws IOException {
		final CompletableFuture<String> future = new CompletableFuture<>();
		Message request = Message.request(CODE);
		request(
			request, 
			new ResponseAdapter(
				CODE, 
				response -> {
					String result = response.in().readUTF();
					System.out.println("PROCESSED REQUEST: " + result);
					future.complete(result);
				},
				error -> future.completeExceptionally(new IOException(error))
			)
		); 
	
		return future;
	}
 		
	// ---- responses ----
	Message respond(Message request) throws IOException {
		Message response = request.respond(CODE);
		response.out().writeUTF("Hello World!");
	
		return response;
	}
}
