package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.util.ComplexName;

public class Silberkiste extends Item {

	public Silberkiste() {
		super(0, 0);
	}
	
	public String getName() {
		if (anzahl != 1) return "Silberkiste";
		return "Silberkiste";
	}


	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Silberkiste", "Silberkiste", null);
	}

}
