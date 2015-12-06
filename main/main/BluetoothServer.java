package main;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;

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
				LCD.drawString("Waiting connexion ...", 0, 0);
				connected = server.accept();
				bufferReader = new BufferedReader(new InputStreamReader (connected.getInputStream()));
				LCD.clear();
				LCD.drawString("Command Center found!", 0, 0);
				
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
	
	
	
	static final double trigger_dead_zone = 0.4;
	int MAX_TRIGGER_SPEED = 300;
	
	public void listen() {
		
		String fromclient;
		try {
			bos = new BufferedOutputStream(connected.getOutputStream());
			bos.write( ("ready\n").getBytes());
			bos.flush();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		while( ! connected.isClosed()) {
			try {
				fromclient = bufferReader.readLine();
				if(fromclient.startsWith("trigger")){
					String val = fromclient.split(":")[1];
					int x = (int) (MAX_TRIGGER_SPEED *  apply_trigger_dead_zone(new Double(val)));
					if(fromclient.startsWith("triggerL")){
						robot.armsMotor.forward();
					}
					else{
						robot.armsMotor.backward();
					}
					robot.armsMotor.setSpeed(x);
				}
				else if( fromclient.startsWith("move:") ){

					int speedR, speedL;

					String[] val = fromclient.split(":")[1].split(",");
					speedL = new Integer(val[0]);
					speedR = new Integer(val[1]);
					robot.motorL.forward();
					robot.motorR.forward();
					
					if( speedL < 0 ){
						robot.motorL.backward();
						speedL = -speedL;
					}
					if( speedR < 0 ){
						robot.motorR.backward();
						speedR = -speedR;
					}
					
					int min = 1;			
					robot.motorL.setSpeed(Math.max((speedL), min));
					robot.motorR.setSpeed(Math.max((speedR), min));
				}
				else if( fromclient.startsWith("end_") ){
					Sound.beep();
					Sound.beep();
					LCD.clear();
					LCD.drawString("END !!!", 0, 0);
					break;
				}
				
			} catch (IOException e) {
				
				e.printStackTrace();
				System.out.println(e.getMessage());
			}
		}
		
	}
	
	
	private double apply_trigger_dead_zone(double x){
		if(x > -trigger_dead_zone && x < trigger_dead_zone)
			return 0;
		return x - trigger_dead_zone;
	}

	
	
}
