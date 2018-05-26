package de.x8bit.Fantasya.Host.Terraforming;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.x8bit.Fantasya.Atlantis.Atlantis;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Regions.Ozean;
import de.x8bit.Fantasya.Atlantis.Regions.Sandstrom;
import de.x8bit.Fantasya.util.MapSelection;
import de.x8bit.Fantasya.util.Random;

/**
 *
 * @author hb
 */
public class KleinerSandstrom extends ProtoInsel {
	private static final long serialVersionUID = 1L;

    public KleinerSandstrom(int welt, String inselName) {
		super();
		
		this.name = inselName;
		this.zielGroesse = rndGroesse();
		this.setWelt(welt);
    }

	@Override
	public void create() {
		fuellTyp = Sandstrom.class;
		if (Random.W(6) > 1) fuellTyp = Ozean.class;
		
		// zwei Keime pflanzen:
		Coords c1 = new Coords(0, 0, this.getWelt());
		MapSelection endpunktSuche = new MapSelection();
		endpunktSuche.add(c1);
		endpunktSuche.wachsen(this.getZielGroesse());
		Coords c2 = endpunktSuche.getInnenKontur().zufaelligeKoordinate();
		
		MapSelection ma = new MapSelection();
		ma.add(c1);
		Region r = new Sandstrom(); r.setCoords(c1); r.setName(getName() + "-Anfang!");
		this.putRegion(r);
		
		MapSelection mb = new MapSelection();
		mb.add(c2);
		r = new Sandstrom(); r.setCoords(c2); r.setName(getName() + "-Ende!");
		this.putRegion(r);
		
		
		// die 10000 ist nur zur Sicherheit, normalerweise findet vorher ein break; statt!
		for (int loop = 0; loop < 10000; loop++) {
			// von c1 in eine beliebige Richtung wachsen:
			MapSelection kontur = ma.getAussenKontur();
			Coords wachsen1 = kontur.zufaelligeKoordinate();
			// new Debug("ma wächst: " + wachsen1 + " / " + kontur.toString());
			if (mb.contains(wachsen1)) break;
			ma.add(wachsen1);
			
			// von c2 nur mit gleichem oder geringeren Abstand zu c1 wachsen:
			int minDistance = Integer.MAX_VALUE;
			for (Coords c : mb)	if (c.getDistance(c1) < minDistance) minDistance = c.getDistance(c1);
			List<Coords> kandidaten = new ArrayList<Coords>();
			for (Coords c : mb.getAussenKontur()) if (c.getDistance(c1) <= minDistance) kandidaten.add(c);
			Collections.shuffle(kandidaten);
			Coords wachsen2 = kandidaten.get(0);
			if (ma.contains(wachsen2)) break;
			mb.add(wachsen2);
			
			
			r = new Sandstrom(); r.setCoords(wachsen1); r.setName(getName() + "-Anfang@" + loop);
			// new Debug("Region @ " + r.getCoords());
			this.putRegion(r);
			// new Debug("Insel enthält: " + this.get(wachsen1.getX()).get(wachsen1.getY()));
			r = new Sandstrom(); r.setCoords(wachsen2); r.setName(getName() + "-Ende@" + loop);
			this.putRegion(r);
		}

		// Lücken füllen (Binnenseen):
		binnenSeenFuellen();
		
		// und der Mittelpunkt ist:
		this.mittelpunkt = null;
		Coords m = this.getMittelpunkt(true); // mit Ozean
		if (this.getRegion(m.getX(), m.getY()) != null) {
			r = this.getRegion(m.getX(), m.getY());
			r.setName("M-" + r.getName());
		}
		this.shift(0 - m.getX(), 0 - m.getY());

		luxusProdukteSetzen();
	}
	
	/**
	 * @return W6 + W2 + 1 = ~ 6 (zwischen 3 und 9 Regionen)
	 */
	@Override
	protected int rndGroesse() {
		return Random.W(6) + Random.W(2) + 1;
	}

	@Override
	protected int rndAbstand() {
		return (0);
	}

	@Override
	public Map<Class<? extends Atlantis>, Double> getTerrainProbabilities() {
		throw new UnsupportedOperationException("Not supported yet.");
	}


}

