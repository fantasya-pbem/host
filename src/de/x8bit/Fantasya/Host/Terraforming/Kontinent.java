package de.x8bit.Fantasya.Host.Terraforming;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.x8bit.Fantasya.Atlantis.Atlantis;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Richtung;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.Debug;
import de.x8bit.Fantasya.Atlantis.Regions.Berge;
import de.x8bit.Fantasya.Atlantis.Regions.Ebene;
import de.x8bit.Fantasya.Atlantis.Regions.Gletscher;
import de.x8bit.Fantasya.Atlantis.Regions.Hochland;
import de.x8bit.Fantasya.Atlantis.Regions.Ozean;
import de.x8bit.Fantasya.Atlantis.Regions.Sumpf;
import de.x8bit.Fantasya.Atlantis.Regions.Wald;
import de.x8bit.Fantasya.Atlantis.Regions.Wueste;
import de.x8bit.Fantasya.Host.EVA.util.ZATMode;
import de.x8bit.Fantasya.util.MapSelection;
import de.x8bit.Fantasya.util.Random;

/**
 *
 * @author hb
 */
public class Kontinent extends ProtoInsel {
	private static final long serialVersionUID = 1L;

	private int kategorie;

    public Kontinent(int welt, String inselName) {
		super();
		
		this.name = inselName;
		this.zielGroesse = rndGroesse();
		this.setWelt(welt);

		this.kategorie = W(4) - 1;
    }

	public int getKategorie() {
		return kategorie;
	}

