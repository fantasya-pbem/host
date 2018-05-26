package de.x8bit.Fantasya.Atlantis.Skills;

import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.util.ComplexName;

public class Tarnung extends Skill {

	@Override
	public String Lernen(Unit u) {
		String message = "";
        if (u.getSkill(this.getClass()).getLerntage() == 0) {
			// Anfänger! Wir tarnen uns implizit:
			u.setSichtbarkeit(1);
			message = u + " beginnt sich in Tarnung zu üben und versteckt sich erstmal: TARNE EINHEIT implizit ausgeführt.";
		}

        String superMessage = super.Lernen(u);
		if (message.length() > 0) {
            return message;
        } else {
            return superMessage;
        }
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Tarnung", "Tarnung", null);
	}


	
}
