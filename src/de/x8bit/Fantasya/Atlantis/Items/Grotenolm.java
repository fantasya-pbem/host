package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.util.ComplexName;

public class Grotenolm extends Item implements HerbalResource {
	public Grotenolm()
	{
		super(1, 0);
	}
	
	@Override
	public String getName()
	{
		if (anzahl != 1) return "Grotenolme";
		return "Grotenolm";
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Grotenolm", "Grotenolme", null);
	}
}
