package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.util.ComplexName;

public class Wirzblatt extends Item implements HerbalResource {
	public Wirzblatt()
	{
		super(1, 0);
	}
	
	@Override
	public String getName()
	{
		if (anzahl != 1) return "Wirzblaetter";
		return "Wirzblatt";
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Wirzblatt", "Wirzblaetter", null);
	}
}
