package main;

public class Main {

	
	
	
	public static void main(String... args) throws Exception{

		
		Robot robot = new Robot();
		//Motor.A.setSpeed(1);
		//Motor.B.setSpeed(1);
		//robot.turn(90);
		//robot.do_scenario();
		robot.server.listen();
		
		
		/*while(true){
			robot.readColor();
			System.out.println(robot.readRedMode());
			Delay.msDelay(10);
		}*/

	}
	
	
	

	
/*	
	public static void main(String[] args) throws Exception {
		Robot r = new Robot();
		//r.choisirScenario();
		r.followLine(r.black);

	}
	
	*/
}
