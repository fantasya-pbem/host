package de.x8bit.Fantasya.Host.Terraforming;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Helper.StartPosition;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Regions.Ebene;
import de.x8bit.Fantasya.Atlantis.Regions.Feuerwand;
import de.x8bit.Fantasya.Atlantis.Regions.Gletscher;
import de.x8bit.Fantasya.Atlantis.Regions.Hochland;
import de.x8bit.Fantasya.Atlantis.Regions.Ozean;
import de.x8bit.Fantasya.Host.EVA.util.ZATMode;
import de.x8bit.Fantasya.util.MapSelection;
import de.x8bit.Fantasya.util.Random;
import de.x8bit.Fantasya.util.StatSerie;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author hb
 */
public class InselGenerator {
    Random rnd = new Random();

    public InselGenerator() {
    }

	public ProtoInsel addNewInselTo(ProtoInsel vorhanden) {
		int alteInselKennung = -1;
		for (Region r : vorhanden.alleRegionen()) {
			if (r.getInselKennung() > alteInselKennung) {
				// new Debug("Jüngere Insel: " + r + " ( " + r.getInselKennung() + " in " + r.getEnstandenIn() + ")");
				alteInselKennung = r.getInselKennung();
			}
		}
		int inselKennung = alteInselKennung + 1;
		
		int welt = vorhanden.getWelt();
		
		if (welt < 0) {
			// o, dann suchen wir stattdessen die kleinste (negative) Inselkennung:
			for (Region r : vorhanden.alleRegionen()) {
				if (r.getInselKennung() < inselKennung) {
					// new Debug("Jüngere Insel: " + r + " ( " + r.getInselKennung() + " in " + r.getEnstandenIn() + ")");
					inselKennung = r.getInselKennung();
				}
			}
			inselKennung --; // das ist dann eine neue
		}


		// jetzt alle Regionen der jüngsten Insel sammeln - deren Mittelpunkt
		// ist der Anker für das Anlegen (im doppelten Sinn) neuer Inseln
		MapSelection juengsteAlte = new MapSelection();
		for (Region r : vorhanden.alleRegionen()) {
			if (r.getInselKennung() == alteInselKennung) juengsteAlte.add(r.getCoords());
		}
		Coords anlegePunkt = juengsteAlte.getMittelpunkt();

		// muss nachher wieder zurückverschoben werden, zumindest für die neue Insel!
		vorhanden.shift(0 - anlegePunkt.getX(), 0 - anlegePunkt.getY());

		if (ZATMode.CurrentMode().isDebug()) {
			vorhanden.saveCR("./temp/I" + alteInselKennung + "-vorhanden.cr");
			// throw new RuntimeException("...und mehr wollten wir gar nicht.");
		}


		ProtoInsel neu = null;
		int schicksal = Random.W(1000);

		if (welt == 1) { // Oberwelt
			if (schicksal < 600) {
				neu = new KleinMeer(welt, "Kleinmeer #" + inselKennung);
			} else if (schicksal < 630) {
				neu = new MittelMeer(welt, "Mittelmeer #" + inselKennung);
			} else if (schicksal < 800) {
				neu = new KleinInsel(welt, "Klein #" + inselKennung);
			} else if (schicksal < 830) {
				neu = new StreuInsel(welt, "Streu #" + inselKennung);
			} else if (schicksal < 960) {
				neu = new KegelInsel(welt, "Kegel #" + inselKennung);
			} else if (schicksal < 990) {
				neu = new Kontinent(welt, "Kontinent #" + inselKennung);
			} else {
				neu = new Meer(welt, "Meer #" + inselKennung);
			}
		} else if (welt == -1) { // Unterwelt
			if (schicksal < 400) {
				neu = new UnterweltKegelInsel(welt, "Unterwelt-Kegel #" + inselKennung);
			} else if (schicksal < 500) {
				neu = new KleinerLavastrom(welt, "Kleiner Lavastrom #" + inselKennung);
			} else if (schicksal < 850) {
				neu = new MittelMeer(welt, "Mittelmeer #" + inselKennung);
			} else {
				neu = new KleinerSandstrom(welt, "Kleiner Sandstrom #" + inselKennung);
			}
		}

		// eigene (typische) Zielgröße setzen:
		neu.setZielGroesse(neu.rndGroesse());


		neu.create();
		neu.setInselkennung(inselKennung);

		
		int abstand = neu.rndAbstand();

		String wasIsses = neu.getClass().getSimpleName();
		new SysMsg("Neu: " + wasIsses + " #" + inselKennung + " mit " + neu.alleRegionen().size() + " eigenen Regionen, " + abstand + " von der nächsten bekannten Region.");

		Coords m = neu.getMittelpunkt(true); // Ozean mit einbeziehen

		// den Mittelpunkt einzeichnen:
		Region r = neu.getRegion(m.getX(), m.getY());
		if (r == null) { r = new Ozean(); r.setCoords(m); }
		r.setName("ME-" + inselKennung);

		neu.shift(0 - m.getX(), 0 - m.getY());
		if (ZATMode.CurrentMode().isDebug())
			neu.saveCR("./temp/I" + inselKennung + "-einzelinsel.cr");


		ProtoInsel neueWelt = combine(vorhanden, neu, abstand);

		// neueWelt.nackteKuestenBewaessern();
		neueWelt.ozeanLueckenFuellen();

		partitionenEntwickeln(neueWelt);

		neueWelt.ozeanLueckenFuellen();
		
		neueWelt.luxusErgaenzen(); // ggf. Füll-Ozeane (Sand- und Lavaströme...) mit Luxus-Gütern versehen

		return neueWelt;
	}

