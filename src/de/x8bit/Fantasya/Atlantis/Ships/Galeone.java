package de.x8bit.Fantasya.Atlantis.Ships;

import de.x8bit.Fantasya.Atlantis.Ship;
import de.x8bit.Fantasya.Atlantis.Helper.ConstructionContainer;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Skills.Schiffbau;
import de.x8bit.Fantasya.util.ComplexName;
import de.x8bit.Fantasya.Atlantis.Unit;

public class Galeone extends Ship
{
	public Galeone()
	{
		geschwindigkeit = 4;
		kapazitaet = 600000;
		matrosen = 80;
		kapitaenTalent = 5;
		
		// Konstruktion des Schiffes
		setConstructionSize(350);
		setConstructionItems( new ConstructionContainer [] { new ConstructionContainer(Holz.class, 1) } );
		setConstructionSkills( new ConstructionContainer [] { new ConstructionContainer(Schiffbau.class, 5) } );
	}

	@Override
	public int getVerfall() {
		if (this.istFertig()) return 35;
		return 70;
	}

    @Override
    public void Zerstoere(Unit u) {
        super.Zerstoere(u, new Item [] { new Holz(1) });
    }

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Galeone", "Galeonen", null);
	}

}
