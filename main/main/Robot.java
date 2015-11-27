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
	public final NXTRegulatedMotor pivotRotation = Motor.B;
	public final NXTRegulatedMotor motorRotation = Motor.A;
	public final NXTRegulatedMotor motorL = Motor.B;
	public final NXTRegulatedMotor motorR = Motor.A;
	public final NXTRegulatedMotor armsMotor = Motor.D;
	
	
	private void  initGyro(){
		gyro = new EV3GyroSensor(LocalEV3.get().getPort("S1"));
		gyroSampler =  gyro.getAngleMode();
		gyroSample = new float[1];
	}
	static final byte position_angle_dans_sample = 0;
	static int nombre_de_degre_par_rotation_moteur = 61;
	static final int rotation_minimale_pour_se_deplacer = 3;//TODO à régler à la main avec le robot final
	private void calibrateGyro(){
		gyro.reset();
		motorRotation.rotate(360);
		gyroSampler.fetchSample(gyroSample, 0);
		nombre_de_degre_par_rotation_moteur = (int)gyroSample[position_angle_dans_sample];
	}

	


	
	public enum Modes {Teleguide, FollowLine};
	public BluetoothServer server;
	
	public Robot() throws Exception{

	//	initBluetooth();
	//	
		initColorSensor();
		initGyro();
	}

	

	private void initBluetooth(){
		server = new BluetoothServer(this);
		
	}

	
	public void close(){
		colorSensor.close();
		gyro.close();
	}

	public void calibrer(){
		//Sérialiser les valeurs ?
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
			throw new Exception("Index du menu non géré: " + menu[index]);
		}
		Sound.beep();
		followLine(selectedColor);
	}

	private int colorID = Color.NONE;
	public boolean colorDetected(int goal) {
		colorID = readColor();
		if ( colorID == goal){
			return true;
		}
		return false;
		
	}

	public int objectif = 135;
	private int delay = 10;//Taux de rafraichissement pendant le suivit de ligne
	private long begin;
	private final int INIT_SPEED = 100, MAX_SPEED = 250;//550;
	public void followLine(int color) throws Exception{
		
		startup();
		
		//move_until_white();
		
		int baseSpeed = 1,
			minSpeed = 20;


		double kp , virage, error;

		int read ;
		
		kp = .25;
		
		
		initBegin();
		
		while( ! colorDetected(color) ){
			
			read = readRedMode();

			error = objectif - read;
			
			virage = kp * error ;
			
			if(isInWhite(read)){
				if ( isInWhiteSinceTooLong()){ 
					baseSpeed = takeBend();
					initBegin();
					continue;//Pour ne pas faire directement un mouvement avec les anciennes valeurs de virage
				}
			}
			else{
				initBegin();
			}
			
			baseSpeed = incSpeed(baseSpeed, MAX_SPEED);
			
			motorL.setSpeed((int) Math.min( Math.max( baseSpeed - virage, minSpeed), 720));
			motorR.setSpeed((int) Math.min( Math.max( baseSpeed + virage, minSpeed), 720));

			Delay.msDelay(delay);

		}
		motorR.setSpeed(0);
		motorL.setSpeed(0);

		motorL.stop();
		motorR.stop();

		Sound.beep();
		
		//this.server.listen();
	}

	private int incSpeed(int initSpeed, int max_speed){
		return Math.min(initSpeed + 10, max_speed);
	}
	
	
	private long getTacho(){
		return - (motorL.getTachoCount() + motorR.getTachoCount())/2;
	}
	private final double wheel_diam = 5.5;//TODO: refaire une mesure pr�cise
	private final double perim = wheel_diam * Math.PI;
	private final double tacho_per_cm = 360/perim;
	private final double max_cm_in_white = 5.5;
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
		//return colorID == Color.WHITE;
		return  read >= 205;
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
		motorR.forward();
		motorL.setSpeed(speed);
		motorR.setSpeed(speed);
		Sound.beep();
		int read;
		do{
			read = readRedMode();
			speed = incSpeed(speed, Math.max(read - objectif, 1));
			motorL.setSpeed(speed);
			motorR.setSpeed(speed);
			Delay.msDelay(delay);
		}while(read >= objectif);
		/*speed = 50;
		motorL.setSpeed(speed);
		motorR.setSpeed(speed);
		do{
			read = readRedMode();
			Delay.msDelay(delay);
		}while(read <= objectif*1.3);
		
		*/
		motorR.backward();
		
		motorR.setSpeed(1);
		motorL.setSpeed(1);
		
		Delay.msDelay(200);
		
		Sound.buzz();
		return 1;
	}
	
	

	public void turn(int angle){

		//TODO: ne pas oublier de changer la rotation si on change de moteur pivot
		gyroSampler.fetchSample(gyroSample, 0);//Degr� actuel
		angle += gyroSample[position_angle_dans_sample];
		float mesure;
		int relAngle;
		do{
			gyroSampler.fetchSample(gyroSample, 0);
			mesure = gyroSample[position_angle_dans_sample];
			relAngle =  (int) ( (angle - mesure) * 100 / nombre_de_degre_par_rotation_moteur);
			relAngle = (relAngle > 0 ) ? Math.max(relAngle, rotation_minimale_pour_se_deplacer) : Math.min(relAngle, -rotation_minimale_pour_se_deplacer); //Pour éviter de rester bloqué
			pivotRotation.rotate( - relAngle, false);
			Delay.msDelay(50);
			//TODO: attention on pourrait rester bloqué, définir un mouvement minimum
		}while( angle != mesure );

	}




}
