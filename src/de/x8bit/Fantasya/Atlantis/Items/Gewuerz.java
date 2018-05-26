package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.util.ComplexName;

public class Gewuerz extends Item implements LuxusGood {

	public Gewuerz()
	{
		super(100, 0);
	}
	@Override
	public int getPrice() { return 5; }
	
	@Override
	public String getName()
	{
		if (anzahl != 1) return "Gewuerze";
		return "Gewuerz";
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Gewuerz", "Gewuerze", null);
	}
}
