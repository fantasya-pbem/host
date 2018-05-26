package de.x8bit.Fantasya.Atlantis.Regions;


import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Items.Alpaka;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Items.Mastodon;
import de.x8bit.Fantasya.Atlantis.Items.Zotte;
import de.x8bit.Fantasya.util.Random;

public class Trockenwald extends Region
{
	
	public Trockenwald() {
		setSteineFuerStrasse(200);		// Steine für eine Strassenrichtung
	}
	
	@Override
	public String getArtikel() { return "der".intern(); }

	/** Initialisierung der Bauern & so */
	@Override
	public void Init() {
		super.Init();
		
		if (getBauern() == 0) setBauern(Random.rnd(500, 1000));
		if (getBaum() != 0) setResource(Holz.class, getBaum()); else setResource(Holz.class, Random.rnd(100, 150));
		if (getSilber() == 0) setSilber(getBauern() * Random.rnd(11, 16));
		setResource(Zotte.class, Random.rnd(0, 20));
		setResource(Alpaka.class, Random.rnd(0, 20));
		setResource(Mastodon.class, Random.rnd(0, 20));
	}

	@Override
	public int freieArbeitsplaetze() {
		return 2500 - super.benutzteArbeitsplaetze();
	}
}
