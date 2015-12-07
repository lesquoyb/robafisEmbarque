package main;

public class Historic{
	
	int colors[] = new int[30000];
	long gyro[] = new long[30000];
	int lSpeed[] = new int[30000];
	int rSpeed[] = new int[30000];
	int distances[] = new int[30000];
	int index = 0;
	
	public void record(int color, long angle, int lSpeed, int rSpeed, float dist){
		colors[index] = color;
		gyro[index] = angle;
		this.lSpeed[index] = lSpeed;
		this.rSpeed[index] = rSpeed;
		distances[index] = Math.min((int) (dist*250 +1), 250);
		index++;
	}
	
	public byte[] getHistoric(){
		String color = "colors: ", gyro = "gyro: ", dist = "dist: ", lS = "lSpeed: ", rS = "rSpeed: ";
		for(int i = 0 ; i < index+1 ;i++){
			color += Integer.toString(colors[i]) + " ";
			gyro += Long.toString(this.gyro[i]) + " ";
			lS += Integer.toString(lSpeed[i]) + " ";
			rS += Integer.toString(rSpeed[i]) + " ";
			dist += Integer.toString(distances[i]) + " ";
		}
		return (color + "\n" + gyro + "\n" + dist + "\n" + lS +"\n"+ rS + "\n").getBytes();
	}

}
