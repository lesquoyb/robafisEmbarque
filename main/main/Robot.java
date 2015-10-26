package main;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.Motor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.internal.ev3.EV3MotorPort;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;
import lejos.utility.TextMenu;



public class Robot {

	
	enum Color{Yellow, Green, Red};
	
	
	private SampleProvider colorSampler;
	private EV3ColorSensor colorSensor;
	private void initColorSensor(){
		colorSensor = new EV3ColorSensor(LocalEV3.get().getPort("S1"));
		colorSampler = colorSensor.getRGBMode();
	}
	private void calibrateBlackAndWhite(){
		//TODO: flemme
	}
	
	private EV3GyroSensor gyro;
	private SampleProvider gyroSampler;
	private final NXTRegulatedMotor pivotRotation = Motor.B;
	private final NXTRegulatedMotor motorRotation = Motor.A;
	private void  initGyro(){
		gyro = new EV3GyroSensor(LocalEV3.get().getPort("S2"));
		gyroSampler =  gyro.getAngleMode();
		sample = new float[1];
	}
	static float[] sample;
	static final byte position_angle_dans_sample = 0;
	static int nombre_de_degre_par_rotation_moteur = 61;
	private void calibrateGyro(){
		gyro.reset();
		motorRotation.rotate(360);
		gyroSampler.fetchSample(sample, 0);
		nombre_de_degre_par_rotation_moteur = (int)sample[position_angle_dans_sample];
	}
	
	
	
	public Robot(){
		
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
		calibrateBlackAndWhite();
		calibrateGyro();
	}
	
	public void choisirScenario() throws Exception{
		String[] menu = new String[]{"Rouge","Vert","Jaune", "Quitter"};
		TextMenu m = new TextMenu(menu);
		int index = m.select();
		Color selectedColor = null;
		switch(menu[index]){
			case "Rouge":
				selectedColor = Color.Red;
				break;
				
			case "Vert":
				selectedColor = Color.Green;				
				break;
				
			case "Jaune":
				selectedColor = Color.Yellow;
				break;
				
			case "Quitter":
				m.quit();
				return;
			default:
				throw new Exception("Index du menu non géré: " + menu[index]);
		}
		Sound.beep();
		followLine(selectedColor);
	}
	
	
	private boolean colorDetected(int r, int g, int b, Color color) throws Exception{
		switch(color){
		//TODO: faire ça proprement
		case Green:
			return g > r + b;
		case Red:
			return r > g + b;
		case Yellow:
			return r + g/3 > b;
			default:
				throw new Exception("couleur non implémentée");
		}
	}
	
	
	private void followLine(Color color) throws Exception{

		float[] sample = new float[3];
		
		int r, g, b, t;
		int baseSpeed = 50;
		
		
		double kp = 2; //TODO: tester différentes valeurs
		double ki = 1; //TODO: idem
		double kd = 0; //TODO: idem
		
		double virage;
		

		double error, sum_errors = 0, last_error = 0;
		
		//ugly but who cares ?
		colorSampler.fetchSample(sample, 0);
		r = (int) (sample[0]*255);
		g = (int) (sample[1]*255);
		b = (int) (sample[2]*255);//TODO: ok, I care, I'll fix it someday
		
		
		Motor.A.backward();
		Motor.B.backward();
		while( ! colorDetected(r, g, b, color) ){
			colorSampler.fetchSample(sample, 0);

			r = (int) (sample[0]*255);
			g = (int) (sample[1]*255);
			b = (int) (sample[2]*255);
			
			t = (r+g+b)/3;

			//System.out.println(t);
			
			error = t - 20;
			sum_errors += error;
			
			
			
			virage = kp * error + ki * sum_errors;
			
			Motor.A.setSpeed((int) (baseSpeed + virage));
			Motor.B.setSpeed((int) (baseSpeed - virage));

			Delay.msDelay(50);
		}
		Motor.A.stop();
		Motor.B.stop();
		
		Sound.beep();
	}
	
	
	
	private void turn(int angle){
		
		//TODO: ne pas oublier de changer la rotation si on change de moteur pivot
		angle %= 360;
		System.out.println("angle demande: " + angle);
		
		gyro.reset();//ancrage
		float mesure;
		int relAngle;
		do{
			gyroSampler.fetchSample(sample, 0);
			mesure = sample[position_angle_dans_sample];
			relAngle =  (int) ( (angle - mesure) * 100 / nombre_de_degre_par_rotation_moteur);
			pivotRotation.rotate( - relAngle, true);
			Delay.msDelay(50);
			//TODO: attention on pourrait rester bloqué, définir un mouvement minimum
		}while( ( angle > 0 && mesure < angle  )  || ( angle < 0 && mesure > angle) );
	}
	
}