    public void make() throws InstantiationException, IllegalAccessException {
        // ProtoInsel insel = new KegelInsel(1, "1st");
        // ProtoInsel insel = new Kontinent(1, "1st");
		ProtoInsel insel = new UnterweltKegelInsel(-1, "1st");

		insel.setZielGroesse(30);
		insel.create();
		insel.nackteKuestenBewaessern();

//        KegelInsel insel2 = insel.clone();
//        insel2.rotateCCW(60);

        // this.inselKonturieren(insel, 1);

        for (int i = 1; i < 10; i++) {
			System.out.println((i+1) + ". Insel");
			// if ((i % 20) == 0) insel.konturieren(1);
			insel = addNewInselTo(insel);

			// den Ursprung (0;0) der kombinierten Insel in die neue Mitte verschieben.
			insel.mittelpunkt = null;
			Coords m = insel.getMittelpunkt(false);
			insel.shift(0 - m.getX(), 0 - m.getY());
			Region mr = insel.getRegion(0, 0);
			if (mr != null) mr.setBeschreibung(mr.getBeschreibung()+" - Mittelpunkt der Welt nach Insel " + (i+1));

			insel.saveCR("./temp/gesamtwelt-" + (i+1) + ".cr");
		}

		Coords m = insel.getMittelpunkt(false);
		insel.shift(0 - m.getX(), 0 - m.getY());

        // insel.konturieren(1);
		insel.ozeanLueckenFuellen();

		int i = 1;
		for (StartPosition sp : insel.findeStartPositionen()) {
			for (Coords c : sp) {
				Region r = insel.getRegion(c.getX(), c.getY());
				String name = r.getName();
				r = Gletscher.class.newInstance();
				r.setCoords(c);
				r.setName("SP" + i + "-" + name);
				if (c.equals(sp.getZentrum())) r.setName("SP" + i + "-ZENTRUM-" + name);
				insel.putRegion(r);
			}
			i ++;
		}

        insel.saveCR("./temp/neueInseln-kombiniert.cr");
    }

	protected void partitionenEntwickeln(ProtoInsel insel) {
		List<WeltPartition> partitionen = WeltPartition.Partitioniere(insel);
		if (partitionen.size() > 1) {
			new SysMsg("Es gibt jetzt " + partitionen.size() + " getrennte Welten (Partitionen).");
			int cnt = 0;
			for (WeltPartition p : partitionen) {
				if (cnt == 0) { cnt++; continue; }
				for (Coords c : p.getCoords()) {
					Region temp = insel.getRegion(c.getX(), c.getY());
					temp.setBeschreibung(temp.getBeschreibung() + " - Partition " + (cnt + 1));
				}
				cnt ++;
			}

			// alle Partitionen wachsen auf Partition #1 zu:
			Coords zentrum = partitionen.get(0).getMittelpunkt();
			for (int j=1 ; j<partitionen.size(); j++) {
				WeltPartition satellit = partitionen.get(j);

				// wir suchen diejenige Grenzregion, die am nähesten zum
				// Welt-Mittelpunkt (der anderen Partition) liegt:
				Coords bridge = null;
				int minDistance = Integer.MAX_VALUE;
				for (Coords c : satellit.getGrenzen()) {
					int d = c.getDistance(zentrum);
					if (d < minDistance) {
						bridge = c;
						minDistance = d;
					}
				}

				if (bridge != null) {
					Coords extendCoords = bridge.shift(bridge.getRichtungNach(zentrum));

					Region extend;
					try {
						extend = Ozean.class.newInstance();
						extend.setCoords(extendCoords);
						insel.putRegion(extend);
					} catch (InstantiationException ex) {
						new BigError(ex);
					} catch (IllegalAccessException ex) {
						new BigError(ex);
					}

					new SysMsg("Verbindung von Partition " + (j+1) + " wächst von " + bridge.xy() + " nach " + extendCoords.xy() + ", Ziel: " + zentrum.xy() + ".");
				}
			}
		}
	}


