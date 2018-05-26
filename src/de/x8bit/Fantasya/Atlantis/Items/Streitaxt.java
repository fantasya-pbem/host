package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Buildings.Schmiede;
import de.x8bit.Fantasya.Atlantis.Helper.ConstructionCheats;
import de.x8bit.Fantasya.Atlantis.Helper.ConstructionContainer;
import de.x8bit.Fantasya.Atlantis.Skills.Waffenbau;
import de.x8bit.Fantasya.util.ComplexName;

public class Streitaxt extends Item implements Weapon {

	public Streitaxt()
	{
		super(100, 0);
		setConstructionSkills(new ConstructionContainer [] { new ConstructionContainer(Waffenbau.class, 5) } );
		setConstructionItems(new ConstructionContainer [] { new ConstructionContainer(Holz.class, 2), new ConstructionContainer(Eisen.class, 3) } );
		setConstructionCheats(new ConstructionCheats [] { 
				new ConstructionCheats(Schmiede.class, new ConstructionContainer [] { new ConstructionContainer(Eisen.class, 2)}),
				new ConstructionCheats(Schmiede.class, new ConstructionContainer [] { new ConstructionContainer(Stein.class, 2)}),
				new ConstructionCheats(Schmiede.class, new ConstructionContainer [] { new ConstructionContainer(Holz.class, 2)})
				});
	}

	@Override
	public String getName()
	{
		if (anzahl != 1) return "Streitaexte";
		return "Streitaxt";
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Streitaxt", "Streitaexte", null);
	}
}
