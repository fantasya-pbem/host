package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.util.ComplexName;

public class RingDerKraft extends Item implements MagicItem {

	public RingDerKraft()
	{
		super(0,0);
	}
	
    @Override
	public String getName()
	{
		if (anzahl != 1) return "Ringe der Kraft";
		return "Ring der Kraft";
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Ring der Kraft", "Ringe der Kraft", new String[]{"RingDerKraft"});
	}
}
