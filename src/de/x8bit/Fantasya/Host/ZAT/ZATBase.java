package de.x8bit.Fantasya.Host.ZAT;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Ship;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.SysErr;
import de.x8bit.Fantasya.Atlantis.Messages.ZATMsg;
import de.x8bit.Fantasya.Host.Datenbank;
import de.x8bit.Fantasya.Host.Paket;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;

abstract public class ZATBase {

	/**
	 * <h3><font color="red">der Konstruktor ist fÃ¼r vorwiegend Allgemeines zustÃ¤ndig</font></h3>
	 * <br><br> 
	 * der einfache Konstruktor ... hier wird einfach nur das System aufgerÃ¤umt (vom
	 * letzten ZAT-Schritt) und der GC von Hand gestartet
	 * <b>hier wird kein PostAction bzw. PreAction aufgerufen, selber machen !!</b>
	 * @param message - Meldung fÃ¼r das Logging zu diesem Schritt
	 */
	//private static Unit debug = Unit.Load(Atlantis.toBase10("qo"));
	public ZATBase(String message) {
		new ZATMsg(message);
		CleanUp();
	}

	/**
	 * <h3><font color="red">der Konstruktor ist vorwiegend fÃ¼r Befehle die eine gesamte Region betreffen</font></h3>
	 * <br><br>
	 * durchsucht die DB nach allen Regionen in denen Einheiten stehen die diesen Befehl 
	 * ausfÃ¼hren wollen ... das Problem bei einigen Befehle ist die Interaktion mit anderen
	 * Einheiten bzw. das Limit an Resourcen in der Region (MACHE HOLZ, etc.)
	 * @param befehl - der Befehl soll ausgefÃ¼hrt werden
	 * @param message - Meldung fÃ¼r das System-Logging
	 */
	public ZATBase(String befehl, String message) {
		new ZATMsg(message);

		PreAction();

		// hier werden jetzt alle Regionen gesucht wo Einheiten den gewÃ¼nschten Befehl ausfÃ¼hren wollen
		Datenbank db = new Datenbank(befehl);
		// SELECT b.*, e.koordx, e.koordy, e.welt FROM befehle b, einheiten e WHERE befehl LIKE 'mache%' AND e.nummer = b.nummer GROUP BY b.nummer, e.koordx, e.koordy, e.welt ORDER BY sortierung
		db.myQuery = "SELECT e.koordx, e.koordy, e.welt FROM einheiten e, befehle b WHERE e.nummer = b.nummer AND b.befehl LIKE '" + befehl + "%' GROUP BY koordx, koordy, welt";
		ResultSet rs = db.Select();
		try {
			while (rs.next()) {
				Region r = Region.Load(rs.getInt("koordx"), rs.getInt("koordy"), rs.getInt("welt"));
				DoAction(r, befehl);

//				// da hier der Speicher definitiv voll lÃ¤uft, nach jeder Region aufrÃ¤umen
//				ZATBase.SaveAll();
//				-- deaktiviert am 07.01.2010
			}
		} catch (Exception ex) {
			new BigError(ex);
		}
		db.Close();

		ZATBase.ClearProxy(); // das muss hier ausgefÃ¼hrt werden - sonst wird im PostAction ggf. alles gelÃ¶scht

		PostAction();
		CleanUp();
	}

	/**
	 * <h3><font color="red">der Konstruktor ist vorwiegend fÃ¼r Befehle die spezifisch sind fÃ¼r eine Einheit</font></h3>
	 * <br><br> 
	 * fÃ¼hrt ein suchen nach den Einheiten aus welche den Befehl ausfÃ¼hren wollen
	 * @param befehl - der Befehl soll ausgefÃ¼hrt werden
	 * @param message - Meldung fÃ¼r das Logging zu diesem Schritt
	 * @param minSize - mind. Anzahl der Befehlsteile (MACHE HOLZ -> 2, GIB einheit Holz -> 3, etc.) 
	 */
	public ZATBase(String befehl, String message, int minSize) {
		new ZATMsg(message);

		PreAction();

		Datenbank db = new Datenbank(message);
		ResultSet rs = db.SelectACommand(befehl);
		try {
			while (rs.next()) {
				Unit unit = Unit.Load(rs.getInt("nummer"));
				Action(unit, befehl.toLowerCase(), minSize);

				// AufrÃ¤umen nach jeder Einheit erzwingen
				//force_clear = true; // seit 0.12.4 - es wird sonst nicht immer gespeichert !! - Bug ist UNKLAR !!
				if (force_clear) {
					ZATBase.ClearProxy();
				}
			}
		} catch (Exception ex) {
			new BigError(ex);
		}

		db.Close();

		PostAction();
		CleanUp();
	}
	/** erzwingt das AufrÃ¤umen nach jeder Einheit */
	protected boolean force_clear = false;

