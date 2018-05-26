package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Buildings.Sattlerei;
import de.x8bit.Fantasya.Atlantis.Helper.ConstructionCheats;
import de.x8bit.Fantasya.Atlantis.Helper.ConstructionContainer;
import de.x8bit.Fantasya.Atlantis.Skills.Ruestungsbau;
import de.x8bit.Fantasya.util.ComplexName;

public class Kettenhemd extends Item
{
	public Kettenhemd()
	{
		super(200, 0);
		setConstructionSkills(new ConstructionContainer [] { new ConstructionContainer(Ruestungsbau.class, 2) } );
		setConstructionItems(new ConstructionContainer [] { new ConstructionContainer(Eisen.class, 3) } );
		setConstructionCheats(new ConstructionCheats [] { 
				new ConstructionCheats(Sattlerei.class, new ConstructionContainer [] { new ConstructionContainer(Eisen.class, 2)})
				});
	}
	
	@Override
	public String getName()
	{
		if (anzahl != 1) return "Kettenhemden";
		return "Kettenhemd";
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Kettenhemd", "Kettenhemden", null);
	}
}
