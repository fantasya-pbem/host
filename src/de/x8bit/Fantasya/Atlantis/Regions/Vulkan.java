package de.x8bit.Fantasya.Atlantis.Regions;


import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Items.Alpaka;
import de.x8bit.Fantasya.Atlantis.Items.Eisen;
import de.x8bit.Fantasya.Atlantis.Items.Gold;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Items.Stein;
import de.x8bit.Fantasya.util.Random;

public class Vulkan extends Region {
	
	public Vulkan()	{
		setSteineFuerStrasse(250);		// Steine für eine Strassenrichtung
	}
	
	@Override
	public String getArtikel() { return "der".intern(); }

	/** Initialisierung der Bauern & so */
	@Override
	public void Init() {
		super.Init();
		
		if (getBauern() == 0) setBauern(Random.rnd(10, 120));
		if (getBaum() != 0) setResource(Holz.class, getBaum()); else setResource(Holz.class, Random.rnd(0, 10));
		if (getSilber() == 0) setSilber(getBauern() * Random.rnd(11, 16));
		setResource(Eisen.class, Random.rnd(20, 40));
		setResource(Gold.class, Random.rnd(10, 20));
		setResource(Stein.class, Random.rnd(20, 40));
		setResource(Alpaka.class, Random.rnd(0, 10));
	}

	/**
	 * @return An Vulkanen geht es sehr fruchtbar zu - Bauern bekommen hier 2 Silber pro Monat mehr Lohn als anderswo. Bis - ja bis...
	 */
	@Override
	public int getLohn() {
		return super.getLohn() + 2;
	}
	
	

	@Override
	public int freieArbeitsplaetze() {
		return 250 - super.benutzteArbeitsplaetze();
	}
}
