package de.x8bit.Fantasya.Atlantis;

import de.x8bit.Fantasya.Atlantis.Items.LuxusGood;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.Atlantis.Regions.Chaos;
import de.x8bit.Fantasya.Host.Datenbank;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.Paket;
import de.x8bit.Fantasya.Host.Terraforming.AltInsel;
import de.x8bit.Fantasya.Host.Terraforming.InselGenerator;
import de.x8bit.Fantasya.Host.Terraforming.Kontinent;
import de.x8bit.Fantasya.Host.Terraforming.ProtoInsel;
import de.x8bit.Fantasya.Host.Terraforming.UnterweltKegelInsel;
import de.x8bit.Fantasya.util.Random;
import java.util.List;

public class Welt {

	/**
	 * erstellt eine Welt aus der Datenbank
	 * @param ebene - Ebene die erstellt werden soll
	 */
	public Welt(int ebene) {
		Datenbank db = new Datenbank("Welterstellung");
		db.CreateSelect("welt", "w");
		db.AddFirstWhere("ebene", ebene);
		ResultSet rs = db.Select();

		try {
			if (rs.next()) {
				Size_max = rs.getInt("size_max");
				Size_min = rs.getInt("size_min");
				Ebene = rs.getInt("ebene");
				Ansprechpartner = rs.getString("mail");
				istSicher = rs.getInt("sicher");
				gotit = rs.getInt("gotit");
			}
		} catch (SQLException e) {
			new SysMsg("die Welt mit der Ebene #" + ebene + " existiert nicht - (" + e + ")");
		}
	}

	/**
	 * erstellt eine völlig neue Welt
	 * @see #Create
	 */
	private Welt() { /* keine weitere Funktionalität */ }
	/** Größe der Welt zur alten Version - Kompatibilität */
	public int Size_min = 0;
	/** Größe der Welt zur alten Version - Kompatibilität */
	public int Size_max = 0;
	/** Ebene dieser Welt ... wenn negativ, dann ist es die Unterwelt */
	public int Ebene = 0;
	/** Ansprechpartner für Probleme */
	public String Ansprechpartner = "mogel@x8bit.de";
	/** kann nicht geändert werden, weil privates Spiel (unter Freunden) */
	public int istSicher = 0;
	/** Wert für alles Mögliche */
	public int gotit = 0;

	/**
	 * erzeugt eine neue Welt (<b>ohne</b> Unterwelt)
	 * @param ebene - Nummer der Welt
	 * @param ansprechpartner - Ansprechpartner für Probleme & Fragen
	 * @param sizemin - Kompatibilität zu Fanta
	 * @param sizemax - Kompatibilität zu Fanta
	 */
	public static void Create(int ebene, String ansprechpartner, int sizemin, int sizemax) {
		System.out.println("erzeuge neue Welt");

		Welt w = new Welt();
		w.Size_max = sizemax;
		w.Size_min = sizemin;
		w.Ansprechpartner = ansprechpartner;
		w.Ebene = ebene;

		// Datenbank öffnen
		Datenbank db = new Datenbank("Create Welt");

		// Oberwelt in DB speichern
		System.out.println(" - speichere");
		db.CreateInsert("welt", "size_min, size_max, ebene, mail, sicher, gotit");
		db.AddFirstValue(w.Size_max);
		db.AddValue(w.Size_min);
		db.AddValue(w.Ebene);
		db.AddValue(w.Ansprechpartner);
		db.AddValue(w.istSicher);
		db.AddLastValue(w.gotit);
		db.Insert();

		db.Close();
	}
	/** Blocksize ... bei Änderung muss der Teil in InitDB angepasst werden */
	public static final int BLOCKSIZE = 7;
	/** Das ist der Anfangs-Seed zum Erstellen der Insel auf dem Block */
	private static final int INITIALBLOCKHEIGHT_MIN = 11;
	/** Das ist der Anfangs-Seed zum Erstellen der Insel auf dem Block */
	private static final int INITIALBLOCKHEIGHT_MAX = 20;
	/** wird zum Blockerstellen benötigt */
	private static int newterrains[][];

	public static void MakeBlock(int startx, int starty) {
		// Inselkennung holen
		int insel = GameRules.getInselkennungErzeugung();
		GameRules.setInselkennungErzeugung(insel + 1);

		MakeBlock(startx, starty, 1, insel);
		// MakeBlock(startx, starty, -1, 0 - insel); -- erstmal keine Unterwelt
	}

