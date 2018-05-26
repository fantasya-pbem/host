package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Helper.ConstructionContainer;
import de.x8bit.Fantasya.Atlantis.Skills.Pferdedressur;
import de.x8bit.Fantasya.util.ComplexName;
import de.x8bit.Fantasya.util.Random;

/**
 * Ein Pegasus ist ein fliegendes Pferd. Es kann 75 GE tragen, davon wiegt es selbst 50 GE.
 */
public class Pegasus extends Pferd {
	public Pegasus() {
        super(5000, 7500);
        
        // Pegasi sind schwer zu fangen:
		setConstructionSkills(new ConstructionContainer [] { new ConstructionContainer(Pferdedressur.class, 10) } );
    }
	
	@Override
	public String getName() {
		if (anzahl != 1) return "Pegasi";
		return "Pegasus";
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Pegasus", "Pegasi", null);
	}
	
	/**
	 * ein Pegasus hat eine 75%-Chance
	 * @return 
	 */
	@Override
	public boolean surviveBattle() { return Random.W(100) <= 75; }
}
