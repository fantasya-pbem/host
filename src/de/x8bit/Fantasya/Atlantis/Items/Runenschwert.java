package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.util.ComplexName;

public class Runenschwert extends Item implements Weapon,MagicItem  {

	public Runenschwert()
	{
		super(0, 0);
	}

	@Override
	public String getName()
	{
		if (anzahl != 1) return "Runenschwerter";
		return "Runenschwert";
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Runenschwert", "Runenschwerter", null);
	}
}