	/**
	 * erzeugt einen Block von Regionen ... der Block ist BLOCKSIZExBLOCKSIZE Regionen groß,
	 * somit passend zu Fanta ... es müssen die Koordinaten einer Region
	 * innerhalb dieses Blockes übergeben werden ... die entsprechenden
	 * Regionen für diesen Block werden selber berechnet!
	 * @param startx - Koordinate einer Region
	 * @param starty - Koordinate einer Region
	 * @param welt - Welt des Blockes/Region
	 * @param insel - Inselkennung
	 */
	public static void MakeBlock(int startx, int starty, int welt, int insel) {
		// ist irgendwie zu einfach
		startx = (startx / BLOCKSIZE) * BLOCKSIZE;
		starty = (starty / BLOCKSIZE) * BLOCKSIZE;
		new SysMsg("erzeuge neuen Block an Regionen bei [" + startx + "/" + starty + "/" + welt + "]");

		// erstmal alles "löschen"
		newterrains = new int[BLOCKSIZE][BLOCKSIZE];
		for (int y = 0; y < BLOCKSIZE; y++) {
			for (int x = 0; x < BLOCKSIZE; x++) {
				newterrains[y][x] = 0;
			}
		}

		// die Mitte wird zur Quelle ... von hieraus verteilen ... etwas spielen
		// mit der INITIALBLOCKHEIGHT ... das Ganze basiert auf einer großen Säule
		// von der (entsprechend INITIALBLOCKHEIGHT/count) an Wassertropfen runter laufen
		// auf den einzelnen Nachbarfeldern bilden die Tropfen ebenfalls wieder Säulen
		// und so weiter ............
		GenerateBlock(BLOCKSIZE / 2, BLOCKSIZE / 2, Random.rnd(INITIALBLOCKHEIGHT_MIN, INITIALBLOCKHEIGHT_MAX));

		// Handelsgüter festlegen
		ArrayList<Item> ali = new ArrayList<Item>(); // enthält alle Handelsgüter
		for (Paket p : Paket.getPaket("Items")) {
			if (p.Klasse instanceof LuxusGood) {
				ali.add((Item) p.Klasse);
			}
		}
		int i1 = Random.rnd(0, ali.size());
		int i2 = Random.rnd(0, ali.size());
		while (i1 == i2) {
			i2 = Random.rnd(0, ali.size());
		}

		for (int y = 0; y < BLOCKSIZE; y++) {
			for (int x = 0; x < BLOCKSIZE; x++) {
				Region r = null;
				if (welt < 0) {
					newterrains[x][y] = (newterrains[x][y] != 0) ? 0 : 1;
				}
				if (newterrains[x][y] == 0) {
					if (welt > 0) {
						r = Region.Create("Ozean", startx + x, starty + y, welt);
					} else {
						r = Region.Create("Lavastrom", startx + x, starty + y, welt);
					}
				} else {
					String terrains[];
					if (welt > 0) {
						terrains = new String[]{"Ebene", "Berge", "Gletscher", "Hochland", "Sumpf", "Wald", "Wueste"};
					} else {
						terrains = new String[]{"Oedland", "Vulkan", "aktiverVulkan", "Geroellebene", "Moor", "Trockenwald", "Sandstrom"};
					}
					r = Region.Create(terrains[Random.rnd(0, terrains.length)], startx + x, starty + y, welt);
				}
				r.setInselKennung(insel);
				if (Random.rnd(0, 100) > 50) {
					// r.getLuxus().get(i1).setNachfrage(0 - r.getLuxus().get(i1).getNachfrage());
					r.getLuxus().get(i1).setNachfrage(-1.0f);
				} else {
					// r.getLuxus().get(i2).setNachfrage(0 - r.getLuxus().get(i2).getNachfrage());
					r.getLuxus().get(i1).setNachfrage(-1.0f);
				}
			}
		}

		// AddCave(welt, insel);
	}

	/**
	 * der Teil zum Erzeugen der Insel 
	 * @param x - relative Koordinate <b>im</b> Block
	 * @param y - relative Koordinate <b>im</b> Block
	 * @param count - wieviel für den Seed noch zu haben sind
	 */
	private static void GenerateBlock(int x, int y, int count) {
		if (count <= 0) {
			return;	// wenn mit 0 aufgerufen wurde, dann gleich wieder zurück
		}
		if ((x < 0) || (y < 0) || (x >= BLOCKSIZE) || (y >= BLOCKSIZE)) {
			return;	// Ziel wäre außerhalb
		}
		if (newterrains[x][y] > count) {
			return;	// nur wenn noch nicht belegt
		}
		// Anzahl setzen
		newterrains[x][y] = count;

		// weiter verteilen
		GenerateBlock(x, y - 1, Random.rnd(0, count - 1));	// oben
		GenerateBlock(x, y + 1, Random.rnd(0, count - 1));	// unten
		GenerateBlock(x - 1, y, Random.rnd(0, count - 1));	// links
		GenerateBlock(x + 1, y, Random.rnd(0, count - 1));	// rechts
	}

