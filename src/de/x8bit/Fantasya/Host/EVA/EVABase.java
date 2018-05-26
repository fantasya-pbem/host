package de.x8bit.Fantasya.Host.EVA;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Ship;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.Debug;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.SysErr;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.Atlantis.Messages.ZATMsg;
import de.x8bit.Fantasya.Atlantis.NamedItem;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.Datenbank;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.Main;
import de.x8bit.Fantasya.Host.NeueSpieler;
import de.x8bit.Fantasya.Host.Paket;
import de.x8bit.Fantasya.Host.EVA.util.BefehleKlassifizieren;
import de.x8bit.Fantasya.Host.EVA.util.EVAFastLoader;
import de.x8bit.Fantasya.Host.EVA.util.EVAFastSaver;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.ZATMode;
import de.x8bit.Fantasya.util.ComplexName;
import de.x8bit.Fantasya.util.comparator.UnitSortierungComparator;
import fantasya.library.io.order.DirectoryOrderReader;

import java.util.Collections;
import java.util.HashSet;

abstract public class EVABase {

	/**
	 * Konstante für die Definition von Befehlsmuster - Varianten: wenn deren
	 * Code für Art diesen Wert als binäres Flag enthält, dann ist eine TEMP-
	 * Einheit involviert.
	 */
	public static final int TEMP = 1024;

	/**
	 * <h3><font color="red">der Konstruktor ist für vorwiegend Allgemeines zuständig</font></h3>
	 * <br><br> 
	 * der einfache Konstruktor ... hier wird einfach nur das System aufgeräumt (vom
	 * letzten ZAT-Schritt) und der GC von Hand gestartet
	 * <b>hier wird kein PostAction bzw. PreAction aufgerufen, selber machen !!</b>
	 * @param message - Meldung für das Logging zu diesem Schritt
	 */
	//private static Unit debug = Unit.Load(Atlantis.toBase10("qo"));
	public EVABase(String message) {
		if (!ZATMode.CurrentMode().getSkip(this.getClass())) {
			if ((message != null) && message.length() > 0) {
				new ZATMsg(message);
			}

			PreAction();
			PostAction();

		} else {
			new ZATMsg("Überspringe " + this.getClass().getSimpleName() + " (1 Param.)");
		}
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
	public EVABase(String befehl, String message) {
		if (!ZATMode.CurrentMode().getSkip(this.getClass())) {
			Set<Coords> regionen = BefehlsSpeicher.getInstance().getCoords(this.getClass());

			if (message != null) new ZATMsg(message + " in " + regionen.size() + " Regionen.");

			PreAction();

			// Falls mal alle Befehle via "Einzelbefehl" gelöst sind, kann dieser Aufruf generell erfolgen-
			// die Klassen müssen dann selbst entscheiden, ob und was sie basierend auf Regionen oder Einheiten macht.
			if ((this.getClass() == NachUndRoute.class)
					|| (this.getClass() == Folgen.class)
					|| (this.getClass() == SammelBeute.class)
					|| (this.getClass() == BefehleBestaetigen.class)
					|| (this.getClass() == Vergessen.class)) {
				for (Einzelbefehl eb : BefehlsSpeicher.getInstance().getAll(this.getClass())) {
					this.DoAction(eb);
				}

			} else {

				// der klassische Normalfall:
				for (Coords c : regionen) {
					if (c.getWelt() == 0) {
						continue; // virtuelle Einheiten machen nix! (t, te, tem ...)
					}					// new SysMsg("Region " + Region.Load(c) + "...");
					DoAction(Region.Load(c), befehl);
				}

			}

			PostAction();

		} else {
			new ZATMsg("Überspringe " + this.getClass().getSimpleName() + " (2 Param.)");
		}
	}

	/**
	 * <h3><font color="red">der Konstruktor ist vorwiegend für Befehle die spezifisch sind für eine Einheit</font></h3>
	 * <br><br> 
	 * führt ein suchen nach den Einheiten aus welche den Befehl ausführen wollen
	 * @param befehl - der Befehl soll ausgeführt werden
	 * @param message - Meldung für das Logging zu diesem Schritt
	 * @param minSize - mind. Anzahl der Befehlsteile (MACHE HOLZ -> 2, GIB einheit Holz -> 3, etc.) 
	 */
	public EVABase(String befehl, String message, int minSize) {
		if (!ZATMode.CurrentMode().getSkip(this.getClass())) {
			new ZATMsg(message);

			PreAction();

			List<Unit> units = getUnits4Command(befehl);

			// damit wird sichergestellt, dass die Befehle in der Reihenfolge der
			// Sortierung durch die Besitzer (oder implizit durch die Reihenfolge
			// des Erscheinens der Einheit in der Region) abgearbeitet werden
			Collections.sort(units, new UnitSortierungComparator());

			for (Unit unit : units) {
				Action(unit, befehl.toLowerCase(), minSize);
			}

			PostAction();
		} else {
			new ZATMsg("Überspringe " + this.getClass().getSimpleName() + " (3 Param.)");
		}
	}

	/**
	 * löscht alle Proxy's
	 */
	public static void ClearProxy() {
		// TODO mit EVA sollte das aber funktionieren, oder?
		// wenn die Proxy's nicht gelöscht werden, gibt es durchaus disharmonien beim Bewegen ... die Einheiten
		// sind noch in der Region selber gelistet, sind dort aber nicht mehr ... und in der neuen Region
		// sind sie noch nicht gelistet
		if (Main.getBFlag("EVA")) {
			ClearProxy(false);
			return;
		}

		ClearProxy(true);
	}

	/**
	 * lÃ¶scht alle Proxys
	 * @param clear - TRUE wenn die Proxys gelÃ¶scht werden sollen, sonst wird nur in die DB gespeichert
	 */
	public static void ClearProxy(boolean clear) {
		if (!Datenbank.isEnabled()) {
			return; // FLAG EVA
		}
		// Parteien lÃ¶schen
		if (clear) {
			Partei.PROXY.clear();
		}

		// Schiffe lÃ¶schen
		if (clear) {
			Ship.PROXY.clear();
		}

		// Gebäude löschen
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
	 * die absolute Verwaltung der Befehlsausführung ... hier werden die Ganzen Einheiten
	 * dazu aufgefordert den Befehl auszuführen ... DoAction() leitet dazu an den jeweiligen
	 * ZAT-Schritt zurück um dort dann verarbeitet zu werden
	 * @param unit - Einheit für die der Befehl ausgefÃ¼hrt werden soll
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

				// if (u.getLongOrder().length() == 0) {
				if (true) {
					// es darf ein langer Befehl ausgeführt werden:
					if (DoAction(u, b)) {
						// true -> Befehl muss übernommen werden ... z.B. LIEFERE-Befehl
						// oder PERMANENT in der Zeile
						befehle.add(line);
					}
					// LongOrder wird/muss von den Kindern gesetzt (werden)
//                } else {
//                    new Fehler("'" + line + "' - " + u + " hat bereits einen langen Befehl.", u, u.getCoords());
				}
			} else {
				// Befehlszeile einfach übernehmen ... da das gerade
				// nicht der aktuelle Befehl ist
				befehle.add(line);
			}

		} // next line

