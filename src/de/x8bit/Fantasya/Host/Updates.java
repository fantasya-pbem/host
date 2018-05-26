package de.x8bit.Fantasya.Host;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Items.LuxusGood;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.Host.ZAT.ZATBase;
import de.x8bit.Fantasya.util.Random;

public class Updates
{
	/**
	 * check ob die DB ein Update benötigt
	 * @param version - diese Version wird erwartet
	 */
	public Updates(String version)
	{
		makeUpdates(version);
	}
	
	private void makeUpdates(String version)
	{
		if (!Current().equals(version))	{
			new SysMsg("Version '" + Current() + "' gefunden - aber '" + version + "' erwartet");
			
			// Updates starten
			if (Current().equals("Version 2 - 0.0.0")) Update_0_0_1();
			if (Current().equals("0.0.1")) Update_0_0_2();
			if (Current().equals("0.0.2")) Update_0_0_3();
			if (Current().equals("0.0.3")) Update_0_0_4();
			if (Current().equals("0.0.11-4")) Update_0_0_12();
			if (Current().equals("0.13.2")) Update_0_13_3();
			if (Current().equals("0.14.2")) Update_0_14_3();
			if (Current().equals("0.14.13")) Update_0_14_14();
			if (Current().equals("0.14.16")) Update_0_14_17();
			if (Current().equals("0.15.9")) Update_0_15_10();
			
			new SysMsg("setze neue Version");
			Datenbank.Update("UPDATE settings SET value = '" + version + "' WHERE name LIKE 'version'");
		}
	}
	
	private String Current()
	{
		return Datenbank.Select("SELECT value FROM settings WHERE name LIKE 'version'", "0.0.0");
	}
	
	/** Update von "Version 2 -0.0.0" auf "0.0.1" */
	private void Update_0_0_1()
	{
		new SysMsg("starte mit Update auf V0.0.1");
		
		Helper_SQL_File("update/0.0.1/durchreise.sql");
		Helper_SQL_File("update/0.0.1/resourcen.sql");
		Helper_SQL_File("update/0.0.1/strassen.sql");
		Helper_SQL_Line("update/0.0.1/update.sql");
		
		// jetzt müssen noch alle Resourcen neu geschrieben werden
		// dazu wird einfach für jede Region erneut die Initialisierungs-Routine Region::Init() aufgerufen
		new SysMsg("Korregiere Regionen (Resourcen/Bauern/etc.)");
		
		Datenbank db = new Datenbank("Update -> 0.0.1");
		db.myQuery = "SELECT koordx, koordy, welt FROM regionen";
		ResultSet rs = db.Select();
		try
		{
			while(rs.next())
			{
				Region r = Region.Load(rs.getInt("koordx"), rs.getInt("koordy"), rs.getInt("welt"));
				r.Init();
				
				// sofortiges speichern der Region erzwingen
				ZATBase.ClearProxy();
			}
		} catch(Exception ex) { new BigError(ex); }
		
		new SysMsg("setze neue Version");
		Datenbank.Update("UPDATE settings SET value = '0.0.1' WHERE name = 'version'");
		
		db.Close();
	}
	
	private void Update_0_0_2()
	{
		new SysMsg("starte mit Update auf V0.0.2");
		Datenbank db = new Datenbank("Update -> 0.0.2");
		Helper_SQL_File("update/0.0.2/luxus.sql");

		new SysMsg(1, " - lege Handelsgüter an");
		
		// Handelsgüter neu verteilen
		db.myQuery = "SELECT koordx, koordy, welt FROM regionen";
		ResultSet rs = db.Select();
		try
		{
			while(rs.next()) {
				Region.Load(rs.getInt("koordx"), rs.getInt("koordy"), rs.getInt("welt")).Init_Handel();
				ZATBase.ClearProxy();
			}
		} catch(Exception ex) { new BigError(ex); }
		
		new SysMsg(1, " - lege Produktionsgüter fest");
		
		// Produktion festlegen
		ArrayList<Item> ali = new ArrayList<Item>(); // enthält alle Handelsgüter
		for(Paket p : Paket.getPaket("Items")) {
			if (p.Klasse instanceof LuxusGood) {
				ali.add((Item)p.Klasse);
			}
		}
		// - max. Inselkennung holen
		int max = db.ReadSettings(GameRules.INSELKENNUNG_ERZEUGUNG, 0);
		if (max == 0) new BigError("inselkennung ist 0, sollte aber größer sein - keine DB vorhanden?");
		for(int kennung = 0; kennung < max; kennung++)
		{
			// - 2 Handelsgüter holen ... pro Kennung/Insel erneut
			int i1 = Random.rnd(0, ali.size() - 1);
			int i2 = Random.rnd(0, ali.size() - 1);
			while(i1 == i2) i2 = Random.rnd(0, ali.size() - 1);
			db.myQuery = "SELECT koordx, koordy, welt FROM regionen WHERE insel = " + kennung;
			rs = db.Select();
			try
			{
				while(rs.next())
				{ 
//					// nun noch festlegen was die Bauern produzieren
//					// dazu wird die Nachfrage (= Anzahl) umgedreht
//					Region region = Region.Load(rs.getInt("koordx"), rs.getInt("koordy"), rs.getInt("welt"));
//					if (Random.rnd(0, 100) > 50)
//					{
//						region.luxus.get(i1).setAnzahl(0 - region.luxus.get(i1).getAnzahl());
//					} else
//					{
//						region.luxus.get(i2).setAnzahl(0 - region.luxus.get(i2).getAnzahl());
//					}
					new BigError(new Exception("not supported"));
				}
			} catch(Exception ex) { new BigError(ex); }
			ZATBase.ClearProxy();
		}
		
		db.Close();
	}

