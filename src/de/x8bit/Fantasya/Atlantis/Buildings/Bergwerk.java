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

public class Bergwerk extends Building {

	@Override
	public void Zerstoere(Unit u) {
		super.Zerstoere(u, new Item[]{new Stein(3), new Eisen(1), new Holz(3)});
	}

	@Override
	public String getTyp() {
		return "Bergwerk";
	}

	@Override
	public int GebaeudeUnterhalt() {
		return 100;
	}

	@Override
	public int UnterhaltEinheit() {
		return 5;
	}

	/**
	 * erstellt ein Gebäude bzw. baut daran weiter
	 * @param u - diese Einheit will daran bauen
	 */
	@Override
	public void Mache(Unit u) {
		// nochmal holen ... ist größer als Null
		int tw = u.Talentwert(Burgenbau.class);
		if (tw < 5) {
			new Fehler(u + " hat nicht genügend Talent um an " + this + " zu bauen", u, u.getCoords());
			return;
		}

		// zusätzlichen Gebäude testen
		Region region = Region.Load(u.getCoords());
		if (!region.hatGebaeude(Burg.class, 10, u)) {
			new Fehler(u + " - in " + region + " fehlt ein Turm um " + getTyp() + " bauen zu können", u, u.getCoords());
			return;
		}

		GenericMake(u, 0, Burgenbau.class, 5, new Item[]{new Stein(6), new Eisen(3), new Holz(6), new Silber(250)});
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Bergwerk", "Bergwerke", null);
	}
}
