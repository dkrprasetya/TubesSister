package com.moedikra.tubes3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Server yang melakukan pemrosesan request dan melakukan penyimpanan data.
 * Menggunakan ServerThread untuk menjadi "klien" bagi server lain.
 */
public class Server {
	
	// server uji
	public final static int N_SERVER = 4;
	public final static String[] serverHostNames = { "167.205.33.194", "167.205.33.196", "167.205.33.197", "167.205.33.220" };
	public final static int[] serverHostPorts = { 5000, 5020, 5040, 5060 };
	
	/*
	//server lokal
	public final static int N_SERVER = 2;
	public final static String[] serverHostNames = { "192.168.1.18", "192.168.1.17" };
	public final static int[] serverHostPorts = { 5040, 5020 };
	*/
	
	public static String getKey(String elemen)
	{
		return elemen.substring(1, elemen.indexOf(","));
	}
	
	public static String getCurrentTimeStamp()
	{
		java.util.Date date = new java.util.Date();
		return (new Timestamp(date.getTime())).toString();
	}
	
	public static boolean checkKeyExistInMap(HashMap<String, ArrayList<String>> m, String key)
	{
		return (m.get(key)!=null);
	}
	
	public static void readFromFile(String fileName, HashMap<String, ArrayList<String>> m)
	{
		/* Reset hashmap */
		m.clear();
		
		/* Fill hashmap with file data */
		try (BufferedReader br = new BufferedReader(new FileReader(fileName)))
		{
			String line;

			while ((line = br.readLine()) != null) {
				String k = line;
				int n_vals = Integer.parseInt(br.readLine());
				ArrayList<String> vals = new ArrayList<String>();
				for (int i = 0; i < n_vals; i++){
					vals.add(br.readLine());
				}
				
				m.put(k, vals);
			}
 
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	public static void saveToFile(String fileName, HashMap<String, ArrayList<String>> m)
	{
		try
        {
			/* File database */
			File file = new File(fileName);
			
			/* Hapus file original */
			file.delete();
			
			PrintWriter pw = new PrintWriter(new FileWriter(file));

			/* Print database ke file temporary */
			for (Map.Entry<String, ArrayList<String>> entry : m.entrySet()){
				String k = entry.getKey();
				ArrayList<String> vals = entry.getValue();
				
				pw.println(k);
				pw.println(vals.size());
				for (String val : vals) {
					pw.println(val);
				}
			}
			
			pw.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
	}
	
	
    public static void main(String[] args) throws IOException {
		
		/*
			Struktur yang dipakai: <nama_table>,<list_of_value>
		*/
    	
        HashMap<String, ArrayList<String>> map = new HashMap<>();
        String fileDB = "db.txt";
        
		if (args.length != 1) {
            System.err.println("Usage: java KnockKnockServer <port number>");
            System.exit(1);
        }
		
        int portNumber = Integer.parseInt(args[0]);        
        ArrayList<ServerThread> servers = new ArrayList<ServerThread>();
        
        try ( 
            ServerSocket serverSocket = new ServerSocket(portNumber);
            Socket clientSocket = serverSocket.accept();
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ) {
        	readFromFile(fileDB, map); // init hashmap
        	
            String inputLine; //data dari client

            inputLine = in.readLine();
            
            if (inputLine.equals("client")){
            	for (int i = 0; i < N_SERVER; i++){
                	if (serverHostPorts[i] != portNumber){
                		ServerThread serverThread = new ServerThread(serverHostNames[i], serverHostPorts[i]);
                		serverThread.start();
                		servers.add(serverThread);
                	}
                }
            }
            
            while ((inputLine = in.readLine()) != null) 
			{
				if(inputLine.contains("create table"))
				{
					String[] parts = inputLine.split(" ");
					if(parts.length != 3)
					{
						out.println("create table <nama_tabel>");
					}
					else
					{
						String nama_table = parts[2];
						
						if(!checkKeyExistInMap(map, nama_table))
						{
							map.put(nama_table, new ArrayList<String>());
							out.println("tabel " + nama_table + " berhasil dibuat");
							
							for (ServerThread server : servers){
								server.doCreate(nama_table);
							}
							
							saveToFile(fileDB, map);
						}
						else
						{
							out.println("tabel " + nama_table + " sudah ada");
						}
					}
				}
				else if(inputLine.contains("display"))
				{
					String[] parts = inputLine.split(" ");
					if(parts.length != 2)
					{
						out.println("display <name_table>");
					}
					else
					{
						String nama_table = parts[1];
						if(!checkKeyExistInMap(map, nama_table))
						{
							out.println("tabel " + nama_table + " tidak ada");
						}
						else
						{
							String tmp = "";
							
							ArrayList<String> KeyList = new ArrayList<>();
							for(String elemen: (map.get(nama_table)))
							{
								/* 
									Karena elemen ditambahkan secara LIFO
									jadi elemen teratas pasti yang paling baru
									jadi cukup dicari si elemen dengan key ini
									pernah muncul atau enggak.
								*/
								
								if(!KeyList.contains(getKey(elemen)))
								{
									KeyList.add(0, getKey(elemen));
									tmp += elemen + "&";
								}
							}
							
							for (ServerThread server : servers){
								tmp += server.doDisplay(nama_table);
							}
							out.println(tmp);
						}
					}
				}
				else if(inputLine.contains("insert"))
				{
					String[] parts = inputLine.split(" ");
					if(parts.length != 4)
					{
						out.println("insert <nama_table> <key> <value>");
					}
					else
					{
						String nama_table = parts[1];
						String key = parts[2];
						String value = parts[3];
						for (ServerThread server : servers){
							int hash = key.hashCode();
							if (hash % servers.size() == servers.indexOf(server)) {
								server.doInsert(nama_table, key, value);
							}
						}
						if(!checkKeyExistInMap(map, nama_table))
						{
							out.println("tabel " + nama_table + " tidak ada");
						}
						else
						{
							String timestamp = getCurrentTimeStamp();
							
							String elemen = "<" + key + ", " + value + ", " + timestamp + ">";
							
							(map.get(nama_table)).add(0, elemen); //tambahkan elemen ke ArrayList table
							out.println(elemen + " berhasil ditambahkan");
							
							saveToFile(fileDB, map);
						}
					}
				}
				else
				{
					out.println("Perintah yang Anda masukkan salah");
				}
            }
        } catch (IOException e) {
        	for (ServerThread server : servers){
        		server.doExit();
        	}
            System.out.println("Exception caught when trying to listen on port "
                + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }
}
