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
 
package rs.igram.kiribi.examples.services.lease;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import rs.igram.kiribi.io.VarInput;
import rs.igram.kiribi.io.VarOutput;
import rs.igram.kiribi.service.Message;
import rs.igram.kiribi.service.Scope;
import rs.igram.kiribi.service.Service;
import rs.igram.kiribi.service.ServiceAddress;
import rs.igram.kiribi.service.ServiceId;
import rs.igram.kiribi.service.Session;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * 
 *
 * @author Michael Sargent
 */
public abstract class LeaseSession extends Session {
	// 30 seconds
	public static final long DEFAULT_LEASE_DURATION = 30*1000;
	
	private static final byte CLIENT_REQUEST_RENEW	 = 20;
	private static final byte CLIENT_REQUEST_CANCEL  = 21;
	private static final byte SERVICE_RESPONSE_RENEWED 	= 20;
	private static final byte SERVICE_RESPONSE_CANCELED = 21;

	protected static final ForkJoinPool executor = ForkJoinPool.commonPool();
    private Future<?> renewer;
    
    protected long id = -1;
	
	protected LeaseSession(Service service) {
		super(service);
	}
		
	protected LeaseSession(Scope scope, ServiceAddress address) {
		super(scope, address);
	}
	
	@Override
	public void close() {
		try{
			if(id != -1) cancel(id).get(3, SECONDS);
		}catch(Exception e){
			e.printStackTrace();
		}
		super.close();
	}	
	
	// ---- requests ----	
	protected void renew() {
		try{
			var request = Message.request(CLIENT_REQUEST_RENEW);
			var out = request.out();
			out.writeLong(id);
			request(request, 
				new ResponseAdapter(
					SERVICE_RESPONSE_RENEWED, 
					response -> {
						switch(response.code()){
						case SERVICE_RESPONSE_RENEWED:
							// continue
							break;
						case SERVICE_RESPONSE_CANCELED:
							// cancel renewer if exists
							if(renewer != null) renewer.cancel(true);
							canceled();
							break;
						default:
							// ?
							canceled();
						}
					},
					error -> canceled() // ?
				)
			); 
		}catch(IOException e){
			// ?
			canceled();
		}
	}
	
	protected  Future<Void> cancel(long id) throws IOException {	
		final var future = new CompletableFuture<Void>();
		var request = Message.request(CLIENT_REQUEST_CANCEL);
		var out = request.out();
		out.writeLong(id);
		request(
			request, 
			new ResponseAdapter(
				SERVICE_RESPONSE_CANCELED,
				response -> future.complete(null),
				error -> future.completeExceptionally(new IOException(error))
			)
		); 
		return future;
	}

	protected void manage(long lease) {
		if(renewer != null || lease < 5) return;
		final long interval = (lease * 4) / 5;
		renewer = submitTask(() -> {
			while(!Thread.currentThread().isInterrupted()){
				try{
					TimeUnit.MILLISECONDS.sleep(interval);
					executor.submit(() -> renew());
				}catch(Exception e){
					break;
				}
			}
		});
	}
	
	protected void canceled() {}
	
	protected static final Future<?> submitTask(Runnable task) {
		var t = ForkJoinTask.adapt(task);
		return executor.submit(t);
	}
}
