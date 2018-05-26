package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.util.ComplexName;

public class Schnupfnies extends Item implements HerbalResource {
	
	public Schnupfnies()
	{
		super(1, 0);
	}
	
	@Override
	public String getName()
	{
		if (anzahl != 1) return "Schnupfniese";
		return "Schnupfnies";
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Schnupfnies", "Schnupfniese", null);
	}
}
