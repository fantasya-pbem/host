package de.x8bit.Fantasya.Atlantis.Regions;

import java.awt.image.BufferedImage;


import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Items.Eisen;
import de.x8bit.Fantasya.Atlantis.Items.Elefant;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Items.Kamel;
import de.x8bit.Fantasya.Atlantis.Items.Pferd;
import de.x8bit.Fantasya.Atlantis.Items.Stein;
import de.x8bit.Fantasya.util.Random;

public class Hochland extends Region
{
	public Hochland()
	{
		setSteineFuerStrasse(100);		// Steine für eine Strassenrichtung
	}

	@Override
	public String getArtikel() { return "das".intern(); }

	/** Initialisierung der Bauern & so */
	@Override
	public void Init()
	{
		super.Init();
		
		
		if (getBauern() == 0) setBauern(Random.rnd(0, 200));		// 2oo - 5oo Bauern
		setResource(Holz.class, Random.rnd(100, 200));
		if (getSilber() == 0) setSilber(getBauern() * Random.rnd(11, 16));
		setResource(Pferd.class, Random.rnd(50, 150));
		setResource(Kamel.class, Random.rnd(50, 150));
		setResource(Elefant.class, Random.rnd(0, 20));
		
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
	
	private static BufferedImage image = null;

	/**
	 * liefert die noch freien Arbeitsplätze
	 * @return freien Arbeitsplätze
	 */
	@Override
	public int freieArbeitsplaetze()
	{
		return 4000 - super.benutzteArbeitsplaetze();
	}
}
