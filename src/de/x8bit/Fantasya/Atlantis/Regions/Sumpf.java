package de.x8bit.Fantasya.Atlantis.Regions;



import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Items.Elefant;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Items.Kamel;
import de.x8bit.Fantasya.Atlantis.Items.Pferd;
import de.x8bit.Fantasya.util.Random;

public class Sumpf extends Region
{
	public Sumpf()
	{
		setSteineFuerStrasse(200);		// Steine für eine Strassenrichtung
	}
	
	@Override
	public String getArtikel() { return "der".intern(); }

	/** Initialisierung der Bauern & so */
	@Override
	public void Init()
	{
		super.Init();
		
		if (getBauern() == 0) setBauern(Random.rnd(0, 100));		// 2oo - 5oo Bauern
		setResource(Holz.class, Random.rnd(50, 100));
		if (getSilber() == 0) setSilber(getBauern() * Random.rnd(11, 16));
		setResource(Pferd.class, Random.rnd(0, 20));
		setResource(Kamel.class, Random.rnd(0, 20));
		setResource(Elefant.class, Random.rnd(100, 150));
	}

	/**
	 * liefert die noch freien Arbeitsplätze
	 * @return freien Arbeitsplätze
	 */
	@Override
	public int freieArbeitsplaetze()
	{
		return 2000 - super.benutzteArbeitsplaetze();
	}
}
