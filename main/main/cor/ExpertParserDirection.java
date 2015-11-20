package main.cor;

import lejos.hardware.motor.Motor;
import main.Robot;
import main.Robot.Modes;

public class ExpertParserDirection extends ExpertParser {

	

	static final double dead_zone = 0.2;
	static final int XMAXSPEED = 500;
	static final int YMAXSPEED = 300;
	
	
	@Override
	public boolean _parse(String toParse, Robot robot) {
		if( toParse.startsWith("move:") ){

			double x_speed, y_speed;
			
			String[] val = toParse.split(":")[1].split(",");
			double x = new Double(val[1]);
			double y = new Double(val[0]);
			x = apply_dead_zone(x);
			y = apply_dead_zone(y);
		
			x_speed = XMAXSPEED*x;
			y_speed = YMAXSPEED*y;			
			
			if (x >= 0){
				Motor.A.forward();
				Motor.B.forward();	
				y_speed = - y_speed;
	
			} else {
				Motor.A.backward();
				Motor.B.backward();
			}
	
			Motor.A.setSpeed((int) (x_speed + y_speed));
			Motor.B.setSpeed((int) (x_speed - y_speed));
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
