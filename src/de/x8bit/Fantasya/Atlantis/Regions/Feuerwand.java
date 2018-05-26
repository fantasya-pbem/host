package de.x8bit.Fantasya.Atlantis.Regions;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;

/**
 * In Fantasya gibt es keine Feuerwände, die Klasse ist also nur ein Hilfsmittel
 * für die Software-Entwicklung (als Marker-Region / Regions-Marker...)
 */
public class Feuerwand extends Region {

	public Feuerwand() {
	}

	public Feuerwand(int x, int y, int welt) {
		this();
        this.setCoords(new Coords(x, y, welt));
	}

	@Override
	public String getArtikel() {
		return "die";
	}

	@Override
	public void Init() {
		// nix. Die Region bleibt wüst und leer
	}

	@Override
	public boolean istBetretbar(Unit unit) {
		return false;
	}
	
	@Override
	public boolean canNachfrage() { return false; }
}