	private void Update_0_0_3()
	{
		new SysMsg("starte mit Update auf V0.0.3");
		Datenbank db = new Datenbank("Update -> 0.0.3");
		Helper_SQL_Line("update/0.0.3/update.sql");
		db.Close();
	}

	private void Update_0_0_4()
	{
		new SysMsg("starte mit Update auf V0.0.4");
		Datenbank db = new Datenbank("Update -> 0.0.4");
		Helper_SQL_Line("update/0.0.4/update.sql");
		db.Close();
	}

	private void Update_0_0_12()
	{
		new SysMsg("starte mit Update auf V0.0.12");
		Datenbank db = new Datenbank("Update -> 0.0.12");
		Helper_SQL_Line("update/0.0.12/update.sql");
		Helper_SQL_File("update/0.0.12/steuern.sql");
		db.Close();
	}
	
	private void Update_0_13_3()
	{
		new SysMsg("starte mit Update auf V0.13.3");
		Datenbank db = new Datenbank("Update -> 0.13.3");
		Helper_SQL_File("update/0.13.3/property_einheiten.sql");
		Helper_SQL_File("update/0.13.3/property_regionen.sql");
		Helper_SQL_File("update/0.13.3/property_schiffe.sql");
		Helper_SQL_File("update/0.13.3/property_gebaeude.sql");
		db.Close();
	}

	private void Update_0_14_3()
	{
		new SysMsg("starte mit Update auf V0.14.3");
		Datenbank db = new Datenbank("Update -> 0.14.3");
		Helper_SQL_Line("update/0.14.3/update.sql");
		db.Close();
	}
	
	private void Update_0_14_14()
	{
		new SysMsg("starte mit Update auf V0.14.14");
		Datenbank db = new Datenbank("Update -> 0.14.14");
		Helper_SQL_File("update/0.14.14/spells.sql");
		db.Close();
	}
	
	private void Update_0_14_17()
	{
		new SysMsg("starte mit Update auf V0.14.17");
		Datenbank db = new Datenbank("Update -> 0.14.17");
		Helper_SQL_File("update/0.14.17/effekte.sql");
		Helper_SQL_File("update/0.14.17/property.sql");
		db.Close();
		Datenbank.Update("UPDATE settings SET value = '0.15.5' WHERE name = 'version'");
	}
		
	private void Update_0_15_10()
	{
		new SysMsg("starte mit Update auf V0.15.10");
		Datenbank db = new Datenbank("Update -> 0.15.10");
		Helper_SQL_File("update/0.15.10/partei_beschreibung.sql");
		Helper_SQL_File("update/0.15.10/property_parteien.sql");
		db.Close();
		Datenbank.Update("UPDATE settings SET value = '0.15.10' WHERE name = 'version'");
	}

    /**
     * Überprüft, ob die Spalte 'entstandenin' in der Tabelle 'regionen' vorhanden ist - 
     * wenn nicht, wird sie angelegt. Arbeitet ansonsten zerstörungsfrei, kann aber auf
     * Dauer (und nach Anpassung des DB-create-Skripts) wieder aus dem Code entfernt 
     * werden.
     */
    public static void UpdateRegionenEntstandenIn() {
		Datenbank db = new Datenbank("updateRegionenEntstandenIn()");

        try {
            db.myQuery = "SHOW FIELDS FROM regionen WHERE `field` LIKE 'entstandenin';";
            ResultSet rs = db.Select();
            if (!rs.next()) {
                new SysMsg("regionen.enstandenin fehlt; erweitere die Tabelle entsprechend...");

                db.myQuery = "ALTER TABLE `regionen` ADD COLUMN `entstandenin` INT(11) DEFAULT '-1' NOT NULL COMMENT 'Entstehungsrunde der Region' AFTER `ralter`;";
                db.Update();
                
                new SysMsg("... OK!");
            }
        } catch (SQLException ex) {
            new BigError(ex);
        }

        db.Close();
    }
    
