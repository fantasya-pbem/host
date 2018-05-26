package de.x8bit.Fantasya.Atlantis.Regions;

import de.x8bit.Fantasya.Atlantis.Items.Alpaka;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Items.Kamel;
import de.x8bit.Fantasya.Atlantis.Items.Pferd;
import de.x8bit.Fantasya.Atlantis.Items.Zotte;
import de.x8bit.Fantasya.util.Random;

public class Wueste extends Region
{

	public Wueste()
	{
		setSteineFuerStrasse(150);		// Steine für eine Strassenrichtung
	}
	
	/** Initialisierung der Bauern & so */
	@Override
	public void Init()
	{
		super.Init();
		
		
		if (this.getCoords().getWelt() > 0) {
			// Wüsten in der Oberwelt:
			if (getBauern() == 0) setBauern(Random.W(200) - 1);
			
			int baeume = Random.W(20) - 10;
			setResource(Holz.class, (baeume > 0) ? baeume : 0);
			setResource(Pferd.class, Random.rnd(0, 50));
			setResource(Kamel.class, Random.rnd(100, 200));
		} else {
			// Wüsten in der Unterwelt:
			if (getBauern() == 0) {
				int bauern = Random.W(20) + Random.W(20) - 15;
				setBauern((bauern > 0) ? bauern : 0);
			}
			
			int baeume = Random.W(20) - 15;
			setResource(Holz.class, (baeume > 0) ? baeume : 0);
			
			setResource(Zotte.class, Random.rnd(0, 10));
			setResource(Alpaka.class, Random.rnd(0, 30));
		}
		
		if (getSilber() == 0) setSilber(getBauern() * Random.rnd(11, 16));
	}

	/**
	 * liefert die noch freien Arbeitsplätze
	 * @return freien Arbeitsplätze
	 */
	@Override
	public int freieArbeitsplaetze() {
		if (getCoords() == null) return 0;
		if (getCoords().getWelt() > 0) {
			// Oberwelt:
			return 500 - super.benutzteArbeitsplaetze();
		} else {
			// Unterwelt:
			return 125 - super.benutzteArbeitsplaetze();
		}
	}
}
