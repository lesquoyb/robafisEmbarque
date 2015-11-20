package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Calendar;
import java.util.Date;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.Motor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;
import lejos.utility.TextMenu;



public class Robot {


	private SampleProvider colorSampler;
	private float[] colorSample;
	private EV3ColorSensor colorSensor;
	private void initColorSensor(){
		colorSensor = new EV3ColorSensor(LocalEV3.get().getPort("S3"));
		colorSampler = colorSensor.getRedMode();
		colorSample = new float[3];
	}

	public ReadColor black = new ReadColor(15, 15, 15), 
			white = new ReadColor(255,255,255),
			objectif = new ReadColor(135, 135, 135);
	private void calibrateBlackAndWhite(){
		white = new ReadColor(0, 0, 0);
		black = new ReadColor(255, 255, 255);
		ReadColor tmp;
		for(int i = 0 ; i < 36 ; i++){
			turn(10);
			tmp = readColor();
			if(tmp.isClearerThan(white)){
				white = tmp;
			}
			else if (tmp.isDarkerThan(black)){
				black = tmp;
			}
		}
		int average = white.getAverage() - black.getAverage();
		objectif = new ReadColor(average, average, average);
	}
	public ReadColor readColor(){
		colorSampler.fetchSample(colorSample, 0);
		return new ReadColor(Math.min( (int) (colorSample[0]*255), 255), 
				Math.min( (int) (colorSample[0]*255), 255),
				Math.min( (int) (colorSample[0]*255), 255)); 
		//valide seulement avec lecture d'une couleur
		//TODO: avec les trois													
	}



	private EV3GyroSensor gyro;
	private SampleProvider gyroSampler;
	private static float[] gyroSample;
	private final NXTRegulatedMotor pivotRotation = Motor.B;
	private final NXTRegulatedMotor motorRotation = Motor.A;
	private final NXTRegulatedMotor motorL = Motor.B;
	private final NXTRegulatedMotor motorR = Motor.A;
	private void  initGyro(){
		gyro = new EV3GyroSensor(LocalEV3.get().getPort("S2"));
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

	
	public Robot() throws Exception{

		//initBluetooth();
		initColorSensor();
		//initGyro();
	}


	private void initBluetooth() throws IOException{
		
		BluetoothServer server = new BluetoothServer(this);
		Thread t = new Thread(server);
		t.start();
		
	}

	
	public void close(){
		colorSensor.close();
		gyro.close();
	}

	public void calibrer(){
		//Sérialiser les valeurs ?
		calibrateBlackAndWhite();
		System.out.println("black: " + black + "\n white: " +white);
		Delay.msDelay(10000);
		calibrateGyro();
	}
	
	

	public void choisirScenario() throws Exception{
		String[] menu = new String[]{"Rouge","Vert","Jaune", "Calibrer", "Quitter"};
		TextMenu m = new TextMenu(menu);
		int index = m.select();
		ReadColor selectedColor = null;
		switch(menu[index]){
		case "Rouge":
			selectedColor = ReadColor.Red;
			break;

		case "Vert":
			selectedColor = ReadColor.Green;				
			break;

		case "Jaune":
			selectedColor = ReadColor.Yellow;
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


	private boolean colorDetected(ReadColor read, ReadColor goal) throws Exception{
		//TODO
		//colorSensor.setFloodlight(lejos.robotics.Color.BLUE);
		
		return false;
	}


	private int delay = 10;//Taux de rafraichissement pendant le suivit de ligne
	private final double wheel_diam = 5.5;
	private final double perim = wheel_diam * Math.PI;
	private final double tacho_per_cm = 360/perim;//TODO: cette valeur est fauuuuuuuuuuuuuuuuuuuuuuuuuuuusssssssssseeeeeeeeeeeeeeeeeeee
	private final double max_cm_in_white = 5.5;
	private final double marge_cm_in_white = .0; //TODO: j'ai fait � l'arrache, v�rifier/affiner les valeurs
	private long begin;
	public void followLine(ReadColor color) throws Exception{
		
		startup();
		
		//move_until_white();
		
		int baseSpeed = 600,
			minSpeed = 20;


		double kp , virage, error;

		ReadColor read = readColor() ;


		colorSensor.setFloodlight(true);
		
		kp = .35;
		
		int marge = 50;
		
		initBegin();
		
		while( ! colorDetected(read, color) ){
			
			read = readColor();

			error = objectif.getAverage() - read.getAverage();
			
			virage = kp * error ;
			
			if( read.getAverage()  >= (white.getAverage() - marge) ){
				System.out.println("blanc");
				if ( - motorL.getTachoCount() - begin >= tacho_per_cm * (max_cm_in_white+marge_cm_in_white) ){
				
					takeBend();

					System.out.println("virage");
					initBegin();
				}
			}
			else{
				initBegin();
			}
			
			motorL.setSpeed((int) Math.min( Math.max( baseSpeed - virage, minSpeed), 720));
			motorR.setSpeed((int) Math.min( Math.max(  baseSpeed +  virage, minSpeed), 720));

			Delay.msDelay(delay);

		}
	
		motorL.stop();
		motorR.stop();

		Sound.beep();
	}
	
	
	private void startup(){
		motorL.setSpeed(1);
		motorR.setSpeed(1);
		
		motorL.backward();
		motorR.backward();
	}
	
	private void move_until_white(){
		int speed = 300;


		motorL.setAcceleration(8000);
		motorR.setAcceleration(8000);
		ReadColor read = readColor();
		int marge = 50;
		do{

			motorL.setSpeed(speed);
			motorR.setSpeed(speed);
			Delay.msDelay(delay);
			read = readColor();
			speed += 5;
		}while(read.getAverage() <= objectif.getAverage() - marge);

	}
	
	private void initBegin(){
		begin = - motorL.getTachoCount();
	}
	
	private void takeBend(){
		motorL.setSpeed(300);
		motorR.setSpeed(20);
		ReadColor read = readColor();
		int marge = 50;
		do{
			Delay.msDelay(delay);
			read = readColor();
		}while(read.getAverage() >= black.getAverage() + marge);

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