    /**
     * Die Verweise auf beide Inseln werden nach Ausführung ungültig!
	 * @param stator Die "Hauptinsel" - bleibt unbewegt
	 * @param motor Die neue Insel - wird eingepasst, dabei verschoben und ggf. gedreht.
	 * @param minDistance
     * @return
     */
	@SuppressWarnings("unchecked")
    public ProtoInsel combine(ProtoInsel stator, ProtoInsel motor, int minDistance) {
		int inselkennung = motor.getInselkennung(motor.getWelt());

        List<InselKombinationsScore> scores = new ArrayList<InselKombinationsScore>();

        int xBand = Math.max(stator.getWidth(), motor.getWidth()) + minDistance + 1;
        int yBand = Math.max(stator.getHeight(), motor.getHeight()) + minDistance + 1;

		List<CoordRelation> possibleShifts = CoordRelation.AllRelations(1, 0-xBand, 0-yBand, xBand, yBand);

        // System.out.println("combine bands: +/- " + xBand + "; +/- "+yBand);
        System.out.println("Es gibt " + possibleShifts.size() + " mögliche Verschiebe-Positionen.");

//		int statorR = stator.getMinDefinierterRadius();
//		int motorR = motor.getMinDefinierterRadius();
//		int vorherFangenWirGarNichtAn = statorR + motorR + minDistance - 1;
//		System.out.println(
//				"Erste sinnvolle Verschiebungsdistanz: " + vorherFangenWirGarNichtAn
//				+" (rad[S]: " + statorR + ", rad[M]: " + motorR + ", dist:" + minDistance + ")"
//		);

		int cnt = 0;
		int ueberlappende = 0;
		int maxDistance = 0;
		int firstSuccess = Integer.MAX_VALUE - 1000;
		int radiusErweiterung = 6;

		for (CoordRelation rel : possibleShifts) {
//			if (rel.getDistance() < vorherFangenWirGarNichtAn) continue;
			
			int dx = rel.getC().getX();
			int dy = rel.getC().getY();
			if (rel.getDistance() > maxDistance) {
				if (scores.size() > 0) {
					System.out.println(cnt + " geprüfte Verschiebungen bei Distanz " + maxDistance + ", jetzt gibt es " + scores.size() + " gültige Lösungen.");
				}
				maxDistance = rel.getDistance();
			}
			
			motor.shift(dx, dy);

			if (!stator.hasOverlap(motor)) {
				StatSerie stats = motor.distanceTo(stator);
				// StatSerie stats = motor.distanceToZero(stator);
				int myMinDistance = stats.getMinValue();
				if (myMinDistance >= minDistance) {
					InselKombinationsScore score = new InselKombinationsScore(dx, dy, myMinDistance, stats.getAverage());
					score.setShiftDistance(rel.getDistance());
					scores.add(score);
					if (firstSuccess > rel.getDistance()) firstSuccess = rel.getDistance();
				}
			} else {
				ueberlappende ++;
				if (ueberlappende % 1000 == 0) {
					System.out.println(ueberlappende + " überlappende Lösungen...");
				}
			}

			// zurück verschieben
			motor.shift(-dx, -dy);
			cnt ++;

			if (scores.size() > 2000) break;

			radiusErweiterung = 6 + (int)Math.floor((double)maxDistance * 0.07d);
			// System.out.println("Radiuserweiterung: " + radiusErweiterung);

			if (maxDistance > firstSuccess + radiusErweiterung) break;
        }

        Collections.sort(scores);
//        cnt = 0;
//        for (InselKombinationsScore iks : scores) {
//            if ((cnt < 10) || (cnt >= scores.size() -10)) {
//                // System.out.println(cnt + ". " + iks.getDx()+";"+iks.getDy()+" - "+iks.getDistance()+" - "+iks.getAvgDistance());
//            }
//			cnt++;
//        }
//
//        // ...and the winner is:
//        InselKombinationsScore winner = null;
//        try {
//            winner = scores.get(0);
//        } catch (IndexOutOfBoundsException ex) {
//            System.err.println("Fehler: Keine mögliche Kombination gefunden...");
//            motor.saveCR("./motor.cr");
//            stator.saveCR("./stator.cr");
//        }
//		System.out.println(
//				"Gewählter Anschluss: Verschiebedistanz " + winner.getShiftDistance()
//				+ ", Distanz (min/avg): " + winner.getDistance()
//				+ " / " + winner.getAvgDistance()
//		);


		// die besten jetzt nochmal durchprobieren, welche insgesamt am nahesten
		// an der alten Welt sind.
		List<InselKombinationsScore> bestOfTheBest = new ArrayList<InselKombinationsScore>();
		int nBeste = scores.size();	if (nBeste > 100) nBeste = 100;
		for (int i = 0; i < nBeste; i++) {
			InselKombinationsScore iks = scores.get(i);

			int dx = iks.getDx(); int dy = iks.getDy();
			motor.shift(dx, dy);

			StatSerie distance = motor.distanceTo(stator);
			bestOfTheBest.add(new InselKombinationsScore(dx, dy, distance.getMinValue(), distance.getAverage()));

			// zurück verschieben
			motor.shift(-dx, -dy);
        }
		Collections.sort(bestOfTheBest);
        InselKombinationsScore winner = bestOfTheBest.get(0);






        motor.shift(winner.getDx(), winner.getDy());
		motor.debugDistanceTo(stator);

        Set<Region> neueRegionen = new HashSet<Region>();
		for (Region r : motor.alleRegionen()) {
            neueRegionen.add(r);
			stator.putRegion(r);
        }

        ProtoInsel retval = stator.clone();
        stator.clear();
        motor.clear();

		// Debug-Info: Die besten und schlechtesten (und einfach alle)
		// Verschiebungs-Scores als Regionen einzeichnen
		try {
			ProtoInsel debug = retval.clone();

			for (Region r : neueRegionen) debug.removeRegion(r);

			cnt = 0;
			for (InselKombinationsScore iks : scores) {
				// die Debug-Regionen auf jeden Fall eintragen:
				String beschreibung = "iks #" + (cnt + 1);
				Coords thisShift = new Coords(iks.getDx(), iks.getDy(), stator.getWelt());
				Region r = null;
				if (cnt < 3) {
					r = Feuerwand.class.newInstance();
				} else if (cnt < 20) {
					r = Ebene.class.newInstance();
				} else if (cnt >= scores.size() - 50) {
					r = Hochland.class.newInstance();
				} else {
					r = Gletscher.class.newInstance();
				}
				r.setCoords(thisShift);
				r.setBeschreibung(beschreibung);
				debug.putRegion(r);

				cnt++;
			}
			if (ZATMode.CurrentMode().isDebug())
				debug.saveCR("./temp/I" + inselkennung + "-kombiscores.cr");
			
		} catch (Exception ex) {
			new BigError(ex);
		}


		if (ZATMode.CurrentMode().isDebug())
			retval.saveCR("./temp/I" + inselkennung + "-welt.cr");
		return retval;
    }