	/**
	 * rÃ¤umt das ganze System auf ... dazu werden die ganzen Proxy's gelÃ¶scht und anschlieÃŸend
	 * noch der GC aufgerufen
	 */
	private void CleanUp() {
		// alte Datenbankverbindungen aufrÃ¤umen
		Datenbank.CleanUp();

		// alle Proxy's lÃ¶schen
		ClearProxy();
	}

	/**
	 * lÃ¶scht alle Proxy's
	 */
	public static void ClearProxy() {
		ClearProxy(true);

		// wenn die Proxy's nicht gelÃ¶scht werden, gibt es durchaus disharmonien beim Bewegen ... die Einheiten
		// sind noch in der Region selber gelistet, sind dort aber nicht mehr ... und in der neuen Region
		// sind sie noch nicht gelistet
//		SaveAll(false);
	}

	/**
	 * lÃ¶scht alle Proxys
	 * @param clear - TRUE wenn die Proxys gelÃ¶scht werden sollen, sonst wird nur in die DB gespeichert
	 */
	public static void ClearProxy(boolean clear) {
		// Parteien lÃ¶schen
		if (clear) {
			Partei.PROXY.clear();
		}

		// Schiffe lÃ¶schen
		if (clear) {
			Ship.PROXY.clear();
		}

		// GebÃ¤ude lÃ¶schen
		if (clear) {
			Building.PROXY.clear();
		}

		// Einheiten lÃ¶schen
		if (clear) {
			Unit.CACHE.clear();
		}

		// Regionen zum Schluss
		if (clear) {
			Region.CACHE.clear();
		}

		// den GC erzwingen damit die Objekte auch wirklich den Speicher frei rÃ¤umen
		if (clear) {
			System.gc();
		}
	}

	/**
	 * die absolute Verwaltung der BefehlsausfÃ¼hrung ... hier werden die Ganzen Einheiten
	 * dazu aufgefordert den Befehl auszufÃ¼hren ... DoAction() leitet dazu an den jeweiligen
	 * ZAT-Schritt zurÃ¼ck um dort dann verarbeitet zu werden
	 * @param unit - Einheit fÃ¼r die der Befehl ausgefÃ¼hrt werden soll
	 * @param befehl - das ist der komplette Befehle, ungeparst & so
	 * @param minSize - soviele Befehlsteile muss der Befehl haben, sonst wird nicht an DoAction weiter geleitet
	 */
	public void Action(Unit u, String befehl, int minSize) {
		if (u == null) {
			new SysErr("Einheit wurde nicht gefunden/geladen/sonstwas - ZATBase::Action()");
			return;
		}

		ArrayList<String> befehle = new ArrayList<String>();

		// kann keinen einfachen Iterator verwenden, weil die Befehle selbst
		// unter Umständen Zeilen an u.Befehle anhängen (z.B. DEFAULT ...)
		for (int i = 0; i < u.Befehle.size(); i++) {
			String line = u.Befehle.get(i);

			if (line.toLowerCase().startsWith(befehl)) {
				// gotcha!

				String b[] = line.split("\\ ");
				if (b.length < minSize) {
					new Fehler("Fehler im Syntax des Befehls '" + line + "'.", u, u.getCoords());
					continue;
				}

				if (line.toLowerCase().startsWith("bewache")
						|| line.toLowerCase().startsWith("default")) {
					// diese Befehle sind okay, egal ob schon ein langer Befehl ausgeführt wurde:
					if (DoAction(u, b)) {
						// true -> Befehl muss übernommen werden
						befehle.add(line);
					}
					continue;
				}

				if (u.getLongOrder().length() == 0) {
					// if (Main.getBFlag("DEBUG")) new TestMsg("Will ausführen:" + StringUtils.join(b, " "));
					// es darf ein langer Befehl ausgeführt werden:
					if (DoAction(u, b)) {
						// true -> Befehl muss übernommen werden ... z.B. LIEFERE-Befehl
						// oder PERMANENT in der Zeile
						befehle.add(line);
					}
					// LongOrder wird/muss von den Kindern gesetzt (werden)
				} else {
					new Fehler("'" + line + "' - " + u + " hat bereits einen langen Befehl.", u, u.getCoords());
				}
			} else {
				// Befehlszeile einfach übernehmen ... da das gerade
				// nicht der aktuelle Befehl ist
				befehle.add(line);
			}

		} // next line

		u.Befehle = befehle;
	}

