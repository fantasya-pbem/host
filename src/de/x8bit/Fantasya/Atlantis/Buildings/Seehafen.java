package de.x8bit.Fantasya.Atlantis.Buildings;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Eisen;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Items.Stein;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Skills.Burgenbau;
import de.x8bit.Fantasya.util.ComplexName;

public class Seehafen extends Building
{
	public void Zerstoere(Unit u)
	{
		super.Zerstoere(u, new Item [] {  new Stein(7), new Eisen(5), new Holz(5)  });	
	}
	
	public String getTyp() { return "Seehafen"; }
	
	public int GebaeudeUnterhalt() { return 300; }
	
	public boolean hatFunktion()
	{
		if (getSize() < 20) return false;
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
		if (tw < 5)
		{
			new Fehler(u + " hat nicht genügend Talent um an " + this + " zu bauen", u, u.getCoords());
			return;
		}
		
		// zusätzlichen Gebäude testen
		Region region = Region.Load(u.getCoords());
		if (!region.hatGebaeude(Burg.class, 50, u))
		{
			new Fehler(u + " - in " + region + " fehlt ein Schloss um " + getTyp() + " bauen zu können", u, u.getCoords());
			return;
		}
		
		GenericMake(u, 20, Burgenbau.class, 5, new Item [] { new Stein(10), new Eisen(10), new Holz(10), new Silber(500) } );
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Seehafen", "Seehäfen", null);
	}
}
