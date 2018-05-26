package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.util.ComplexName;

public class Flugdrachenei extends Item implements Resource {
	public Flugdrachenei() {
		super(100, 0);
	}
	
    @Override
	public String getName()	{
		if (anzahl != 1) return "Dracheneier";
		return "Drachenei";
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Flugdrachenei", "Flugdracheneier", new String[]{"Drachenei", "Dracheneier"});
	}
}
