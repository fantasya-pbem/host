package de.x8bit.Fantasya.Host;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Stack;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Welt;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.SysErr;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.Host.EVA.util.EVAFastSaver;
import de.x8bit.Fantasya.util.Codierung;
import de.x8bit.Fantasya.util.StringUtils;

// ALTER TABLE tabelle_blah_blah AUTO_INCREMENT = 1

/**
 * <b>die Verbindung zur Datenbank</b><br><br>
 * am einfachsten ist es eine Instanz zu erstellen und den
 * Query direkt an myQuery zu übergeben .... anschließend
 * nur noch Select() oder Insert() und gut ist<br>
 * <code>
 * <br>Datenbank db = new Datenbank("Demo");
 * <br>db.myQuery = "SELECT * FROM settings";
 * <br>ResultSet rs = db.Select();
 * </code><br><br>
 * für komplexe SQL-Statements sind wohl eher die AddXXX-Funktionen
 * zu empfehlen<br>
 * <code>
 * <br>Datenbank db = new Datenbank("Demo");
 * <br>db.CreateSelect("Tabelle", "t1");
 * <br>db.AddFirstWhere("id", 3);
 * <br>ResultSet rs = db.Select();
 * </code><br><br>
 */
public class Datenbank
{
	/** hält die Verbindung "fest" */
	private static Connection	connection	= null;

	/** das Statement für folgende Aktionen */
	private Statement statement = null;

	/** das aktuell verwendete ResultSet */
	private ResultSet resultset = null;

	/** Name des Verweisen zur besseren Verweiszählung / Debug */
	private String VerweisName = "no name";

	/** Sammlung aller Verweise */
	private static Stack<Datenbank> Verweise = new Stack<Datenbank>();



	/** Anzahl der Datenbank Aktionen */
	private static int countQuery = 0;


	private static boolean enabled = true;
	public static boolean isEnabled() { return enabled; }

	public static void Disable()
	{
		if (Main.getBFlag("EVA"))
		{
			new SysMsg("--> deaktiviere die Verwendung der Datenbank");
			enabled = false;
		} else
		{
			new BigError("Deaktivierung der Datenbank nur bei EVA-ZAT (Option -flags) !!");
		}
	}

	public static void Enable()
	{
		if (Main.getBFlag("EVA"))
		{
			new SysMsg("--> aktiviere die Verwendung der Datenbank");
			enabled = true;
		} else
		{
			new BigError("Aktivierung der Datenbank nur bei EVA-ZAT (Option -flags) !!");
		}
	}

	/**
	 * bei jedem öffnen wird ein Statement erzeugt ... zusätzlich wird eine Verweiszählung
	 * geführt ... welche zum Aufräumen mittels <i>CleanUp()</i> erzwungen werden kann
	 * @see #CleanUp()
	 */
	public Datenbank(String verweisname)
	{
		if (Main.getBFlag("EVA") && !enabled) new BigError(new RuntimeException("Die Datenbank ist nicht aktiv geschaltet - !! EVA-ZAT ist aktiviert (Option -flags) !!"));
		if (connection == null) OpenDB();
		if (IsOpen()) try { statement = connection.createStatement(); } catch(Exception e) { System.out.println("[ERR] Fehler beim erzeugen eines Statements - " + e); }
		VerweisName = verweisname;
		Verweise.push(this);
	}

	/** der Name der Datenbank (default db_fantasya) */
	private static String	Datenbank	= "fantasya";
	/** IP:Port des Datenbank-Servers (nur MySQL, default 127.0.0.1:3306) */
	private static String	Server		= "127.0.0.1:3306";
	/** Name des Benutzers (default fantasya) */
	private static String	Benutzer	= "fantasya";
	/** Passwort des Benutzers (default fantasya) */
	private static String	Passwort	= "fantasya";

	/** setzt den Datenbank-Namen */
	public static void SetDatenbank(String value) { Datenbank = value; System.out.println(" - setze Datenbank auf '" + value + "'"); }
	public static String GetDatenbank() { return Datenbank; }

