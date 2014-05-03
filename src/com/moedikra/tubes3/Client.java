package com.moedikra.tubes3;
import java.io.*;
import java.net.*;

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
				String result = fromServer.replace("&", "\n");
				if(result.charAt(result.length()-1) == '\n')
				{
					result = result.substring(0, result.length()-1);
				}
				System.out.println(result);
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
