package de.x8bit.Fantasya.Atlantis.Buildings;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Eisen;
import de.x8bit.Fantasya.Atlantis.Items.Gold;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Items.Stein;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Skills.Burgenbau;
import de.x8bit.Fantasya.util.ComplexName;

public class Kueche extends Building
{
	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Küche", "Küchen",
				new String[]{"Kueche", "Kuechen"});
	}

	public String getTyp() { return "Küche"; }

	public void Zerstoere(Unit u) {
		super.Zerstoere(u, new Item [] { new Stein(2), new Eisen(2), new Holz(2) });	
	}
	
	public int GebaeudeUnterhalt() { return 300; }
	public int UnterhaltEinheit() { return 10; }

	public boolean hatFunktion()
	{
		if (getSize() < 10) return false;
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
		if (tw < 4)
		{
			new Fehler(u + " hat nicht genügend Talent um an " + this + " zu bauen", u, u.getCoords());
			return;
		}
		
		// zusätzlichen Gebäude testen
		Region region = Region.Load(u.getCoords());
		if (!region.hatGebaeude(Burg.class, 250, u))
		{
			new Fehler(u + " - in " + region + " fehlt ein Schloss um " + getTyp() + " bauen zu können", u, u.getCoords());
			return;
		}
		
		GenericMake(u, 10, Burgenbau.class, 4, new Item [] { new Stein(5), new Eisen(5), new Holz(5), new Gold(5), new Silber(400) } );
	}
}
