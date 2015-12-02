package main;

import lejos.hardware.motor.Motor;
import lejos.robotics.Color;
import lejos.utility.Delay;

public class Main {

	
	
	
	public static void main(String... args) throws Exception{

		
		Robot robot = new Robot();
		//Motor.A.setSpeed(1);
		//Motor.B.setSpeed(1);
		//robot.turn(90);
		robot.choisirScenario();
		
		
		/*while(true){
			
			robot.readColor();
			System.out.println(robot.readRedMode());
			Delay.msDelay(10);
		}*/
		
		
		//robot.server.listen();		
	}
	
	
	

	
/*	
	public static void main(String[] args) throws Exception {
		Robot r = new Robot();
		//r.choisirScenario();
		r.followLine(r.black);

	}
	
	*/
}
