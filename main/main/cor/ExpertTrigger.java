package main.cor;

import main.Robot;

public class ExpertTrigger extends ExpertParser{

	static final double dead_zone = 0.4;
	int MAXSPEED = 300;
	@Override
	public boolean _parse(String toParse, Robot robot) {
		if(toParse.startsWith("trigger")){
			String val = toParse.split(":")[1];
			int x =(int) (MAXSPEED *  apply_dead_zone(new Double(val)));
			if(toParse.startsWith("triggerL")){
				robot.armsMotor.forward();
			}
			else{
				robot.armsMotor.backward();
			}
			//System.out.println("trig speed: " + x);
			robot.armsMotor.setSpeed(x);
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
