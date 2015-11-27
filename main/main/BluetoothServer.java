package main;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import main.Robot.Historic;
import main.cor.ParserFacade;

public class BluetoothServer {

	

	private ServerSocket server;
	private BufferedReader bufferReader;
	private BufferedOutputStream bos;
	private Socket connected ;
	private Robot robot;
	public BluetoothServer(Robot r) {
		robot = r;
		
	}

	public void establishConnection(){
		
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
	
	public void sendHistoric(Historic h){
		if(connected != null){
			try {
				bos = new BufferedOutputStream(connected.getOutputStream());
				bos.write(h.getHistoric());
				bos.flush();
			} catch (IOException e) {
				e.printStackTrace();
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
				
				System.out.println("received: " + fromclient);
			
				/*
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
