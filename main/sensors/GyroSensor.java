package sensors;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.robotics.SampleProvider;

public class GyroSensor {


	private EV3GyroSensor gyro;
	private SampleProvider gyroSampler;
	private static float[] gyroSample;
	
	
	static final byte position_angle_dans_sample = 0;
	static int nombre_de_degre_par_rotation_moteur = -101;
	static final int rotation_minimale_pour_se_deplacer = 6;
	
	
	public GyroSensor(){
		gyro = new EV3GyroSensor(LocalEV3.get().getPort("S1"));
		gyroSampler =  gyro.getAngleMode();
		gyroSample = new float[1];
	}
	
	public void calibrate(NXTRegulatedMotor motor){
		gyro.reset();
		motor.rotate(360);
		gyroSampler.fetchSample(gyroSample, 0);
		nombre_de_degre_par_rotation_moteur = (int)gyroSample[position_angle_dans_sample];
		System.out.println(nombre_de_degre_par_rotation_moteur);
	}
	
	public int getValue(){
		gyroSampler.fetchSample(gyroSample, 0);
		return (int)gyroSample[0];
	}
	
	public void reset(){
		gyro.reset();
	}
	
	public void close(){
		gyro.close();
	}

	
}
