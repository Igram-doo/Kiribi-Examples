package com.kiribi.services.lease;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.kiribi.Kiribi;
import com.kiribi.auth.Authenticator;
import com.kiribi.crypto.Key;
import com.kiribi.io.VarInput;
import com.kiribi.io.VarOutput;
import com.kiribi.service.Message;
import com.kiribi.service.ServiceId;
import com.kiribi.service.Session;

import static java.util.concurrent.TimeUnit.SECONDS;

import static com.kiribi.services.lease.LeaseProtocol.*;

public abstract class LeaseSession extends Session {
	protected static final ForkJoinPool executor = ForkJoinPool.commonPool();
    private Future<?> renewer;
    
    protected long id = -1;

	public LeaseSession(Key key, ServiceId id){
		super(Authenticator.SystemAuthenticator.factory(key), id);
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
			Message request = Message.request(CLIENT_REQUEST_RENEW);
			VarOutput out = request.out();
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
		final CompletableFuture<Void> future = new CompletableFuture<>();
		Message request = Message.request(CLIENT_REQUEST_CANCEL);
		VarOutput out = request.out();
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
					Kiribi.submit(() -> renew());
				}catch(Exception e){
					break;
				}
			}
		});
	}
	
	protected void canceled() {}
	
	protected static final Future<?> submitTask(Runnable task) {
		ForkJoinTask<?> t = ForkJoinTask.adapt(task);
		return executor.submit(t);
	}
}
