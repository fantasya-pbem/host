package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.util.ComplexName;

public class AmuletDesSehens extends Item implements MagicItem {

	public AmuletDesSehens()
	{
		super(0, 0);
		
		setName("Amulett des wahren Sehens");
	}
    @Override
	public String getName()	{
		if (anzahl != 1) return "Amulette des wahren Sehens";
		return "Amulett des wahren Sehens";
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Amulett des wahren Sehens", "Amulette des wahren Sehens",  new String[]{"AmuletDesSehens"});
	}
}
