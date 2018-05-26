package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Buildings.Steinbruch;
import de.x8bit.Fantasya.Atlantis.Helper.ConstructionCheats;
import de.x8bit.Fantasya.Atlantis.Helper.ConstructionContainer;
import de.x8bit.Fantasya.Atlantis.Skills.Steinbau;
import de.x8bit.Fantasya.util.ComplexName;

public class Stein extends Item implements PersistentResource {
	
	public Stein()
	{
		super(6000, 0);
		setConstructionSkills(new ConstructionContainer [] { new ConstructionContainer(Steinbau.class, 1) } );
		setConstructionCheats(new ConstructionCheats [] { 
				new ConstructionCheats(Steinbruch.class, new ConstructionContainer [] { new ConstructionContainer(Stein.class, 2)})
				});
	}
	
	public Stein(int anzahl)
	{
		super(6000, 0);
		setAnzahl(anzahl);
		setConstructionSkills(new ConstructionContainer [] { new ConstructionContainer(Steinbau.class, 1) } );
		setConstructionCheats(new ConstructionCheats [] { 
				new ConstructionCheats(Steinbruch.class, new ConstructionContainer [] { new ConstructionContainer(Stein.class, 2)})
				});
	}

	@Override
	public String getName()
	{
		if (anzahl != 1) return "Steine";
		return "Stein";
	}
	
	@Override
	public boolean surviveBattle() { return true; }

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Stein", "Steine", null);
	}
}
