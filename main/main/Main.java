package main;

import lejos.robotics.Color;

public class Main {

	
	
	
	public static void main(String... args) throws Exception{

		
		Robot robot = new Robot();
		//Motor.A.setSpeed(1);
		//Motor.B.setSpeed(1);
		//robot.turn(90);
		robot.followLine(Color.RED);
		//robot.choisirScenario();
		//robot.turn(90);
		/*
		while(true){
			
			robot.readColor();
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
