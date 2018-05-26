package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.util.ComplexName;

public class MantelDerUnverletzlichkeit extends Item implements MagicItem {

	public MantelDerUnverletzlichkeit()
	{
		super(0, 0);
	}
	
    @Override
	public String getName()
	{
		if (anzahl != 1) return "Mäntel der Unverletzlichkeit";
		return "Mantel der Unverletzlichkeit";
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName(
                "Mantel der Unverletzlichkeit", 
                "Mäntel der Unverletzlichkeit", 
                new String[]{"MantelDerUnverletzlichkeit", "Maentel der Unverletzlichkeit"}
        );
	}
}
