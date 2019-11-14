package de.x8bit.Fantasya.Atlantis.Ships;

import de.x8bit.Fantasya.Atlantis.Ship;
import de.x8bit.Fantasya.Atlantis.Buildings.Hafen;
import de.x8bit.Fantasya.Atlantis.Buildings.Seehafen;
import de.x8bit.Fantasya.Atlantis.Helper.ConstructionContainer;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Skills.Schiffbau;
import de.x8bit.Fantasya.util.ComplexName;
import de.x8bit.Fantasya.Atlantis.Unit;

@SuppressWarnings("rawtypes")
public class Drachenschiff extends Ship
{

	public Drachenschiff()
	{
		geschwindigkeit = 5;
		kapazitaet = 100000;
		matrosen = 50;
		kapitaenTalent = 3;
		
		// Konstruktion des Schiffes
		setConstructionSize(100);
		setConstructionItems( new ConstructionContainer [] { new ConstructionContainer(Holz.class, 1) } );
		setConstructionSkill( new ConstructionContainer(Schiffbau.class, 2) );
	}
	
	@SuppressWarnings("unchecked")
	public Class[] neededHarbour() {  return new Class [] { Hafen.class, Seehafen.class }; }

	@Override
	public int getVerfall() {
		if (this.istFertig()) return 10;
		return 20;
	}

    @Override
    public void Zerstoere(Unit u) {
        super.Zerstoere(u, new Item [] { new Holz(1) });
    }

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Drachenschiff", "Drachenschiffe", null);
	}

}
