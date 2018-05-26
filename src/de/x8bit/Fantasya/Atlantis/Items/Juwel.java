package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.util.ComplexName;

public class Juwel extends Item implements LuxusGood {
	
	public Juwel()
	{
		super(100, 0);
	}
	@Override
	public int getPrice() { return 7; }
	
	@Override
	public String getName()
	{
		if (anzahl != 1) return "Juwelen";
		return "Juwel";
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Juwel", "Juwelen", null);
	}
}
