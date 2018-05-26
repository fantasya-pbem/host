package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.util.ComplexName;

public class Balsam extends Item implements LuxusGood {

	public Balsam()
	{
		super(100, 0);
	}
	@Override
	public int getPrice() { return 4; }
	
	@Override
	public String getName()
	{
		if (anzahl != 1) return "Balsam";
		return "Balsam";
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Balsam", "Balsam", null);
	}
}
