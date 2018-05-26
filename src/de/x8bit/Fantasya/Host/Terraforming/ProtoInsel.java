package de.x8bit.Fantasya.Host.Terraforming;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import de.x8bit.Fantasya.Atlantis.Atlantis;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Helper.Nachfrage;
import de.x8bit.Fantasya.Atlantis.Helper.StartPosition;
import de.x8bit.Fantasya.Atlantis.Items.LuxusGood;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.Debug;
import de.x8bit.Fantasya.Atlantis.Regions.Lavastrom;
import de.x8bit.Fantasya.Atlantis.Regions.Ozean;
import de.x8bit.Fantasya.Atlantis.Regions.Sandstrom;
import de.x8bit.Fantasya.Host.Paket;
import de.x8bit.Fantasya.Host.EVA.CleanUp;
import de.x8bit.Fantasya.util.MapSelection;
import de.x8bit.Fantasya.util.Random;
import de.x8bit.Fantasya.util.StatSerie;

/**
 *
 * @author hapebe
 */
@SuppressWarnings("serial")
public abstract class ProtoInsel extends TreeMap<Integer, Map<Integer, Region>> {
	Coords mittelpunkt = null;
	String name = null;
	int welt = 0;
	int zielGroesse = 0;

	Item luxury1;
	Item luxury2;
	
	Class<? extends Region> fuellTyp = Ozean.class;

	// hack; the random number generator system is broken anyway, there are
	// hundreds of places that use it; it must be centralized a lot more.
	SecureRandom rnd = new SecureRandom();
	

	public ProtoInsel() {
		// Handelsgüter festlegen
		List<Item> ali = new ArrayList<Item>(); // enthält alle Handelsgüter
		for(Paket p : Paket.getPaket("Items")) if (p.Klasse instanceof LuxusGood) ali.add((Item)p.Klasse);
		int i1 = Random.rnd(0, ali.size());
		int i2 = Random.rnd(0, ali.size());
		while(i1 == i2) i2 = Random.rnd(0, ali.size());

		luxury1 = ali.get(i1);
		luxury2 = ali.get(i2);
	}

	public abstract void create();

	public abstract Map<Class<? extends Atlantis>, Double> getTerrainProbabilities();

	protected abstract int rndGroesse();

	protected abstract int rndAbstand();

	public void setInselkennung(int ik) {
		for (Region r : alleRegionen()) {
			r.setInselKennung(ik);
		}
	}

	/**
	 * @return die höchste enthaltene Inselkennung für die Oberwelt (1), die niedrigste enthaltene Inselkennung für die Unterwelt (-1)
	 */
	public int getInselkennung(int welt) {
		if (welt >= 0) {
			int retval = -1;
			for (Region r : alleRegionen()) {
				if (r.getInselKennung() > retval) retval = r.getInselKennung();
			}
			return retval;
		} else {
			int retval = Integer.MAX_VALUE;
			for (Region r : alleRegionen()) {
				if (r.getInselKennung() < retval) retval = r.getInselKennung();
			}
			return retval;
		}
	}

	public List<Coords> alleKoordinaten() {
		List<Coords> retval = new ArrayList<Coords>();
		for (Region r : this.alleRegionen()) {
			retval.add(r.getCoords());
		}
		return retval;
	}

	public List<Region> alleRegionen() {
		final List<Region> retval = new ArrayList<Region>();
		for (int x : this.keySet()) {
			for (int y : this.get(x).keySet()) {
				retval.add(this.get(x).get(y));
			}
		}
		return retval;
	}

