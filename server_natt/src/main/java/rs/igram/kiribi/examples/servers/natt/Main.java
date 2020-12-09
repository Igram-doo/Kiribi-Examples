package rs.igram.kiribi.examples.servers.natt;

import java.io.Console;

import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.util.Enumeration;

import rs.igram.kiribi.net.natt.NATTServer;

public class Main {
	protected final Console console = System.console();
	protected NATTServer natt;
	
	public static void main(String[] args) {
		int port = args.length > 0 ? Integer.parseInt(args[0]) : NATTServer.SERVER_PORT;
		new Main().start(port);		
	}
	
	protected void start(int port) {
		System.out.println("Starting NAT Server --");
		natt = new NATTServer();  
		natt.start(internal(), port);
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
}