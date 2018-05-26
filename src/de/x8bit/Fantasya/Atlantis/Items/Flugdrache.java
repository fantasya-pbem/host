package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.util.ComplexName;
import de.x8bit.Fantasya.util.Random;

// Flugrache ist Resource, da aus Dracheneiern hergestellt.

public class Flugdrache extends Item implements AnimalResource {
	public Flugdrache()
	{
		super(12000, 17000);
	}
	
	@Override
	public String getName()
	{
		if (anzahl != 1) return "Flugdrachen";
		return "Flugdrache";
	}

	@Override
	public boolean willWandern(Region r) {
		return (Random.W(10000) < 100); // 1%
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Flugdrache", "Flugdrachen", null);
	}

}
