package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.util.ComplexName;

public class Silber extends Item
{
	public Silber()
	{
		super(1, 0);
	}
	
	public Silber(int anzahl)
	{
		super(1, 0);
		setAnzahl(anzahl);
	}
	
	public String getName()
	{
		if (anzahl != 1) return "Silber";
		return "Silber";
	}
	
	public boolean surviveBattle() { return true; }

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Silber", "Silber", null);
	}
}