	/**
	 * @param welt 1 für die Oberwelt, -1 für die Unterwelt. Letzteres funktioniert nur mit der "April2011"-Methode! 
	 */
	public static void NeueRegionen(int welt) {
		String methode = GameRules.GetOption(GameRules.NEUE_INSEL_METHODE);

		if ((null == methode) || ("classic".equalsIgnoreCase(methode))) {
			/* Klassische Methode (default)
			 * dabei wird einfach "inselkennung.welt" verwendet als Ausgangspunkt ... hier wird eine
			 * beliebige Region genommen und von dort aus alle Himmelsrichtungen (N/S/O/W) abgeklappert
			 * und geschaut wann Chaos auftaucht oder eine andere Inselkennung
			 * <br/>
			 * <br/>1 - wenn neue Inselkennung, dann die nächste Richtung
			 * <br/>2 - wenn alle Richtungen durch, dann "inselkennung.welt" eins hochzählen, weiter bei 1
			 * <br/>3 - wenn neue Region Chaos, dann *hurra* hier einen Block erstellen :)
			 */

			boolean ende = false;
			GameRules.setInselkennungWelt(1);
			int kennung = GameRules.getInselkennungWelt();

			new SysMsg("Erzeuge neuen Regions-Block (klassische Methode)");

			// Endlosschleife ... entweder er findet Chaos oder es gibt
			// ein ernsthaftes Problem -> BigError()
			boolean dummy = true;
			while (dummy) {
				new SysMsg(1, " - suche nach Chaos bei Insel " + kennung);


				List<Region> inselRegionen = new ArrayList<Region>();
				for (Region r : Region.CACHE.values()) {
					if (r.getInselKennung() == kennung) {
						inselRegionen.add(r);
					}
				}
				if (inselRegionen.isEmpty()) {
					new BigError("Problem !!! 'inselkennung.welt' zeigt auf eine nicht vorhandene Insel");
				}
				Region start = inselRegionen.get(0);

				// die Richtung ist normal Dezimal, also nicht aus dem Enum
				// 0 - Norden .. 1 - Osten .. 2 - Süden .. 3 - Westen
				for (int richtung = 0; richtung < 4; richtung++) {
					// schneller Zugriff auf die Koordinaten
					int x = start.getCoords().getX();
					int y = start.getCoords().getY();
					int w = start.getCoords().getWelt();

					new SysMsg(2, " - verarbeite Richtung '" + "NOSW".substring(richtung, richtung + 1) + "'");
					ende = false;
					while (!ende) {
						switch (richtung) {
							case 0:
								y += BLOCKSIZE;
								break;
							case 1:
								x += BLOCKSIZE;
								break;
							case 2:
								y -= BLOCKSIZE;
								break;
							case 3:
								x -= BLOCKSIZE;
								break;
						}
						Region r = Region.Load(x, y, w);
						if (r instanceof Chaos) {
							new SysMsg("Chaos gefunden :) - erzeuge neuen Block");
							Welt.MakeBlock(x, y);
							new SysMsg("neuen Block erzeugt - und weiter gehts mit den Spielern");

							// und raus ... ohne irgend was
							return;
						}
						if (r.getInselKennung() != start.getInselKennung()) {
							ende = true;
							new SysMsg(2, " - andere Insel (" + r.getInselKennung() + ") gefunden, ändere Richtung");
						}
					}
				}

				new SysMsg(2, " - Insel ist komplett von anderen Inseln umgeben, nehme eine neue Insel");

				kennung++;
				GameRules.setInselkennungWelt(kennung);
			}
			return;
			// endif "classic"
		} else if ("April2011".equalsIgnoreCase(methode)) {
			new SysMsg("Welt.NeueRegionen() [April2011-Variante]");

			List<Region> vorhanden = new ArrayList<Region>();
			for (Region r : Region.CACHE.values()) {
				if (r.getCoords().getWelt() == welt) {
					vorhanden.add(r);
				}
			}

			ProtoInsel neu = null;
			if (vorhanden.isEmpty()) {
				// absoluter Neustart:
				if (welt == 1) {
					int inselKennung = 1;
					ProtoInsel fantasya = new Kontinent(1, "Kontinent #" + inselKennung);
					fantasya.create(); // die Zeile aller Zeilen...
					fantasya.setInselkennung(inselKennung);

					inselKennung++;

					// Alle Regionen anlegen, die es in der Echtwelt noch nicht gibt:
					for (Region pReg : fantasya.alleRegionen()) {
						Coords c = pReg.getCoords();
						Region r = Region.Create(pReg.getClass().getSimpleName(), c);
						r.setInselKennung(pReg.getInselKennung());
						r.setLuxus(pReg.getLuxus());
						r.setEnstandenIn(-1); // soll für alle gleich sichtbar sein
					}

					for (int i = 0; i < 10; i++) {
						neu = inselZuwachs(welt);

						// Alle Regionen anlegen, die es in der Echtwelt noch nicht gibt:
						for (Region maybe : neu.alleRegionen()) {
							Coords c = maybe.getCoords();
							Region r = Region.Load(c);
							if ((r == null) || (r instanceof Chaos)) {
								// neu!
								r = Region.Create(maybe.getClass().getSimpleName(), c);
								r.setInselKennung(maybe.getInselKennung());
								r.setLuxus(maybe.getLuxus());
								r.setEnstandenIn(-1); // soll für alle gleich sichtbar sein
							}
						}
					}
				} else if (welt == -1) {
					// Unterwelt neu anlegen...
					int inselKennung = -1;
					ProtoInsel unterwelt = new UnterweltKegelInsel(-1, "Unterwelt #" + inselKennung);
					unterwelt.create(); // die Zeile aller Zeilen...
					unterwelt.setInselkennung(inselKennung);

					inselKennung--;

					// Alle Regionen anlegen, die es in der Echtwelt noch nicht gibt:
					for (Region pReg : unterwelt.alleRegionen()) {
						Coords c = pReg.getCoords();
						Region r = Region.Create(pReg.getClass().getSimpleName(), c);
						r.setInselKennung(pReg.getInselKennung());
						r.setLuxus(pReg.getLuxus());
						r.setEnstandenIn(-1); // soll für alle gleich sichtbar sein
					}

					for (int i = 0; i < 15; i++) {
						neu = inselZuwachs(welt);

						// Alle Regionen anlegen, die es in der Echtwelt noch nicht gibt:
						for (Region maybe : neu.alleRegionen()) {
							Coords c = maybe.getCoords();
							Region r = Region.Load(c);
							if ((r == null) || (r instanceof Chaos)) {
								// neu!
								r = Region.Create(maybe.getClass().getSimpleName(), c);
								r.setInselKennung(maybe.getInselKennung());
								r.setLuxus(maybe.getLuxus());
								r.setEnstandenIn(-1); // soll für alle gleich sichtbar sein
							}
						}
					}
				} else {
					throw new RuntimeException("Neue Regionen für Welt " + welt + " sollen angelegt werden?");
				}

			} else {
				// erweitern:
				neu = inselZuwachs(welt);

				// Alle Regionen anlegen, die es in der Echtwelt noch nicht gibt:
				for (Region maybe : neu.alleRegionen()) {
					Coords c = maybe.getCoords();
					Region r = Region.Load(c);
					if ((r == null) || (r instanceof Chaos)) {
						// neu!
						r = Region.Create(maybe.getClass().getSimpleName(), c);
						r.setInselKennung(maybe.getInselKennung());
						r.setLuxus(maybe.getLuxus());
					}
				}
			}


			// endif methode == "April2011"
		} else {
			throw new RuntimeException("Inselerzeugungsmethode " + methode + " nicht gefunden.");
		}

	}

	protected static ProtoInsel inselZuwachs(int welt) {
		ProtoInsel vorhanden = new AltInsel(welt);
		Coords m = vorhanden.getMittelpunkt(true);
		vorhanden.shift(-m.getX(), -m.getY());

		InselGenerator ig = new InselGenerator();
		ProtoInsel neu = ig.addNewInselTo(vorhanden);

		// Ursprungsregion suchen und Koordinaten zurückverschieben:
		Coords shift = null;
		for (Region r : neu.alleRegionen()) {
			if (AltInsel.URSPRUNGS_MARKER.equals(r.getBeschreibung())) {
				Coords now = r.getCoords();
				shift = new Coords(-now.getX(), -now.getY(), 0);
				break;
			}
		}
		if (shift == null) {
			throw new IllegalStateException("Kann den Ursprungsmarker in der neuen Welt nicht mehr finden.");
		}

		neu.shift(shift.getX(), shift.getY());

		return neu;
	}
}
