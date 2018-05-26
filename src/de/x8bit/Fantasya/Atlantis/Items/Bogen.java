package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Buildings.Schmiede;
import de.x8bit.Fantasya.Atlantis.Helper.ConstructionCheats;
import de.x8bit.Fantasya.Atlantis.Helper.ConstructionContainer;
import de.x8bit.Fantasya.Atlantis.Skills.Bogenbau;
import de.x8bit.Fantasya.util.ComplexName;

public class Bogen extends Item implements Weapon {

	public Bogen()
	{
		super(100, 0);
		setConstructionSkills(new ConstructionContainer [] { new ConstructionContainer(Bogenbau.class, 3) } );
		setConstructionItems(new ConstructionContainer [] { new ConstructionContainer(Holz.class, 1) } );
		setConstructionCheats(new ConstructionCheats [] {
				new ConstructionCheats(Schmiede.class, new ConstructionContainer [] { new ConstructionContainer(Holz.class, 2)})
				});
	}
	
	@Override
	public String getName()
	{
		if (anzahl != 1) return "Boegen";
		return "Bogen";
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Bogen", "Boegen", null);
	}
}
