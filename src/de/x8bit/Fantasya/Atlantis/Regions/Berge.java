package de.x8bit.Fantasya.Atlantis.Regions;



import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Items.Eisen;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Items.Pferd;
import de.x8bit.Fantasya.Atlantis.Items.Stein;
import de.x8bit.Fantasya.Atlantis.Items.Zotte;
import de.x8bit.Fantasya.util.Random;

public class Berge extends Region
{
	public Berge() {
		setSteineFuerStrasse(250);		// Steine für eine Strassenrichtung
	}
	
	/** Initialisierung der Bauern & so */
	@Override
	public void Init()
	{
		super.Init();
		
		if (getBauern() == 0) setBauern(Random.rnd(200, 500));		// 2oo - 5oo Bauern
		if (getBaum() != 0) setResource(Holz.class, getBaum()); else setResource(Holz.class, Random.rnd(0, 100));
		if (getSilber() == 0) setSilber(getBauern() * Random.rnd(11, 16));
		this.InitMinerals();
		if (this.getCoords().getWelt() > 0) {
			setResource(Pferd.class, Random.rnd(0, 50));
		}
		else {
			setResource(Zotte.class, Random.rnd(0, 50));
		}
	}

	/**
	 * <p>legt die Vorräte an Bodenschätzen fest.</p>
	 * <p>ausgegliedert, damit selbige beim Terraforming (durch Magie) jeweils neu bestimmt werden können.</p>
	 */
	public void InitMinerals() {
		setResource(Eisen.class, Random.rnd(70, 130));
		setResource(Stein.class, Random.rnd(70, 130));
	}
	
	/**
	 * liefert die noch freien Arbeitsplätze
	 * @return freien Arbeitsplätze
	 */
	@Override
	public int freieArbeitsplaetze()
	{
		return 1000 - super.benutzteArbeitsplaetze();
	}
}
