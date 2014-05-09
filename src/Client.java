package com.moedikra.tubes3;
import java.io.*;
import java.net.*;
import java.util.Arrays;

public class Client {
    public static void main(String[] args) throws IOException {  
    	    	
        if (args.length != 2) {
            System.err.println(
                "Usage: java EchoClient <host name> <port number>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        try (
            Socket kkSocket = new Socket(hostName, portNumber);
            PrintWriter out = new PrintWriter(kkSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(kkSocket.getInputStream())); //ini nerima data dari server, utk fetch-nya in.readLine()
        ) {
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            String fromServer;
            String fromUser;
            
            out.println("client");
            
            while (true) {
				System.out.print("> ");
				fromUser = stdIn.readLine();
				
				if(fromUser.equals("exit"))
				{
					System.exit(0);
				}
				else
				{
					out.println(fromUser);
				}
				
				fromServer = in.readLine();
				
				if (fromServer.equals("")){
					System.out.println("tidak ada data pada tabel");
				} else {
					String[] result = fromServer.split("&");
					Arrays.sort(result);
					for (String s : result){
						System.out.println(s);
					}
				}
            }
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
