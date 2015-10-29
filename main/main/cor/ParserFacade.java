package main.cor;

import main.Robot;

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