	@SuppressWarnings("rawtypes")
	private class InselKombinationsScore implements Comparable {
        final int dx;
        final int dy;
        final int distance;
        final double avgDistance;

		/**
		 * optionale Angabe, bei welcher Verschiebe-Entfernung dieser Score entstanden ist.
		 */
		int shiftDistance;

        public InselKombinationsScore(int dx, int dy, int distance, double avgDistance) {
            this.dx = dx;
            this.dy = dy;
            this.distance = distance;
            this.avgDistance = avgDistance;
        }

        public double getAvgDistance() {
            return avgDistance;
        }

        public int getDistance() {
            return distance;
        }

        public int getDx() {
            return dx;
        }

        public int getDy() {
            return dy;
        }

		@SuppressWarnings("unused")
		public int getShiftDistance() {
			return shiftDistance;
		}

		public void setShiftDistance(int shiftDistance) {
			this.shiftDistance = shiftDistance;
		}


        @Override
        public int compareTo(Object o) {
            if (!(o instanceof InselKombinationsScore)) throw new RuntimeException("InselKombinationsScore: unvergleichlich.");

            final InselKombinationsScore iks2 = (InselKombinationsScore) o;

            if (this.getAvgDistance() < iks2.getAvgDistance()) return -1;
            if (this.getAvgDistance() > iks2.getAvgDistance()) return 1;

            if (this.getDistance() < iks2.getDistance()) return -1;
            if (this.getDistance() > iks2.getDistance()) return 1;

            return 0;
        }

    }
}
