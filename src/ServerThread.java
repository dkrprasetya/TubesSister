package com.moedikra.tubes3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class ServerThread extends Thread {
		
	private final String hostName;
	private final int portNumber;
	
	private String result;
	
	private boolean finished;	
	
	private PrintWriter out;
	private BufferedReader in;
	
	public ServerThread(String hostName, int portNumber) {
		super();
		
		this.hostName = hostName;
		this.portNumber = portNumber;
		finished = false;
	}
	
	public void doCreate(String table) throws IOException{
		out.println("create table " + table);
		in.readLine();
	}
	
	public String doDisplay(String table) throws IOException {
		out.println("display " + table);		
		return in.readLine();
	}
	
	public String doInsert(String table, String key, String val){
		return result;
	}
	
	public void doExit(){
		finished = true;
	}
	
	public void run(){
		try (
	            Socket kkSocket = new Socket(hostName, portNumber);
	            PrintWriter out = new PrintWriter(kkSocket.getOutputStream(), true);
	            BufferedReader in = new BufferedReader(new InputStreamReader(kkSocket.getInputStream())); //ini nerima data dari server, utk fetch-nya in.readLine()
	        ) {
			this.out = out;
			this.in = in;
				out.println("server");
	            while (!finished) {}
	        } catch (UnknownHostException e) {
	            System.err.println("Don't know about host " + hostName);
	            System.exit(1);
	        } catch (IOException e) {
	            System.err.println("Couldn't get I/O for the connection to " +
	                hostName);
	            System.exit(1);
	        }
	}
	
}
