package de.x8bit.Fantasya.Host.Terraforming;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.x8bit.Fantasya.Atlantis.Atlantis;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Regions.Berge;
import de.x8bit.Fantasya.Atlantis.Regions.Ebene;
import de.x8bit.Fantasya.Atlantis.Regions.Gletscher;
import de.x8bit.Fantasya.Atlantis.Regions.Hochland;
import de.x8bit.Fantasya.Atlantis.Regions.Ozean;
import de.x8bit.Fantasya.Atlantis.Regions.Sumpf;
import de.x8bit.Fantasya.Atlantis.Regions.Wald;
import de.x8bit.Fantasya.Atlantis.Regions.Wueste;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.util.Random;

/**
 *
 * @author hb
 */
public class KleinInsel extends ProtoInsel {
	private static final long serialVersionUID = 8299133873009098704L;

    public KleinInsel(int welt, String inselName) {
		super();
		
		this.name = inselName;
		this.zielGroesse = rndGroesse();
		this.setWelt(welt);
    }

	public void create() {
		Map<Class<? extends Atlantis>, Double> chances = getTerrainProbabilities();
		// den "Keim" pflanzen:
		Region keim = this.randomRegion(chances);
		keim.setCoords(new Coords(0, 0, this.getWelt()));
		this.putRegion(keim);
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
	
	public Map<Class<? extends Atlantis>, Double> getTerrainProbabilities() {
		Map<Class<? extends Atlantis>, Double> myLogP = new HashMap<Class<? extends Atlantis>, Double>();
		Map<Class<? extends Atlantis>, Double> linearP = new HashMap<Class<? extends Atlantis>, Double>();

		// default: alle Terrains gleich Wahrscheinlich (+/- 0)
		for (Class<? extends Region> typ : GameRules.OberWeltTerrains()) {
			myLogP.put(typ, 0d);
		}

		// Kleininseln: kein expliziter Ozean / Seen
        myLogP.remove(Ozean.class);

		myLogP.put(Ebene.class, 0d);
		myLogP.put(Wald.class, 0d);
		myLogP.put(Sumpf.class, 0.5d);
        myLogP.put(Hochland.class, -1d);
		myLogP.put(Gletscher.class, -2d);
		myLogP.put(Berge.class, 0d);
		myLogP.put(Wueste.class, -4d);

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


	/**
	 * @return 2W3 + (0/1?) + (0/1?) = ~ 5 (zwischen 2 und 8 Regionen)
	 */
	protected int rndGroesse() {
		return Random.W(3) + Random.W(3) + Random.rnd(0, 2) + Random.rnd(0, 2);
	}

	protected int rndAbstand() {
		return Random.rnd(1, 3); // 1 oder 2
	}







}

