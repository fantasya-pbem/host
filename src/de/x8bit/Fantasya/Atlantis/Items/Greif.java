package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.util.ComplexName;
import de.x8bit.Fantasya.util.Random;

// Greif ist Resource, da aus Greifeneiern hergestellt.

public class Greif extends Item implements AnimalResource {

	public Greif()
	{
		super(12000, 17000);
	}
	
	@Override
	public String getName()
	{
		if (anzahl != 1) return "Greife";
		return "Greif";
	}

	@Override
	public boolean willWandern(Region r) {
		return (Random.W(10000) < 100); // 1%
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Greif", "Greife", null);
	}

}
