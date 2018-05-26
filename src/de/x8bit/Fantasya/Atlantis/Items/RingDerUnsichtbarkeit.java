package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.util.ComplexName;

public class RingDerUnsichtbarkeit extends Item implements MagicItem {

	public RingDerUnsichtbarkeit()
	{
		super(0, 0);
	}
	
    @Override
	public String getName()
	{
		if (anzahl != 1) return "Ringe der Unsichtbarkeit";
		return "Ring der Unsichtbarkeit";
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Ring der Unsichtbarkeit", "Ringe der Unsichtbarkeit", new String[]{"RingDerUnsichtbarkeit"});
	}
}
