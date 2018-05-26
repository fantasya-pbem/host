package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Buildings.Sattlerei;
import de.x8bit.Fantasya.Atlantis.Helper.ConstructionCheats;
import de.x8bit.Fantasya.Atlantis.Helper.ConstructionContainer;
import de.x8bit.Fantasya.Atlantis.Skills.Ruestungsbau;
import de.x8bit.Fantasya.util.ComplexName;

public class Holzschild extends Item implements Weapon {

	public Holzschild()
	{
		super(100, 0);
		setConstructionSkills(new ConstructionContainer [] { new ConstructionContainer(Ruestungsbau.class, 2) } );
		setConstructionItems(new ConstructionContainer [] { new ConstructionContainer(Holz.class, 2) } );
		setConstructionCheats(new ConstructionCheats [] { 
				new ConstructionCheats(Sattlerei.class, new ConstructionContainer [] { new ConstructionContainer(Holz.class, 2)})
				});
	}
		
	@Override
	public String getName()
	{
		if (anzahl != 1) return "Holzschilde";
		return "Holzschild";
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Holzschild", "Holzschilde", null);
	}
}
