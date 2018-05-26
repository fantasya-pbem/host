package de.x8bit.Fantasya.Atlantis.Regions;


import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.util.Random;

public class Moor extends Region
{
	
	public Moor() {
		setSteineFuerStrasse(400);		// Steine für eine Strassenrichtung
	}
	
	@Override
	public String getArtikel() { return "das".intern(); }

	/** Initialisierung der Bauern &amp; so */
	@Override
	public void Init() {
		super.Init();
		if (getBauern() == 0) {
			int bauern = Random.W(20) + Random.W(20) - 25;
			if (bauern > 0)	setBauern(bauern);
		}
		if (getSilber() == 0) setSilber(getBauern() * Random.rnd(11, 16));
	}

	@Override
	public int freieArbeitsplaetze() {
		return 500 - super.benutzteArbeitsplaetze();
	}
}
