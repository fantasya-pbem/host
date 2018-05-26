package de.x8bit.Fantasya.Host;

import java.io.FileWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.Atlantis.Messages.ZATMsg;
import de.x8bit.Fantasya.Host.EVA.EVABase;
import de.x8bit.Fantasya.Host.EVA.util.EVAFastLoader;
import de.x8bit.Fantasya.Host.EVA.util.ZATMode;
import de.x8bit.Fantasya.Host.GUI.MainFrame;
// import de.x8bit.Fantasya.Host.ManualTests.TestWorld;
import de.x8bit.Fantasya.Host.Reports.ReportXML;
import de.x8bit.Fantasya.Host.Reports.Zipping;



/*
   			Unit u = (Unit) Class.forName("de.x8bit.Fantasya.Atlantis.Units.Monsters.Zombie").newInstance();
			u.setPersonen(1);
			u.setLerntage(Skill.Alchemie, 30);
			System.out.println(u.Talentwert(Skill.Alchemie));
			Skill s = Enum.valueOf(Skill.MaxSkills.getDeclaringClass(), "Alchemie");
			System.out.println(s);
*/

/*
 * 	alle Zeilen zählen -> find . -type f -name *.java | xargs cat | egrep -v '^[^a-zA-Z0-9_/*;,.:#<>(){}=+-]*$' | wc -l
 */

/*
 *  ungültige FK's finden ... Tabellen- und Spaltennamen anpassen nicht vergessen
 *  SELECT * FROM tabelle1 t1 WHERE t1.spalte > 0 AND NOT EXISTS (SELECT NULL FROM tabelle2 t2 WHERE t2.spalte = t1.spalte)
 *  
 */

public class Main
{
	private static boolean args_gui = false;
	private static boolean args_zat = false;
	private static boolean args_befehlscheck = false;
	private static boolean args_debug = false;
	private static boolean args_checkdb = false;
	private static boolean args_initdb = false;
	// private static String args_testworld = null;
	// private static boolean args_verify = false;
	private static boolean args_clrmessages = false;
	private static boolean args_reporte = false;
	// private static boolean args_email = false;
	private static boolean args_newplayers = false;
	private static boolean args_crmap = false;
	private static boolean args_bugfix = false;
	private static boolean args_config = false;
	private static int args_X = -1;
	public static String GameID = "00000000";
	

	
	/** aktuelle Fantasya Version */
	public static String FANTAVERSION = "0.17";

	public final static long start = System.currentTimeMillis();
	
	
	
	/** 
	 * alle Flags die über Commando-Zeile gesetzt werden können<br/><br/> 	 
	 * <table>
	 * <tr><th>Flag</th><th>Type</th><th>Beschreibung</th></tr>
	 * <tr><td>OverrideVersion</td><td>String</td><td>Überschreibt die aktuelle Fantasya-Version</td></tr>
	 * <tr><td>EVA</td><td>boolean</td><td>aktiviert EVA-ZAT</td></tr>
	 * </table>
	 */
	private static Map<String, Object> flags = new HashMap<String, Object>();
	
	/** holt ein entsprechendes Flag bzw. fügt ein unbenutzes mit einem Default-Wert ein */
	private static Object getFlag(String name, Object value)
	{
		if (flags.containsKey(name)) return flags.get(name);
		flags.put(name, value);
		return value;
	}
	
	/** liefert den Wert des Flags als String */
	public static String getSFlag(String name)
	{
		Object value = getFlag(name, "");
		if (value == null) return "";
		return value.toString();
	}
	
	/** liefert den Wert des Flags als Integer */
	public static int getIFlag(String name)
	{
		Object value = getFlag(name, 0);
		if (value == null) return 0;
		int number;
		try { number = Integer.parseInt(value.toString()); } catch(Exception ex) { return 0; }
		return number;
	}
	
