package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.util.ComplexName;

public class Flachtupf extends Item implements HerbalResource {

	public Flachtupf()
	{
		super(1, 0);
	}
	
	@Override
	public String getName()
	{
		if (anzahl != 1) return "Flachtupfe";
		return "Flachtupf";
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Flachtupf", "Flachtupfe", null);
	}
}
