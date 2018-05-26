package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.util.ComplexName;

public class Pelz extends Item implements LuxusGood {

	public Pelz()
	{
		super(100, 0);
	}

	@Override
	public int getPrice() { return 7; }
	
	@Override
	public String getName()
	{
		if (anzahl != 1) return "Pelze";
		return "Pelz";
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Pelz", "Pelze", null);
	}
}
