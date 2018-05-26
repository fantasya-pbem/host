package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.util.ComplexName;

public class Schildstein extends Item implements MagicItem {

	public Schildstein()
	{
		super(0, 0);
	}
	
	@Override
	public String getName()
	{
		if (anzahl != 1) return "Schildsteine";
		return "Schildstein";
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Schildstein", "Schildsteine", null);
	}
}
