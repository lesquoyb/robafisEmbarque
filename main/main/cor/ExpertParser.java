package main.cor;

import main.Robot;

/**
 * Classe abstraite définissant le comportement d'un expert parser (c'est à dire qui analyse du texte).
 * Defini l'objet {@link ExpertParser} suivant, pour le DP COR.
 * @author baptiste
 *
 */
public abstract class ExpertParser extends Expert {

	private ExpertParser suivant;

	public ExpertParser() {
		suivant = null;
	}
	@Override
	public boolean parse(String toParse,Robot robot){
		if( _parse(toParse,robot) == false){
			if(suivant != null){
				return suivant.parse(toParse,robot);
			}
			else{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * La méthode à implémenter par tous les {@link ExpertParser} dans {@link Expert.draw()}
	 * @param toParse la chaîne à parser
	 * @param robot le robot sur lequel il faut effectuer une action
	 * @return true si la méthode a fonctionné, false pour passer à l'expert suivant
	 */
	public abstract boolean _parse(String toParse, Robot robot);
	
	/**
	 * met à jour la valeur du suivant dans la chaine de responsabilité
	 * @param ex l'expert suivant dans la chaine
	 */
	public void ajouterSuivant(ExpertParser ex){
		if(suivant != null){
			suivant.ajouterSuivant(ex);
		}
		else{
			suivant = ex;	
		}
		
	}
}
