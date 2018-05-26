package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.util.ComplexName;
import de.x8bit.Fantasya.util.Random;

public class Einhorn extends Item implements AnimalResource,MagicItem {

	public Einhorn()
	{
		super(2500, 5000);	// wiegt nichts
	}
	
	@Override
	public String getName()
	{
		if (anzahl != 1) return "Einhörner";
		return "Einhorn";
	}

	@Override
	public boolean willWandern(Region r) {
		return (Random.W(10000) < 100); // 1%
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Einhorn", "Einhörner", new String[]{"Einhoerner"});
	}
}
