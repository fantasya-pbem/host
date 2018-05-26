package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.util.ComplexName;

public class Oel extends Item implements LuxusGood {

	public Oel()
	{
		super(100, 0);
	}
	@Override
	public int getPrice() { return 3; }
	
	@Override
	public String getName()
	{
		if (anzahl != 1) return "Oele";
		return "Oel";
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Oel", "Oele", null);
	}
}
