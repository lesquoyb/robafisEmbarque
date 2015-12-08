package sensors;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;

public class UltrasonicSensor {

	private EV3UltrasonicSensor sensor;
	private SampleProvider sampler;
	private static float[] sample;

	public UltrasonicSensor(){
		sensor = new EV3UltrasonicSensor(LocalEV3.get().getPort("S2"));
		sampler = sensor.getDistanceMode();
		sample = new float[1];
	}
	
	public float getDistance(){
		sampler.fetchSample(sample, 0);
		return sample[0];
	}
	
	public void close(){
		sensor.close();
	}
}
