package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Helper.ConstructionContainer;
import de.x8bit.Fantasya.Atlantis.Skills.Waffenbau;
import de.x8bit.Fantasya.util.ComplexName;
import de.x8bit.Fantasya.util.Random;

public class Kriegselefant extends Item implements Weapon {

	public Kriegselefant()
	{
		super(35000, 37000);
		setConstructionSkills(new ConstructionContainer [] { new ConstructionContainer(Waffenbau.class, 5) } );
		setConstructionItems(new ConstructionContainer [] { new ConstructionContainer(Holz.class, 2), new ConstructionContainer(Elefant.class, 1), new ConstructionContainer(Elefantenpanzer.class, 1) } );
	}
	
	@Override
	public int Unterhalt() { return this.getAnzahl() * 5; }	// 5 Silber

	@Override
	public String getName()
	{
		if (anzahl != 1) return "Kriegselefanten";
		return "Kriegselefant";
	}
	
	/** Kriegselefanten haben nur 1/3 Chance, den Tod ihres Reiters zu überstehen */
	@Override
    public boolean surviveBattle() {
        if (Random.rnd(0, 100) < 33) return true; 
        return false;
    }

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Kriegselefant", "Kriegselefanten", null);
	}
}