	/** setzt ServerIP:Port für den Datenbank-Server */
	public static void SetServer(String value) { Server = value; System.out.println(" - setze Server auf '" + value + "'"); }
	/** setzt den Benutzer */
	public static void SetBenutzer(String value) { Benutzer = value; System.out.println(" - setze Benutzer auf '" + value + "'"); }
	/** setztd as Passwort für den Benutzer */
	public static void SetPasswort(String value) { Passwort = value; System.out.println(" - setze Passwort auf 'xHJkdk6756&'"); }

	/** setzt ServerIP:Port für den Datenbank-Server */
	public static String GetServer() { return Server; }
	/** setzt den Benutzer */
	public static String GetBenutzer() { return Benutzer; }
	/** setztd as Passwort für den Benutzer */
	public static String GetPasswort() { return Passwort; }

	/** TRUE wenn es das Beta-Spiel ist */
	public static boolean isBetaGame() { return Datenbank.contains("3d34f4a6"); }

	/** liefert true wenn die DB geöffnet wurde */
	public static boolean IsOpen()
	{
		return (connection != null);
	}

	/**
	 * öffnet die Datenbank
	 * @return true - wenn erfolgreich
	 */
	private static boolean OpenDB()
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e)
		{
			System.out.println("[ERR] Keine Treiber-Klasse!");
			System.out.println(e.getMessage());
			return false;
		}

		try
		{
			String url = "jdbc:mysql://" + Server + "/" + Datenbank;
            url += "?connectionCollation=utf8_general_ci"; // eingefügt von hapebe - UTF-8 Unterstützung vorbereiten
			System.out.println("Verbindung zur DB via '" + url + "'");
			connection = DriverManager.getConnection(url, Benutzer, Passwort);
			System.out.println(" - Verbindung hergestellt");
		} catch (SQLException e) {
			System.out.println("[ERR] keine Verbindung zur Datenbank: " + e.getMessage());
			return false;
		}

		return true;
	}

	/**
	 * schließt die Datenbank wieder - Hinweis falls die DB schon geschlossen bzw. nie offen war
	 * <b>nur Aufrufen wenn die DB auch wirklich geschlossen werden soll!</b> ... um das Statement
	 * zu schließen ist <i>Close()</i> zu verwenden!
	 */
	public static void CloseDB()
	{
		if (connection != null)
		{
			System.out.println("schließe Verbindung zur Datenbank");
			try
			{
				connection.close();
			} catch (SQLException e)
			{
				e.printStackTrace();
			}
		} else
		{
			System.out.println("Datenbank ist bereits geschlossen");
		}
	}

	/**
	 * Aufräumen von verweisten Verweisen ... also ein schließen aller Statements erzwingen
	 */
	public static void CleanUp()
	{
		if (!enabled) return; // wenn nichts verwendet werden darf, dann existiert hier auch nix weiter

		boolean first = true;
		while(!Verweise.empty())
		{
			if (first) { System.out.println("schließe Datenbankverweise"); first = false; }
			Datenbank db = Verweise.pop();
			db.Close();
			new SysErr("Datenbank noch offen - Verweisname: " + db.VerweisName);
		}

		Datenbank db = new Datenbank("Statistik");
		db.SaveSettings("datenbank.querycount", countQuery + 1);
		db.Close();
	}

	/**
	 * listet alle Query aller noch laufenden DB-Instanzen
	 */
	public static void ListQueryStack()
	{
		while(!Verweise.empty())
		{
			Datenbank db = Verweise.pop();
			System.err.println(db.myQuery);
		}
	}

	/**
	 * schließt das Statement und das ResultSet ... muss vor jedem
	 * verlassen des Sichtbarkeitsbereiches aufgerufen werden
	 */
	public void Close()
	{
		if (resultset != null) try { resultset.close(); } catch(Exception e) { System.out.println("[ERR] Fehler beim schließen des ResultSet's"); }
		if (statement != null) try { statement.close(); } catch(Exception e) { System.out.println("[ERR] Fehler beim schließen des Statement's"); }
		Verweise.remove(this);
	}

	/**
	 * testet die Verbindung zur Datenbank
	 */
	public void CheckDB()
	{
		System.out.println("teste private Verbindung zur Datenbank");
		if (IsOpen())
		{
			System.out.println(" - öffne Tabelle Settings und lese Runde aus");
			try
			{
				int runde = this.ReadSettings(GameRules.GAME_RUNDE, -1);
				if (runde != -1)
				{
					System.out.println(" - aktuelle Spielrunde ist Nummer " + runde);
					System.out.println("Datenbank-Test war erfolgreich");
				} else
				{
					System.out.println("[ERR] Datenbank ist wahrscheinlich nicht initialisiert (verwende -initdb)");
				}
			} catch(Exception e)
			{
				e.printStackTrace();
			}
		} else
		{
			System.out.println("Fehler beim öffnen der Datenbank (siehe vorherige Fehlermeldung[en])");
		}
	}


	/*
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 *
	 *  Initialisierung der Datenbank
	 *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 */


	/** initialisiert die Datenbank */
	public void InitDB(String version)
	{
		System.out.println("initialisiere die Datenbank");
		if (IsOpen()) {
			TruncateAll(); // alle Spielobjekte löschen

			GameRules.Init(); 	// setzt Spielstatus-Einträge (Runde, Inselkennung etc.) auf die Defaults
			GameRules.Load();

			InitDB_Welt();		// erste Welt erzeugen
			InitDB_Einheiten();	// diverse Einheiten

			try {
				EVAFastSaver.saveAll(false);
			} catch (SQLException ex) {
				new BigError(ex);
			}
		} else {
			System.out.println("Verbindung zur Datenbank ist fehlerhaft (bitte -checkdb verwenden).");
		}
	}

	/**
	 * verschiedene Einheiten-Nummern erzeugen Probleme
	 * t / te / tem / temp ... das passt alles zu temp
	 */
	private void InitDB_Einheiten()
	{
		String nummern[] = { "t", "te", "tem", "temp", "b", "ba", "bau", "baue" };

		for(int i = 0; i < nummern.length; i++)	{
			Unit u = Unit.CreateUnit("Mensch", 0, new Coords(0, 0, 0));

			Unit.CACHE.remove(u);
			u.setNummer(Codierung.fromBase36(nummern[i]));
			Unit.CACHE.add(u);

			u.setName("Nummern-Einheit für [" + nummern[i] + "]");
			u.setPersonen(1);
		}
	}

	public void DisableKeys(String tableName) {
		Datenbank db = new Datenbank("DB.DisableKeys");
		db.myQuery = "ALTER TABLE " + tableName + " DISABLE KEYS;";
		db.Update();
		db.Close();
	}

	public void EnableKeys(String tableName) {
		Datenbank db = new Datenbank("DB.EnableKeys");
		db.myQuery = "ALTER TABLE " + tableName + " ENABLE KEYS;";
		db.Update();
		db.Close();
	}

	public void FlushTables() {
		myQuery = "FLUSH TABLES;";
		this.Update();
	}

	public void Truncate(String tableName) {
		Datenbank db = new Datenbank("DB.Truncate");
		db.myQuery = "TRUNCATE TABLE " + tableName + ";";
		db.Update();
		db.Close();
	}

	/**
	 * Leert alle Tabellen in der Datenbank - also mit Vorsicht einsetzen!
	 * Lässt dabei die Struktur unverändert.
	 */
	public void TruncateAll()
	{
		try {
			Datenbank db2 = new Datenbank(" - leere alle Tabellen");

			myQuery = "SHOW tables;";
			ResultSet rs = Select();
			while (rs.next()) {
				String tableName = rs.getString(1);

				// ah, verstehe - die "Version". Wird sie wieder zurückgesetzt, schlagen
				// die SQL-Struktur-Updates fehl, weil sie ja dann doch schon ausgeführt wurden.
				// Ausweg in Zukunft: bei -testworld alle Tabellen tatsächlich DROPpen, so
				// dass die Struktur-Updates auch wieder passen.
				// hapebe 2010-09-03
				if (tableName.equals("settings")) continue; // Settings nicht leeren

				db2.myQuery = "TRUNCATE TABLE " + tableName + ";";
				@SuppressWarnings("unused")
				int result = db2.Update();
			}
			db2.Close();
		} catch (Exception ex) {
			new SysErr(ex.toString());
		}

		// Settings wird nicht geleert aber die Runde wird dadurch hoch gezählt ... einfach wieder
		// eine abziehen
		GameRules.setRunde(GameRules.getRunde() - 1);
	}

	/**
	 * erzeugt die erste Welt (inkl. Unterwelt) und Regionen
	 */
	private void InitDB_Welt()
	{
		Welt.Create(1, "mogel@x8bit.de", -2, 2);
		// Welt.Create(-1, "mogel@x8bit.de", -2, 2); -- erstmal ohne Unterwelt

		if ("April2011".equalsIgnoreCase(GameRules.GetOption(GameRules.NEUE_INSEL_METHODE))) {
			Welt.NeueRegionen(1);
		} else {
			// klassisch:
			// pauschal einige Regionen erstellen
			Welt.MakeBlock(0, 01);		// Inselkennung #1
			Welt.MakeBlock(0, 7);		// Inselkennung #2
			Welt.MakeBlock(0, -7);		// Inselkennung #3
			Welt.MakeBlock(7, 0);		// Inselkennung #4
			Welt.MakeBlock(-7, 0);		// Inselkennung #5
			Welt.MakeBlock(7, 7);		// Inselkennung #6
			Welt.MakeBlock(-7, 7);		// Inselkennung #7
			Welt.MakeBlock(7, -7);		// Inselkennung #8
			Welt.MakeBlock(-7, -7);		// Inselkennung #9
		}
	}

	/*
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 *
	 *  lesen und schreiben von Einstellungen
	 *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 */

	/**
	 * jede DB-Verbindung kann zum Einlesen von Einstellungen verwendet werden
	 * @param name - Name der Einstellung
	 * @param value - Defaultwert
	 * @return Wert der Einstellung (im Fehlerfall der Default-Wert)
	 */
	public int ReadSettings(String name, int value)
	{
		myQuery = "SELECT value FROM settings WHERE name = '" + name + "'";
		ResultSet rs = Select();
		try
		{
			if (rs.next())
			{
				value = Integer.parseInt(rs.getString("value"));
			} else
			{
				CreateSettings(name, value);
			}
		} catch(Exception e) { new SysMsg("Fehler beim Lesen von Settings '" + name + "'"); }
		return value;
	}

	/**
	 * jede DB-Verbindung kann zum Einlesen von Einstellungen verwendet werden
	 * @param name - Name der Einstellung
	 * @param value - Defaultwert
	 * @return Wert der Einstellung (im Fehlerfall der Default-Wert)
	 */
	public String ReadSettings(String name, String value)
	{
		myQuery = "SELECT value FROM settings WHERE name = '" + name + "'";
		ResultSet rs = Select();
		try
		{
			if (rs.next())
			{
				value = rs.getString("value");
			} else
			{
				CreateSettings(name, value);
			}
		} catch(Exception e) { new SysMsg("Fehler beim Lesen von Settings '" + name + "'"); }
		return value;
	}

	/**
	 * speichert Einstellungen in die Datenbank
	 * @param name - Name der Einstellung
	 * @param value - Wert der Einstellung
	 */
	public void SaveSettings(String name, String value)
	{
		myQuery = "UPDATE settings SET value = '" + value + "' WHERE name = '" + name + "'";
		try { if (Update() == 0) CreateSettings(name, value); } catch(Exception e) { new SysMsg("Fehler beim Schreiben von Settings '" + name + "' - '" + value + "'"); }
	}

	/**
	 * speichert Einstellungen in die Datenbank
	 * @param name - Name der Einstellung
	 * @param value - Wert der Einstellung
	 */
	public void SaveSettings(String name, int value)
	{
		myQuery = "UPDATE settings SET value = " + value + " WHERE name = '" + name + "'";
		try { if (Update() == 0) CreateSettings(name, value); } catch(Exception e) { new SysMsg("Fehler beim Schreiben von Settings '" + name + "' - '" + value + "'"); }
	}

	/**
	 * entfernt Einstellungen aus der Datenbank - hinterher ist kein Record für
	 * den Parameter name mehr vorhanden.
	 * @param name
	 * @return Anzahl der gelöschten DB-Zeilen - sollte also 0 (Fehlschlag bzw. es gab dieses Setting gar nicht) oder 1 sein.
	 */
	public int ClearSettings(String name) {
		myQuery = "DELETE FROM settings WHERE name LIKE '" + name + "'";
		return Delete();
	}

	/**
	 * erzeugt Einstellungen in der Datenbank
	 * @param name - Name der Einstellung
	 * @param value - Wert der Einstellung
	 */
	public void CreateSettings(String name, String value)
	{
		myQuery = "SELECT value FROM settings WHERE name = '" + name + "'";
		ResultSet rs = Select();
		try
		{
			if (rs.next())
			{
				// gibt es schon - NICHTS machen.
				return;
			}
		} catch(Exception e) { new SysMsg("Fehler beim Lesen von Settings '" + name + "'"); }

		CreateInsert("settings", "name, value");
		AddFirstValue(name);
		AddLastValue(value);
		try { Update(); } catch(Exception e) { new SysMsg("Fehler beim Erzeugen von Settings '" + name + "' - '" + value + "'"); }
	}

	/**
	 * erzeugt Einstellungen in der Datenbank
	 * @param name - Name der Einstellung
	 * @param value - Wert der Einstellung
	 */
	public void CreateSettings(String name, int value)
	{
		CreateInsert("settings", "name, value");
		AddFirstValue(name);
		AddLastValue(value);
		try { Update(); } catch(Exception e) { new SysMsg("Fehler beim Erzeugen von Settings '" + name + "' - '" + value + "'"); }
	}

	/*
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 *
	 *  Alles zum Zusammenbau eines SQL-Statements
	 *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 */

    /**
	 * überprüft den Wert für die SQL-Abfrage auf ungültige Zeichen
     * @param value
     * @param insert true, wenn der Wert eingefügt werden soll (Wildcards sind nicht aktiv); false, wenn der Wert als Matching-Parameter dienen soll (Wildcards werden escaped)
     * @return
     */
	public static String CheckValue(String value, boolean insert) {
		StringBuilder sb = new StringBuilder();

		// auf ' prüfen ... werden für Zeichenketten im Statement
		// benötigt ... dürfen somit im Value nicht vorkommen bzw.
		// müssen escaped werden
		for(int i = 0; i < value.length(); i++)
		{
			boolean add = true;
			if (value.charAt(i) == '\'') sb.append('\\');
			// -- ? -- if (value.charAt(i) == '"') sb.append('\\');
			if (value.charAt(i) == '\\') add = false;
			if (!insert) {
                if (value.charAt(i) == '%') sb.append('\\');
                if (value.charAt(i) == '+') sb.append('\\');
            }
			if (add) sb.append(value.charAt(i));
		}

		return sb.toString();
	}

	/**
	 * die Abfrage für die Datenbank ... der Zugriff erfolgt hier pauschal über
	 * public ... da ich nicht an alles denken kann und somit der Query noch
	 * von Hand bearbeitet werden kann
	 */
	public String myQuery = "";

	/**
	 * führt ein Select aus
	 * @return das ResultSet des Query
	 */
	public ResultSet Select()
	{
		// wenn schon offen dann schließen !
		if (resultset != null) try { resultset.close(); } catch(Exception e) {  new SysErr(e.toString()); }

		//System.out.println("query -> " + myQuery);
		try { resultset = statement.executeQuery(myQuery); countQuery++; } catch(Exception e) { new BigError(e); return null; }
		return resultset;
	}

	/**
	 * führt den übergebenen SQL-Query aus und liefert die erste Spalte zurück
	 * @param query - der SQL-Query der ausgeführt werden soll
	 * @param value - Wert der bei einem Fehlschlag zurück gegeben wird
	 * @return das Ergebniss
	 */
	public static int Select(String query, int value)
	{
		Datenbank db = new Datenbank("quick & dirty - select");
		db.myQuery = query;
		if (!IsOpen()) return value;

		ResultSet rs = db.Select();
		try
		{
			if (rs.next()) value = rs.getInt(1);
		} catch(Exception ex) { new BigError(ex.getMessage()); }

		db.Close();

		return value;
	}

	/**
	 * führt den übergebenen SQL-Query aus und liefert die erste Spalte zurück
	 * @param query - der SQL-Query der ausgeführt werden soll
	 * @param value - Wert der bei einem Fehlschlag zurück gegeben wird
	 * @return das Ergebniss
	 */
	public static String Select(String query, String value)
	{
		Datenbank db = new Datenbank("quick & dirty - select");
		db.myQuery = query;
		if (!IsOpen()) return value;

		ResultSet rs = db.Select();
		try
		{
			if (rs.next()) value = rs.getString(1);
		} catch(Exception ex) { new BigError(ex.getMessage()); }

		db.Close();

		return value;
	}

	/**
	 * führt ein Select aus, welches alle Einheiten (nur die Nummern [via rs.getInt("nummer")]) liefert die diesen Befehl verwenden
	 * @param like - ein Befehl mittels Like-Syntax ... 'mache%temp' ... das letzte '%' wird automatisch angehangen -> 'mache%temp%'
	 * @return das ResultSet mit allen Einheiten
	 */
	public ResultSet SelectACommand(String like)
	{
		// wenn schon offen dann schließen !
		if (resultset != null) try { resultset.close(); } catch(Exception e) {  new SysErr(e.toString()); }

		// SELECT b.*, e.koordx, e.koordy, e.welt FROM befehle b, einheiten e WHERE befehl LIKE 'mache%' AND e.nummer = b.nummer GROUP BY b.nummer, e.koordx, e.koordy, e.welt ORDER BY sortierung
		myQuery = "SELECT b.*, e.koordx, e.koordy, e.welt FROM befehle b, einheiten e WHERE befehl LIKE '" + like + "%' AND e.nummer = b.nummer GROUP BY b.nummer, e.koordx, e.koordy, e.welt ORDER BY sortierung";

		try { resultset = statement.executeQuery(myQuery); countQuery++; } catch(Exception e) { new BigError(e); return null; }
		return resultset;
	}

	/**
	 * führt ein UPDATE / INSERT / DELETE / ... aus
	 * @return Anzahl der Zeilen
	 */
	public int Update()
	{
		if (!IsOpen()) return 0;

		// Deutsche-Umlaute ersetzen
		myQuery = myQuery.replace("=C4", "Ä");
		myQuery = myQuery.replace("ä", "ae");
		myQuery = myQuery.replace("ö", "oe");
		myQuery = myQuery.replace("ü", "ue");
		myQuery = myQuery.replace("Ä", "Ae");
		myQuery = myQuery.replace("Ö", "Oe");
		myQuery = myQuery.replace("Ü", "Ue");
		myQuery = myQuery.replace("ß", "ss");
		myQuery = StringUtils.only7bit(myQuery);


		int count = 0;
		try {
			count = statement.executeUpdate(myQuery); countQuery++;
		} catch(Exception ex) {
			// new SysErr(ex.getMessage());
			new BigError(ex);
		}

		// if (Main.getBFlag("DEBUG")) System.out.println("DB.Update :\n" + myQuery + "### " + count + " rows affected.");

		return count;
	}

	/**
	 * ein kleines schnelles Instant-Update
	 * @param query - SQL-Query
	 * @return wieviel Zeilen betroffen
	 */
	public static int Update(String query)
	{
		int count = 0;
		Datenbank db = new Datenbank("quick & dirty - update");
		db.myQuery = query;
		if (!IsOpen()) return 0;
		try { count = db.Update(); }  catch(Exception e) { new BigError(e); }
		db.Close();
		return count;
	}

	public static int Delete(String query) { return Update(query); }

	/**
	 * führt ein INSERT aus ... wird an Update() weiter geleitet
	 * @return Anzahl der Zeilen
	 * @see #Update
	 */
	public int Insert()
	{
		return Update();
	}

	/**
	 * führt ein INSERT DELAYED (!) aus
	 * @return Anzahl der Zeilen
	 * @see #Update
	 */
	public int InsertDelayed()
	{
		this.myQuery = this.myQuery.replace("INSERT", "INSERT DELAYED");
		return Insert();
	}

	/**
	 * führt ein DELETE aus ... wird an Update() weiter geleitet
	 * @return Anzahl der Zeilen
	 * @see #Update
	 */
	public int Delete()
	{
		return Update();
	}

	public static String MakeInsertInto(Map<String, Object> fields) {
		StringBuilder sb = new StringBuilder();

		for (String key : fields.keySet()) {
			if (sb.length() == 0) {
				sb.append("(").append(key);
			} else {
				sb.append(", ").append(key);
			}
		}
		sb.append(")");

		return sb.toString();
	}

	public static String MakeInsertValues(Map<String, Object> fields) {
		StringBuilder sb = new StringBuilder();

		for (String key : fields.keySet()) {
			Object o = fields.get(key);
            if (o == null) throw new RuntimeException("DB.MakeInsertValues: Soll für das Feld '" + key + "' einen NULL-Wert eintragen - das mache ich nicht.");

			String value = null;
			if (o instanceof String) value = "'" + CheckValue((String)o, true) + "'";
			if (o instanceof Integer) value = ((Integer)o).toString();
			if (o instanceof Float) value = ((Float)o).toString();
			if (o instanceof Double) value = ((Double)o).toString();
			if (o instanceof Boolean) value = ((Boolean)o).booleanValue()?"1":"0";

			if (value == null) throw new RuntimeException("DB.MakeInsertValues: Unerwarteter Datentyp " + o.getClass().getName());
			if (sb.length() == 0) {
				sb.append("(").append(value);
			} else {
				sb.append(", ").append(value);
			}
		}
		sb.append(")");

		return sb.toString();
	}

	/**
	 * initialisiert einen Query für ein Insert ... alle weiteren Daten werden mit AddXXXValue angehangen
	 * <br><br>
	 * <b>Beispiel:</b><br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;CreateInsert("settings", "name, value")<br>
	 * <i>erzeugt</i><br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;myQuery = "INSERT INTO <b>settings</b> (<b>name, value</b>) VALUES ("
	 * @param tabelle - Tabelle wo die Daten rein sollen
	 * @param spalten - Spalten, welche gefüllt werden sollen
	 * @see #AddFirstValue
	 * @see #AddValue
	 * @see #AddLastValue
	 */
	public void CreateInsert(String tabelle, String spalten)
	{
		myQuery = "INSERT INTO " + tabelle + " (" + spalten + ") VALUES (";
	}

	/**
	 * fügt den ersten Wert zur Abfrage hinzu
	 * @param value - Wert für die Abfrage
	 */
	public void AddFirstValue(int value)
	{
		myQuery += value;
	}

	/**
	 * fügt den ersten Wert zur Abfrage hinzu
	 * @param value - Wert für die Abfrage
	 */
	public void AddFirstValue(String value)
	{
		value = CheckValue(value, true);
		myQuery += "'" + value + "'";
	}

	/**
	 * fügt einen Integer zum Query hinzu
	 * @param value - der Wert
	 */
	public void AddValue(int value)
	{
		myQuery += ", " + value;
	}

	/**
	 * fügt einen String zum Query hinzu
	 * @param value - der Wert
	 */
	public void AddValue(String value)
	{
		value = CheckValue(value, true);
		myQuery += ", '" + value + "'";
	}
	/**
	 * fügt eine Kommazahl dem Query hinzu
	 * @param value - der Wert
	 */
	public void AddValue(float value)
	{
		myQuery += ", " + value;
	}
	/**
	 * fügt den letzten Wert zur Abfrage hinzu
	 * @param value - Wert für die Abfrage
	 */
	public void AddLastValue(int value)
	{
		myQuery += ", " + value + ")";
	}

	/**
	 * fügt den letzten Wert zur Abfrage hinzu
	 * @param value - Wert für die Abfrage
	 */
	public void AddLastValue(String value)
	{
		value = CheckValue(value, true);
		myQuery += ", '" + value + "')";
	}

	/**
	 * initialisiert einen Query für ein Update ... alle weiteren Daten werden mit AddXXXSet angehangen
	 * <br><br>
	 * <b>Beispiel:</b><br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;CreateUpdate("settings");<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;AddFirstSet("runde", 3);<br>
	 * <i>erzeugt</i><br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;myQuery = "UPDATE <b>settings</b> SET <b>runde</b> = <b>3</b>"
	 * @param tabelle - Tabelle wo die Daten rein sollen
	 * @see #AddFirstSet
	 * @see #AddNextSet
	 */
	public void CreateUpdate(String tabelle)
	{
		myQuery = "UPDATE " + tabelle + " SET ";
	}

	/**
	 * fügt ein SET-Statement der Updatefrage hinzu
	 * @param spalte - Name der Spalte
	 * @param value - Wert der gesetzt werden soll
	 */
	public void AddFirstSet(String spalte, int value)
	{
		myQuery += spalte + " = " + value;
	}

	/**
	 * fügt ein SET-Statement der Updatefrage hinzu
	 * @param spalte - Name der Spalte
	 * @param value - Wert der gesetzt werden soll
	 */
	public void AddFirstSet(String spalte, String value)
	{
		value = CheckValue(value, true);
		myQuery += spalte + " = '" + value + "'";
	}

	/**
	 * fügt ein weiteres SET-Statement der Updatefrage hinzu
	 * @param spalte - Name der Spalte
	 * @param value - Wert der gesetzt werden soll
	 */
	public void AddNextSet(String spalte, int value)
	{
		myQuery += ", " + spalte + " = " + value;
	}

	/**
	 * fügt ein weiteres SET-Statement der Updatefrage hinzu
	 * @param spalte - Name der Spalte
	 * @param value - Wert der gesetzt werden soll
	 */
	public void AddNextSet(String spalte, String value)
	{
		value = CheckValue(value, true);
		myQuery += ", " + spalte + " = '" + value + "'";
	}

	/**
	 * initialisiert einen Query für ein Select ... es wird immer alle zurück geliefert, also ein Stern !!
	 * <br><br>
	 * <b>Beispiel:</b><br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;CreateSelect("settings", "s1");<br>
	 * <i>erzeugt</i><br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;myQuery = "SELECT <u>*</u> FROM <b>settings s1</b>"
	 * @param tabelle - Tabelle wo die Daten rein sollen
	 * @param abbr - Abkürzung für die Tabelle
	 */
	public void CreateSelect(String tabelle, String abbr) // abbreviation
	{
		myQuery = "SELECT * FROM " + tabelle + " " + abbr;
	}

	/**
	 * fügt die erste WHERE-Klausel hinzu
	 * @param name - Name der Spalte
	 * @param value - Wert in der Spalte
	 */
	public void AddFirstWhere(String name, int value)
	{
		myQuery += " WHERE " + name + " = " + value;
	}
}
