package main;

import lejos.robotics.Color;
import lejos.utility.Delay;

public class Main {

	
	
	
	public static void main(String... args) throws Exception{

		
		Robot robot = new Robot();
		//robot.server.establishConnection();
		robot.followLine(Color.GREEN);
		
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
