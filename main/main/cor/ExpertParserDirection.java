package main.cor;

import main.Robot;

public class ExpertParserDirection extends ExpertParser {



	static final double dead_zone = 0.1;
	static final int XMAXSPEED = 400;
	static final int YMAXSPEED = 200;


	@Override
	public boolean _parse(String toParse, Robot robot) {
		if( toParse.startsWith("move:") ){

			double speedR, speedL;

			String[] val = toParse.split(":")[1].split(",");
			double x = new Double(val[0]);
			double y = new Double(val[1]);
			x = apply_dead_zone(x);
			y = apply_dead_zone(y);

			speedR = speedL = Math.abs(XMAXSPEED * x);



			if (x > 0){
				robot.motorL.backward();
				robot.motorR.backward();

				if (-0.8 < y)
					robot.motorR.forward();
				if (y < 0.8)
					robot.motorL.forward();
				
				if (-0.8 < y && y < 0.8){
					speedR += y * YMAXSPEED;
					speedL -= y * YMAXSPEED;
				}
			}
			

			if (x < 0){
				robot.motorL.forward();
				robot.motorR.forward();
				
				if (-0.8 < y)
					robot.motorR.backward();
				if (y < 0.8)
					robot.motorL.backward();
				
				if (-0.8 < y && y < 0.8){
					speedR += y * YMAXSPEED;
					speedL -= y * YMAXSPEED;
				}
					
			}
			
			if ( x == 0 ){
				if (y > 0.3){
					speedR = y * XMAXSPEED/2;
					speedL = y * XMAXSPEED/2;
					robot.motorR.forward();
					robot.motorL.backward();
				}
				if (-0.3 > y){
					speedR = y * XMAXSPEED/2;
					speedL = y * XMAXSPEED/2;
					robot.motorL.forward();
					robot.motorR.backward();
				}
			}


			int min = 1;

			
			robot.motorL.setSpeed(Math.max((int) (speedL), min));
			robot.motorR.setSpeed(Math.max((int) (speedR), min));

			return true;
		}
		return false;
	}


	private double apply_dead_zone(double x){
		if( Math.abs(x) < dead_zone)
			return 0;
		return x;
	}

}
