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
 
package rs.igram.kiribi.examples.servers.natt;

import java.io.Console;

import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.util.Enumeration;

import rs.igram.kiribi.net.NetworkMonitor;
import rs.igram.kiribi.net.natt.NATTServer;

/**
 * <pre>
 * Options:
 *	  -p:[port]
 *	  -i:[network interface]
 * </pre>
 *
 * @author Michael Sargent
 */
public class Main {
	protected final Console console = System.console();
	protected NATTServer natt;
	
	public static void main(String[] args) {
		try{
			int port = NATTServer.SERVER_PORT;
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
			System.out.println("ERROR: " + t.getMessage());
			System.exit(1);
		}
	}
	
	protected void start(InetAddress inet, int port) {
		System.out.println("Starting NAT Server --");
		natt = new NATTServer();  
		natt.start(inet, port);
		try{
			final Console console = System.console();
			if(console != null){
				Thread t = new Thread(() -> {
					while(!Thread.currentThread().isInterrupted()){
						process(read());
					}
				}, "NATT Daemon");
				t.setDaemon(false);
				t.start();
			}else{
				Thread t = new Thread(() -> {
					try{
						Thread.sleep(Long.MAX_VALUE);
					}catch(InterruptedException e){
						return;
					}
				}, "NATT Daemon");
				t.setDaemon(false);
				t.start();
				System.out.println("no console");
			}
		}catch(Throwable t){
			t.printStackTrace();
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
/*		   
	static InetAddress internal() {
		try{
			for(Enumeration<NetworkInterface> e1 = NetworkInterface.getNetworkInterfaces(); e1.hasMoreElements();){
				NetworkInterface i = e1.nextElement();
				 if(!i.isLoopback() && i.isUp() && !i.toString().contains("Teredo") && !i.isVirtual()){
					for(Enumeration<InetAddress> e2 = i.getInetAddresses(); e2.hasMoreElements();){
						InetAddress a = e2.nextElement();
						if(a instanceof Inet4Address && !a.isLinkLocalAddress()){
							return a;
						}
					}
				}
			}			
		}catch(Exception e){
			// ignore
		}
		
		return null;
	}
	*/
}