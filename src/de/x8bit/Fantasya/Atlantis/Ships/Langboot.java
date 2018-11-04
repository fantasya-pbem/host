package de.x8bit.Fantasya.Atlantis.Ships;

import de.x8bit.Fantasya.Atlantis.Ship;
import de.x8bit.Fantasya.Atlantis.Buildings.Hafen;
import de.x8bit.Fantasya.Atlantis.Buildings.Seehafen;
import de.x8bit.Fantasya.Atlantis.Buildings.Steg;
import de.x8bit.Fantasya.Atlantis.Helper.ConstructionContainer;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Skills.Schiffbau;
import de.x8bit.Fantasya.util.ComplexName;
import de.x8bit.Fantasya.Atlantis.Unit;


public class Langboot extends Ship
{
	public Langboot()
	{
		geschwindigkeit = 4;
		kapazitaet = 50000;
		matrosen = 10;
		kapitaenTalent = 2;
		
		// Konstruktion des Schiffes
		setConstructionSize(50);
		setConstructionItems( new ConstructionContainer [] { new ConstructionContainer(Holz.class, 1) } );
		setConstructionSkill(  new ConstructionContainer(Schiffbau.class, 1) );
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Class[] neededHarbour() {  return new Class [] { Steg.class, Hafen.class, Seehafen.class }; }

	@Override
	public int getVerfall() {
		if (this.istFertig()) return 5;
		return 10;
	}

    @Override
    public void Zerstoere(Unit u) {
        super.Zerstoere(u, new Item [] { new Holz(1) });
    }

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Langboot", "Langboote", null);
	}

}
