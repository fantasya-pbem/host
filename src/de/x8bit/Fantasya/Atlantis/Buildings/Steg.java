package de.x8bit.Fantasya.Atlantis.Buildings;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Eisen;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Skills.Burgenbau;
import de.x8bit.Fantasya.util.ComplexName;

public class Steg extends Building
{
	public void Zerstoere(Unit u)
	{
		super.Zerstoere(u, new Item [] {  new Holz(1)  });	
	}
	
	public String getTyp() { return "Steg"; }
	
	public int GebaeudeUnterhalt() { return 30; }

	public boolean hatFunktion()
	{
		if (getSize() < 5) return false;
		return super.hatFunktion(); 
	}
	

	/**
	 * erstellt ein Gebäude bzw. baut daran weiter
	 * @param u - diese Einheit will daran bauen
	 */
	public void Mache(Unit u)
	{
		// nochmal holen ... ist größer als Null
		int tw = u.Talentwert(Burgenbau.class);
		if (tw < 2)	{
			new Fehler(u + " hat nicht genügend Talent um an " + this + " zu bauen.", u, u.getCoords());
			return;
		}
		
		GenericMake(u, 5, Burgenbau.class, 2, new Item [] { new Eisen(1), new Holz(2), new Silber(50) } );
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Steg", "Stege", null);
	}
}
