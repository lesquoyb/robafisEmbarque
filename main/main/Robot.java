package main;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.Motor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.robotics.Color;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;
import lejos.utility.TextMenu;



public class Robot {


	private SampleProvider colorSampler;
	private SampleProvider redSampler;
	private EV3ColorSensor colorSensor;
	private float[] sample;
	
	public class Historic{
		
		int colors[] = new int[30000];
		long gyro[] = new long[30000];
		int distances[] = new int[30000];
		int index = 0;
		
		public void record(int color, long angle){
			colors[index] = color;
			gyro[index] = angle;
			index++;
		}
		
		public byte[] getHistoric(){
			String color = "colors: ", gyro = "gyro: ", dist = "dist: ";
			for(int i = 0 ; i < index+1 ;i++){
				color += Integer.toString(colors[i]) + " ";
				gyro += Long.toString(this.gyro[i]) + " ";
				//dist += Integer.toString(distances[i]) + " ";
			}
			return (color + "\n" + gyro + "\n" + dist + "\n").getBytes();
		}

	}

	private void initColorSensor(){
		colorSensor = new EV3ColorSensor(LocalEV3.get().getPort("S3"));
		redSampler = colorSensor.getRedMode();
		sample = new float[3];
		colorSampler = colorSensor.getColorIDMode();
		
	}



	public int readRedMode(){
		redSampler.fetchSample(sample, 0);
		return (int) (sample[0]*255);
	}
	
	public int readColor(){
		colorSampler.fetchSample(sample, 0);
		return (int) sample[0];
	}


	private EV3GyroSensor gyro;
	private SampleProvider gyroSampler;
	private static float[] gyroSample;
	public final NXTRegulatedMotor motorL = Motor.B;
	public final NXTRegulatedMotor motorR = Motor.A;
	public final NXTRegulatedMotor pivotRotation = motorR;//TODO: en fait c'est pas le pivot c'est l'autre, renommer please
	public final NXTRegulatedMotor armsMotor = Motor.D;
	
	
	private void  initGyro(){
		gyro = new EV3GyroSensor(LocalEV3.get().getPort("S1"));
		gyroSampler =  gyro.getAngleMode();
		gyroSample = new float[1];
	}
	static final byte position_angle_dans_sample = 0;
	static int nombre_de_degre_par_rotation_moteur = -101;
	static final int rotation_minimale_pour_se_deplacer = 6;
	private void calibrateGyro(){
		gyro.reset();
		pivotRotation.rotate(360);
		gyroSampler.fetchSample(gyroSample, 0);
		nombre_de_degre_par_rotation_moteur = (int)gyroSample[position_angle_dans_sample];
		System.out.println(nombre_de_degre_par_rotation_moteur);
	}

	


	
	public enum Modes {Teleguide, FollowLine};
	public BluetoothServer server;
	public Historic historic;
	
	public Robot() throws Exception{

		
		historic = new Historic();
		initColorSensor();
		initGyro();
		initBluetooth();
	}

	

	private void initBluetooth(){
		server = new BluetoothServer(this);
		server.establishConnection();
		
	}

	
	public void close(){
		colorSensor.close();
		gyro.close();
	}

	public void calibrer(){
		//S√©rialiser les valeurs ?
		//calibrateBlackAndWhite();
		//System.out.println("black: " + black + "\n white: " +white);
		calibrateGyro();
		
	}
	
	

	public void choisirScenario() throws Exception{
		String[] menu = new String[]{"Rouge","Vert","Jaune", "Calibrer", "Quitter"};
		TextMenu m = new TextMenu(menu);
		int index = m.select();
		int selectedColor = 0;
		switch(menu[index]){
		case "Rouge":
			selectedColor = Color.RED;
			break;

		case "Vert":
			selectedColor = Color.GREEN;				
			break;

		case "Jaune":
			selectedColor = Color.YELLOW;
			break;
		case "Calibrer":
			m.quit();
			calibrer();
			choisirScenario();
			return ;
		case "Quitter":
			m.quit();
			return;
		default:
			throw new Exception("Index du menu non g√©r√©: " + menu[index]);
		}
		
		followLine(selectedColor);		
		this.server.sendHistoric(historic);
		Sound.beep();
		turn(90);
		System.out.println("finito?");
		this.server.listen();
		go_home_you_re_drunk(selectedColor);
	}
	
	private void go_home_you_re_drunk(int selectedColor){
		
		boolean the_end = false;

		motorL.setSpeed(720);
		motorR.setSpeed(720);
		int read;

		initBegin();
		while( ! the_end ){
			read = readRedMode();
			if(isInBlack(read)){
				if(isInBlackSinceTooLong()){
					motorR.setSpeed(500);
					Delay.msDelay(100);
					read = readRedMode();
					if(isInBlack(read)){
						the_end = true;
					}
				}
			}
			else{
				initBegin();
			}
		}
		
		motorL.stop();
		motorR.stop();
		Sound.buzz();
	}
	
