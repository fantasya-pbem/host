package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.util.ComplexName;

public class Sumpfkraut extends Item implements HerbalResource {
	
	public Sumpfkraut()
	{
		super(1, 0);
	}
	
	@Override
	public String getName()
	{
		if (anzahl != 1) return "Sumpfkraeuter";
		return "Sumpfkraut";
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Sumpfkraut", "Sumpfkraeuter", null);
	}
}
