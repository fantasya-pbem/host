package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.util.ComplexName;

public class Myhrre extends Item implements LuxusGood {
	
	public Myhrre()
	{
		super(100, 0);
	}
	@Override
	public int getPrice() { return 5; }
	
	@Override
	public String getName()	{
		// Mantis #184 - Handel testen! CR und NR testen!
		if (anzahl != 1) return "Myhrre";
		return "Myhrre";
	}

	@Override
	public ComplexName getComplexName() {
		 return new ComplexName("Myhrre", "Myhrre",
				new String[] {"Myrrhe", "Myrre", "Sack Myrrhe", "Säcke Myrrhe"});
	}
}
