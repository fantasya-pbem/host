package de.x8bit.Fantasya.Atlantis.Buildings;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Eisen;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Items.Stein;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.SysErr;
import de.x8bit.Fantasya.Atlantis.Skills.Burgenbau;
import de.x8bit.Fantasya.util.ComplexName;

public class Wegweiser extends Building
{
	public void Zerstoere(Unit u)
	{
		new Fehler(u + " - ein Wegweiser kann nicht zerstört werden", u, u.getCoords());
		return;
	}
	
	public String getTyp() { return "Wegweiser"; }
	
	/**
	 * erstellt ein Gebäude bzw. baut daran weiter
	 * @param u - diese Einheit will daran bauen
	 */
	public void Mache(Unit u)
	{
		// nochmal holen ... ist größer als Null
		int tw = u.Talentwert(Burgenbau.class);
		if (tw < 1)
		{
			new Fehler(u + " hat nicht genügend Talent um an " + this + " zu bauen", u, u.getCoords());
			return;
		}
		
		GenericMake(u, 1, Burgenbau.class, 1, new Item [] { new Stein(1), new Eisen(1), new Holz(1), new Silber(50) } );
	}
	
	public void Enter(Unit unit)
	{
		new Fehler(unit + " - ein Wegweiser kann nicht betreten werden", unit, unit.getCoords());
	}
	
	protected void Zerstoere(Unit u, Item items[])
	{
		new SysErr("System versucht Wegweiser zu zerstören");
		return; // wird vom System ausgelöst
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Wegweiser", "Wegweiser", null);
	}
}
