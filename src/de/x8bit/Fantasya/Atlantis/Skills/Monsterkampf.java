package de.x8bit.Fantasya.Atlantis.Skills;

import de.x8bit.Fantasya.Atlantis.Helper.Monster;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.util.ComplexName;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Unit;

public class Monsterkampf extends Skill {
	
	/**
	 * das funktioniert nur für Abkömmlinge von Monster.class
	 * @param u 
	 */
	@Override
	public String Lernen(Unit u) {
		if (!(u instanceof Monster)) {
			new Fehler(u + " weiß nicht so recht.", u);
			return null;
		}
		
		return super.Lernen(u);
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Monsterkampf", "Monsterkampf", null);
	}
	
	
}
