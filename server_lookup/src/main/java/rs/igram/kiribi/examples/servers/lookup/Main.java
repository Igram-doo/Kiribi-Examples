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
 
package rs.igram.kiribi.examples.servers.Lookup;

import java.io.Console;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;

import rs.igram.kiribi.net.NetworkMonitor;
import rs.igram.kiribi.net.lookup.LookupServer;

/**
 * <pre>
 * Options:
 *	  -p:[port]
 *	  -i:[network interface]
 * </pre>
 *
 * @author Michael Sargent
 */
public class Main implements Runnable {
	protected final Console console = System.console();
	protected LookupServer lookup;
	
	public static void main(String[] args) {
		try{
			int port = LookupServer.SERVER_PORT;
			String iname = null;
			
			for(int i = 0; i < args.length; i++) {
				String[] components = args[i].split(":");
				switch(components[0]){
				case "-p":
					port = Integer.parseInt(components[1]);
					break;
				case "-i":
					iname = components[1];
					break;
				default:
					break;
				}
			}
			
			InetAddress inet = iname == null ?
				NetworkMonitor.	inet() :
				NetworkMonitor.	inet(NetworkInterface.getByName(iname));
				
			new Main().start(inet, port);		
		
		} catch(Throwable t) {
			System.err.println("ERROR: " + t.getMessage());
			System.exit(1);
		}
	}
	
	protected void start(InetAddress inet, int port) throws IOException, SocketException {
		System.out.println("Starting Lookup Server: " + inet + ":" + port);
		lookup = new LookupServer();  
		lookup.start(new InetSocketAddress(inet, port));
		
		Thread t = new Thread(this, "NATT Server");
		t.setDaemon(false);
		t.start();
	}
	
	@Override
	public void run() {
		if(console != null){
			while(!Thread.currentThread().isInterrupted()){
				process(read());
			}
		} else {
			try{
				Thread.sleep(Long.MAX_VALUE);
			}catch(InterruptedException e){
				return;
			}
		}
	}
	
	protected String read() {
		if(console != null){
			return console.readLine();
		}else{
			return null;
		}
	}
	
	protected void write(String s) {
		if(console != null){
			console.writer().println(s);
		}else{
			
		}
	}
	
	protected void process(String cmd) {
		switch(cmd){
		// shutdown
		case "q":
			System.exit(0);
			break;
		default: 
			write("Unknown Command.");
			break;
		}
	}
}