		// u.Befehle = befehle;
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
	 * <p>hier wird der ganze Kram für den ZAT-Schitt ausgeführt.</p>
	 * <p>Sollte normalerweise mit der gesamten Liste der Einzelbefehle für den
	 * jeweiligen ZAT-Schritt (&quot;Prozessor&quot;) aufgerufen werden.</p>
	 * @param eb der auszuführende Befehl
	 */
	abstract public void DoAction(Einzelbefehl eb);

	/**
	 * wird ausgeführt <b>bevor</b> die Befehle für die Einheiten abgearbeitet werden ... kann also
	 * benutzt werden um Tabellen zu löschen und andere Operationen durchzuführen
	 */
	abstract public void PreAction();

	/**
	 * wird ausgeführt <b>nachdem</b> die Befehle für die Einheiten abgearbeitet wurden ... kann also
	 * benutzt werden um Tabellen zu löschen und andere Operationen durchzuführen
	 */
	abstract public void PostAction();

	/**
	 * @param paket
	 * @return Alle Namen, die der Klasse des paket zugeordnet sind.
	 * @see ComplexName
	 */
	public static Set<String> getNames(Paket paket) {
		Set<String> retval = new HashSet<String>();
		if (paket.Klasse instanceof NamedItem) {
			NamedItem item = (NamedItem) paket.Klasse;
			for (String variante : item.getComplexName().getAliases()) {
				retval.add(variante.toLowerCase());
			}
		} else {
			retval.add(paket.ClassName.toLowerCase());
		}

		return retval;
	}
	protected static Writer template = null;

	protected static void addTemplate(String tpl, Paket paket) {
		addTemplate(tpl, paket, null);
	}

	protected static void addTemplate(String tpl, Paket paket, String additions) {
		if (additions != null) {
			additions = " " + additions;
		} else {
			additions = "";
		}
		if (paket.Klasse instanceof Item) {
			for (String name : getNames(paket)) {
				addTemplate(tpl + " (" + name + ")" + additions);
			}
		} else {
			addTemplate(tpl + " (" + paket.ClassName.toLowerCase() + ")" + additions);
		}
	}

