package de.x8bit.Fantasya.Atlantis.Regions;



import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Items.Elefant;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Items.Kamel;
import de.x8bit.Fantasya.Atlantis.Items.Pferd;
import de.x8bit.Fantasya.util.Random;

public class Wald extends Region
{
	public Wald()
	{
		setBauern(Random.rnd(0, 2000));
		setResource(Holz.class, Random.rnd(600, 900));
		setSteineFuerStrasse(50);		// Steine für eine Strassenrichtung
	}

	@Override
	public String getArtikel() { return "der".intern(); }

	/** Initialisierung der Bauern & so */
	@Override
	public void Init()
	{
		super.Init();
		
		if (getBauern() == 0) setBauern(Random.rnd(0, 2000));
		if (getBaum() != 0) setResource(Holz.class, getBaum()); else setResource(Holz.class, Random.rnd(600, 900));
		if (getSilber() == 0) setSilber(getBauern() * Random.rnd(11, 16));
		setResource(Pferd.class, Random.rnd(0, 20));
		setResource(Kamel.class, Random.rnd(0, 20));
		setResource(Elefant.class, Random.rnd(0, 20));
	}

	/**
	 * liefert die noch freien Arbeitsplätze ... eigentlich hat der Wald ja nur 4000 Arbeitsplätze da
	 * 6000 Arbeitsplätze schon irgendwie durch den Wald belegt sind ... aber da in der Rechnung
	 * der Basis-Klasse die Bäume mit drinnen sind, können hier auch die 10000 von der Ebene verwendet werden
	 * @return freien Arbeitsplätze
	 */
	@Override
	public int freieArbeitsplaetze()
	{
		return 10000 - super.benutzteArbeitsplaetze();
	}
}
