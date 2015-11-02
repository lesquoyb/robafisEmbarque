package main.cor;

import java.io.File;

import lejos.hardware.Sound;
import main.Robot;
import main.Robot.Modes;

/**
 * Cette classe est une facade du Design Pattern Chain Of Responsibility 
 * permettant de parser une chaine et d'afficher l'image correspondente.
 * @author baptiste
 *
 */
public class ParserFacade {
	
	ExpertParser first;
	
	public ParserFacade(){
		
		first = new ExpertParserDirection();
		first.ajouterSuivant(new ExpertParser() {
			
			@Override
			public boolean _parse(String toParse, Robot robot)  {
				try {
					if( toParse.equals("a") ){
						
							robot.setMode(Modes.Teleguide);
						return true;
					}
					else if (toParse.equals("b")){
						robot.setMode(Modes.FollowLine);
						return true;
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return false;
			}
		});
		first.ajouterSuivant(new ExpertParserSpeed());
		first.ajouterSuivant(new ExpertParserMode());
		
	}
	
	/**
	 * La méthode à appeler pour lancer la façade.
	 * @param toParse
	 * @param toDraw
	 * @return
	 */
	public boolean parse(String toParse, Robot robot){
		return first.parse(toParse, robot);
	}

}