	/**
	 * hier wird der ganze Kram fÃ¼r den ZAT-Schritt ausgefÃ¼hrt ... dazu wird das durchkÃ¤mmen der Befehle
	 * nach dem entsprechenden Befehl schon von der Basis-Klasse ausgefÃ¼hrt ... es wird einfach nur noch
	 * die Methode mit der Einheit und den bereits zerlegten Befehl aufgerufen
	 * <br><br>
	 * <b>diese Methode wird nicht von der Klasse <unit>TempEinheiten</unit> verwendet, siehe dort</b>
	 * <br><br>
	 * @param unit - die Einheit fÃ¼r die der Befehl ausgefÃ¼hrt werden soll
	 * @param befehl - der Befehl ... bereits an den Leerzeichen zerlegt und als Array Ã¼bergeben
	 * @return true wenn der Befehl behalten werden soll (z.B. LIEFERE)
	 */
	abstract public boolean DoAction(Unit u, String befehl[]);

	/**
	 * hier wird der ganze Kram fÃ¼r den ZAT-Schritt ausgefÃ¼hrt ... dazu wird das durchkÃ¤mmen der Befehle
	 * nach dem entsprechenden Befehl schon von der Basis-Klasse ausgefÃ¼hrt ... es wird einfach nur noch
	 * die Methode mit der Einheit und den bereits zerlegten Befehl aufgerufen
	 * <br><br>
	 * <b>diese Methode wird nicht von der Klasse <unit>TempEinheiten</unit> verwendet, siehe dort</b>
	 * <br><br>
	 * @param r - die Region fÃ¼r die diverse Befehl ausgefÃ¼hrt werden soll, bzw. fÃ¼r die Einheiten in der Region
	 * @param befehl - der Befehl der wichtig ist
	 */
	abstract public void DoAction(Region r, String befehl);

	/**
	 * wird ausgefÃ¼hrt <b>bevor</b> die Befehle fÃ¼r die Einheiten abgearbeitet werden ... kann also
	 * benutzt werden um Tabellen zu lÃ¶schen und andere Operationen durchzufÃ¼hren
	 */
	abstract public void PreAction();

	/**
	 * wird ausgefÃ¼hrt <b>nachdem</b> die Befehle fÃ¼r die Einheiten abgearbeitet wurden ... kann also
	 * benutzt werden um Tabellen zu lÃ¶schen und andere Operationen durchzufÃ¼hren
	 */
	abstract public void PostAction();
	protected static Writer template = null;

	protected static void addTemplate(String tpl, Paket paket) {
		addTemplate(tpl, paket, null);
	}

	protected static void addTemplate(String tpl, Paket paket, String additions) {
		if (additions != null) {
			additions = " " + additions;
		} else {
			additions = "".intern();
		}
		if (paket.Klasse instanceof Item) {
			for (String name : getNames(paket)) {
				addTemplate(tpl + " (" + name + ")" + additions);
			}
		} else {
			addTemplate(tpl + " (" + paket.ClassName.toLowerCase() + ")" + additions);
		}
	}

