package main.cor;

import main.Robot;

/**
 * La classe abstraite définissant le comportement d'un expert quelconque.
 * @author baptiste
 *
 */
public abstract class Expert {
	
	/**
	 * La méthode utilisé par tout expert pour essayer de résoudre le problème
	 * @param toParse la chaîne à parser
	 * @param robot le robot
	 * @return si tous c'est bien passé elle retourne true, si on n'a pas trouvé d'expert pour parser on retourne false
	 */
	public abstract boolean parse(String toParse, Robot robot);
	
}
