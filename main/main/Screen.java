package main;

import lejos.hardware.lcd.LCD;
import lejos.robotics.Color;
import lejos.utility.TextMenu;

public class Screen {
	
	
	
	public int selectScenario() throws Exception{
		LCD.clear();
		String[] menu = new String[]{"Rouge","Jaune","Vert"};
		TextMenu m = new TextMenu(menu);
		int index = m.select();
		int selectedColor = 0;
		switch(menu[index]){
		case "Rouge":
			selectedColor = Color.RED;
			break;

		case "Vert":
			selectedColor = Color.GREEN;				
			break;

		case "Jaune":
			selectedColor = Color.YELLOW;
			break;
		default:
			throw new Exception("Index du menu non géré: " + menu[index]);
		}
		return selectedColor;
	}

}
