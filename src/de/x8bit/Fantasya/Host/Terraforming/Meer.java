package de.x8bit.Fantasya.Host.Terraforming;

import de.x8bit.Fantasya.Atlantis.Atlantis;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
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
public class Meer extends ProtoInsel {
	private static final long serialVersionUID = 1L;

    public Meer(int welt, String inselName) {
		super();
		
		this.name = inselName;
		this.zielGroesse = rndGroesse();
		this.setWelt(welt);
    }

	public void create() {
		// den "Keim" pflanzen:
		try {
			Region keim = Ozean.class.newInstance();
			keim.setCoords(new Coords(0, 0, 1));
			this.putRegion(keim);
		} catch (InstantiationException ex) {
			new BigError(ex);
		} catch (IllegalAccessException ex) {
			new BigError(ex);
		}
		int loop = 0;
		
		while (alleRegionen().size() < getZielGroesse()) {
			// alle leeren Regionen in Nachbarschaft der existierenden finden
			// Nähe zum Ursprung bevorzugen
			List<Coords> kandidaten = new ArrayList<Coords>();
			kandidaten.addAll(this.getAussenKontur());
			Coords c = null;
			while (c == null) {
				Collections.shuffle(kandidaten, rnd);
				// ...und einsetzen:
				c = kandidaten.get(0);
			}
			Region r = new Ozean();
			r.setCoords(c);
			r.setName(getName() + "@" + loop);
			this.putRegion(r);
			loop++;
		}


		// Lücken füllen:
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
	


	protected int rndGroesse() {
		return Random.rnd(3, 11) + Random.rnd(2, 11) + Random.rnd(1, 11) + Random.rnd(1, 81) + Random.rnd(1, 81);
	}

	protected int rndAbstand() {
		return (1);
	}

	@Override
	public Map<Class<? extends Atlantis>, Double> getTerrainProbabilities() {
		throw new UnsupportedOperationException("Not supported.");
	}


}

