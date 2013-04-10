/**
 * 
 */
package edu.berkeley.cs162;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 * @author amos0528
 *
 */
public class PlayConsole {

	public static SocketServer server = null;
	public static KVServer key_server = null;	
	
	private static class ServerThread extends Thread{
		PrintStream out = null;
		public ServerThread(PrintStream out){
			super();
			this.setName("ServerThread");
			this.out = out;
		}
		
		public void run(){
			try{
				KVServer key_server1 = new KVServer(100, 10);
				SocketServer server1 = new SocketServer("localhost", 8080);
				
				server = server1;
				key_server = key_server1;
				
				NetworkHandler handler = new KVClientHandler(key_server);
				server.addHandler(handler);
				server.connect();
				out.println("server is ready");
				server.run();
			}catch(Exception e){
				out.println("socket is closed");
			}
		}	
	}
	
	public static void main(String [] args){
		ServerThread st = new ServerThread(System.out);
		st.start();
		
		KVClient kc = new KVClient("localhost", 8080);
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String input = null;
		try{
			while ((input=reader.readLine())!=null){
				if (input.equals("quit")){
					handleQuit();
					System.exit(0);
				}
				
				String [] inputs = input.split(",");
				
				if (input.length() < 1 || input.length()>3){
					System.out.println("could not recognize ur command");
				}
				
				String command = inputs[0];
				if (command.equals("put")){
					if (inputs.length!=3){
						System.out.println("could not recognize ur command");
						continue;
					}
					kc.put(inputs[1],inputs[2]);
				}else if(command.equals("get")){
					if (inputs.length!=2){
						System.out.println("could not recognize ur command");
						continue;
					}
					kc.get(inputs[1]);
				}else if(command.equals("del")){
					if (inputs.length!=2){
						System.out.println("could not recognize ur command");
						continue;
					}
					kc.del(inputs[1]);
					
				//Extend so that it can accept more commands, such as dumpCache and cumpStore
				}else{
					System.out.println("could not recognize ur command");
				}
				
			}
		}catch(Exception e){
			e.printStackTrace();
			System.exit(1);
		}
		
	}
	
	public static void handleQuit(){
		System.out.println("quiting");
		server.stop();
	}
}
