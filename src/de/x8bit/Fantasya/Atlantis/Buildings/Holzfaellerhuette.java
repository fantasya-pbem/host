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

public class Holzfaellerhuette extends Building
{
	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Holzfällerhütte", "Holzfällerhütten",
				new String[] {"Holzfaellerhuette", "Holzfaellerhuetten"});
	}

	public String getTyp() { return "Holzfällerhütte"; }

	public void Zerstoere(Unit u) {
		super.Zerstoere(u, new Item [] { new Stein(2), new Eisen(1)  });	
	}
	
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
		
		GenericMake(u, 0, Burgenbau.class, 3, new Item [] { new Stein(3), new Eisen(2), new Holz(1), new Silber(100) } );
	}
}