     /**
      * Rüstet das Feld für Meldungstexte für extrem lange Texte auf.
      * So etwas kommt u.U. bei Kampfmeldungen vor.
      */
    public static void UpdateMessageLongtext() {
		Datenbank db = new Datenbank("updateRegionenEntstandenIn()");

        try {
            db.myQuery = "SHOW FIELDS FROM `meldungen` WHERE `field` LIKE 'text';";
            ResultSet rs = db.Select();
            if (!rs.next()) { throw new RuntimeException("Feld 'text' in Tabelle 'meldungen' nicht gefunden."); }

            String alterTyp = rs.getString("Type");
            if (!alterTyp.equalsIgnoreCase("longtext")) {
                new SysMsg("Ändere Feld 'text' in 'meldungen' zu LONGTEXT...");

                db.myQuery = 
                        "ALTER TABLE `meldungen` " +
                        "CHANGE `text` `text` LONGTEXT NOT NULL " +
                        "COMMENT 'die eigentliche Meldung, muss nur noch 1:1 in den Report kopiert werden';";
                db.Update();
                
                new SysMsg("... OK!");
            }
        } catch (SQLException ex) {
            new BigError(ex);
        }

        db.Close();
    }
    

     /**
      * Rüstet das Feld für Property-Werte für extrem lange Texte auf.
      * Damit wird u.U. möglich, die individuelle Karte von Parteien so zu speichern,
     *  oder auch umfangreichere Aufzeichnungen zur Geschichte.
      */
    public static void UpdateParteiPropertiesLongtext() {
		Datenbank db = new Datenbank("UpdateParteiPropertiesLongtext()");

        try {
            db.myQuery = "SHOW FIELDS FROM `property_parteien` WHERE `field` LIKE 'value';";
            ResultSet rs = db.Select();
            if (!rs.next()) { throw new RuntimeException("Feld 'text' in Tabelle 'property_parteien' nicht gefunden."); }

            String alterTyp = rs.getString("Type");
            if (!alterTyp.equalsIgnoreCase("longtext")) {
                new SysMsg("Ändere Feld 'value' in 'property_parteien' zu LONGTEXT...");

                db.myQuery = 
                        "ALTER TABLE `property_parteien` " +
                        "CHANGE `value` `value` LONGTEXT NOT NULL " +
                        "COMMENT 'Wert der Eigenschaft';";
                db.Update();
                
                new SysMsg("... OK!");
            }
        } catch (SQLException ex) {
            new BigError(ex);
        }

        db.Close();
    }
    
    
    
	/**
	 * programmatisches Updates der Inselerzeugungsmethode - für den Echtbetrieb.
	 */
	public static void UpdateInselErzeugung() {
		GameRules.SetOption(GameRules.NEUE_INSEL_METHODE, "April2011");
	}

	public static void UpdateNegativesEinkommen() {
		Datenbank db = new Datenbank("UpdateNegativesEinkommen()");

		db.myQuery = "ALTER TABLE `einheiten` CHANGE `einkommen` `einkommen` INT( 10 ) NOT NULL DEFAULT '0' COMMENT 'das gesamte Einkommen dieser Einheit';";
		db.Update();

        db.Close();
	}
	
    public static void UpdateUnterweltAktivieren() {
		if ( "1".equals(GameRules.GetOption(GameRules.UNTERWELT_AKTIV)) ) return;
		
		GameRules.SetOption(GameRules.NEUE_INSEL_METHODE, "April2011");
		GameRules.SetOption(GameRules.UNTERWELT_AKTIV, "1");
		GameRules.Save();
	}
	

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
	
	private void Helper_SQL_File(String filename)
	{
		new SysMsg(" - verwende '" + filename.toString()+ "'");
		
		try
		{
			String query = "";
		
			// Query einlesen
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String hs = br.readLine();
			while(hs != null) { query += hs; hs = br.readLine(); }
			br.close();
			
			// Tabelle erzeugen / Query ausführen
			Datenbank.Update(query);
		} catch(Exception e) { System.out.println("[ERR] bei Verarbeitung von '" + filename.toString() + "'"); new BigError(e); }				
	}

	private void Helper_SQL_Line(String filename)
	{
		new SysMsg(" - verwende '" + filename.toString()+ "'");
		
		try
		{
			// Query einlesen
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String hs = br.readLine();
			while(hs != null)
			{
				if (hs.length() > 0) Datenbank.Update(hs);
				hs = br.readLine();
			}
			br.close();
		} catch(Exception e) { System.out.println("[ERR] bei Verarbeitung von '" + filename.toString() + "'"); new BigError(e); }				
	}
}
