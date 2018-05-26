package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.util.ComplexName;

public class HelmDer7Winde extends Item implements MagicItem {

	public HelmDer7Winde()
	{
		super(0, 0);
		
		setName("Helm der sieben Winde");
	}
	
    @Override
	public String getName()
	{
		if (anzahl != 1) return "Helme der sieben Winde";
		return "Helm der sieben Winde";
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Helm der sieben Winde", "Helme der sieben Winde", new String[]{"HelmDer7Winde", "Helm der 7 Winde", "Helme der 7 Winde"});
	}
}
