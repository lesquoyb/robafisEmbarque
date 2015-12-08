package main;

import lejos.hardware.Sound;
import lejos.hardware.motor.Motor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.robotics.Color;
import lejos.utility.Delay;
import sensors.ColorSensor;
import sensors.GyroSensor;
import sensors.UltrasonicSensor;



public class Robot {


	public static final boolean DEBUG  = false;
	
	
	

	public final NXTRegulatedMotor motorL = Motor.B;
	public final NXTRegulatedMotor motorR = Motor.A;
	public final NXTRegulatedMotor armsMotor = Motor.D;
	


	public BluetoothServer server;
	public Historic historic;
	
	
	public GyroSensor gyroSensor;
	public ColorSensor colorSensor;
	public Screen screen;
	public UltrasonicSensor ultrasonic;
	
	public Robot() throws Exception{

		if(DEBUG){
			historic = new Historic();
			ultrasonic = new UltrasonicSensor();
		}
		
		colorSensor = new ColorSensor();	
		gyroSensor = new GyroSensor();
		screen = new Screen();
		initBluetooth();
	}

	

	private void initBluetooth(){
		server = new BluetoothServer(this);
		server.establishConnection();
	}

	
	public void close(){
		colorSensor.close();
		gyroSensor.close();
	}
	
	
	
	public void do_scenario(){
		
		int selectedColor = 0;
		boolean selected = false;
		while(! selected)
		try {
			selectedColor = screen.selectScenario();
			selected = true;
		} catch (Exception e) {
			selected = false;
		}
		server.sendColor(selectedColor);
		followLine(selectedColor);
		colorSensor.close();
		turn(50);
		gyroSensor.close();
		if(DEBUG){
			ultrasonic.close();
		}
		Sound.beep();
		
		motorL.setSpeed(1);
		motorR.setSpeed(1);
		this.server.listen();
	
	}
	
	

	public int objectif = 170;
	private int delay = 10; //Taux de rafraichissement pendant le suivit de ligne
	private long begin;
	private final int MAX_SPEED = 550;
	private final int minSpeed = 20;
	private long last_pos = 0 ;
	public void followLine(int color) {
		
		startup();
		
		int baseSpeed = move_until_white();
		
		double kp , virage, error;

		int read ;
		
		kp = .2;
		
		
		initBegin();
		
		while( ! colorDetected(color) ){
			
			read = colorSensor.readRedMode();

			error = objectif - read;
			
			virage = kp * error ;
			
			if(colorSensor.isInWhite(read)){
				if ( isInWhiteSinceTooLong()){ 
						
						baseSpeed = takeBend();
						initBegin();
						if ( 	(last_pos == 0 && gyroSensor.getValue() % 360 < -150 )
							|| 	(last_pos != 0 && gyroSensor.getValue() < -360)	){ //TODO: tester �a proprement, r�flexion faite ce code merde s'il fait un 360 avant le second virage et qu'il refait des virage dans la derni�re ligne
							last_pos = getTacho();
						}
						continue;//Pour ne pas faire directement un mouvement avec les anciennes valeurs de virage
				}
			}
			else{
				initBegin();
			}
			
			baseSpeed = incSpeed(baseSpeed, MAX_SPEED);
			
			motorL.setSpeed((int) Math.min( Math.max( baseSpeed - virage, minSpeed), 720));
			motorR.setSpeed((int) Math.min( Math.max( baseSpeed + virage, minSpeed), 720));
			if(DEBUG){
				record(read,getTacho());
			}
			Delay.msDelay(delay);
		}
		

	}
	
	

	public boolean colorDetected(int goal) {
		if ( 	last_pos != 0 && (	( (getTacho() - last_pos ) / tacho_per_cm  > 15 && goal == Color.RED)
				||	( (getTacho() - last_pos )/tacho_per_cm   > 50 && goal == Color.YELLOW)
				|| 	( (getTacho() - last_pos )/tacho_per_cm   > 111 && goal == Color.GREEN)
				||	( colorSensor.readColor() == goal))
				){
			return true;
		}
		return false;
	}
	
	private int move_until_white(){
		int speed = minSpeed, read;
		do{
			speed = incSpeed(speed, MAX_SPEED);
			motorR.setSpeed(speed);
			motorL.setSpeed(speed);
			read = colorSensor.readRedMode();
			if(DEBUG){
				record(read, gyroSensor.getValue());
			}
			Delay.msDelay(delay);
		}while(read < objectif/2);
		return speed;
	}
	
	private void record(int color, long angle){
		historic.record(color, angle, motorL.getSpeed(), motorR.getSpeed(), ultrasonic.getDistance());
		server.sendHistoric(historic);
	}

	private int incSpeed(int initSpeed, int max_speed){
		return Math.min(initSpeed + 10, max_speed);
	}
	
	
	private long getTacho(){
		return - (motorL.getTachoCount() + motorR.getTachoCount())/2;
	}
	private final double wheel_diam = 7;
	private final double perim = wheel_diam * Math.PI;
	private final double tacho_per_cm = 360/perim;
	private double max_cm_in_white = 10;
	private boolean isInWhiteSinceTooLong(){
		 return getTacho() - begin >= tacho_per_cm * max_cm_in_white;
	}
	
	private void startup(){
		motorL.setSpeed(1);
		motorR.setSpeed(1);
		
		motorL.resetTachoCount();
		motorR.resetTachoCount();
		gyroSensor.reset();
		motorL.backward();
		motorR.backward();
	}
	
	private void initBegin(){
		begin = getTacho();
	}
	
	
	/**
	 * 
	 * @return la vitesse des moteurs � la sortie du virage
	 */
	private int takeBend(){
		int speed = 1;
		long prev = gyroSensor.getValue();
		motorL.setSpeed(speed);
		motorR.forward();
		motorR.setSpeed(1);
		
		int read;
		do{
			read = colorSensor.readRedMode();
			speed = incSpeed(speed, (int) (Math.max(read - objectif, 1)*1.5) );

			motorL.setSpeed(speed);
			motorR.setSpeed(speed);
			if(DEBUG){
				record(read, gyroSensor.getValue());
			}
			Delay.msDelay(delay);
			
		}while(read >= objectif);

		if(Math.abs(prev - gyroSensor.getValue()) > 45){
			do{
				read = colorSensor.readRedMode();
				speed = incSpeed(speed, (int) (Math.max(180 - read , 1)) );
	
				motorL.setSpeed(speed);
				motorR.setSpeed(speed);
				if(DEBUG){
					record(read, gyroSensor.getValue());
				}
				Delay.msDelay(delay);
				
			}while(read < 180);
		}

		motorR.backward();
		
		return speed;
	}
	
	

	
	public void turn(int angle){

		//TODO: dans l'autre sens aussi
		angle += gyroSensor.getValue();

		motorR.backward();
		motorL.forward();
		
		int speed = 50;
		do{
			speed = incSpeed(speed, 400);
			motorL.setSpeed(speed);
			motorR.setSpeed(speed);
			Delay.msDelay(10);
		}while( angle > gyroSensor.getValue() );
		motorL.stop(); //TODO: enlever ?
		motorR.stop();
	}




}
