package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.util.ComplexName;

public class Drachenpanzer extends Item
{
	public Drachenpanzer()
	{
		super(0,0);
	}
	
	public String getName()
	{
		if (anzahl != 1) return "Drachenpanzer";
		return "Drachenpanzer";
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Drachenpanzer", "Drachenpanzer", null);
	}
}
