package main.cor;

import main.Robot;

public class ExpertParserDirection extends ExpertParser {

	

	static final double dead_zone = 0.3;
	static final int XMAXSPEED = 50;
	static final int YMAXSPEED = 50;
	
	
	@Override
	public boolean _parse(String toParse, Robot robot) {
		if( toParse.startsWith("move:") ){

			double x_speed, y_speed;
			
			String[] val = toParse.split(":")[1].split(",");
			double x = new Double(val[0]);
			double y = new Double(val[1]);
			x = apply_dead_zone(x);
			y = apply_dead_zone(y);
		
			x_speed = Math.abs(XMAXSPEED * x);
			y_speed = YMAXSPEED * y;
			
			if (x >= 0){
				robot.motorL.forward();
				robot.motorR.forward();	
	
			} else {
				robot.motorL.backward();
				robot.motorR.backward();
			}
			int min = 1;
			System.out.println("received speed: " + x_speed + " " + y_speed);
			robot.motorL.setSpeed(Math.max((int) (x_speed + y_speed), min));
			robot.motorR.setSpeed(Math.max((int) (x_speed - y_speed), min));
			return true;
		}
		return false;
	}

	
	private double apply_dead_zone(double x){
		if(x > -dead_zone && x < dead_zone)
			return 0;
		return x;
	}
	
}
