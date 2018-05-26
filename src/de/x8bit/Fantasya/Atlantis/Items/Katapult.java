package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Buildings.Werkstatt;
import de.x8bit.Fantasya.Atlantis.Helper.ConstructionCheats;
import de.x8bit.Fantasya.Atlantis.Helper.ConstructionContainer;
import de.x8bit.Fantasya.Atlantis.Skills.Wagenbau;
import de.x8bit.Fantasya.util.ComplexName;

public class Katapult extends Item implements Weapon {

	public Katapult()
	{
		super(6000, 6000);
		setConstructionSkills(new ConstructionContainer [] { new ConstructionContainer(Wagenbau.class, 3) } );
		setConstructionItems(new ConstructionContainer [] { new ConstructionContainer(Holz.class, 10) } );
		setConstructionCheats(new ConstructionCheats [] { 
				new ConstructionCheats(Werkstatt.class, new ConstructionContainer [] { new ConstructionContainer(Holz.class, 2)})
				});
	}
	
	@Override
	public String getName()
	{
		if (anzahl != 1) return "Katapulte";
		return "Katapult";
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Katapult", "Katapulte", null);
	}
}
