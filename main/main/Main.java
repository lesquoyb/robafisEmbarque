package main;

import lejos.hardware.motor.Motor;
import lejos.utility.Delay;

public class Main {

	

	private int delay = 10;//Taux de rafraichissement pendant le suivit de ligne
	private final static double wheel_diam = 5.5;
	private final static double perim = wheel_diam * Math.PI;
	private final static double tacho_per_cm = 360/perim;//TODO: cette valeur est fauuuuuuuuuuuuuuuuuuuuuuuuuuuusssssssssseeeeeeeeeeeeeeeeeeee
	private final static double max_cm_in_white = 15.0;
	private final static double marge_cm_in_white = .0; //TODO: j'ai fait à l'arrache, vérifier/affiner les valeurs
	private static long begin;
	
	
	
	public static void main(String... args) throws Exception{

		
		Robot robot = new Robot();
		
		robot.followLine(ReadColor.Red);
		
		while(true){
			System.out.println(robot.readColor().getAverage());
			Delay.msDelay(10);
		}
		
	}
	
	
	private static void initBegin(){
		begin = - Motor.B.getTachoCount();
	}
	
	

	
/*	
	public static void main(String[] args) throws Exception {
		Robot r = new Robot();
		//r.choisirScenario();
		r.followLine(r.black);

	}
	
	*/
}
