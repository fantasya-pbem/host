package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Buildings.Schmiede;
import de.x8bit.Fantasya.Atlantis.Helper.ConstructionCheats;
import de.x8bit.Fantasya.Atlantis.Helper.ConstructionContainer;
import de.x8bit.Fantasya.Atlantis.Skills.Bogenbau;
import de.x8bit.Fantasya.util.ComplexName;

public class Armbrust extends Item implements Weapon {

	public Armbrust()
	{
		super(100, 0);
		setConstructionSkills(new ConstructionContainer [] { new ConstructionContainer(Bogenbau.class, 2) } );
		setConstructionItems(new ConstructionContainer [] { new ConstructionContainer(Holz.class, 1) } );
		setConstructionCheats(new ConstructionCheats [] { 
				new ConstructionCheats(Schmiede.class, new ConstructionContainer [] { new ConstructionContainer(Eisen.class, 2)}),
				new ConstructionCheats(Schmiede.class, new ConstructionContainer [] { new ConstructionContainer(Stein.class, 2)}),
				new ConstructionCheats(Schmiede.class, new ConstructionContainer [] { new ConstructionContainer(Holz.class, 2)})
				});
	}
	
	@Override
	public String getName()
	{
		if (anzahl != 1) return "Armbrueste";
		return "Armbrust";
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Armbrust", "Armbrueste", null);
	}
}