	public void create() {
		urgebirge();
		urgebirgeWaechst();
		bergeUndHochlaender();
		landGewinnung(W(20) + W(20) + getKategorie() * 10, true); // bevorzugt weit vom Zentrum
		landGewinnung(W(20) + getKategorie() * W(20), false); // bevorzugt nahe am Zentrum
		lueckenFuellen();
		fluesse();
		wuesten();
		erosion();
		landschaft();


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

	/**
	 * Erstellt das "Urgebirge", eine Art Rückgrat für den Kontinent (aus Gletschern - Terrain kann sich später noch ändern)
	 */
	private void urgebirge() {
		try {
			int d12 = W(3) + 5 + getKategorie(); // 6 .. 8 + KAT
			int d23 = W(3) + 3 + getKategorie(); // 4 .. 6 + KAT
			int d13 = d12 + d23 - 3;

			Coords c1 = new Coords(0, 0, 1);
			Region r = Gletscher.class.newInstance(); r.setCoords(c1); this.putRegion(r);

			MapSelection ms = new MapSelection();
			ms.add(c1);
			ms.wachsen(d12);
			Coords c2 = ms.getInnenKontur().zufaelligeKoordinate();
			r = Gletscher.class.newInstance(); r.setCoords(c2); this.putRegion(r);

			ms = new MapSelection();
			ms.add(c2);
			ms.wachsen(d23);
			MapSelection moeglichFuer3 = ms.getInnenKontur();
			MapSelection okayFuer3 = new MapSelection();
			for (Coords c : moeglichFuer3) {
				int d = c1.getDistance(c);
				if (Math.abs(d - d13) <= 1) okayFuer3.add(c);
			}
			if (okayFuer3.isEmpty()) new BigError("Kein Kandidat für die 3. Urgebirgs-Region des Kontinents gefunden.");
			Coords c3 = okayFuer3.zufaelligeKoordinate();
			r = Gletscher.class.newInstance(); r.setCoords(c3); this.putRegion(r);

			new Debug("Urgebirge: " + c1.xy() + ", " + c2.xy() + ", " + c3.xy());

			// Bergkette bilden
			Coords cursor = c1;
			do {
				Richtung ri = cursor.getRichtungNach(c2);
				cursor = cursor.shift(ri);
				r = Gletscher.class.newInstance(); r.setCoords(cursor); this.putRegion(r);
			} while (!cursor.equals(c2));
			do {
				Richtung ri = cursor.getRichtungNach(c3);
				cursor = cursor.shift(ri);
				r = Gletscher.class.newInstance(); r.setCoords(cursor); this.putRegion(r);
			} while (!cursor.equals(c3));

			if (ZATMode.CurrentMode().isDebug())
				this.saveCR("./temp/urgebirge.cr");

		} catch (InstantiationException ex) {
			new BigError(ex);
		} catch (IllegalAccessException ex) {
			new BigError(ex);
		}
	}


	private void urgebirgeWaechst() {
		try {
			for (int i=0; i < 4 + getKategorie(); i++) {
				List<Coords> kandidaten = new ArrayList<Coords>();
				kandidaten.addAll(this.getAussenKontur());
				Collections.sort(kandidaten, new EntfernungZuAllenComparator(this));

				// es wachsen:
				// die X nächsten,
				int x = W(4);
				for (int j=0; j<x; j++) {
					Coords c = kandidaten.get(j);
					Region r = Wueste.class.newInstance(); r.setCoords(c); this.putRegion(r);
				}
				//  die X "mittelsten",
				x = W(3) + 1;
				for (int j=0; j<x; j++) {
					int idx = kandidaten.size()/2 + j;
					Coords c = kandidaten.get(idx);
					Region r = Wueste.class.newInstance(); r.setCoords(c); this.putRegion(r);
				}
				//  und die 1 entferntesten Regionen.
				x = 1;
				for (int j=0; j<x; j++) {
					int idx = kandidaten.size() - (j+1);
					Coords c = kandidaten.get(idx);
					Region r = Wueste.class.newInstance(); r.setCoords(c); this.putRegion(r);
				}

				// und noch X völlig zufällige:
				x = W(3) - 1;
				for (int j=0; j<x; j++) {
					Coords c = this.getAussenKontur().zufaelligeKoordinate();
					Region r = Wueste.class.newInstance(); r.setCoords(c); this.putRegion(r);
				}


				if (ZATMode.CurrentMode().isDebug())
					this.saveCR("./temp/urgebirge-wachstum-" + i + ".cr");
			}
		} catch (InstantiationException ex) {
			new BigError(ex);
		} catch (IllegalAccessException ex) {
			new BigError(ex);
		}
	}

	private void bergeUndHochlaender() {
		try {
			// alle Wüsten zu Hochländern
			for (Region r : this.alleRegionen()) {
				if (r instanceof Wueste) {
					Coords c = r.getCoords();
					this.remove(c.getX(), c.getY());
					
					Region newR = Hochland.class.newInstance();
					newR.setCoords(c);
					this.putRegion(newR);
				}
			}

			// alle Hochländer, die nicht am Außenrand liegen, zu Bergen
			MapSelection alle = new MapSelection();
			alle.addAll(this.alleKoordinaten());
			MapSelection grenzen = alle.getInnenKontur();
			for (Coords c : alle) {
				if (grenzen.contains(c)) continue;

				Region r = this.getRegion(c.getX(), c.getY());
				if (r instanceof Hochland) {
					this.remove(c.getX(), c.getY());

					Region newR = Berge.class.newInstance();
					newR.setCoords(c);
					this.putRegion(newR);
				}
			}

			// Hochländer, die an wenigstens 2 andere Regionen grenzen, haben eine Chance zum Berg zu werden
			List<Region> kandidaten = new ArrayList<Region>();
			for (Region r : this.alleRegionen()) {
				if (r instanceof Hochland) {
					if (this.getNeighbors(r).size() > 1) {
						kandidaten.add(r);
					}
				}
			}
			Collections.shuffle(kandidaten);
			float anteil = W(50) + 25f;
			int letzterBerg = (int)Math.floor((float)kandidaten.size() * (anteil/100));
			new Debug("Berg-Chance für Hochländer: " + anteil + "%, " + letzterBerg + " von " + kandidaten.size() + " werden zu Bergen.");
			for (int i=0; i < letzterBerg; i++) {
				Region r = kandidaten.get(i);
				Coords c = r.getCoords();
				this.remove(c.getX(), c.getY());

				Region newR = Berge.class.newInstance();
				newR.setCoords(c);
				this.putRegion(newR);
			}

			if (ZATMode.CurrentMode().isDebug())
				this.saveCR("./temp/berge-und-hochlaender.cr");
		} catch (InstantiationException ex) {
			new BigError(ex);
		} catch (IllegalAccessException ex) {
			new BigError(ex);
		}
	}

	private void landGewinnung(int n, boolean exzentrisch) {
		try {
			for (int i=0; i < n; i++) {
				List<Coords> kandidaten = this.getAussenKontur().asList();
				Collections.sort(kandidaten, new EntfernungZuAllenComparator(this));
				Region r = null;
				if (exzentrisch) {
					Collections.reverse(kandidaten);
					r = Sumpf.class.newInstance();
				} else {
					r = Ebene.class.newInstance();;
				}
				double x = rnd.nextDouble();
				double pointer = x * x * (double)kandidaten.size();
				Coords c = kandidaten.get((int)Math.floor(pointer));
				r.setCoords(c);
				this.putRegion(r);
			}

			if (ZATMode.CurrentMode().isDebug())
				this.saveCR("./temp/landgewinnung.cr");
		} catch (InstantiationException ex) {
			new BigError(ex);
		} catch (IllegalAccessException ex) {
			new BigError(ex);
		}
	}

	private void lueckenFuellen() {
		ozeanLueckenFuellen();

		try {
			for (Region r : this.alleRegionen()) {
				if (r instanceof Ozean) {
					Coords c = r.getCoords();
					this.remove(c.getX(), c.getY());

					Region newR = Ebene.class.newInstance();
					newR.setCoords(c);
					this.putRegion(newR);
				}
			}
		} catch (InstantiationException ex) {
			new BigError(ex);
		} catch (IllegalAccessException ex) {
			new BigError(ex);
		}

		if (ZATMode.CurrentMode().isDebug())
			this.saveCR("./temp/gefuellt.cr");
	}

	private void fluesse() {
		Map<Coords, Integer> kEntfernungen = new HashMap<Coords, Integer>();

		int maxE = Integer.MIN_VALUE;
		for (Region r : alleRegionen()) {
			boolean found = false;
			for (int d = 0; !found; d++) {
				MapSelection nachbarn = new MapSelection();
				nachbarn.add(r.getCoords());
				nachbarn.wachsen(d + 1);
				nachbarn = nachbarn.getInnenKontur();

				for (Coords c : nachbarn) {
					if (this.getRegion(c.getX(), c.getY()) == null) {
						found = true;
						r.setName("D" + d);
						kEntfernungen.put(r.getCoords(), d);
						if (d > maxE) maxE = d;
						break;
					}
				}
			}
		}

		if (ZATMode.CurrentMode().isDebug())
			this.saveCR("./temp/kuestenEntfernungen.cr");

		try {
			// wenn die maximale Küsterferne mindestens 3 ist, dann gibt es einen Fluss:
			if (maxE >= 3) {
				MapSelection fluss = new MapSelection();

				List<Coords> startKandidaten = new ArrayList<Coords>();
				for (Coords c : kEntfernungen.keySet()) {
					if (kEntfernungen.get(c) == (maxE - 1)) startKandidaten.add(c);
				}
				Collections.shuffle(startKandidaten);
				Coords start = startKandidaten.get(0);
				fluss.add(start);

				// alle Nachbarn der Quelle werden mindestens zu Hochländern erhöht:
				for (Region n : this.getNeighbors(this.getRegion(start.getX(), start.getY()))) {
					if (n instanceof Gletscher) continue;
					if (n instanceof Berge) continue;
					if (n instanceof Hochland) continue;

					Coords c = n.getCoords();
					this.remove(c.getX(), c.getY());
					Region newR = Hochland.class.newInstance();
					newR.setCoords(c);
					this.putRegion(newR);
				}

				Coords cursor = start;
				boolean gemuendet = false;
				int currentD = kEntfernungen.get(start);
				while (!gemuendet) {
					Region von = this.getRegion(cursor.getX(), cursor.getY());
					List<Region> kandidaten = this.getNeighbors(von);

					// Kandidaten, die bereits an zwei Flussabschnitte grenzen, nehmen wir nicht.
					List<Region> nichtGut = new ArrayList<Region>();
					for (Region r : kandidaten) {
						int nachbarn = 0;
						for (Coords c : r.getCoords().getNachbarn()) {
							if (fluss.contains(c)) nachbarn ++;
						}
						if (nachbarn > 1) nichtGut.add(r);
					}
					kandidaten.removeAll(nichtGut);

					// welche der Kandidaten bringen uns wie weit weg von der Küste?
					int maxD = Integer.MIN_VALUE;
					for (Region r : kandidaten) {
						int d = kEntfernungen.get(r.getCoords());
						if (d > maxD) maxD = d;
					}

					List<Coords> ersteWahl = new ArrayList<Coords>();
					if (maxD > currentD) {
						// es gibt Richtungen, die weiter ins Landesinnere gehen!
						for (Region r : kandidaten) {
							int d = kEntfernungen.get(r.getCoords());
							if (d == maxD) ersteWahl.add(r.getCoords());
						}
					}
					// alle anderen:
					List<Coords> zweiteWahl = new ArrayList<Coords>();
					for (Region r : kandidaten) {
						if (!ersteWahl.contains(r.getCoords())) zweiteWahl.add(r.getCoords());
					}

					if (!ersteWahl.isEmpty()) {
						Collections.shuffle(ersteWahl);
						Coords nach = ersteWahl.get(0);
						fluss.add(nach);
						cursor = nach;
					} else {
						Collections.shuffle(zweiteWahl);
						Coords nach = zweiteWahl.get(0);
						fluss.add(nach);
						cursor = nach;
					}

					// haben wir den Ozean erreicht?
					Region cursorRegion = this.getRegion(cursor.getX(), cursor.getY());
					if (this.getNeighbors(cursorRegion).size() < 6) gemuendet = true;
				}


				// fließen:
				for (Coords c : fluss) {
					this.remove(c.getX(), c.getY());

					Region newR = Ozean.class.newInstance();
					newR.setCoords(c);
					this.putRegion(newR);
				}

				if (ZATMode.CurrentMode().isDebug())
					this.saveCR("./temp-fluesse.cr");
				
			}
		} catch (InstantiationException ex) {
			new BigError(ex);
		} catch (IllegalAccessException ex) {
			new BigError(ex);
		}

	}

	private void wuesten() {
		Map<Coords, Integer> kEntfernungen = new HashMap<Coords, Integer>();

		int maxE = Integer.MIN_VALUE;
		for (Region r : alleRegionen()) {
			boolean found = false;
			for (int d = 0; !found; d++) {
				MapSelection nachbarn = new MapSelection();
				nachbarn.add(r.getCoords());
				nachbarn.wachsen(d + 1);
				nachbarn = nachbarn.getInnenKontur();

				for (Coords c : nachbarn) {
					Region maybe = this.getRegion(c.getX(), c.getY());
					if ((maybe == null) || (maybe instanceof Ozean)) {
						found = true;
						r.setName("D" + d);
						kEntfernungen.put(r.getCoords(), d);
						if (d > maxE) maxE = d;
						break;
					}
				}
			}
		}

		if (ZATMode.CurrentMode().isDebug())
			this.saveCR("./temp-kuestenEntfernungen-Wueste.cr");

		try {
			// pro (Küstenentfernung + 1) eine 5%-Chance
			for (Region r : this.alleRegionen()) {
				if (r instanceof Ozean) continue; // Flüsse verwüsten nicht

				int d = kEntfernungen.get(r.getCoords()) + 1;
				boolean verwuesten = false;
				for (int i = 0; i < d; i++) {
					if(W(20) == 1) {
						verwuesten = true;
						break;
					}
				}
				if (verwuesten) {
					Coords c = r.getCoords();
					this.remove(c.getX(), c.getY());

					Region newR = Wueste.class.newInstance();
					newR.setCoords(c);
					this.putRegion(newR);
				}
			}
		} catch (InstantiationException ex) {
			new BigError(ex);
		} catch (IllegalAccessException ex) {
			new BigError(ex);
		}

		if (ZATMode.CurrentMode().isDebug())
			this.saveCR("./temp-wuesten.cr");
	}

	private void erosion() {
		try {
			// Gletscher mit weniger als 2 Berg-Nachbarn haben guten Chancen auf ein normales Berg-Dasein:
			for (Region r : alleRegionen()) {
				if (!(r instanceof Gletscher)) continue;
				int nachbarBerge = 0;
				for (Coords n : r.getCoords().getNachbarn()) {
					if (getRegion(n.getX(), n.getY()) == null) continue;
					if (getRegion(n.getX(), n.getY()) instanceof Gletscher) nachbarBerge ++;
					if (getRegion(n.getX(), n.getY()) instanceof Berge) nachbarBerge ++;
				}
				boolean schmelze = false;
				if ((nachbarBerge < 5) && (W(20) <= 2)) schmelze = true;
				if ((nachbarBerge < 4) && (W(20) <= 12)) schmelze = true;
				if ((nachbarBerge < 3) && (W(20) <= 15)) schmelze = true;
				if ((nachbarBerge < 2) && (W(20) <= 19)) schmelze = true;

				if (schmelze) {
					Region newR = Berge.class.newInstance();
					newR.setCoords(r.getCoords());
					this.putRegion(newR);
				}
			}

			// Berge mit vielen Nachbar-Bergen / -Gletschern / -Hochländern haben
			// eine gute Chance zu vereisen:
			for (Region r : alleRegionen()) {
				if (!(r instanceof Berge)) continue;
				int nachbarBerge = 0;
				for (Coords n : r.getCoords().getNachbarn()) {
					if (getRegion(n.getX(), n.getY()) == null) continue;
					if (getRegion(n.getX(), n.getY()) instanceof Gletscher) nachbarBerge ++;
					if (getRegion(n.getX(), n.getY()) instanceof Berge) nachbarBerge ++;
					if (getRegion(n.getX(), n.getY()) instanceof Hochland) nachbarBerge ++;
				}
				boolean vereisung = false;
				if ((nachbarBerge == 6) && (W(20) <= 19)) vereisung = true;
				if ((nachbarBerge > 4) && (W(20) <= 12)) vereisung = true;
				if ((nachbarBerge > 3) && (W(20) <= 6)) vereisung = true;
				if ((nachbarBerge > 2) && (W(20) <= 4)) vereisung = true;

				if (vereisung) {
					Region newR = Gletscher.class.newInstance();
					newR.setCoords(r.getCoords());
					this.putRegion(newR);
				}
			}
			

		} catch (InstantiationException ex) {
			new BigError(ex);
		} catch (IllegalAccessException ex) {
			new BigError(ex);
		}

		if (ZATMode.CurrentMode().isDebug())
			this.saveCR("./temp-erosion.cr");
	}


	private void landschaft() {
		try {
			// Sümpfe ohne Nachbarschaft zu anderen Regionstypen werden zu 40% zu Ebenen:
			for (Region r : alleRegionen()) {
				if (!(r instanceof Sumpf)) continue;
				boolean ueppig = false;
				for (Coords n : r.getCoords().getNachbarn()) {
					if (getRegion(n.getX(), n.getY()) == null) continue;
					if (!(getRegion(n.getX(), n.getY()) instanceof Sumpf)) { ueppig = true; break; }
				}
				if (!ueppig) {
					if ((W(10) <= 4)) {
						Region newR = Ebene.class.newInstance();
						newR.setCoords(r.getCoords());
						this.putRegion(newR);
					}
				}
			}


			// Hochländer haben eine spontane 40%-Chance zu Ebenen zu werden:
			for (Region r : alleRegionen()) {
				if (!(r instanceof Hochland)) continue;
				if (W(10) <= 4) {
					Region newR = Ebene.class.newInstance();
					newR.setCoords(r.getCoords());
					this.putRegion(newR);
				}
			}

			// Ebenen ohne Wasseranschluss können zu 60% Hochländer werden:
			for (Region r : alleRegionen()) {
				if (!(r instanceof Ebene)) continue;
				boolean amWasser = false;
				for (Coords n : r.getCoords().getNachbarn()) {
					if (getRegion(n.getX(), n.getY()) == null) { amWasser = true; break; }
					if (getRegion(n.getX(), n.getY()) instanceof Ozean) { amWasser = true; break; }
				}
				if (!amWasser) {
					if ((W(10) <= 6)) {
						Region newR = Hochland.class.newInstance();
						newR.setCoords(r.getCoords());
						this.putRegion(newR);
					}
				}
			}

			// Ebenen zu 60% zu Wäldern:
			for (Region r : alleRegionen()) {
				if (!(r instanceof Ebene)) continue;
				if (W(10) <= 6) {
					Region newR = Wald.class.newInstance();
					newR.setCoords(r.getCoords());
					this.putRegion(newR);
				}
			}



		} catch (InstantiationException ex) {
			new BigError(ex);
		} catch (IllegalAccessException ex) {
			new BigError(ex);
		}

		if (ZATMode.CurrentMode().isDebug())
			this.saveCR("./temp-landschaft.cr");
	}


	protected int rndGroesse() {
		return W(20) + W(20) + W(20) + W(100) + 20;
		// ~ 10,5 + 10,5 + 10,5 + 50,5 + 20 = 104 (24 - 180)
	}

	protected int rndAbstand() {
		int d = Random.rnd(1, 7) - 2; // W6 - 2, aber mindestens 1
		return (d > 1?d:1);
	}

	@Override
	public Map<Class<? extends Atlantis>, Double> getTerrainProbabilities() {
		throw new UnsupportedOperationException("Not supported.");
	}

}

