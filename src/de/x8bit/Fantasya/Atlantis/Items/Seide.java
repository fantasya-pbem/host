package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.util.ComplexName;

public class Seide extends Item implements LuxusGood {
	
	public Seide()
	{
		super(100, 0);
	}
	@Override
	public int getPrice() { return 6; }
	
	@Override
	public String getName()
	{
		if (anzahl != 1) return "Ballen feinster Seide";
		return "Seide";
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Seide", "Ballen feinster Seide", null);
	}
}
