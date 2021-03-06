package de.x8bit.Fantasya.Atlantis.Ships;

import de.x8bit.Fantasya.Atlantis.Ship;
import de.x8bit.Fantasya.Atlantis.Helper.ConstructionContainer;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Skills.Schiffbau;
import de.x8bit.Fantasya.util.ComplexName;
import de.x8bit.Fantasya.Atlantis.Unit;

public class Karavelle extends Ship
{
	public Karavelle()
	{
		geschwindigkeit = 6;
		kapazitaet = 300000;
		matrosen = 30;
		kapitaenTalent = 4;
		
		// Konstruktion des Schiffes
		setConstructionSize(250);
		setConstructionItems( new ConstructionContainer [] { new ConstructionContainer(Holz.class, 1) } );
		setConstructionSkill(  new ConstructionContainer(Schiffbau.class, 4) );
	}

	@Override
	public int getVerfall() {
		if (this.istFertig()) return 25;
		return 50;
	}

    @Override
    public void Zerstoere(Unit u) {
        super.Zerstoere(u, new Item [] { new Holz(1) });
    }

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Karavelle", "Karavellen", null);
	}
}
