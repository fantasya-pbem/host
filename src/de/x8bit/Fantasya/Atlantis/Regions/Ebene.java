package de.x8bit.Fantasya.Atlantis.Regions;



import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Items.Elefant;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Items.Kamel;
import de.x8bit.Fantasya.Atlantis.Items.Pferd;
import de.x8bit.Fantasya.util.Random;

public class Ebene extends Region
{
	public Ebene()
	{
		setSteineFuerStrasse(50);		// Steine für eine Strassenrichtung
	}
	
	/** Initialisierung der Bauern & so */
	@Override
	public void Init()
	{
		super.Init();
		
		if (getBauern() == 0) setBauern(Random.rnd(5000, 8000));		// 2oo - 5oo Bauern
		if (getBaum() != 0) setResource(Holz.class, getBaum()); else setResource(Holz.class, Random.rnd(0, 100));
		if (getSilber() == 0) setSilber(getBauern() * Random.rnd(11, 16));
		setResource(Pferd.class, Random.rnd(50, 150));
		setResource(Kamel.class, Random.rnd(0, 100));
		setResource(Elefant.class, Random.rnd(0, 100));
	}
	
	/**
	 * liefert die noch freien Arbeitsplätze
	 * @return freien Arbeitsplätze
	 */
	@Override
	public int freieArbeitsplaetze()
	{
		return 10000 - super.benutzteArbeitsplaetze();
	}
}
