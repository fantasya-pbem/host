package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Buildings.Schmiede;
import de.x8bit.Fantasya.Atlantis.Helper.ConstructionCheats;
import de.x8bit.Fantasya.Atlantis.Helper.ConstructionContainer;
import de.x8bit.Fantasya.Atlantis.Skills.Waffenbau;
import de.x8bit.Fantasya.util.ComplexName;
import de.x8bit.Fantasya.util.Random;

public class Kriegshammer extends Item implements Weapon {

	public Kriegshammer()
	{
		super(100, 0);
		setConstructionSkills(new ConstructionContainer [] { new ConstructionContainer(Waffenbau.class, 5) } );
		setConstructionItems(new ConstructionContainer [] { new ConstructionContainer(Holz.class, 1), new ConstructionContainer(Eisen.class, 5), new ConstructionContainer(Stein.class, 1) } );
		setConstructionCheats(new ConstructionCheats [] { 
				new ConstructionCheats(Schmiede.class, new ConstructionContainer [] { new ConstructionContainer(Eisen.class, 2)}),
				new ConstructionCheats(Schmiede.class, new ConstructionContainer [] { new ConstructionContainer(Stein.class, 2)}),
				new ConstructionCheats(Schmiede.class, new ConstructionContainer [] { new ConstructionContainer(Holz.class, 2)})
				});
	}

	@Override
	public String getName()
	{
		if (anzahl != 1) return "Kriegshammer";
		return "Kriegshammer";
	}
	
	@Override
	public boolean surviveBattle() { return Random.rnd(0, 100) < 90; }

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Kriegshammer", "Kriegshammer", null);
	}
}
