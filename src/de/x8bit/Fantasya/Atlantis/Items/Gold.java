package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Buildings.Bergwerk;
import de.x8bit.Fantasya.Atlantis.Helper.ConstructionCheats;
import de.x8bit.Fantasya.Atlantis.Helper.ConstructionContainer;
import de.x8bit.Fantasya.Atlantis.Skills.Bergbau;
import de.x8bit.Fantasya.util.ComplexName;

public class Gold extends Item implements PersistentResource {
	public Gold()
	{
		super(500, 0);
		setConstructionSkills(new ConstructionContainer [] { new ConstructionContainer(Bergbau.class, 1) } );
		setConstructionCheats(new ConstructionCheats [] { 
				new ConstructionCheats(Bergwerk.class, new ConstructionContainer [] { new ConstructionContainer(Gold.class, 2)})
				});
	}
	
	public Gold(int anzahl)
	{
		super(500, 0);
		setAnzahl(anzahl);
		setConstructionSkills(new ConstructionContainer [] { new ConstructionContainer(Bergbau.class, 1) } );
		setConstructionCheats(new ConstructionCheats [] { 
				new ConstructionCheats(Bergwerk.class, new ConstructionContainer [] { new ConstructionContainer(Gold.class, 2)}),
				});
	}
	
	@Override
	public String getName()
	{
		if (anzahl != 1) return "Gold";
		return "Gold";
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Gold", "Gold", null);
	}
}
