package testGyro;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.Motor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class TestGyro {
	
	static EV3GyroSensor gyro;
	static SampleProvider sampleProvider;
	static float[] sample;
	static final byte position_angle_dans_sample = 0;
	static int nombre_de_degre_par_rotation_moteur = 61;
	
	
	
	static void  init(){
		Port gyroPort = LocalEV3.get().getPort("S2");       
		gyro = new EV3GyroSensor(gyroPort);
		sampleProvider =  gyro.getAngleMode();
		sample = new float[1];

	}
	
	static void calibrate(){
		gyro.reset();
		Motor.A.rotate(360);
		sampleProvider.fetchSample(sample, 0);
		nombre_de_degre_par_rotation_moteur = (int)sample[position_angle_dans_sample];
	}
	
	public static void main(String[] args) throws Exception {
		
		init();

		//calibrate();
		
		for( int i = 0; i < 4 ; i++ ){
			turn(-90);
			Delay.msDelay(500);
		}
		for(int i = 0 ; i < 8 ; i++){
			turn(45);
			Delay.msDelay(500);
		}
		
	}
	
	
	static int relAngle;
	static void turn(int angle){

		NXTRegulatedMotor motor = Motor.B; //TODO: ne pas oublier de changer la rotation si on change de moteur pivot
		angle %= 360;
		System.out.println("angle demande: " + angle);
		
		gyro.reset();//ancrage
		float mesure;
		do{
			sampleProvider.fetchSample(sample, 0);
			mesure = sample[position_angle_dans_sample];
			relAngle =  (int) ( (angle - mesure) * 100 / nombre_de_degre_par_rotation_moteur);
			motor.rotate( - relAngle, true);
			Delay.msDelay(50);
			//TODO: attention on pourrait rester bloqué, définir un mouvement minimum
		}while( ( angle > 0 && mesure < angle  )  || ( angle < 0 && mesure > angle) );
	}
	
	
	
	
}
