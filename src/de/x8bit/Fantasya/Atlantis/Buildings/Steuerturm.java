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

public class Steuerturm extends Building
{
	public void Zerstoere(Unit u)
	{
		super.Zerstoere(u, new Item [] {  new Stein(2), new Eisen(2), new Holz(1)  });	
	}
	
	public String getTyp() { return "Steuerturm"; }
	public int GebaeudeUnterhalt() { return 50; }
	public int UnterhaltEinheit() { return 5; }

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
		if (region.hatGebaeude(Steuerturm.class, 0, null))
		{
			new Fehler(u + " - in " + region + " steht schon ein Steuerturm", u, u.getCoords());
			return;
		}
		
		GenericMake(u, 0, Burgenbau.class, 4, new Item [] { new Stein(5), new Eisen(4), new Holz(3), new Silber(250) } );
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Steuerturm", "Steuertürme", null);
	}
}