	/** 
	 * liefert den Wert des Flags als Boolean ... 
	 * es wird für alle Flags TRUE geliefert, wenn er nicht dem Default-Wert entspricht
	 */
	public static boolean getBFlag(String name)
	{
		Object value = getFlag(name, 0);
		if (value == null) return false;
		boolean bool = false;
		if (value.getClass().equals(Boolean.class)) try { bool = Boolean.parseBoolean(value.toString()); } catch(Exception ex) { return false; }
		if (value.getClass().equals(Integer.class)) try { if (getIFlag(name) != 0) bool = true; } catch(Exception ex) { return false; }
		if (value.getClass().equals(String.class)) try { if (getSFlag(name).length() > 0) bool = true; } catch(Exception ex) { return false; }
		return bool;
	}
	
	/** zeigt alle Flags */
	public static void listUsedFlags()
	{
		new SysMsg("liste alle benutzen Flags auf:");
		for ( String key : flags.keySet() )
		{
			String value = getSFlag(key);
			new SysMsg(" - " + key + "=" + value );
		}
	}
	
	/** zerlegt alle übergebenen Flags */
	private static void parseFlags(String args_flags)
	{
		System.out.println("Verwende folgende Flags:"); // (" + args_flags + "):");
		for(String flag : args_flags.split(":"))
		{
			String part[] = flag.split("=");
			flags.put(part[0], part[1]);
			System.out.println(" - '" + part[0] + "' mit Wert '"+ part[1] + "'");
		}
	}
	
	
	
