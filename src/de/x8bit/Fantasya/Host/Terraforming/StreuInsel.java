package de.x8bit.Fantasya.Host.Terraforming;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.x8bit.Fantasya.Atlantis.Atlantis;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
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
public class StreuInsel extends ProtoInsel {
	private static final long serialVersionUID = 1L;

    public StreuInsel(int welt, String inselName) {
		super();
		
		this.name = inselName;
		this.zielGroesse = rndGroesse();
        this.setWelt(welt);
    }

	public Map<Class<? extends Atlantis>, Double> getTerrainProbabilities() {
		Map<Class<? extends Atlantis>, Double> myLogP = new HashMap<Class<? extends Atlantis>, Double>();
		Map<Class<? extends Atlantis>, Double> linearP = new HashMap<Class<? extends Atlantis>, Double>();

		// default: alle Terrains gleich Wahrscheinlich (+/- 0)
		for (Class<? extends Region> typ : GameRules.OberWeltTerrains()) {
			myLogP.put(typ, 0d);
		}

		// Streuinseln: kein expliziter Ozean / Seen
        myLogP.remove(Ozean.class);

		myLogP.put(Ebene.class, 0d);
		myLogP.put(Wald.class, 0d);
		myLogP.put(Sumpf.class, 0.5d);
        myLogP.put(Hochland.class, -2d);
		myLogP.put(Gletscher.class, -2d);
		myLogP.put(Berge.class, -1d);
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
	 * @return 3W6 + W10 ( ~ 16 )
	 */
	protected int rndGroesse() {
		return Random.rnd(1, 7) + Random.rnd(1, 7) + Random.rnd(1, 7) + Random.rnd(1, 11);
	}

	protected int rndAbstand() {
		int d = Random.rnd(1, 7) - 3; // W6 - 3, aber mindestens 1
		return (d > 1?d:1);
	}

	private void createEinzelhaufen(List<Coords> kandidaten, int size) {
		Map<Class<? extends Atlantis>, Double> chances = getTerrainProbabilities();

		Collections.shuffle(kandidaten);

		Set<Coords> me = new HashSet<Coords>();

		// den "Keim" suchen:
		// Eine Region suchen, die an einen Kandidaten grenzt
		// UND nicht neben einer existierenden Region liegt.
		Coords keim = null;
		for (Coords c : kandidaten) {
			for (Coords n : c.getNachbarn()) {
				if (this.getRegion(n.getX(), n.getY()) != null) continue; // nope, mich gibt es schon

				// so, hat dieser Nachbar eines Kandidaten nun selbst schon
				// richtige Nachbarn?
				boolean okay = true;
				for (Coords nn : n.getNachbarn()) {
					Region nachbarsNachbar = this.getRegion(nn.getX(), nn.getY());
					if (nachbarsNachbar != null) {
						okay = false;
						break;
					} // leider nein...
				}

				if (okay) {
					// gotcha!
					keim = n;
					break;
				}
			}
			if (keim != null) break;
		}

		if (keim == null) {
			new BigError(new NullPointerException("Keine Koordinate für neue Region gefunden..."));
		}
		me.add(keim);


		// Schritt 2 - wachsen
		// Regionen suchen, die jeweils an "me" grenzen, aber nicht an bestehende Regionen
		while (me.size() < size) {
			kandidaten.clear();
			for (Coords c : me) {
				for (Coords n : c.getNachbarn()) {
					if (me.contains(n)) continue;
					// ist noch nicht in me.

					if (this.getRegion(n.getX(), n.getY()) != null) continue;
					// existiert noch nicht

					boolean okay = true;
					for (Coords nn : n.getNachbarn()) {
						if (this.getRegion(nn.getX(), nn.getY()) != null) okay = false;
					}
					if (!okay) continue;
					// hat keinen existierenden Nachbarn

					// gotcha!
					kandidaten.add(n);
				}
			}

			if (kandidaten.isEmpty()) {
				new SysMsg("Ooops - kann Einzehaufen nicht weiterwachsen lassen, alles schon besetzt...");
				return;
			}

			Collections.shuffle(kandidaten);
			// nur den ersten anwachsen lassen:
			for (Coords c : kandidaten) {
				me.add(c);
				break;
			}
		}

		// so, jetzt ernst machen und die Regionen erzeugen:
		int i = 1;
		for (Coords c : me) {
			Region r = this.randomRegion(chances);
			r.setCoords(c);
			r.setName(" (Haufen: " + i + "/" + size + "R)");
			this.putRegion(r);
			i ++;
		}
	}

	@Override
	public void create() {
		// den "Keim" vorbereiten:
		List<Coords> kandidaten = new ArrayList<Coords>();
		kandidaten.add(new Coords(0, 0, this.getWelt()));
		int einzelGroesse = 1;

		while (alleRegionen().size() < getZielGroesse()) {
			do {
				einzelGroesse =
					Random.rnd(0, 2) +
					Random.rnd(0, 2) +
					Random.rnd(1, 5);
				if (einzelGroesse < 1) einzelGroesse = 1;

			} while (alleRegionen().size() + einzelGroesse > getZielGroesse());

			// new Debug("StreuInsel.Einzelhaufen mit " + einzelGroesse + " Regionen anlegen...");
			createEinzelhaufen(kandidaten, einzelGroesse);

			kandidaten.clear();
			kandidaten.addAll(this.getAussenKontur());
		}
		
		// und der Mittelpunkt (ohne Berücksichtigung von Ozean) ist:
		this.mittelpunkt = null;
		Coords m = this.getMittelpunkt(false); // ohne Ozean
		if (this.getRegion(m.getX(), m.getY()) != null) {
			Region r = this.getRegion(m.getX(), m.getY());
			r.setName("M-" + r.getName());
		}
		this.shift(0 - m.getX(), 0 - m.getY());


		// für diesen Insel-Typ einen Extra-Ozean-Gürtel umlegen:
		this.nackteKuestenBewaessern();


		int anzahlRegionen = alleRegionen().size();
		for (Region r : alleRegionen()) {
			String ifName = r.getName();
			if (ifName == null) ifName = "";
			r.setName(this.getName() + " (" + anzahlRegionen + "R)" + ifName);
		}

		luxusProdukteSetzen();
	}







}