	protected static List<String> getNames(Paket paket) {
		List<String> retval = new ArrayList<String>();
		if (paket.Klasse instanceof Item) {
			Item item = (Item) paket.Klasse;
			item.setAnzahl(1);
			String singular = item.getName().toLowerCase();
			item.setAnzahl(2);
			String plural = item.getName().toLowerCase();

			if (!singular.equals(plural)) {
				retval.add(singular);
				retval.add(plural);
			} else {
				retval.add(singular);
			}
		} else {
			retval.add(paket.ClassName.toLowerCase());
		}

		return retval;
	}

	protected static void addTemplate(String tpl) {
		try {
			if (template == null) {
				File file = new File("template.tpl");

				template = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF8"));
				// template = new FileWriter("template.tpl");
				template.write("; RegEx-Template für Fantasya 2 bzw. FCheck2\n");
				template.write("\n");
				template.write("\n");
			}
			template.write(tpl + "\n");
			template.flush();
		} catch (Exception ex) {
			new BigError(ex);
		}
	}

	protected String ConcatinateCommand(String[] befehl) {
		StringBuilder sb = new StringBuilder(befehl[0]);
		for (int i = 1; i < befehl.length; i++) {
			sb = sb.append(" ").append(befehl[i]);
		}
		return sb.toString();
	}

	public String[] RecreateCommand(String[] befehl) {
		boolean quotation = false;
		StringBuilder current = null;
		ArrayList<String> als = new ArrayList<String>();

		for (int i = 0; i < befehl.length; i++) {
			if (current == null) {
				current = new StringBuilder(befehl[i]);
			} else {
				current = current.append(" ").append(befehl[i]);
			}
			if (befehl[i].startsWith("\"")) {
				quotation = true;
			}
			if (quotation) {
				if (befehl[i].endsWith("\"")) {
					als.add(current.toString().replace("\"", ""));
					current = null;
					quotation = false;
				} else {
					// ein kleines ZwischenstÃ¼ck ... zb. "der" oder "tausend" bei "Hain der tausend Eichen" 
					// braucht nix gemacht werden
				}
			} else {
				als.add(current.toString());
				current = null;
			}
		}
		if (quotation) {
			// nicht geschlossenes " !
			als.add(current.toString().replace("\"", ""));
		}

		String[] b = new String[als.size()];
		for (int i = 0; i < als.size(); i++) {
			b[i] = als.get(i);
		}
		return b;
	}

