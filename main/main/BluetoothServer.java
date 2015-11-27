package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import main.cor.ParserFacade;

public class BluetoothServer {

	

	private ServerSocket server;
	private BufferedReader bufferReader;
	private Socket connected ;
	private Robot robot;
	public BluetoothServer(Robot r) {
		robot = r;
		
	}

	private void establishConnection(){
		
		boolean notConnected = true;
		while(notConnected){
			try{
				server = new ServerSocket (5000);
				System.out.println ("Waiting connexion ...");
				connected = server.accept();
				bufferReader = new BufferedReader(new InputStreamReader (connected.getInputStream()));
				System.out.println("Command Center found!");
				notConnected = false;
			}
			catch(Exception e){
				System.out.println("error: " + e.getMessage());
			}
		}
	}
	
	
	public void listen() {
		
		establishConnection();
		
		String fromclient;
		ParserFacade parser = new ParserFacade();
		
		while( ! connected.isClosed()) {
			try {
				
				fromclient = bufferReader.readLine();
				parser.parse(fromclient, robot);
				/*
				System.out.println("received: " + fromclient);
				if(parser.parse(fromclient, robot)){
					System.out.println("processed");
				}
				else{
					System.out.println("unprocessed");
				}
				*/
				
			} catch (IOException e) {
				
				e.printStackTrace();
				System.out.println(e.getMessage());
			}
		}
		
	}

	
	
}
