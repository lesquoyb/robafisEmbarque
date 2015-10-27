package main;

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
		colorSensor = new EV3ColorSensor(LocalEV3.get().getPort("S1"));
		colorSampler = colorSensor.getRGBMode();
		colorSample = new float[3];
	}
	
	public ReadColor black = new ReadColor(50, 50, 50), white = new ReadColor(100,100,100);
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
		//TODO: si on garde le noir comme la limite entre la bande blanche et la bande noire, il faut 
		// l'incrémenter proportionnellement à white - black
	}
	public ReadColor readColor(){
		colorSampler.fetchSample(colorSample, 0);
		return new ReadColor(Math.min( (int) (colorSample[0]*255), 255), 
							 Math.min( (int) (colorSample[1]*255), 255),
							 Math.min( (int) (colorSample[2]*255), 255)); 
	}
	
	
	
	private EV3GyroSensor gyro;
	private SampleProvider gyroSampler;
	private static float[] gyroSample;
	private final NXTRegulatedMotor pivotRotation = Motor.B;
	private final NXTRegulatedMotor motorRotation = Motor.A;
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
	
	
	
	public Robot(){
		init();
	}

	
	public void init(){
		initColorSensor();
		initGyro();
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
	
	
	private boolean colorDetected(ReadColor read, ReadColor goal) throws Exception{
		//TODO
		return false;
	}
	
	
	public void followLine(ReadColor color) throws Exception{

		
		int baseSpeed = 150;
		
		
		double kp = baseSpeed/2; //TODO: tester différentes valeurs
		double ki = 0; //TODO: idem
		double kd = baseSpeed/5; //TODO: idem
		
		double virage;
		

		double error, sum_errors = 0, last_error = 0, delta_error;
		
		ReadColor read = readColor();
		
		Motor.A.backward();
		Motor.B.backward();
		
		Motor.A.setSpeed((int) (baseSpeed ));
		Motor.B.setSpeed((int) (baseSpeed ));
		
		while( ! colorDetected(read, color) ){

			read = readColor();

			error = (black.getAverage() - read.getAverage() )/ (double)(white.getAverage()- black.getAverage());
			
			sum_errors += error;
			delta_error = error - last_error;
			last_error = error;
			
			virage = kp * error + ki * sum_errors + kd * delta_error;

			System.out.println(virage);
			
			Motor.A.setSpeed((int) Math.max(baseSpeed - virage,0));//TODO: ralentir dans les virages
			Motor.B.setSpeed((int) Math.max(baseSpeed + virage,0));

			Delay.msDelay(50);
		}
		Motor.A.stop();
		Motor.B.stop();
		
		Sound.beep();
	}
	
	
	
	private void turn(int angle){
		
		//TODO: ne pas oublier de changer la rotation si on change de moteur pivot
		angle %= 360;
		
		gyro.reset();//ancrage
		float mesure;
		int relAngle;
		do{
			gyroSampler.fetchSample(gyroSample, 0);
			mesure = gyroSample[position_angle_dans_sample];
			relAngle =  (int) ( (angle - mesure) * 100 / nombre_de_degre_par_rotation_moteur);
			relAngle = (relAngle > 0 ) ? Math.max(relAngle, rotation_minimale_pour_se_deplacer) : Math.min(relAngle, -rotation_minimale_pour_se_deplacer); //Pour éviter de rester bloqué
			pivotRotation.rotate( - relAngle, true);
			Delay.msDelay(50);
			//TODO: attention on pourrait rester bloqué, définir un mouvement minimum
		}while( ( angle > 0 && mesure < angle  )  || ( angle < 0 && mesure > angle) );
	}
	
}