	/**
	 * DAS ist die Auswertung der Auswertungen
	 */
/*	public static void ZAT(boolean debug) {
		// alte Meldungen löschen
		Message.DeleteAll();

		// Systemstart vermerken
		new SysMsg("SYSTEM Neustart - ZAT-Verarbeitung");

		// GameRules laden
		GameRules.Load();

		// frisches Blut braucht das Land
		new NeueSpieler();

		// die Auswertung
		new CleanUp();				// TEST ... ok
		if (!debug) {
			new BefehleEinlesen();		// TEST ... ok
		} else {
			for (Unit u : Unit.LoadAlle()) {
				if (u.getLongOrder().length() > 0) {
					u.Befehle.add(u.getLongOrder());
					u.setLongOrder("");
				}
			}
			Datenbank.Update("UPDATE einheiten SET longorder = ''");
		}

		// man(n) muss ja die Spieler gängeln
		new Monsterplanung();

		// new ZaubereFS();			// FirstSpell Zaubersprüche abarbeiten

		// -- Kriegsvorbereitungen
		new TempEinheiten();		// TEST ... ok
		new Kampfpositionen();		// TEST ... ok
		new Kriege();				// TEST ... ok

		// -- kurze Befehle
		new NMRs();					// TEST ... ok
		new SpielerLoeschen(true);	// TEST ... ok

		new Passwort();				// TEST ... ok
		new Cheater();				// TEST ... ok
		new Adressen();				// TEST ... ok
		new Website();
		new Kampfzauber();
		new Beschreibungen();		// TEST ... ok
		new Benennungen();			// TEST ... ok
		new BewacheAUS();			// TEST ... ok
		new Ursprung();				// TEST ... ok
		new Zeige();				// TEST ... nicht getestet
		new Kontaktierungen();		// TEST ... ok
		// DEFERRED SORTIERE
		new Allianzen();			// TEST ... ok
		new Gib();					// TEST ... ok
		new Liefere();				// TEST ... ok
		new Nummer();				// TEST ... ok
		new Tarnen();				// TEST ... ok
		new Prefix();				// TEST ... ok
		new Steuerrate();			// TEST ... nicht getestet
		// DEFERRED BENUTZE
		// DEFERRED diverse Tränke wirken jetzt

		// vor dem Rekrutieren sollen alle Einheiten im Proxy sein - Check auf zu viele Magier.
		SortedSet<Unit> alle = Unit.LoadAlle();
		new Rekrutieren();			// TEST ... ok
		// DEFERRED NMR's testen
		new Stirb();				// TEST ... ok
		new SpielerLoeschen();		// TEST ... ok
		new Betreten();				// TEST ... ok
		new GibKommando();			// TEST ... ok
		new Verlasse();				// TEST ... ok
		new GebaeudeUnterhalt();	// TEST ... ok

		// -- lange Befehle
		new Zaubern();
		new Zerstoere();			// TEST ... ok ... keine Strassen, Schiffe
		// DEFERRED SPIONIERE
		new Lehren();				// TEST ... ok
		new Lernen();				// TEST ... ok
		new Belagerung();			// TEST ... nicht getestet
		new Produktion();			// TEST ... ok
		new BauernArbeiten();		// TEST ... ok
		new Unterhalten();			// TEST ... ok
		new Handeln();				// TEST ... ok
		new SteuernErpressen();		// TEST ... ok
		new Steuertuerme();			// TEST ... nicht getestet
		new Diebstahl();			// TEST ... ok
		new Bewegungen();			// TEST ... ok
		new BewegungenRoute();
		// DEFERRED ROUTE
		// DEFERRED FOLGE
		new BewacheAN();			// TEST ... ok
		// DEFERRED Schiffe treiben

		new DurchreiseMeldungen();

		new Environment();			// TEST ... ok
		new SpielerLoeschen();		// TEST ... ok

		new DefaultBefehl();		// alle Einheiten ohne Befehl bekommen pauschal "FAULENZEN"

		// nur noch die ReporteSchreiben schreiben
		new ReporteSchreiben();

		// GameRules speichern (weil Runde oder so verändert wurde)
		GameRules.setRunde(GameRules.getRunde() + 1);
		GameRules.Save();

		// Systemende vermerken ... quasi nette SMS an mich
		new SysMsg("SYSTEM Quit - ZAT erfolgreich");

		// letzmaliges Aufräumen der Verweise und ein kleines Update für
		// die Datenbank-Statistik
		Datenbank.CleanUp();

		Main.listUsedFlags();
	}
*/
	/**
	 * <p>Sucht alle Einheiten mit einem speziellen Befehl aus einer Liste der Einheiten-Nummern.</p>
	 * <p>Grund fÃ¼r die Verwendung von Nummern statt Unit-Objekten: So mÃ¼ssen nicht alle Einheiten
	 * gleichzeitig in einer Liste und damit im Speicher gehalten werden. Einheiten, die nicht passen,
	 * kÃ¶nnen wieder verworfen werden.
	 * @param alleEinheitenNummern List mit dezimalen Einheiten-Nummern
	 * @param befehl Der gewÃ¼nschte Befehl, nur das SchlÃ¼sselwort angeben!
	 * @return List der Einheiten, die den gegebenen Befehl enthalten
	 */
	public static List<Einzelbefehl> getEinzelbefehle(List<Integer> einheitenNummern, String befehl) {
		new Info("Suche nach einzelnen Befehlen: " + befehl, Partei.getPartei(0));
		befehl = befehl.toLowerCase();
		List<Einzelbefehl> retval = new ArrayList<Einzelbefehl>();
		for (int nummer : einheitenNummern) {
			Unit u = Unit.Load(nummer);
			for (int i = 0; i < u.Befehle.size(); i++) {
				String b = u.Befehle.get(i);
				if (b.toLowerCase().startsWith(befehl)) {
					// gotcha!
					new Info("... und habe einen gefunden: " + u + ": " + b + " (Rang " + i + ")", Partei.getPartei(0));
					Einzelbefehl eb = new Einzelbefehl(u, u.getCoords(), b, i);
					retval.add(eb);
					break;
				}
			}
		}
		return retval;
	}
}
