package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.util.ComplexName;

public class Weihrauch extends Item implements LuxusGood {

	public Weihrauch()
	{
		super(100, 0);
	}
	@Override
	public int getPrice() { return 4; }
	
	@Override
	public String getName()
	{
		if (anzahl != 1) return "Weihrauch";
		return "Weihrauch";
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Weihrauch", "Weihrauch", null);
	}
}
