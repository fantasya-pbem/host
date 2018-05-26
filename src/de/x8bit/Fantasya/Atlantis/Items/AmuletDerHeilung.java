package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.util.ComplexName;

public class AmuletDerHeilung extends Item implements MagicItem {

	public AmuletDerHeilung()
	{
		super(0, 0);
		
		setName("Amulett der Heilung");
	}

	@Override
	public String getName()
	{
		if (anzahl != 1) return "Amulette der Heilung";
		return "Amulett der Heilung";
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Amulett der Heilung", "Amulette der Heilung", new String[]{"AmuletDerHeilung"});
	}
}
