package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.util.ComplexName;

public class Greifenei extends Item
{
	public Greifenei()
	{
		super(200, 0);
	}
	
	public Greifenei(int anzahl)
	{
		super(200, 0);
		setAnzahl(anzahl);
	}
	
	public String getName()
	{
		if (anzahl != 1) return "Greifeneier";
		return "Greifenei";
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Greifenei", "Greifeneier", null);
	}
}
