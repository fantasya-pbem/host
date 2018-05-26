package de.x8bit.Fantasya.Atlantis.Regions;


import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Items.Alpaka;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Items.Zotte;
import de.x8bit.Fantasya.util.Random;

public class Oedland extends Region
{
	
	public Oedland() {
		setSteineFuerStrasse(100);		// Steine für eine Strassenrichtung
	}
	
	@Override
	public String getArtikel() { return "das"; }

	/** Initialisierung der Bauern & so */
	@Override
	public void Init() {
		super.Init();
		
		if (getBauern() == 0) setBauern(Random.rnd(100, 500));
		if (getBaum() != 0) setResource(Holz.class, getBaum()); else setResource(Holz.class, Random.rnd(0, 30));
		if (getSilber() == 0) setSilber(getBauern() * Random.rnd(11, 16));
		setResource(Zotte.class, Random.rnd(5, 30));
		setResource(Alpaka.class, Random.rnd(0, 8));
		// setResource(Mastodon.class, Random.rnd(0, 20));
	}

	@Override
	public int freieArbeitsplaetze() {
		return 1000 - super.benutzteArbeitsplaetze();
	}

}
