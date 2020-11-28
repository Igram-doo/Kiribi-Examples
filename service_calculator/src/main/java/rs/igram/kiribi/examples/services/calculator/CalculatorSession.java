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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import rs.igram.kiribi.io.VarInput;
import rs.igram.kiribi.io.VarOutput;
import rs.igram.kiribi.service.Message;
import rs.igram.kiribi.service.Scope;
import rs.igram.kiribi.service.Service;
import rs.igram.kiribi.service.ServiceAddress;
import rs.igram.kiribi.service.ServiceException;
import rs.igram.kiribi.service.ServiceId;
import rs.igram.kiribi.service.Session;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * 
 *
 * @author Michael Sargent
 */
public class CalculatorSession extends Session {
	private static final byte CLIENT_REQUEST_ADD	 		 = 1;
	private static final byte CLIENT_REQUEST_SUBTRACT  	 = 2;
	private static final byte CLIENT_REQUEST_MULTIPLY	 	 = 3;
	private static final byte CLIENT_REQUEST_DIVIDE 		 = 4;
	private static final byte SERVICE_RESPONSE_VALID_INPUT = 1;
	private static final byte SERVICE_RESPONSE_INVALID_INPUT = 2;

	protected CalculatorSession(Service service) {
		super(service);
	}
		
	protected CalculatorSession(Scope scope, ServiceAddress address) {
		super(scope, address);
	}

	// register response handlers
	protected void configure() {
		handle(CLIENT_REQUEST_ADD, this::add);
		handle(CLIENT_REQUEST_SUBTRACT, this::subtract);
		handle(CLIENT_REQUEST_MULTIPLY, this::multiply);
		handle(CLIENT_REQUEST_DIVIDE, this::divide);
	}
	
		
	// ---- responses ----
	Message add(Message request) throws IOException {
		double a = request.in().readDouble();
		double b = request.in().readDouble();
		double result = a + b;
		System.out.println("PROCESSED REQUEST - ADDED: " + result);
		Message response = request.respond(SERVICE_RESPONSE_VALID_INPUT);
		response.out().writeDouble(result);
		
		return response;
	}
		
	Message subtract(Message request) throws IOException {
		double a = request.in().readDouble();
		double b = request.in().readDouble();
		double result = a - b;
		System.out.println("PROCESSED REQUEST - SUBTRACTED: " + result);
		Message response = request.respond(SERVICE_RESPONSE_VALID_INPUT);
		response.out().writeDouble(result);
		
		return response;
	}
		
	Message multiply(Message request) throws IOException {
		double a = request.in().readDouble();
		double b = request.in().readDouble();
		double result = a * b;
		System.out.println("PROCESSED REQUEST - MULTPLIED: " + result);
		Message response = request.respond(SERVICE_RESPONSE_VALID_INPUT);
		response.out().writeDouble(result);
		
		return response;
	}
		
	Message divide(Message request) throws IOException {
		double a = request.in().readDouble();
		double b = request.in().readDouble();
		
		if (b == 0.0) {
			System.out.println("PROCESSED REQUEST - DIVIDED BY ZERO");
			Message response = request.respond(SERVICE_RESPONSE_INVALID_INPUT);
			response.out().writeUTF("Cannot divide by zero");
			return response;
		} else {
			double result = a / b;
			System.out.println("PROCESSED REQUEST - DIVIDED: " + result);
			Message response = request.respond(SERVICE_RESPONSE_VALID_INPUT);
			response.out().writeDouble(result);
		
			return response;
		}
	}
		
	// ---- requests ----
	public final double add(double a, double b, long timeout) throws ServiceException {
		try{
			return add(a, b).get(timeout, SECONDS);
		}catch(Exception e){
			throw new ServiceException(e);
		}
	}
		
	private Future<Double> add(double a, double b) throws IOException {
		final CompletableFuture<Double> future = new CompletableFuture<>();
		Message request = Message.request(CLIENT_REQUEST_ADD);
		request.out().writeDouble(a);
		request.out().writeDouble(b);
		request(
			request, 
			new ResponseAdapter(
				SERVICE_RESPONSE_VALID_INPUT, 
				response -> {
					double result = response.in().readDouble();
					future.complete(result);
				},
				error -> future.completeExceptionally(new IOException(error))
			)
		); 
	
		return future;
	}
	public final double subtract(double a, double b, long timeout) throws ServiceException {
		try{
			return subtract(a, b).get(timeout, SECONDS);
		}catch(Exception e){
			throw new ServiceException(e);
		}
	}
		
	private Future<Double> subtract(double a, double b) throws IOException {
		final CompletableFuture<Double> future = new CompletableFuture<>();
		Message request = Message.request(CLIENT_REQUEST_SUBTRACT);
		request.out().writeDouble(a);
		request.out().writeDouble(b);
		request(
			request, 
			new ResponseAdapter(
				SERVICE_RESPONSE_VALID_INPUT, 
				response -> {
					double result = response.in().readDouble();
					future.complete(result);
				},
				error -> future.completeExceptionally(new IOException(error))
			)
		); 
	
		return future;
	}
	public final double multiply(double a, double b, long timeout) throws ServiceException {
		try{
			return multiply(a, b).get(timeout, SECONDS);
		}catch(Exception e){
			throw new ServiceException(e);
		}
	}
		
	private Future<Double> multiply(double a, double b) throws IOException {
		final CompletableFuture<Double> future = new CompletableFuture<>();
		Message request = Message.request(CLIENT_REQUEST_MULTIPLY);
		request.out().writeDouble(a);
		request.out().writeDouble(b);
		request(
			request, 
			new ResponseAdapter(
				SERVICE_RESPONSE_VALID_INPUT, 
				response -> {
					double result = response.in().readDouble();
					future.complete(result);
				},
				error -> future.completeExceptionally(new IOException(error))
			)
		); 
	
		return future;
	}
	public final double divide(double a, double b, long timeout) throws ServiceException {
		try{
			return divide(a, b).get(timeout, SECONDS);
		}catch(Exception e){
			throw new ServiceException(e);
		}
	}
		
	private Future<Double> divide(double a, double b) throws IOException {
		final CompletableFuture<Double> future = new CompletableFuture<>();
		Message request = Message.request(CLIENT_REQUEST_DIVIDE);
		request.out().writeDouble(a);
		request.out().writeDouble(b);
		request(
			request, 
			new ResponseAdapter(
				SERVICE_RESPONSE_VALID_INPUT, 
				response -> {
					double result = response.in().readDouble();
					future.complete(result);
				},
				error -> future.completeExceptionally(new IOException(error))
			),
			new ResponseAdapter(
				SERVICE_RESPONSE_INVALID_INPUT, 
				response -> {
					String result = response.in().readUTF();
					future.completeExceptionally(new IllegalArgumentException(result));
				},
				error -> future.completeExceptionally(new IOException(error))
			)
		); 
	
		return future;
	}
 }
