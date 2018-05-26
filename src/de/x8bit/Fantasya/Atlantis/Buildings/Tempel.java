package de.x8bit.Fantasya.Atlantis.Buildings;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Eisen;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Items.Stein;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Skills.Burgenbau;
import de.x8bit.Fantasya.util.ComplexName;

public class Tempel extends de.x8bit.Fantasya.Atlantis.Building
{
	public void Zerstoere(Unit u)
	{
		super.Zerstoere(u, new Item [] {  new Stein(0), new Eisen(0), new Holz(0)  });	
	}
	
	public String getTyp() { return "Tempel"; }
	
	/**
	 * erstellt ein Gebäude bzw. baut daran weiter
	 * @param u - diese Einheit will daran bauen
	 */
	public void Mache(Unit u)
	{
		// nochmal holen ... ist größer als Null
		int tw = u.Talentwert(Burgenbau.class);
		if (tw < 2)
		{
			new Fehler(u + " hat nicht genügend Talent um an " + this + " zu bauen", u, u.getCoords());
			return;
		}
		
		GenericMake(u, 0, Burgenbau.class, 2, new Item [] { new Stein(1), new Holz(1), new Silber(50) } );
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Tempel", "Tempel", null);
	}
}