	/** <br>
	 * Fantasya 2 - das nächste Zeitalter<br>
	 * Copyright (C) 2007-2010 by Ronny Gorzelitz<br>
	 * Copyright (C) 2010 by AGPL<br>
	 * <br>
	 * <br>
	 * <br>
	 * folgende Parameter können übergeben werden ... einige Parameter schließen aber Andere aus<br>
	 * <br>
	 * <br>
	 * <table>
	 * <tr><td>gui</td><td>&nbsp;-&nbsp;</td><td>startet die grafische Oberfläche zur Administration</td></tr>
	 * <tr><td>zat</td><td>&nbsp;-&nbsp;</td><td>startet die Auswertung</td></tr>
	 * <tr><td>checkdb</td><td>&nbsp;-&nbsp;</td><td>testet die Verbindung zur Datenbank</td></tr>
	 * <tr><td>initdb</td><td>&nbsp;-&nbsp;</td><td>initialisiert die Datenbank</td></tr>
	 * <tr><td>testworld</td><td>&nbsp;-&nbsp;</td><td>AUSKOMMENTIERT! erwartet ein Argument: Name der Test-Klasse (@see de.x8bit.Fantasya.Host.ManualTests.TestBase). Leert die Test-Datenbank, erzeugt eine komplette Test-Welt und führt einen ZAT darauf durch. Löst automatisch den Debug-ZAT aus (Option -debug)</td></tr>
	 * <tr><td>verify</td><td>&nbsp;-&nbsp;</td><td>AUSKOMMENTIERT! nur zusammen mit -testworld: Die Ergebnisse des entsprechenden Tests werden überprüft.</td></tr>
	 * <tr><td>clrmsg</td><td>&nbsp;-&nbsp;</td><td>löscht alle Meldungen aus der Datenbank</td></tr>
	 * <tr><td>datenbank [name]</td><td>&nbsp;-&nbsp;</td><td>legt den Namen der Datenbank fest (default fantasya)</td></tr>
	 * <tr><td>server [server:port]</td><td>&nbsp;-&nbsp;</td><td>legt den Server & Port fest (default 127.0.0.1:3306)</td></tr>
	 * <tr><td>benutzer [name]</td><td>&nbsp;-&nbsp;</td><td>legt den Benutzer fest (default fantasya)</td></tr>
	 * <tr><td>passwort [passwort]</td><td>&nbsp;-&nbsp;</td><td>legt das Passwort für den Benutzer fest (default fantasya)</td></tr>
	 * <tr><td>newplayers</td><td>&nbsp;-&nbsp;</td><td>setzt neue Spieler aus</td></tr>
	 * <tr><td>reporte</td><td>&nbsp;-&nbsp;</td><td>schreibt die ReporteSchreiben für alle Spieler neu, dabei wird aber keine e-Mail an die Spieler verschickt</td></tr>
	 * <tr><td>crmap</td><td>&nbsp;-&nbsp;</td><td>erstellt einen CR mit den aktuellen Regionen</td></tr>
	 * <tr><td>gameid</td><td>&nbsp;-&nbsp;</td><td>&uuml;bergibt die GameID f&uuml;r das Spiel</td></tr>
	 * <tr><td>bugfix</td><td>&nbsp;-&nbsp;</td><td>korregiert einen Fehler oder macht etwas anderes Unanständiges :)</td></tr>
	 * <tr><td>config</td><td>&nbsp;-&nbsp;</td><td>schreibt die Konfigurationsdatei</td></tr>
	 * <tr><td>debug</td><td>&nbsp;-&nbsp;</td><td>liest nicht die Befehle ein, sondern verwendet die vorhanden in der DB</td></tr>
	 * <tr><td>flags</td><td>&nbsp;-&nbsp;</td><td>setzt verschiedene Flags die den Programmablauf beeinflussen (Format <i>flagname</i>=<i>value</i><b>:</b><i>flagname</i>=...) @see flags</td></tr>
	 * <tr><td>smsuser</td><td>&nbsp;-&nbsp;</td><td>Benutzer für SMSKaufen.de</td></tr>
	 * <tr><td>smspass</td><td>&nbsp;-&nbsp;</td><td>Passwort für SMSKaufen.de</td></tr>
	 * <tr><td>smsapikey</td><td>&nbsp;-&nbsp;</td><td>APIKey für SMSKaufen.de (als Alternative für das Passwort)</td></tr>
	 * <tr><td>smshandy</td><td>&nbsp;-&nbsp;</td><td>Handy Empfänger (Trennung mittels Doppelpunkt)</td></tr>
	 * </table>
	 * <br>
	 * @param args --- diverse verschiedene Argumente ^^
	 */
	public static void main(String[] args)
	{
		// wenn man das Tool "SizeOf" benutzen will, muss ein Argument für den Aufruf der JVM (!) eingebunden werden:
		// java .... -javaagent:<path to>/SizeOf.jar
		// (Wunderwerke der Technik!)
		// 
		// net.sourceforge.sizeof.SizeOf - configuration steps (available in sizeOf.jar)
		// SizeOf.skipStaticField(true);
		// SizeOf.setMinSizeToLog(10*1000*1000);

		Advertisement();

		// EVA wird ab heute (18.04.2011) immer bevorzug - um den alten Modus zu erzwingen
		// einfach "-flags EVA=false" an die Zeile hängen
		flags.put("EVA", "true"); // System.out.println("erzwinge EVA-Auswertung");
		
		// alle Flags einlesen
		for(int i = 0; i < args.length; i++) {
			if (args[i].compareToIgnoreCase("-gui") == 0) args_gui = true;
			if (args[i].compareToIgnoreCase("-zat") == 0) args_zat = true;
			if (args[i].compareToIgnoreCase("-befehlscheck") == 0) args_befehlscheck = true;
			if (args[i].compareToIgnoreCase("-debug") == 0) { args_debug = true; flags.put("DEBUG", 1); }
			if (args[i].compareToIgnoreCase("-checkdb") == 0) args_checkdb = true;
			if (args[i].compareToIgnoreCase("-initdb") == 0) args_initdb = true;
			// if (args[i].compareToIgnoreCase("-testworld") == 0) if (args.length > i+1) args_testworld = args[i+1];
			// if (args[i].compareToIgnoreCase("-verify") == 0) args_verify = true;
			if (args[i].compareToIgnoreCase("-clrmsg") == 0) args_clrmessages = true;
			if (args[i].compareToIgnoreCase("-datenbank") == 0) Datenbank.SetDatenbank(args[i + 1]);
			if (args[i].compareToIgnoreCase("-server") == 0) Datenbank.SetServer(args[i + 1]);
			if (args[i].compareToIgnoreCase("-benutzer") == 0) Datenbank.SetBenutzer(args[i + 1]);
			if (args[i].compareToIgnoreCase("-passwort") == 0) Datenbank.SetPasswort(args[i + 1]);
			if (args[i].compareToIgnoreCase("-newplayers") == 0) args_newplayers = true;
			if (args[i].compareToIgnoreCase("-testcase") == 0) new TestCase();
			if (args[i].compareToIgnoreCase("-reporte") == 0) args_reporte = true;
			if (args[i].compareToIgnoreCase("-crmap") == 0) args_crmap = true;
			if (args[i].compareToIgnoreCase("-bugfix") == 0) args_bugfix = true;
			if (args[i].compareToIgnoreCase("-config") == 0) args_config = true;
			if (args[i].compareToIgnoreCase("-flags") == 0) parseFlags(args[i + 1]);
			if (args[i].compareToIgnoreCase("-smsuser") == 0) SMSKaufen.User = args[i + 1];
			if (args[i].compareToIgnoreCase("-smspass") == 0) SMSKaufen.Password = args[i + 1];
			if (args[i].compareToIgnoreCase("-smsapikey") == 0) SMSKaufen.APIKey = args[i + 1];
			if (args[i].compareToIgnoreCase("-smshandy") == 0) SMSKaufen.Recipients = args[i + 1];
			if (args[i].compareToIgnoreCase("-gameid") == 0) GameID = args[i + 1];
			if (args[i].equals("-X")) args_X = i;
		}

		if (args.length == 0) { printUsage(); System.exit(0); }

		if (args_X == -1) {
            try {
                @SuppressWarnings("unused")
				Datenbank db = new Datenbank("Verbindung herstellen");
            } catch (Exception ex) {
                new BigError(ex);
            }
        }

        // Korrekte Parameter überprüfen:
        /*if (args_verify && (args_testworld == null)) {
            System.err.println("-verify kann nur zusammen mit -testworld verwendet werden!");
            System.exit(-1);
        }*/
		
		// new SysMsg(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date()) + " - es geht los!");

		// Klassen-Daten einlesen
		de.x8bit.Fantasya.Host.Paket.Init();

		if (args_X > -1) {
            String xArg = null; // das 2nd level Argument: wird neben dem "Verb" an den Xecutor weitergereicht
            if (args.length > args_X+2) xArg = args[args_X+2];
			// Xecutor(Verb, Objekt)
            new Xecutor(args.length>args_X+1 ? args[args_X + 1] : null, xArg);
		}

		// Fantasya-Version ggf. überschreiben
		if (getBFlag("OverrideVersion")) FANTAVERSION = getSFlag("OverrideVersion");
		
		// ggf. Initialisieren - also Tabllen anlegen
		if (args_initdb) { Datenbank db = new Datenbank("InitDB"); db.InitDB(FANTAVERSION); db.Close(); return; }

		// testworld: DB leeren, Test-Welt anlegen, ZAT ausführen.
		/* if (args_testworld != null) {
			if (!Datenbank.GetDatenbank().contains("test")) {
				System.err.println("'-testworld' ist nur zulässig, wenn auch eine -datenbank angegeben wird, deren Name 'test' enthält. Grund: Verhindern, dass echte Spieldaten gelöscht werden.");
				System.exit(-1);
			}
			testWorld();
		} */

		// Updates checken ... aber nur wenn nicht gerade die DB vorbereitet wird
		// -- entfernt für Server-Umstellung -- 17.02.2011 !! if (!args_initdb) new Updates(FANTAVERSION);

		// alten Meldungen löschen
		if (args_clrmessages) Message.DeleteAll();

		if (args_checkdb) { Datenbank db = new Datenbank("CheckDB"); db.CheckDB(); db.Close(); return; }
		if (args_gui) { System.out.println("'-gui' gefunden ... starte Oberfläche"); new MainFrame(); return; }
		if (args_newplayers) {
			try {
				EVABase.loadAllEx();
				NeueSpieler();
				EVABase.saveAllEx();
			} catch(Exception ex) {
				new BigError(ex);
			}
		}
		if (args_reporte) Reporte();
		if (args_crmap) CRMap();
		if (args_zat) {
			ZATMode.SetCurrentMode(ZATMode.MODE_NORMAL());
			if (args_debug) {
				ZATMode.CurrentMode().setDebug(true);
			}
			try{
				EVABase.ZAT();
			} catch(Exception ex) {
				new BigError(ex);
			}
		}
		if (args_befehlscheck) {
			ZATMode.SetCurrentMode(ZATMode.MODE_BEFEHLE_CHECKEN());
			if (args_debug) ZATMode.CurrentMode().setDebug(true);
			try {
				EVABase.ZAT();
			} catch (Exception ex) {
				new BigError(ex);
			}
		}
		if (args_bugfix) BugFix();
		if (args_config) new ConfigWriter();

		try {
			Datenbank db = new Datenbank("GameOver");
			db.SaveSettings("zatstatus.text", "ZAT ist Sonntag, 17 Uhr");
			db.SaveSettings("Version", FANTAVERSION);
			db.Close();
		} catch (Exception ex) {
			new BigError(ex);
		}

		new SysMsg(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date()) + " - fertig!");
	}

	private static void printUsage() {
		StringBuilder sb = new StringBuilder();

		sb.append("USAGE:\n");
		sb.append("de.x8bit.Fantasya.Atlantis.Host.Main [-option [parameter]] [-option] ...\n");
		sb.append("\n");
		sb.append("Die folgenden Optionen stehen zur Verfügung:\n");
		sb.append("   (Einige Optionen schließen andere aus!)\n");
		sb.append("\n");
		sb.append("flags [flag1=wert1[:flag2=wert2] ...\n");
		sb.append("                     setzt verschiedene Flags die den Programmablauf\n" +
				  "                     beeinflussen. (@see Main.flags) Sollte nur verwendet\n" +
				  "                     werden, wenn man weiß was man tut.\n");
		sb.append("\n");
		sb.append("Spielzüge & Reporte:\n");
		sb.append("zat                  startet die Auswertung\n" +
				  "                     ACHTUNG: Vorher besser die aktuellen Daten sichern!\n" +
				  "                     In Verbindung mit 'debug' kann die Auswertung getestet\n" +
				  "                     werden, ohne dass Reporte an die Spieler geschickt werden.\n");
		sb.append("reporte              schreibt die Reporte für alle Spieler neu\n");
		sb.append("befehlscheck         prüft neu eingegangene Befehle mittels eines ZAT-\n" +
				  "                     Trockenlaufs (Read-Only)\n");
		sb.append("gameid [gameid]      legt die GameID für den Download-Link der Auswertung fest\n");
		sb.append("\n");
		sb.append("Datenbank-Konfiguration (MySQL):\n");
		sb.append("server [server:port] legt den Server & Port fest (default 127.0.0.1:3306)\n");
		sb.append("benutzer [name]      legt den Benutzer fest (default fantasya)\n");
		sb.append("passwort [passwort]  legt das Passwort für den Benutzer fest (default fantasya)\n");
		sb.append("datenbank [name]     legt den Namen der Datenbank fest (default fantasya)\n");
		sb.append("checkdb              testet die Verbindung zur Datenbank\n");
		sb.append("initdb               initialisiert die Datenbank - legt die Tabellen an,\n" +
				  "                     schreibt einige fundamentale Konfigurationswerte und\n");
		sb.append("                     legt eine fantasysiche Startwelt an.\n");
		sb.append("\n");
		sb.append("SMSKaufen.de:\n");
		sb.append("smsuser              Benutzeraccount für SMSKaufen.de\n");
		sb.append("smspass              Passwort für SMSKaufen.de\n");
		sb.append("smsapikey            APIKey für SMSKaufen.de\n");
		sb.append("smshandy             Handy-Nummer die bei einem Crash informiert werden sollen\n");
		sb.append("                     Trennung erfolgt über Doppelpunkt\n");
		sb.append("\n");
		sb.append("Spiel-Verwaltung:\n");
		sb.append("clrmsg               löscht alle Meldungen aus der Datenbank\n");
		sb.append("crmap                erstellt einen CR mit den aktuellen Regionen\n");
		sb.append("debug                Optionsschalter, der nur zusammen mit anderen Optionen\n" +
				  "                     verwendet werden, besonders 'zat'. Liest nicht die\n" +
				  "                     Befehle ein, sondern verwendet die vorhandenen in der DB.\n" +
				  "                     Führt ansonsten zur Ausgaben von mehr Informationen über\n" +
				  "                     die Verarbeitungsschritte und auch zu verminderter\n" +
				  "                     Ernsthaftigkeit.\n");
		sb.append("newplayers           setzt neue Spieler (ohne Durchführung eines ZAT) aus\n");
		sb.append("config               schreibt die Konfigurationsdatei\n");
		sb.append("gui                  startet die grafische Oberfläche zur Administration\n");
		sb.append("bugfix               korrigiert einen Fehler oder macht etwas anderes\n" +
				  "                     Unanständiges :)\n" +
				  "                     (Die entsprechenden Aktionen müssen vorher im Java-\n" +
				  "                     Quellcode angelegt werden!!!)\n");
		sb.append("\n");
		sb.append("Halbautomatisches Testen:\n");
		/* sb.append("testworld [class]    Erstellt das Test-Szenario in [class] (Name einer Java-\n" +
				  "                     Klasse) oder überprüft die Ergebnisse.\n" +
				  "                     (siehe de.x8bit.Fantasya.Host.ManualTests.TestBase).\n" +
				  "                     Leert die Test-Datenbank, erzeugt eine komplette Test-Welt\n" +
				  "                     und löst (je nach Festlegung in [class]) automatisch den\n" +
				  "                     Debug-ZAT aus. (setzt implizit die Option -debug)\n");
		sb.append("verify               Optionsschalter zu -testworld : Ergebnisse prüfen statt\n" +
				  "                     Test anlegen.\n"); */
		sb.append("\n");
		sb.append("Erweiterte Aktionen:\n");
		sb.append("X [aktion]           Führt erweiterte Prozeduren durch, die dynamisch im Code\n" +
				  "                     \"gemeldet\" sind. 'X' ohne Angabe einer Aktion listet die\n" +
				  "                     verfügbaren Aktionen auf.\n");
		sb.append("\n");
		sb.append("Build-ID: " + BuildInformation.getBuild());
		sb.append("\n");
		sb.append("\n");

		System.out.print(sb.toString());
	}

	/* private static void testWorld() {
        if (!args_verify) {
            Datenbank db = new Datenbank("Testworld - Truncate Tables");
            db.TruncateAll(); db.Close();

            db = new Datenbank("Testworld - InitDB");
            db.InitDB(FANTAVERSION); db.Close();
        } else {
            GameRules.Load();
        }

		TestWorld tw = new TestWorld(args_testworld);

        if (tw.shouldContinueWithZAT()) {
            args_zat = true;
            
            if (!Main.getBFlag("EVA")) args_debug = true; // damit nicht die Befehle überschrieben werden.
        }
	} */
	
	/**
	 * repariert irgend ein blöden <b><u><i>katastrophalen</i></u></b> Fehler
	 */
	private static void BugFix()
	{	
/*		new SysMsg("starte BugFix");
		Datenbank db = new Datenbank("Bugfix");
		db.myQuery = "SELECT * FROM einheiten";
		ResultSet rs = db.Select();
		try
		{
			while(rs.next())
			{
				Unit unit = Unit.Load(rs.getInt("nummer"));
				try 
				{
					unit.Save();					
				} catch(Exception ex)
				{
					Datenbank.ListQueryStack();
				}
			}
		} catch (Exception e)
		{
			new BigError(e);
		}
		
		db.Close();
		
		new SysMsg("beende BugFix");
		
		System.exit(0);
*/
	}
	
	/**
	 * *juhu* neue Spieler
	 */
	private static void NeueSpieler()
	{
		new SysMsg("SYSTEM Start - setze neue Spieler aus");
		
		Zipping.salz = Datenbank.Select("SELECT value FROM settings WHERE name = 'game.salt'", "00");
		
		// Runde laden
		GameRules.Load();
		
		// weil ja eigentlich schon die nächste Runde existiert
		GameRules.setRunde(GameRules.getRunde() - 1);
		
		// die ReporteSchreiben schreiben
		new NeueSpieler();
		
		// falls irgend wann mal der GC Blödsinn wieder aufhört
		// und ein Destruktor existiert
		GameRules.setRunde(GameRules.getRunde() + 1);
		
		new SysMsg("SYSTEM Quit - neue Spieler fertig");
	}
	
	/**
	 * schreibt alle ReporteSchreiben nochmal für die Spieler
	 */
	private static void Reporte()
	{
		// Runde laden
		GameRules.Load();
        
        try {
            EVAFastLoader.loadAll();
        } catch (SQLException ex) {
            new BigError(ex);
        }

		Zipping.salz = Datenbank.Select("SELECT value FROM settings WHERE name = 'game.salt'", "00");
		
		// die ReporteSchreiben schreiben
		new ZATMsg("erstelle Reporte");
		GameRules.setRunde(GameRules.getRunde()); // -> turn of database is turn of report
        new de.x8bit.Fantasya.Host.EVA.Reporte();
		new ReportXML(new Partei());	// "world.xml" erzeugen

		new SysMsg("SYSTEM Quit - Reporte neu geschrieben");
	}
	
	/**
	 * erstellt eine CR-Map von der DB ... dabei werden aber keine Einheiten etc. mit ausgegeben
	 */
	private static void CRMap()
	{
		new SysMsg("SYSTEM Start - CR-Map");
		
		FileWriter file;
		Datenbank db = new Datenbank("CR-Map");
		db.myQuery = "SELECT koordx, koordy, welt, name, typ, insel FROM regionen";
		ResultSet rs = db.Select();
		try
		{
			file = new FileWriter("world.cr", false);
			
			// Basis-Header
			file.write("VERSION 64\n");
			file.write("\"de\";locale\n");
			file.write("\"fantasya\";Spiel\n");
			file.write("0;noskillpoints\n");
			file.write(new Date().getTime() + ";date\n");
			file.write("\"Standart\";Konfiguration\n");
			file.write("\"Hex\";Koordinaten\n");
			file.write("36;Basis\n");
			file.write("1;Umlaute\n");
			file.write(GameRules.getRunde() + ";Runde\n");
			file.write("2;Zeitalter\n");
			file.write("\"befehle@fantasya-pbem.de\";mailto\n");
			file.write("\"Fantasya Befehle\";mailcmd\n");
			while(rs.next())
			{
				file.write("REGION " + rs.getInt("koordx") + " " + rs.getInt("koordy") + " " + rs.getInt("welt") + "\n");
				if (!rs.getString("typ").equals("Ozean")) file.write("\"" + rs.getString("name") + "\";Name\n");
				file.write("\"" + rs.getString("typ") + "\";Terrain\n");
//				file.write("\"" + rs.getInt("insel") + "\";Insel\n");
//				file.write("\"neighbour\";visibility\n");
			}
			file.flush();
			file.close();
		} catch(Exception ex) { new BigError(ex); }
		
		new SysMsg("erzeuge XML-Map");
		new ReportXML(new Partei());
		
		new SysMsg("SYSTEM Quit - CR-Map -> 'world.cr'");
	}
	
	/**
	 * etwas Werbung muss sein
	 */
	private static void Advertisement()
	{
		System.out.println("Fantasya 2 - Version " + FANTAVERSION);
		System.out.println("Copyright (C) 2007-2009 - x8Bit.de");
		System.out.println("Copyright (C) 2010 by AGPL\n");
		System.out.println();
	}

}
