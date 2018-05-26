package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Buildings.Sattlerei;
import de.x8bit.Fantasya.Atlantis.Helper.ConstructionCheats;
import de.x8bit.Fantasya.Atlantis.Helper.ConstructionContainer;
import de.x8bit.Fantasya.Atlantis.Skills.Ruestungsbau;
import de.x8bit.Fantasya.util.ComplexName;

public class Elefantenpanzer extends Item
{
	public Elefantenpanzer()
	{
		super(5000, 0);
		setConstructionSkills(new ConstructionContainer [] { new ConstructionContainer(Ruestungsbau.class, 5) } );
		setConstructionItems(new ConstructionContainer [] { new ConstructionContainer(Eisen.class, 10) } );
		setConstructionCheats(new ConstructionCheats [] { 
				new ConstructionCheats(Sattlerei.class, new ConstructionContainer [] { new ConstructionContainer(Eisen.class, 2)})				});
	}
	
	public Elefantenpanzer(int anzahl)
	{
		super(5000, 0);
		this.anzahl = anzahl;
	}
	
	@Override
	public String getName()	{
		// if (anzahl != 1) return "Elefantenpanzer";
		return "Elefantenpanzer";
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Elefantenpanzer", "Elefantenpanzer", null);
	}
}
