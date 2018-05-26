package de.x8bit.Fantasya.Host.Terraforming;

import de.x8bit.Fantasya.Atlantis.Atlantis;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Regions.*;
import de.x8bit.Fantasya.util.Random;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author hb
 */
public class KleinMeer extends ProtoInsel {
	private static final long serialVersionUID = 1L;

    public KleinMeer(int welt, String inselName) {
		super();
		
		this.name = inselName;
		this.zielGroesse = rndGroesse();
		this.setWelt(welt);
    }

	public void create() {
		// den "Keim" pflanzen:
		Region keim = new Ozean();
		keim.setCoords(new Coords(0, 0, this.getWelt()));
		this.putRegion(keim);
		int loop = 0;
		
		while (alleRegionen().size() < getZielGroesse()) {
			// alle leeren Regionen in Nachbarschaft der existierenden finden
			// Nähe zum Ursprung bevorzugen
			List<Coords> kandidaten = new ArrayList<Coords>();
			kandidaten.addAll(this.getAussenKontur());

			Collections.shuffle(kandidaten, rnd);
			// ...und einsetzen:
			Coords c = kandidaten.get(0);

			Region r = new Ozean();
			r.setCoords(c);
			r.setName(getName() + "@" + loop);
			this.putRegion(r);
			loop++;
		}

		// Lücken füllen (Binnenseen):
		binnenSeenFuellen();
		
		// und der Mittelpunkt ist:
		this.mittelpunkt = null;
		Coords m = this.getMittelpunkt(true); // mit Ozean
		if (this.getRegion(m.getX(), m.getY()) != null) {
			Region r = this.getRegion(m.getX(), m.getY());
			r.setName("M-" + r.getName());
		}
		this.shift(0 - m.getX(), 0 - m.getY());

		luxusProdukteSetzen();
	}
	
	/**
	 * @return W6 + (0/1?) + (0/1?) = ~ 4,5 (zwischen 1 und 8 Regionen)
	 */
	protected int rndGroesse() {
		return Random.rnd(1, 7) + Random.rnd(0, 2) + Random.rnd(0, 2);
	}

	protected int rndAbstand() {
		return (0);
	}

	@Override
	public Map<Class<? extends Atlantis>, Double> getTerrainProbabilities() {
		throw new UnsupportedOperationException("Not supported yet.");
	}


}

