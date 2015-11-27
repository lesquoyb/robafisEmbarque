package main;

import lejos.robotics.Color;
import lejos.utility.Delay;

public class Main {

	

	private int delay = 10;//Taux de rafraichissement pendant le suivit de ligne
	private final static double wheel_diam = 5.5;
	private final static double perim = wheel_diam * Math.PI;
	private final static double tacho_per_cm = 360/perim;
	private final static double max_cm_in_white = 15.0;
	private final static double marge_cm_in_white = .0; 
	private static long begin;
	
	
	
	public static void main(String... args) throws Exception{

		
		Robot robot = new Robot();
		//robot.server.listen();
		
		robot.followLine(Color.RED);
		
		/*
		while(true){
			System.out.println(robot.readRedMode());
			Delay.msDelay(10);
		}
		*/
		
		
		
	}
	
	
	

	
/*	
	public static void main(String[] args) throws Exception {
		Robot r = new Robot();
		//r.choisirScenario();
		r.followLine(r.black);

	}
	
	*/
}
