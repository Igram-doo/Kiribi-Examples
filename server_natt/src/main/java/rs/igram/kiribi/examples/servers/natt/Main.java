package rs.igram.kiribi.examples.servers.natt;

import java.io.Console;

import rs.igram.kiribi.net.natt.NATTServer;

public class Main {
	protected final Console console = System.console();
	protected NATTServer natt;
	
	public static void main(String[] args) throws Exception {
		new Main().start();		
	//	}catch(Exception e){
	//		e.printStackTrace();
	//		System.exit(1);
	//	}	
	}
	
	protected void start() throws Exception {
		System.out.println("Starting NAT Server --");
		natt = new NATTServer();                     
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
}