package de.x8bit.Fantasya.Atlantis.Ships;

import de.x8bit.Fantasya.Atlantis.Ship;
import de.x8bit.Fantasya.Atlantis.Helper.ConstructionContainer;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Skills.Schiffbau;
import de.x8bit.Fantasya.util.ComplexName;
import de.x8bit.Fantasya.Atlantis.Unit;

public class Tireme extends Ship
{
	public Tireme()
	{
		geschwindigkeit = 8;
		kapazitaet = 200000;
		matrosen = 120;
		kapitaenTalent = 5;
		
		// Konstruktion des Schiffes
		setConstructionSize(200);
		setConstructionItems( new ConstructionContainer [] { new ConstructionContainer(Holz.class, 1) } );
		setConstructionSkill( new ConstructionContainer(Schiffbau.class, 5) );
	}

	@Override
	public int getVerfall() {
		if (this.istFertig()) return 20;
		return 40;
	}

    @Override
    public void Zerstoere(Unit u) {
        super.Zerstoere(u, new Item [] { new Holz(1) });
    }

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Trireme", "Triremen", null);
	}

}
