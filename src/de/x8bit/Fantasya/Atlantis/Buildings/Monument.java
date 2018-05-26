package de.x8bit.Fantasya.Atlantis.Buildings;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Eisen;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Items.Stein;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Skills.Burgenbau;
import de.x8bit.Fantasya.util.ComplexName;

public class Monument extends Building
{
	public void Zerstoere(Unit u)
	{
		new Fehler(u + " will nicht den einzigen Beweisen seines gloreichen Volkes vernichten", u, u.getCoords());
		return;
	}
	
	public String getTyp() { return "Hafen"; }
	
	public int GebaeudeUnterhalt() { return 100; }

	
	/**
	 * erstellt ein Gebäude bzw. baut daran weiter
	 * @param u - diese Einheit will daran bauen
	 */
	public void Mache(Unit u)
	{
		// nochmal holen ... ist größer als Null
		int tw = u.Talentwert(Burgenbau.class);
		if (tw < 3)
		{
			new Fehler(u + " hat nicht genügend Talent um an " + this + " zu bauen", u, u.getCoords());
			return;
		}
		
		GenericMake(u, 0, Burgenbau.class, 3, new Item [] { new Stein(7), new Eisen(5), new Holz(2), new Silber(150) } );
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Monument", "Monumente", null);
	}
}
