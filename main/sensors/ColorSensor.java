package sensors;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.Color;
import lejos.robotics.SampleProvider;

public class ColorSensor {


	private SampleProvider colorSampler;
	private SampleProvider redSampler;
	private EV3ColorSensor colorSensor;
	private float[] sample;
	public static final int WHITE = 205;
	
	
	public ColorSensor(){
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
	
	
	public void close(){
		colorSensor.close();
	}
	
	
	public boolean isInWhite(int read){
		return  read >= WHITE;
	}
	
}
