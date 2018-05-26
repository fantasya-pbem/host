package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.util.ComplexName;

public class Trockenwurz extends Item implements HerbalResource {
	
	public Trockenwurz()
	{
		super(1, 0);
	}
	
	@Override
	public String getName()
	{
		if (anzahl != 1) return "Trockenwurze";
		return "Trockenwurz";
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Trockenwurz", "Trockenwurze", null);
	}
}