	public boolean isInBlack(int read){
		return read <= 40;
	}
	public boolean isInBlackSinceTooLong(){
		return getTacho() - begin >= tacho_per_cm * 3;
	}

	public boolean colorDetected(int goal) {

		//TODO: empÍcher de valider avec les mauvaises valeurs de gyro/cm parcourus
		if ( 	last_pos != 0 &&(	( (getTacho() - last_pos )/tacho_per_cm  > 15 && goal == Color.RED)
				||	( (getTacho() - last_pos )/tacho_per_cm   > 50 && goal == Color.YELLOW)
				|| 	( (getTacho() - last_pos )/tacho_per_cm   > 111 && goal == Color.GREEN))
				||	( readColor() == goal)
				){
			return true;
		}
		return false;
	
	}

	public int objectif = 170;
	private int delay = 10;//Taux de rafraichissement pendant le suivit de ligne
	private long begin;
	private final int MAX_SPEED = 600;//550;
	private long last_pos = 0 ;
	public void followLine(int color) throws Exception{
		
		startup();
		//move_until_white();
		
		int baseSpeed = 1,
			minSpeed = 20;


		double kp , virage, error;

		int read ;
		
		kp = .2;
		
		
		initBegin();
		
		while( ! colorDetected(color) ){
			
			read = readRedMode();

			error = objectif - read;
			
			virage = kp * error ;
			
			if(isInWhite(read)){
				if ( isInWhiteSinceTooLong()){ 
					baseSpeed = takeBend();
					initBegin();
					
					if(last_pos == 0 && getGyroValue() % 360 < -150 ){
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
			record(read,getTacho());
			Delay.msDelay(delay);
		}
		
		motorR.setSpeed(1);
		motorL.setSpeed(1);

	}
	
	private void record(int color, long angle){
		historic.record(color, angle);
		server.sendHistoric(historic);
	}

	private int incSpeed(int initSpeed, int max_speed){
		return Math.min(initSpeed + 20, max_speed);
	}
	
	
	private long getTacho(){
		return - (motorL.getTachoCount() + motorR.getTachoCount())/2;
	}
	private final double wheel_diam = 7;//TODO: refaire une mesure prÈcise
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
		motorL.backward();
		motorR.backward();
	}
	
	
	private boolean isInWhite(int read){
		return  read >= 205;
	}
	private void initBegin(){
		begin = getTacho();
	}
	
	
	/**
	 * 
	 * @return la vitesse des moteurs ‡ la sortie du virage
	 */
	private int takeBend(){
		int speed = 1;
		long prev = getGyroValue();
		motorL.setSpeed(speed);
		motorR.forward();
		motorR.setSpeed(1);
		
		int read;
		do{
			read = readRedMode();
			speed = incSpeed(speed, (int) (Math.max(read - objectif, 1)*1.5) );

			motorL.setSpeed(speed);
			motorR.setSpeed(speed);
			Delay.msDelay(delay);
			record(read, getGyroValue());
		}while(read >= objectif);

		if(Math.abs(prev - getGyroValue()) > 45){
			do{
				read = readRedMode();
				speed = incSpeed(speed, (int) (Math.max(180 - read , 1)) );
	
				motorL.setSpeed(speed);
				motorR.setSpeed(speed);
				Delay.msDelay(delay);
				record(read, getGyroValue());
			}while(read < 180);
		}
		
		motorL.setSpeed(1);
		motorR.setSpeed(1);
		
		Delay.msDelay(200);
		Sound.buzz();
		
		motorR.backward();
		
		
		return 1;
	}
	
	
	public int getGyroValue(){
		gyroSampler.fetchSample(gyroSample, 0);
		return (int)gyroSample[0];
	}

	
	public void turn(int angle){

		//TODO: ne pas oublier de changer la rotation si on change de moteur pivot
		//TODO: dans l'autre sens aussi
		gyroSampler.fetchSample(gyroSample, 0);//DegrÈ actuel
		angle += gyroSample[position_angle_dans_sample];
		pivotRotation.setSpeed(0);
		pivotRotation.stop();
		float mesure;
		double relAngle ;
		do{
			gyroSampler.fetchSample(gyroSample, 0);
			mesure = gyroSample[position_angle_dans_sample];
			relAngle = (angle - mesure) * 360 /(double) nombre_de_degre_par_rotation_moteur;
			relAngle = 		(relAngle > 0 ) 
						? Math.max(relAngle,  rotation_minimale_pour_se_deplacer) 
						: Math.min(relAngle, -rotation_minimale_pour_se_deplacer); //Pour √©viter de rester bloqu√©

			pivotRotation.rotate( (int)relAngle, false);	
			Delay.msDelay(50);
			//TODO: attention on pourrait rester bloqu√©, d√©finir un mouvement minimum
		}while( angle != mesure );

	}




}