	protected void binnenSeenFuellen() {
		Set<Coords> unclear = new HashSet<Coords>();
		// der äußere Rahmen dieser Insel - was außerhalb liegt,
		// ist keinesfalls füll-pflichtig.
		int minX = this.getMin().getX();
		int maxX = this.getMax().getX();
		int minY = this.getMin().getY();
		int maxY = this.getMax().getY();
		// alle Füllkandidaten in ein Set:
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				unclear.add(new Coords(x, y, this.getWelt()));
			}
		}
		// die Landregionen gleich raus:
		for (Region r : this.alleRegionen()) {
			if (r.istBetretbar(null)) unclear.remove(r.getCoords());
		}
		// false: nur Seen fuellen, die vollständig von Land umgeben sind.
		this.fuellen(unclear, false);
	}

	protected void luxusProdukteSetzen() {
		for (Region r : this.alleRegionen()) {
			Item produce = null;
			if (Random.rnd(0, 101) < 60) {
				produce = luxury1;
			} else {
				produce = luxury2;
			}
			if (produce == null) throw new IllegalStateException("Kein Luxusgut zur Produktion vorgesehen?");

			boolean found = false;
            if (r.getLuxus().isEmpty()) r.Init_Handel();
			for (Nachfrage n : r.getLuxus()) {
				if (n.getItem().equals(produce.getClass())) {
					// n.setNachfrage(-1 * n.getNachfrage());
					n.setNachfrage(-1.0f);
					found = true;
					break;
				}
			}
			if (!found) throw new IllegalStateException("Konnte das Luxusgut " + produce.getClass().getSimpleName() + " in " + r + r.getCoords() +  " nicht als Produkt setzen.");
		}
	}
	
	protected void luxusErgaenzen() {
		for (Region r : this.alleRegionen()) {
			if (r.getProduce() == null) {
				Class <? extends Item> produce = CleanUp.luxusProduktRaten(r);
				
				boolean found = false;
                if (r.getLuxus().isEmpty()) r.Init_Handel();
				for (Nachfrage n : r.getLuxus()) {
					if (n.getItem().equals(produce)) {
						// n.setNachfrage(-1 * n.getNachfrage());
						n.setNachfrage(-1.0f);
						found = true;
						break;
					}
				}
				if (!found) throw new IllegalStateException("Konnte das Luxusgut " + produce.getClass().getSimpleName() + " in " + r + r.getCoords() +  " nicht als Produkt setzen.");
			}
		}
		
	}

	@Override
	public ProtoInsel clone() {
		ProtoInsel retval = new Meer(this.welt, this.getName());
		for (Region r : alleRegionen()) {
			retval.putRegion(r.cloneAs(r.getClass()));
		}
		return retval;
	}

	public void debugDistanceTo(ProtoInsel other) {
		for (Region r : this.alleRegionen()) {
			int sum = 0;
			int cnt = 0;
			for (Region otherRegion : other.alleRegionen()) {
				if (otherRegion instanceof Ozean) continue;
				int distance = r.getCoords().getDistance(otherRegion.getCoords());
				sum += distance;
				cnt++;
			}
			if (cnt > 0) {
				double avg = (double)sum / (double)cnt;
				String beschr = r.getBeschreibung();
				if (beschr.length() > 0) beschr += "; ";
				r.setBeschreibung(beschr + "Durchschnitts-Entfernung von der Au\u00dfenwelt: " + avg);
			}
		}
	}

	/**
	 * Ziemlich rechenaufwändig...
	 * @param other
	 * @return Die Liste aller Entfernungen zwischen jedem Paar von Regionen dieser Insel und von other.
	 */
	public StatSerie distanceTo(ProtoInsel other) {
		StatSerie retval = new StatSerie();
		for (Region r : this.alleRegionen()) {
			for (Region otherRegion : other.alleRegionen()) {
				int d = r.getCoords().getDistance(otherRegion.getCoords());
				retval.add(d);
			}
		}
		return retval;
	}

	/**
	 * @param other
	 * @return Die Liste aller Entfernungen zwischen jedem Paar von Regionen dieser Insel und dem Mittelpunkt von other.
	 */
	public StatSerie distanceToCenterOf(ProtoInsel other) {
		Coords oCenter = other.getMittelpunkt(true);

		StatSerie retval = new StatSerie();
		for (Region r : this.alleRegionen()) {
			int d = r.getCoords().getDistance(oCenter);
			retval.add(d);
		}
		return retval;
	}

	/**
	 * @param other
	 * @return Die Liste aller Entfernungen zwischen jedem Paar von Regionen dieser Insel und dem Punkt (0,0,welt) von other.
	 */
	public StatSerie distanceToZero(ProtoInsel other) {
		Coords zero = new Coords(0, 0, other.getWelt());
		return distanceTo(zero);
	}

	/**
	 * @param other
	 * @return Die Liste aller Entfernungen zwischen allen Regionen dieser Insel und der Koordinate other.
	 */
	public StatSerie distanceTo(Coords other) {
		StatSerie retval = new StatSerie();
		for (Region r : this.alleRegionen()) {
			retval.add(r.getCoords().getDistance(other));
		}
		return retval;
	}

	/**
	 * Füllt undefinierte Lücken dieser Insel, sofern sie in unclear enthalten 
	 * sind. Lücken sind solche Gebiete, die komplett von definierten Regionen 
	 * begrenzt werden.
	 * @param unclear Set von Koordinaten, bei denen nach Lücken gesucht werden soll
	 * @param ozeanLuecken wenn false, werden Ozeane wie undefinierte Regionen behandelt
	 */
	protected void fuellen(Set<Coords> unclear, boolean ozeanLuecken) {

		// der äußere Rahmen dieser Insel - was außerhalb liegt,
		// ist keinesfalls füll-pflichtig.
		int minX = this.getMin().getX();
		int maxX = this.getMax().getX();
		int minY = this.getMin().getY();
		int maxY = this.getMax().getY();
		while (unclear.size() > 0) {
			Set<Coords> dieserSee = new HashSet<Coords>();
			dieserSee.add(unclear.iterator().next());
			boolean istEinBinnenSee = true;
			for (boolean grown = true; grown;) {
				grown = false;
				Set<Coords> neue = new HashSet<Coords>();
				for (Coords drinnen : dieserSee) {
					for (Coords n : drinnen.getNachbarn()) {
						Region r = this.getRegion(n.getX(), n.getY());
						if ((r != null) && r.istBetretbar(null)) continue;
						if (ozeanLuecken && (r != null)) continue;
						if (!dieserSee.contains(n)) {
							neue.add(n);
							grown = true;
						}
					}
				}
				// enthält "neue" Koordinaten, die nicht innerhalb des Rahmens liegen?
				for (Coords c : neue) {
					boolean foul = false;
					if ((c.getX() < minX) || (c.getX() > maxX)) foul = true;
					if ((c.getY() < minY) || (c.getY() > maxY)) foul = true;
					if (foul) {
						grown = false; // Signal, dass wir hier erstmal nicht weitermachen müssen.
						istEinBinnenSee = false;
					}
				}
				dieserSee.addAll(neue);
			}
			// ist nicht gewachsen oder über den Rand gegangen:
			if (istEinBinnenSee) {
				for (Coords c : dieserSee) {
					Region r = this.getRegion(c.getX(), c.getY());
					if (r == null) {
						try {
							r = fuellTyp.newInstance();
						} catch (InstantiationException ex) {
							new BigError(ex);
						} catch (IllegalAccessException ex) {
							new BigError(ex);
						}
						r.setCoords(c);
						this.putRegion(r);
					}
				}
			}
			unclear.removeAll(dieserSee);
		}
	}

	/**
	 * @return Eine MapSelection aller Koordinaten, die neben Regionen dieser Insel liegen und selbst nicht mit Regionen belegt sind. Keine Probleme mit "unzusammenhängenden" Inseln.
	 */
	public MapSelection getAussenKontur() {
		MapSelection sel = new MapSelection();
		for (Region r : alleRegionen()) {
			for (Coords n : r.getCoords().getNachbarn()) {
				if (getRegion(n.getX(), n.getY()) == null) sel.add(n);
			}
		}
		return sel;
	}

	public int getHeight() {
		return getMax().getY() - getMin().getY();
	}

	public Coords getMax() {
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		for (Coords c : alleKoordinaten()) {
			if (c.getX() > maxX) maxX = c.getX();
			if (c.getY() > maxY) maxY = c.getY();
		}
		return new Coords(maxX, maxY, this.welt);
	}

	public Coords getMin() {
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		for (Coords c : alleKoordinaten()) {
			if (c.getX() < minX) minX = c.getX();
			if (c.getY() < minY) minY = c.getY();
		}
		return new Coords(minX, minY, this.welt);
	}

	public int getMinDefinierterRadius() {
		Set<Coords> growing = new HashSet<Coords>();
		this.mittelpunkt = null;
		growing.add(getMittelpunkt(false));
		this.mittelpunkt = null;
		int i = 0;
		boolean goOn = true;
		for (i = 0; goOn; i++) {
			Set<Coords> add = new HashSet<Coords>();
			for (Coords c : growing) {
				for (Coords n : c.getNachbarn()) {
					add.add(n);
					if (this.getRegion(n.getX(), n.getY()) == null) {
						// wir haben die Lücke gefunden!
						goOn = false;
						break;
					}
				}
			}
			growing.addAll(add);
		}
		return i;
	}

	public Coords getMittelpunkt(boolean includeOzean) {
		if (mittelpunkt == null) {
			MapSelection ms = new MapSelection();
			for (Region r : alleRegionen()) {
				if (r.getClass() != Ozean.class) {
					ms.add(r.getCoords());
				} else {
					if (includeOzean) ms.add(r.getCoords());
				}
			}
			this.mittelpunkt = ms.getMittelpunkt();
		}
		return mittelpunkt;
	}

	public String getName() {
		return name;
	}

	/**
	 * @param r
	 * @return Liefert die Liste aller existierenden Nachbarn von r
	 */
	public List<Region> getNeighbors(Region r) {
		List<Region> retval = new ArrayList<Region>();
		for (Coords c : r.getCoords().getNachbarn()) {
			Region n = this.getRegion(c.getX(), c.getY());
			if (n != null) retval.add(n);
		}
		return retval;
	}

	public Region getRegion(int x, int y) {
		if (this.get(x) == null) return null;
		if (this.get(x).get(y) == null) return null;
		return this.get(x).get(y);
	}

	public void setWelt(int welt) {
		this.welt = welt;
		if (welt == -1) {
			if (Random.rnd(0, 1001) < 300) {
				fuellTyp = Lavastrom.class;
			} else {
				fuellTyp = Sandstrom.class;
			}
		}
	}
	
	public int getWelt() {
		return welt;
	}

	public int getWidth() {
		return getMax().getX() - getMin().getX();
	}

	public int getZielGroesse() {
		return zielGroesse;
	}

	public List<Region> grenzRegionen() {
		List<Region> retval = new ArrayList<Region>();
		for (Region r : alleRegionen()) {
			for (Coords c : r.getCoords().getNachbarn()) {
				if (this.getRegion(c.getX(), c.getY()) == null) {
					retval.add(r);
					break;
				}
			}
		}
		return retval;
	}

	public boolean hasOverlap(ProtoInsel other) {
		// gibt es eine Region auf other, bei deren Koordinaten wir auch eine Region haben?
		for (Region otherRegion : other.alleRegionen()) {
			Coords c = otherRegion.getCoords();
			Region myRegion = this.getRegion(c.getX(), c.getY());
			if (myRegion != null) return true;
		}
		// ... nein!
		return false;
	}

	public void konturieren(int breite) {
		for (int i = 0; i < breite; i++) {
			List<Coords> fuellung = new ArrayList<Coords>();
			for (Region r : this.alleRegionen()) {
				for (Coords c : r.getCoords().getNachbarn()) {
					if (this.getRegion(c.getX(), c.getY()) == null) {
						fuellung.add(c);
					}
				}
			}
			for (Coords c : fuellung) {
				Region ozean = new Ozean();
				ozean.setCoords(c);
				this.putRegion(ozean);
			}
		}
	}

	public void nackteKuestenBewaessern() {
		for (Region r : this.alleRegionen()) {
			if (r instanceof Ozean) continue; // Wasser wird nicht bewässert.
			for (Coords n : r.getCoords().getNachbarn()) {
				if (this.getRegion(n.getX(), n.getY()) == null) {
					Region fueller = null;
					try {
						fueller = fuellTyp.newInstance();
					} catch (InstantiationException ex) {
						new BigError(ex);
					} catch (IllegalAccessException ex) {
						new BigError(ex);
					}
					fueller.setCoords(n);
					this.putRegion(fueller);
				}
			}
		}
	}

	public void ozeanLueckenFuellen() {
		Set<Coords> unclear = new HashSet<Coords>();
		// der äußere Rahmen dieser Insel - was außerhalb liegt,
		// ist keinesfalls füll-pflichtig.
		int minX = this.getMin().getX();
		int maxX = this.getMax().getX();
		int minY = this.getMin().getY();
		int maxY = this.getMax().getY();
		// alle Füllkandidaten in ein Set:
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				unclear.add(new Coords(x, y, this.getWelt()));
			}
		}
		// alle definierten Regionen gleich raus:
		for (Region r : this.alleRegionen()) {
			unclear.remove(r.getCoords());
		}
		// true: Weiße Flecken füllen, wenn sie von Ozean umschlossen sind.
		this.fuellen(unclear, true);
	}

	/**
	 * Konvention: nur freie, besiedelbare Regionen, die mindestens 3 ebensolche Nachbarn haben.
	 * @return
	 */
	public Set<StartPosition> findeStartPositionen() {
		Set<StartPosition> retval = new HashSet<StartPosition>();

		List<Coords> kandidaten = this.alleKoordinaten();
		do {

			List<Coords> neu = new ArrayList<Coords>();
			for (Coords c : kandidaten) {
				Region r = this.getRegion(c.getX(), c.getY());

				if (!r.istBetretbar(null)) continue; // Ozean und so wollen wir nicht.
				if (r.getAlter() > 0) continue; // "Alter" muss bei 0 liegen - also seit längerer Zeit nicht betreten.

				// der Kandidat muss wenigstens 3 betretbare Nachbarn haben:
				int guteNachbarn = 0;
				for (Coords n : c.getNachbarn()) {
					Region nachbar = this.getRegion(n.getX(), n.getY());
					if (nachbar == null) continue;
					if (!nachbar.istBetretbar(null)) continue;
					if (nachbar.getAlter() > 0) continue;
					guteNachbarn++;
				}
				if (guteNachbarn < 3) continue;

				// okay:
				neu.add(c);
			}
			kandidaten = neu;

			// Collections.sort(kandidaten);
			// new Debug(kandidaten.size() + " Kandidaten für Startpositionen: " + StringUtils.aufzaehlung(kandidaten));
			if (kandidaten.isEmpty()) break;


			Collections.shuffle(kandidaten);

			StartPosition sp = new StartPosition();
			for (Coords c : kandidaten) {
				// die erstbeste Koordinate wird's:
				sp.add(c);
				sp.setZentrum(c);

				Coords nachbarEins = null;
				List<Coords> nachbarn = new ArrayList<Coords>();
				nachbarn.addAll(c.getNachbarn());
				Collections.shuffle(nachbarn);
				for (Coords n : nachbarn) {
					Region nachbar = this.getRegion(n.getX(), n.getY());
					if (nachbar == null) continue;
					if (!nachbar.istBetretbar(null)) continue;

					// yippie!
					nachbarEins = n;
					break;
				}
				if (nachbarEins == null) throw new NullPointerException("Nachbar Eins ist null.");
				sp.add(nachbarEins);
				nachbarn.remove(nachbarEins);

				Coords nachbarZwei = null;
				for (Coords n : nachbarn) {
					Region nachbar = this.getRegion(n.getX(), n.getY());
					if (nachbar == null) continue;
					if (!nachbar.istBetretbar(null)) continue;

					// yippie!
					nachbarZwei = n;
					break;
				}
				if (nachbarZwei == null) throw new NullPointerException("Nachbar Zwei ist null.");
				sp.add(nachbarZwei);

				break; // wir wollen ja nur die erstbeste StartPositition aus den Kandidaten errechnen
			}
			if (!sp.istOkay()) throw new IllegalStateException("Die Startposition " + sp + " ist nicht okay.");

			// die Nachbarregionen fallen flach:
			for (Coords weg : sp) {
				this.getRegion(weg.getX(), weg.getY()).setAlter(10);
				for (Coords auchWeg : weg.getNachbarn()) {
					Region nn = this.getRegion(auchWeg.getX(), auchWeg.getY());
					if (nn != null) nn.setAlter(9);
				}
			}

			new Debug("Startposition " + sp + " gefunden.");
			retval.add(sp);
			
		} while (!kandidaten.isEmpty());

		return retval;
	}


	public void putRegion(Region r) {
		mittelpunkt = null;
		int x = r.getCoords().getX();
		int y = r.getCoords().getY();
		if (this.get(x) == null) this.put(x, new TreeMap<Integer, Region>());
		this.get(x).put(y, r);
	}

	public void removeRegion(Region r) {
		mittelpunkt = null;
		int x = r.getCoords().getX();
		int y = r.getCoords().getY();
		if (this.get(x) == null) return;
		this.get(x).remove(y);
	}


	protected Region randomRegion(Map<Class<? extends Atlantis>, Double> chances) {
		Region r = null;
		List<Class<? extends Atlantis>> types = new ArrayList<Class<? extends Atlantis>>();
		types.addAll(chances.keySet());
		while (r == null) {
			Collections.shuffle(types, rnd);
			Class<? extends Atlantis> maybe = types.get(0);
			if (rnd.nextDouble() < chances.get(maybe)) {
				try {
					r = (Region)maybe.newInstance();
				} catch (InstantiationException ex) {
					new BigError(ex);
				} catch (IllegalAccessException ex) {
					new BigError(ex);
				}
			}
		}
		return r;
	}

	/**
	 * entfernt eine Region aus dieser Insel
	 * @param x
	 * @param y
	 */ public void remove(int x, int y) {
		if (this.get(x) == null) return;
		this.get(x).remove(y);
	}

	public void rotateCCW(int angle) {
		mittelpunkt = null;
		int angle2 = angle;
		if (angle < 0) {
			int multiplier = angle / -360 + 1;
			angle2 = (angle + multiplier * 360);
		}
		angle2 %= 360;
		if (angle2 % 60 != 0) throw new RuntimeException("Winkel " + angle + "\u00b0 ist kein Vielfaches von 60\u00b0.");
		for (int steps = angle2 / 60; steps > 0; steps--) {
			List<Region> gedrehte = new ArrayList<Region>();
			for (Region r : alleRegionen()) {
				Coords alt = r.getCoords();
				Coords neu = new Coords(0 - alt.getY(), alt.getY() + alt.getX(), alt.getWelt());
				r.setCoords(neu);
				gedrehte.add(r);
			}
			this.clear();
			for (Region r : gedrehte) {
				this.putRegion(r);
			}
		}
	}

	public void saveCR(String filename) {
		File f = new File(filename);
		Writer out = null;
		try {
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF8"));
			out.write("VERSION 64\n" + "\"utf-8\";charset\n" + "\"de\";locale\n" + "\"fantasya\";Spiel\n" + "\"Standard\";Konfiguration\n" + "\"Hex\";Koordinaten\n" + "36;Basis\n" + "1;Runde\n" + "2;Zeitalter\n");
			for (Region r : this.alleRegionen()) {
				Coords c = r.getCoords();
				out.write("REGION " + c.getX() + " " + c.getY() + " " + c.getWelt() + "\n");
				out.write("\"" + r.getName() + "\";Name\n");
				
				String terrain = r.getClass().getSimpleName();
				out.write("\"" + terrain + "\";Terrain\n");
				
				out.write("\"" + r.getBeschreibung() + "\";Beschr\n");
				out.write(r.getInselKennung() + ";Inselkennung\n");
				out.write(r.getEnstandenIn() + ";entstandenIn\n");
			}
			out.close();
		} catch (FileNotFoundException ex) {
			new BigError(ex);
		} catch (UnsupportedEncodingException ex) {
			new BigError(ex);
		} catch (IOException ex) {
			new BigError(ex);
		}
	}

	public void setZielGroesse(int zielGroesse) {
		this.zielGroesse = zielGroesse;
	}

	public void shift(int dx, int dy) {
		// new Debug("dx: " + dx + ", dy:" + dy);
		mittelpunkt = null;
		List<Region> shifted = new ArrayList<Region>();
		for (Region r : alleRegionen()) {
			int x = r.getCoords().getX();
			int y = r.getCoords().getY();
			r.setCoords(new Coords(x + dx, y + dy, welt));
			shifted.add(r);
		}
		this.clear();
		for (Region r : shifted) {
			this.putRegion(r);
		}
	}

	/**
	 * @param seiten Seiten des Würfels
	 * @return Ergebnis eines Würfelwurfs
	 */
	public static int W(int seiten) {
		return Random.W(seiten);
	}


	public class EntfernungZuAllenComparator implements Comparator<Coords> {
		final ProtoInsel insel;

		public EntfernungZuAllenComparator(ProtoInsel insel) {
			this.insel = insel;
		}

		@Override
		public int compare(Coords c1, Coords c2) {
			double avgD1 = insel.distanceTo(c1).getAverage();
			double avgD2 = insel.distanceTo(c2).getAverage();

			if (avgD1 < avgD2) return -1;
			if (avgD1 > avgD2) return +1;
			return 0;
		}
	}

}
