package de.x8bit.Fantasya.Atlantis.Ships;

import de.x8bit.Fantasya.Atlantis.Ship;
import de.x8bit.Fantasya.Atlantis.Helper.ConstructionContainer;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Skills.Schiffbau;
import de.x8bit.Fantasya.util.ComplexName;
import de.x8bit.Fantasya.Atlantis.Unit;

/**
 * das Boot
 * 
 * @author mogel
 *
 */
@SuppressWarnings("rawtypes")
public class Boot extends Ship
{
	public Boot()
	{
		geschwindigkeit = 2;
		kapazitaet = 5000;
		matrosen = 2;
		kapitaenTalent = 1;
		
		// Konstruktion des Schiffes
		setConstructionSize(5);
		setConstructionItems( new ConstructionContainer [] { new ConstructionContainer(Holz.class, 1) } );
		setConstructionSkill( new ConstructionContainer(Schiffbau.class, 1) );
	}
	
	@SuppressWarnings("unchecked")
	public Class[] neededHarbour() { return null; }

	@Override
	public int getVerfall() {
		if (this.istFertig()) return 1;
		return 2;
	}

    @Override
    public void Zerstoere(Unit u) {
        super.Zerstoere(u, new Item [] { new Holz(1) });
    }

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Boot", "Boote", null);
	}
}
