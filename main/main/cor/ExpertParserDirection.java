package main.cor;

import main.Robot;

public class ExpertParserDirection extends ExpertParser {





	@Override
	public boolean _parse(String toParse, Robot robot) {
		if( toParse.startsWith("move:") ){

			int speedR, speedL;

			String[] val = toParse.split(":")[1].split(",");
			speedL = new Integer(val[0]);
			speedR = new Integer(val[1]);
			robot.motorL.forward();
			robot.motorR.forward();
			
			if( speedL < 0 ){
				robot.motorL.backward();
				speedL = -speedL;
			}
			if( speedR < 0 ){
				robot.motorR.backward();
				speedR = -speedR;
			}
			
			int min = 1;			
			robot.motorL.setSpeed(Math.max((speedL), min));
			robot.motorR.setSpeed(Math.max((speedR), min));

			return true;
		}
		return false;
	}


}
