package de.x8bit.Fantasya.Atlantis.Regions;


import de.x8bit.Fantasya.Atlantis.Region;

public class Sandstrom extends Region {

	public Sandstrom() {
		setSteineFuerStrasse(200);		// Steine für eine Strassenrichtung
	}

	@Override
	public String getArtikel() { return "der".intern(); }

	/** Initialisierung der Bauern & so */
	@Override
	public void Init() {
		super.Init();

//		if (getBauern() == 0) setBauern(Random.rnd(10, 50));
//		if (getBaum() != 0) setResource(Holz.class, getBaum()); else setResource(Holz.class, Random.rnd(0, 10));
//		if (getSilber() == 0) setSilber(getBauern() * Random.rnd(11, 16));
//		setResource(Zotte.class, Random.rnd(0, 10));
//		setResource(Alpaka.class, Random.rnd(50, 100));
	}

	@Override
	public int freieArbeitsplaetze() {
		return 0 - super.benutzteArbeitsplaetze();
	}

	@Override
	public boolean canNachfrage() { return false; }
}