	protected static void addTemplate(String tpl) {
		try {
			if (template == null) {
				File file = new File("template2.tpl");

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

	// TODO eliminieren - das erledigt Einzelbefehl: Einzelbefehl.getBefehlCanonical()
	protected String ConcatinateCommand(String[] befehl) {
		StringBuilder sb = new StringBuilder(befehl[0]);
		for (int i = 1; i < befehl.length; i++) {
			sb = sb.append(" ").append(befehl[i]);
		}
		return sb.toString();
	}

	// TODO eliminieren - das erledigt Einzelbefehl für sich selbst.
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
			quotation = false; // Ordnung muss sein. (?)
			current = null;
		}

		String[] b = new String[als.size()];
		for (int i = 0; i < als.size(); i++) {
			b[i] = als.get(i);
		}
		return b;
	}

	/**
	 * liefert alle Einheiten, deren Befehle mit <i>command</i> beginnt 
	 * @param command - diesen Befehl soll einen Befehl ausführen
	 * @return alle Einheiten mit den Befehlen
	 * @deprecated
	 */
	protected List<Unit> getUnits4Command(String command) {
		List<Unit> units = new ArrayList<Unit>();

		for (Unit unit : Unit.CACHE) {
			for (String befehl : unit.Befehle) {
				if (befehl.toLowerCase().startsWith(command.toLowerCase())) {
					units.add(unit);	// merken
					break;				// nächste Einheit - kann ja sein das eine Einheit mehr als eine TEMP-Einheit macht
				}
			}
		}

		return units;
	}

	/**
	 * DAS ist die Auswertung der Auswertungen
	 */
	public static void ZAT() {
		new SysMsg("SYSTEM Neustart - ZAT-Verarbeitung");

		if (Main.getBFlag("IMAP")) {
			ZATMode.CurrentMode().setImapAbrufen(true);
		}

		new SysMsg("Modus:\n" + ZATMode.CurrentMode().toString());

		GameRules.Load();

		// loadAll();	// Deaktiviert automatisch die Datenbank
		loadAllEx();

		new VolksZaehlung(VolksZaehlung.MODUS_T0); // Zählung vorher
		new AtlantenLesen();

		// die Auswertung
		new CleanUp();				// TEST ... ok
		Datenbank.Disable();

		// Monster-Parteien können die alten Meldungen noch auswerten:
		new MonsterMeldungenAuswerten();

		// Jetzt werden die Meldungen der Vorrunde gelöscht
		Message.DeleteAll();
		// TODO: Fehlermeldungen wegen ungültiger Befehle *aus der DB* kamen schon,
		// und sollten erhalten bleiben...

		new NeueSpieler();
		CleanUp.RegionsChecks(); // prüft vor allem die Luxus-Güter
		CleanUp.SanityChecks(); // prüft die Spielobjekt-Caches / PROXYs

		// das ist jetzt unabhängig von args_debug !
		if (ZATMode.CurrentMode().isBefehleLesen()) {
			Datenbank.Enable();
			// new DirectoryOrderReader("befehle").readOrderFiles();
			new BefehleEinlesen();	// TEST ... ok
			Datenbank.Disable();
		}

		// man(n) muss ja die Spieler gängeln
		new Monsterplanung();

		new BefehleKlassifizieren();


		// new ZaubereFS();			// FirstSpell Zaubersprüche abarbeiten

		new TempEinheiten();		// TEST ...
		if (ZATMode.CurrentMode().isDebug()) {
			BefehlsSpeicher.getInstance().toFile("BefehlsSpeicher-DUMP-pre.txt"); // nett zum Debuggen, auch zwischen ZAT-Phasen...
		}
		new Kommentare();           // TEST ... okay
		new Vergessen();

		// -- Kriegsvorbereitungen
		new Allianzen();			// TEST ...
		new Kampfpositionen();		// TEST ...
		new Kampfzauber();
		new SammelBeute();
		new Kriege();				// TEST ...

		// -- kurze Befehle
		new NMRs();					// TEST ...
		new SpielerLoeschen(true);	// TEST ...

		new Passwort();				// TEST ...
		new Cheater();				// TEST ...
//		new Adressen();				// TODO -- Befehl löschen -> Änderung erfolgt nur noch über Webseite
		new Website();
		new Beschreibungen();		// TEST ...
		new Benennungen();			// TEST ...
		new BewacheAUS();			// TEST ...
		new Ursprung();				// TEST ...
		new Zeige();				// TEST ... nicht getestet
		new Kontaktierungen();		// TEST ...
		new Sortieren();			// TEST ...
		new Gib(); // inkl. Liefere // TEST ...
		new Nummer();				// TEST ...
		new Tarnen();				// TEST ...
		new Prefix();				// TEST ...
		new Steuerrate();			// TEST ...
//		// DEFERRED BENUTZE
//		// DEFERRED diverse Tränke wirken jetzt
		new Rekrutieren();			// TEST ...
		new Stirb();				// TEST ...
		new SpielerLoeschen(false); // TEST ...
		new Betreten();				// TEST ...
		new GibKommando();			// TEST ...
		new Verlasse();				// TEST ...
		new GebaeudeUnterhalt();	// TEST ...
//		
//		// -- lange Befehle
		new Zaubern();
		new Zerstoere();
 		new Spionieren(); // -> has a bug, if unit is unknown for spy
		new Lehren();
		new Lernen();
		new Belagerung();
		new Produktion();
		new BauernArbeiten();
		new Unterhalten();
		new Handeln();
		new SteuernErpressen();
		new Steuertuerme();
		new Diebstahl();

		new NachUndRoute();
		new Folgen();
		// Regionen aus den "known regions" entfernen, in denen jetzt keine Einheiten mehr sind:
		new VerlasseneRegionenWerdenUnsichtbar();

		new Leuchttuerme();
		new BewacheAN();

		new SchiffeTreiben();
		new DurchreiseMeldungen();

		new Botschaften();          // damit können auch Begrüßungsbotschaften an Neuankömmlinge geschickt werden.

		new Environment();			// TEST ...
		new SpielerLoeschen(false);	// TEST ...

		CleanUp.RegelChecks(); // prüft z.B. Magier- und Migranten-Limit, im Zweifelsfall gibt es SysErrs.
		CleanUp.SanityChecks(); // prüft die Spielobjekt-Caches / PROXYs

		new BefehleBestaetigen();
		new DefaultBefehl();		// alle Einheiten ohne Befehl bekommen pauschal "FAULENZEN"

		// prüfen, ob auch alle Befehle tatsächlich ausgeführt wurden - sonst: Da ist was oberfaul!
		if (ZATMode.CurrentMode().isDebug()) {
			BefehlsSpeicher.getInstance().toFile("BefehlsSpeicher-DUMP.txt");
		}
		new BefehlsFazit();
		new VolksZaehlung(VolksZaehlung.MODUS_T1); // Zählung nachher
		new Kartografieren(); // damit die neu geschriebenen Regions-Properties noch in die DB kommen - ansonsten nur Vorbereitung für die Reporte
		new SkirmishPoints();		// Punkte für Skirmish berechnen

		// new Statistiken(); // noch nicht reif zum Einbau 2010-11-12

		// saveAll();	// Aktiviert automatisch die Datenbank
		if (!ZATMode.CurrentMode().isWorldReadOnly()) {
			saveAllEx(); // Aktiviert automatisch die Datenbank
		} else {
			new ZATMsg("ACHTUNG: Read-only Modus, es wurde nichts gespeichert!");
		}
		Datenbank.Disable();

		new BefehlsCheckMeldungen(); // wird im normalen ZAT übersprungen.
		
		// old turn done, next turn now
		GameRules.setRunde(GameRules.getRunde() + 1);
		
		// nur noch die Reporte schreiben
		new Reporte();

		// GameRules speichern (weil Runde oder so verändert wurde)
		Datenbank.Enable();
		if (!ZATMode.CurrentMode().isWorldReadOnly()) {
			GameRules.setRunde(GameRules.getRunde()); // only save turn
			new Debug("Erhöhe Runde auf " + GameRules.getRunde() + ".");
			GameRules.Save();
		} else {
			new Debug("Erhöhe die Runde(" + GameRules.getRunde() + ") NICHT.");
		}

		// Systemende vermerken ... quasi nette SMS an mich
		new SysMsg("SYSTEM Quit - ZAT erfolgreich");

		// letzmaliges Aufräumen der Verweise und ein kleines Update für
		// die Datenbank-Statistik
		Datenbank.CleanUp();

		Main.listUsedFlags();
	}

	public static void loadAllEx() {
		new ZATMsg("lade alle Einheiten und Regionen aus der DB (loadAllEx)");

		try {
			EVAFastLoader.loadAll();
		} catch (SQLException ex) {
			new BigError("EVAFastLoader hat sich verladen: " + ex.getMessage());
		}
	}

	public static void saveAllEx() {
		try {
			Datenbank.Enable();
			EVAFastSaver.saveAll(false);
			Message.SaveAll();
		} catch (SQLException ex) {
			// TODO: hier sollte ich etwas defensiver vorgehen, immerhin 
			//  geht es im Zweifelsfall um Datenverlust
			new BigError(ex);
		}
	}
}
