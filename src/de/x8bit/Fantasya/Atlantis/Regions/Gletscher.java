package de.x8bit.Fantasya.Atlantis.Regions;



import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Items.Eisen;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Items.Pferd;
import de.x8bit.Fantasya.Atlantis.Items.Stein;
import de.x8bit.Fantasya.util.Random;

public class Gletscher extends Region
{
	public Gletscher()
	{
		setSteineFuerStrasse(350);		// Steine für eine Strassenrichtung
	}

	@Override
	public String getArtikel() { return "der".intern(); }

	/** Initialisierung der Bauern & so */
	@Override
	public void Init()
	{
		super.Init();
		
		if (getBauern() == 0) setBauern(Random.rnd(0, 100));
		if (getBaum() != 0) setResource(Holz.class, getBaum()); else setResource(Holz.class, Random.rnd(0, 10));
		if (getSilber() == 0) setSilber(getBauern() * Random.rnd(11, 16));
		setResource(Pferd.class, Random.rnd(0, 20));

		this.InitMinerals();
	}

	/**
	 * <p>legt die Vorräte an Bodenschätzen fest.</p>
	 * <p>ausgegliedert, damit selbige beim Terraforming (durch Magie) jeweils neu bestimmt werden können.</p>
	 */
	public void InitMinerals() {
		int r;
		r = Random.rnd(-20,20); if (r < 0) r = 0; setResource(Eisen.class, r);
		r = Random.rnd(-20,20); if (r < 0) r = 0; setResource(Stein.class, r);
	}

	/**
	 * liefert die noch freien Arbeitsplätze
	 * @return freien Arbeitsplätze
	 */
	@Override
	public int freieArbeitsplaetze()
	{
		return 100 - super.benutzteArbeitsplaetze();
	}
}
