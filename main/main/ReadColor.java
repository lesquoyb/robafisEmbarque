package main;

public class ReadColor {
	
	public static final ReadColor Red = new ReadColor(255, 0, 0);
	public static final ReadColor Green = new ReadColor(0, 255, 0);
	public static final ReadColor Blue = new ReadColor(0, 0, 255);
	public static final ReadColor Yellow = new ReadColor(255, 255, 0);
	
	
	
	final int r, g, b;

	public ReadColor(int r, int g, int b){
		this.r = r;
		this.g= g;
		this.b = b;
		//TODO: exception si valeurs au dessus de 255
	}
	
	
	public int getAverage(){
		return (r+g+b)/3;
	}
	
	public boolean isDarkerThan(ReadColor other){
		return getAverage() < other.getAverage();
	}
	
	public boolean isClearerThan(ReadColor other){
		return getAverage() > other.getAverage();
	}


	@Override
	public String toString() {
		return "r: " + r + ", g: " + g + ", b: " + b + ", avg: " + getAverage();
	}
	
	

}


