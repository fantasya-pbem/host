package de.x8bit.Fantasya.Host.Terraforming;

import de.x8bit.Fantasya.Atlantis.Atlantis;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Regions.*;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.util.Random;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author hb
 */
public class UnterweltKegelInsel extends ProtoInsel {
	private static final long serialVersionUID = -5371951485787247931L;

    public UnterweltKegelInsel(int welt, String inselName) {
		super();
		
		this.name = inselName;
		this.zielGroesse = rndGroesse();
		this.setWelt(welt);
    }

	@Override
	public void create() {
		fuellTyp = Sandstrom.class;
		if (Random.W(6) > 1) fuellTyp = Ozean.class;
		
		Map<Class<? extends Atlantis>, Double> chances = getTerrainProbabilities();
		// den "Keim" pflanzen:
		try {
			Region keim = Berge.class.newInstance();
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
				int distanceToZero = c.getDistance(new Coords(0,0,c.getWelt()));
				double pAccept = 1d / (double)distanceToZero;
				pAccept *= pAccept;
				if (rnd.nextDouble() > pAccept) c = null;
			}
			Region r = this.randomRegion(chances);
			r.setCoords(c);
			r.setName(getName() + "@" + loop);
			this.putRegion(r);
			loop++;
		}
		// Distanzen jeder Region zu allen anderen Regionen:
		for (Region a : this.alleRegionen()) {
			if (a.getClass() == Ozean.class) continue;
			int summe = 0;
			for (Region b : this.alleRegionen()) {
				if (b.getClass() == Ozean.class) continue;
				if (a.getCoords().equals(b.getCoords())) continue;
				summe += a.getCoords().getDistance(b.getCoords());
			}
			float avg = (float)summe / (float)this.alleRegionen().size();
			String beschr = a.getBeschreibung();
			if (beschr.length() > 0) beschr += "; ";
			a.setBeschreibung(beschr + "Durchschnitts-Entfernung von allen Regionen der Insel: " + avg);
		}
		// Lücken füllen (Binnenseen):
		binnenSeenFuellen();
		// und der Mittelpunkt (ohne Berücksichtigung von Ozean) ist:
		this.mittelpunkt = null;
		Coords m = this.getMittelpunkt(false); // ohne Ozean
		if (this.getRegion(m.getX(), m.getY()) != null) {
			Region r = this.getRegion(m.getX(), m.getY());
			r.setName("M-" + r.getName());
		}
		this.shift(0 - m.getX(), 0 - m.getY());

		luxusProdukteSetzen();
	}
	
	@Override
	public Map<Class<? extends Atlantis>, Double> getTerrainProbabilities() {
		Map<Class<? extends Atlantis>, Double> myLogP = new HashMap<Class<? extends Atlantis>, Double>();
		Map<Class<? extends Atlantis>, Double> linearP = new HashMap<Class<? extends Atlantis>, Double>();


		// default: alle Terrains gleich Wahrscheinlich (+/- 0)
		for (Class<? extends Region> typ : GameRules.UnterWeltTerrains()) {
			myLogP.put(typ, 0d);
		}

		myLogP.remove(Lavastrom.class);
		myLogP.remove(Sandstrom.class);
		myLogP.put(Wueste.class, 1d);
		myLogP.put(aktiverVulkan.class, -0.8d);
		myLogP.put(Vulkan.class, -0.2d);
		// myLogP.put(aktiverVulkan.class, -1e8); // haha, mal sehen was passiert... Oh, kein Problem...


		// Jetzt alle Chancen summieren:
		double totalP = 0d;
		for (Class<? extends Atlantis> typ : myLogP.keySet()) {
			double logChance = myLogP.get(typ);
			double chance = Math.pow(2, logChance);

			totalP += chance;
		}

		// Und normalisiert als lineare Wahrscheinlichkeit speichern:
		for (Class<? extends Atlantis> typ : myLogP.keySet()) {
			double logChance = myLogP.get(typ);
			double chance = Math.pow(2, logChance);

            linearP.put(typ, chance / totalP);
		}

        return linearP;
	}


	@Override
	protected int rndGroesse() {
		return Random.rnd(3, 11) + Random.rnd(2, 11) + Random.rnd(1, 31);
		// ~ 6,5 + 6 + 15,5 = 28
		// min. 6
		// max. 50
	}

	@Override
	protected int rndAbstand() {
		int d = Random.W(4) - 1; // W4 - 1, kann auch 0 sein!
		return (d > 0?d:0);
	}




}

