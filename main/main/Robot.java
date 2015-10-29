package main;

import java.io.IOException;
import java.net.ServerSocket;

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

	public static final ReadColor Red = new ReadColor(255, 0, 0);
	public static final ReadColor Yellow = new ReadColor(255, 255, 0);
	public static final ReadColor Green = new ReadColor(0, 255, 0);
	public static final ReadColor Blue = new ReadColor(0, 0, 255);


	private SampleProvider colorSampler;
	private float[] colorSample;
	private EV3ColorSensor colorSensor;
	private void initColorSensor(){
		colorSensor = new EV3ColorSensor(LocalEV3.get().getPort("S3"));
		colorSampler = colorSensor.getRedMode();
		colorSample = new float[3];
	}

	public ReadColor black = new ReadColor(12, 12, 12), 
			white = new ReadColor(224,224,224),
			objectif = new ReadColor(118, 118, 118);
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
				Math.min( (int) (colorSample[0]*255), 255)); //TODO: valide seulement avec lecture d'une couleur
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

	
	public void lol(){
		
 
	}

	
	public enum Modes {Teleguide, FollowLine};

	
	public Modes mode;
	private ServerSocket Server ;
	public Robot() throws IOException{

		mode = Modes.FollowLine;
		initBluetooth();
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
			selectedColor = Red;
			break;

		case "Vert":
			selectedColor = Green;				
			break;

		case "Jaune":
			selectedColor = Yellow;
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

	public final ReadColor presqueRouge = new ReadColor(200, 200, 200);
	private boolean colorDetected(ReadColor read, ReadColor goal) throws Exception{
		if(read.isDarkerThan(presqueRouge)){
			//TODO
		}
		return false;
	}


	public void followLine(ReadColor color) throws Exception{


		int baseSpeed = 300,
			A_FOND = 500;


		double kp = 1; //TODO: tester différentes valeurs
		double ki = 1; //TODO: idem
		double kd = 2; //TODO: idem

		double virage;


		double error, sum_errors = 0, last_error = 0, delta_error;

		ReadColor read = readColor() ;

		motorL.backward();
		motorR.backward();
		
		int range = white.getAverage() - black.getAverage();

		double lol;
		colorSensor.setFloodlight(true);
		while( ! colorDetected(read, color) ){
			
			read = readColor();

			error = objectif.getAverage() - read.getAverage();
			sum_errors += error;
			delta_error = error - last_error;
			last_error = error;

			virage = kp * error + ki * sum_errors + kd * delta_error;

			System.out.println(virage);
			
			motorL.setSpeed((int) Math.min( Math.max( - virage, 150), 720));//TODO: ralentir dans les virages
			motorR.setSpeed((int) Math.min( Math.max(   virage, 150), 720));

			Delay.msDelay(10);

		}
		motorL.stop();
		motorR.stop();

		Sound.beep();
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
