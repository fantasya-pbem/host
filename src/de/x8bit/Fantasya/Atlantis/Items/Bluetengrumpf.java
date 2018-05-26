package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.util.ComplexName;

public class Bluetengrumpf extends Item implements HerbalResource {

	public Bluetengrumpf()
	{
		super(1, 0);
	}
	@Override
	public String getName()
	{
		if (anzahl != 1) return "Bluetengrumpfe";
		return "Bluetengrumpf";
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Bluetengrumpf", "Bluetengrumpfe", null);
	}
}
