package de.x8bit.Fantasya.Host.EVA.util;

import de.x8bit.Fantasya.Atlantis.Allianz;
import java.sql.ResultSet;
import java.sql.SQLException;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Effect;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Richtung;
import de.x8bit.Fantasya.Atlantis.Ship;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Spell;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.Debug;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.SysErr;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.Atlantis.Regions.Chaos;
import de.x8bit.Fantasya.Atlantis.Steuer;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.Datenbank;
import de.x8bit.Fantasya.Host.serialization.MigrationSerializerFactory;
import de.x8bit.Fantasya.Host.serialization.Serializer;
import de.x8bit.Fantasya.Host.Paket;
import de.x8bit.Fantasya.Host.Updates;
import de.x8bit.Fantasya.util.Codierung;
import de.x8bit.Fantasya.util.UnitIDPool;

/**
 * Der EVAFastLoader soll das gesamte Daten- / Objektmodell aus der MySQL-
 * Datenbank lesen; genau wie es die Load...()-Routinen der einzelnen Objekte
 * tun würden. Das ganze soll schnell sein.
 * @author hb
 */
public class EVAFastLoader {

    public static void loadAll() throws SQLException {
		new SysMsg("EVAFastLoader.loadAll...");

        Updates.UpdateRegionenEntstandenIn(); // sicherstellen, dass es 'regionen.entstandenin' gibt
        Updates.UpdateMessageLongtext(); // Meldungen für extrem lange (Kampf-)Meldungen wappnen
		Updates.UpdateNegativesEinkommen(); // es kann jetzt auch ein negatives Einkommen gespeichert werden (Handel / Einkauf, Gebäudeunterhalt...)
		Updates.UpdateInselErzeugung(); // auf April2011 wechseln
        Updates.UpdateParteiPropertiesLongtext(); // 18.04.2012
        
		// fürs Debuggen (automatisiertes Testen) aus Performance-Gründen deaktiviert.
        if (!ZATMode.CurrentMode().isDebug()) Updates.UpdateUnterweltAktivieren();

        Datenbank db = new Datenbank("EVAFastLoader");

		//-------------------------------------------------------------
		// Hack: Leite das Laden uebergangsweise zum fertig implementierten Teil
		// des neuen Load/Save-Systems um.
		Serializer newStuff = MigrationSerializerFactory.buildSerializer(db);

		// zur Zeit implementiert:
		// - Parteien
		// - Allianzen
		// - Steuern
		// - Parteienproperties
		// - Regionen
		// - Resourcen
		// - Strassen
		// - Luxus
		// - Regionenproperties
		// - Gebaeude + Properties
		// - Schiffe
		// - New Player
		newStuff.loadAll();
		//--------------------------------------------------------------


		int cnt;

//		EVAFastLoader.loadParteien(db);
//		new SysMsg(" " + Partei.PROXY.size() + " Parteien...");
//		int optionCnt = EVAFastLoader.loadAllianzen(db);
//		int allianzCnt = Allianz.getAlle().size();
//		new SysMsg(" " + allianzCnt + " Allianzen mit " + optionCnt + " Optionen...");
//		int steuernCnt = EVAFastLoader.loadSteuern(db);
//		new SysMsg(" " + steuernCnt + " Partei-Steuern...");
//		int pPropCnt = EVAFastLoader.loadParteienProperties(db);
//		new SysMsg(" " + pPropCnt + " Parteien-Properties...");

//		EVAFastLoader.loadRegionen(db);
//		new SysMsg(" " + Region.CACHE.size() + " Regionen...");
//		cnt = EVAFastLoader.loadResourcen(db);
//		new SysMsg(" " + cnt + " Resourcen...");
//		cnt = EVAFastLoader.loadStrassen(db);
//		new SysMsg(" " + cnt + " Straßen...");
//		cnt = EVAFastLoader.loadLuxus(db);
//		new SysMsg(" " + cnt + " Luxuspreise...");
//		cnt = EVAFastLoader.loadRegionenProperties(db);
//		new SysMsg(" " + cnt + " Regionen-Properties...");
//		EVAFastLoader.loadBuildings(db);
//		new SysMsg(" " + Building.PROXY.size() + " Gebäude...");
//		EVAFastLoader.loadBuildingProperties(db);
//		new SysMsg(" " + Building.PROXY.size() + " Gebäude-Properties...");
//		EVAFastLoader.loadShips(db);
//		new SysMsg(" " + Ship.PROXY.size() + " Schiffe...");

//		EVAFastLoader.loadEinheiten(db);
//		new SysMsg(" " + Unit.CACHE.size() + " Einheiten...");

//		cnt = EVAFastLoader.loadItems(db);
//		new SysMsg(" " + cnt + " Items...");
//		cnt = EVAFastLoader.loadTalente(db);
//		new SysMsg(" " + cnt + " Talente...");
//		cnt = EVAFastLoader.loadSpells(db);
//		new SysMsg(" " + cnt + " Zaubersprüche...");
//		cnt = EVAFastLoader.loadEinheitenProperties(db);
//		new SysMsg(" " + cnt + " Einheiten-Properties...");
//		cnt = EVAFastLoader.loadKontakte(db);
//		new SysMsg(" " + cnt + " Kontakte...");
//		cnt = EVAFastLoader.loadEffekte(db);
//		new SysMsg(" " + cnt + " Einheiten-Effekte...");
//		cnt = EVAFastLoader.loadEffekteProperties(db);
//		new SysMsg(" " + cnt + " Effekt-Properties...");
		cnt = EVAFastLoader.loadBefehle(db);
		new SysMsg(" " + cnt + " Befehle...");
        // wenn Befehle neu geladen werden, muss auch der Inhalt des BefehlsSpeichers verworfen werden.
        // TODO
		// BefehlsSpeicher.getInstance().clear();

		cnt = EVAFastLoader.loadMessages(db);
		new SysMsg(" " + cnt + " Meldungen...");

		// cnt = EVAFastLoader.loadNeueSpieler(db);
		// new SysMsg(" " + cnt + " neue Spieler...");

		db.Close();
    }

	private static int loadRegionenProperties(Datenbank db) throws SQLException {
		// Parteien-Properties:
        db.CreateSelect("property_regionen", "e");
        ResultSet rs = db.Select();

		int cnt = 0;
        while (rs.next()) {
        	Coords coords = Coords.fromRegionID(rs.getInt("id"));
        	Region r = Region.Load(coords);
        	
            if (!(r instanceof Chaos))
            	r.setProperty(rs.getString("name"), rs.getString("value"));
            else
            	new SysMsg("Warnung: regionen-Property für nicht-existente Region " + coords.toString() + ": "+ rs.getString("name") + "=" + rs.getString("value"));

			cnt ++;
		}

		return cnt;
	}

	public static void loadParteien(Datenbank db) throws SQLException {
		// Parteien, konsequent ohne .Load() etc.
        Partei.PROXY.clear();
        db.CreateSelect("partei", "p");
        ResultSet rs = db.Select();

        while (rs.next()) {
			@SuppressWarnings("deprecation")
			Partei p = Partei.fromResultSet(rs);

			if (!Partei.PROXY.contains(p)) Partei.PROXY.add(p);
        }

		// Partei 0 kommt nicht in die DB/aus der DB, es gibt sie aber trotzdem:
		Partei p0 = Partei.Create();
		p0.setNummer(0);
        p0.setEMail("");
		p0.setMonster(1);
		Partei.PROXY.add(p0);
	}

	public static int loadAllianzen(Datenbank db) throws SQLException {
        db.CreateSelect("allianzen", "a");
        ResultSet rs = db.Select();

		int cnt = 0;
        while (rs.next()) {
			Partei p = Partei.getPartei(rs.getInt("partei"));
			int partner = rs.getInt("partner");

			// hier wird das Allianz-Objekt ggf. auch erzeugt
			Allianz a = p.getAllianz(partner);

			// jetzt die Option herausfischen und eintragen
			a.readResultSet(rs);

			cnt ++;
        }

		for (Allianz a : Allianz.getAlle()) {
			if (!a.isValid()) new SysErr("Problem mit Allianz...");
		}

		return cnt;
	}

	public static int loadSteuern(Datenbank db) throws SQLException {
        db.CreateSelect("steuern", "s");
        ResultSet rs = db.Select();

		int cnt = 0;
        while (rs.next()) {
			Steuer s = Steuer.fromResultSet(rs);

			Partei.getPartei(s.getOwner()).getSteuern().add(s);

			cnt ++;
        }

		return cnt;
	}

	public static int loadParteienProperties(Datenbank db) throws SQLException {
		// Parteien-Properties:
        db.CreateSelect("property_parteien", "e");
        ResultSet rs = db.Select();

		int cnt = 0;
        while (rs.next()) {
			Partei p = Partei.getPartei(rs.getInt("partei"));
			if (p != null) p.setProperty(rs.getString("name"), rs.getString("value"));
            if (p == null) new SysMsg("Warnung: Parteien-Properties für nicht-existente Partei " + p + ": "+ rs.getString("name") + "=" + rs.getString("value"));
			// new Debug("@EVAFastLoader - Parteien-Property: " + u + " " + rs.getString("name") + "=" + rs.getString("value"));
			cnt ++;
		}

		return cnt;
	}




	public static void loadRegionen(Datenbank db) throws SQLException {
        // Regionen:
        Region.CACHE.clear();
        db.CreateSelect("regionen", "r");
        ResultSet rs = db.Select();

        while (rs.next()) {
			// Coords c = new Coords(rs.getInt("koordx"), rs.getInt("koordy"), rs.getInt("welt"));
            // new TestMsg("loadAll: " + c);

            Region r = Region.fromResultSet(rs);
            Region.CACHE.put(r.getCoords(), r);
        }
	}

	public static int loadResourcen(Datenbank db) throws SQLException {
		// Resourcen:
		db.CreateSelect("resourcen", "r");
        ResultSet rs = db.Select();
		int cnt = 0;
        while (rs.next()) {
			Coords c = new Coords(rs.getInt("koordx"), rs.getInt("koordy"), rs.getInt("welt"));
			String r = rs.getString("resource").toLowerCase();
			//System.out.println(c.toString() + " -> " + r);
			Class<? extends Item> item = Item.getFor(r);
			int anzahl = rs.getInt("anzahl");
			Region.Load(c).setResource(item, anzahl);
			cnt ++;
		}

		return cnt;
	}
	
	@SuppressWarnings("unchecked")
	public static int loadLuxus(Datenbank db) throws SQLException {
		for (Region r : Region.CACHE.values()) {
			r.Init_Handel();
		}

		db.CreateSelect("luxus", "l");
        ResultSet rs = db.Select();

		int cnt = 0;
        while (rs.next()) {
			Coords c = new Coords(rs.getInt("koordx"), rs.getInt("koordy"), rs.getInt("welt"));

            float nachfrage = rs.getFloat("nachfrage");
            // zur Umstellung bei Einführung von EVA:
            if (Math.abs(nachfrage) > 999.9f) nachfrage /= 1000f;

			Class<? extends Item> itemClass = null;
			try {
				itemClass = (Class<? extends Item>) Class.forName("de.x8bit.Fantasya.Atlantis.Items." + rs.getString("luxus"));
			} catch (ClassNotFoundException e) {
				new BigError(e);
			}

			Region.Load(c).setNachfrage(itemClass, nachfrage);
			cnt ++;
		}

		return cnt;
	}

	public static int loadStrassen(Datenbank db) throws SQLException {
		db.CreateSelect("strassen", "s");
        ResultSet rs = db.Select();

		int cnt = 0;
        while (rs.next()) {
			Coords c = new Coords(rs.getInt("koordx"), rs.getInt("koordy"), rs.getInt("welt"));
			Richtung richtung = Richtung.getRichtung(rs.getString("richtung"));
			Region.Load(c).setStrassensteine(richtung, rs.getInt("anzahl"));
			cnt ++;
		}

		return cnt;
	}

	public static void loadBuildings(Datenbank db) throws SQLException {
        Building.PROXY.clear();
        db.CreateSelect("gebaeude", "g");
        ResultSet rs = db.Select();

        while (rs.next()) {
            Building b = Building.fromResultSet(rs);

            Building.PROXY.add(b);
        }
	}
	
	public static int loadBuildingProperties(Datenbank db) throws SQLException {
		// Einheiten-Properties:
        db.CreateSelect("property_gebaeude", "g");
        ResultSet rs = db.Select();

		int cnt = 0;
        while (rs.next()) {
			Building b = Building.getBuilding(rs.getInt("id"));
			if (b != null) b.setProperty(rs.getString("name"), rs.getString("value"));
            if (b == null) new SysErr("Warnung: Gebäude-Properties für nicht-existentes Gebäude " + b + ": "+ rs.getString("name") + "=" + rs.getString("value"));
			// new Debug("@EVAFastLoader - Einheiten-Property: " + u + " " + rs.getString("name") + "=" + rs.getString("value"));
			cnt ++;
		}

		return cnt;
	}

	public static void loadShips(Datenbank db) throws SQLException {
        Ship.PROXY.clear();
        db.CreateSelect("schiffe", "s");
        ResultSet rs = db.Select();
        while (rs.next()) {
            Ship s = Ship.fromResultSet(rs);
            Ship.PROXY.add(s);
        }
		
		for (Ship s : Ship.PROXY) {
			Region.Load(s.getCoords()).getShips().add(s);
		}
	}

	public static void loadEinheiten(Datenbank db) throws SQLException {
		// Einheiten
        Unit.CACHE.clear();
		UnitIDPool.getInstance().clear();

        db.CreateSelect("einheiten", "u");
        ResultSet rs = db.Select();
        while (rs.next()) {
			Unit u = Unit.fromResultSet(rs);
            Unit.CACHE.add(u);
        }
	}

	public static int loadItems(Datenbank db) throws SQLException {
        db.CreateSelect("items", "i");
        ResultSet rs = db.Select();

		int cnt = 0;
        while (rs.next()) {
			Class<? extends Item> item = Item.getFor(rs.getString("item"));
			if (item == null) new BigError(new IllegalArgumentException("Item nicht erkannt: " + rs.getString("item")));
            
			Unit u = Unit.Get(rs.getInt("nummer"));
            if (u != null) {
				u.setItem(item, rs.getInt("anzahl"));
			} else {
				new SysErr("Warnung: Item " + item + " gehört angeblich Einheit [" + Codierung.toBase36(rs.getInt("nummer")) + "], aber die gibt es nicht.");
			}
			cnt ++;
        }

		return cnt;
	}

	public static int loadSpells(Datenbank db) throws SQLException {
        db.CreateSelect("spells", "s");
        ResultSet rs = db.Select();
        
		int cnt = 0;
        while (rs.next()) {
			Paket paket = Paket.FindSpell(rs.getString("spruch"));
			Spell spell = (Spell) paket.Klasse;
			if (spell == null) {
				new BigError(new IllegalArgumentException("Spell nicht erkannt: " + rs.getString("spell")));
			}
			Unit u = Unit.Get(rs.getInt("einheit"));
			if (u != null) {
                u.setSpell(spell);
            }
			cnt ++;
        }
        
		return cnt;
	}

	public static int loadTalente(Datenbank db) throws SQLException {
        // Talente:
        db.CreateSelect("skills", "s");
        ResultSet rs = db.Select();

		int cnt = 0;
        while (rs.next()) {
			Class<? extends Skill> skill = Skill.getFor(rs.getString("talent"));
			if (skill == null) {
				new BigError(new IllegalArgumentException("Skill nicht erkannt: " + rs.getString("talent")));
			}
			Unit u = Unit.Get(rs.getInt("nummer"));
			if (u != null) u.setSkill(skill, rs.getInt("lerntage"));
			cnt ++;
        }

		return cnt;
	}

	public static int loadEinheitenProperties(Datenbank db) throws SQLException {
		// Einheiten-Properties:
        db.CreateSelect("property_einheiten", "e");
        ResultSet rs = db.Select();

		int cnt = 0;
        while (rs.next()) {
			Unit u = Unit.Get(rs.getInt("id"));
			if (u != null) u.setProperty(rs.getString("name"), rs.getString("value"));
            if (u == null) new SysErr("Warnung: Einheiten-Properties für nicht-existente Einheit #" + rs.getInt("id") + ": "+ rs.getString("name") + "=" + rs.getString("value"));
			// new Debug("@EVAFastLoader - Einheiten-Property: " + u + " " + rs.getString("name") + "=" + rs.getString("value"));
			cnt ++;
		}

		return cnt;
	}

	public static int loadKontakte(Datenbank db) throws SQLException {
        // Kontakte:
        db.CreateSelect("kontakte", "k");
        ResultSet rs = db.Select();

		int cnt = 0;
        while (rs.next()) {
			Unit u = Unit.Get(rs.getInt("einheit"));
			if (u != null) {
                int partner = rs.getInt("partner");
                if (!u.Kontakte.contains(partner)) u.Kontakte.add(partner);
            }
			cnt ++;
        }

		return cnt;
	}

	public static int loadEffekte(Datenbank db) throws SQLException {
		// Effekte:
		Effect.PROXY.clear();
        db.CreateSelect("effekt_einheiten", "e");
        ResultSet rs = db.Select();

		int cnt = 0;
        while (rs.next()) {
			Unit u = Unit.Get(rs.getInt("einheit"));
			if (u != null) {
				Effect fx = Effect.fromResultSet(rs);
				u.getEffects().add(fx);
				Effect.PROXY.add(fx);
			}
			cnt++;
		}

		return cnt;
	}

	public static int loadEffekteProperties(Datenbank db) throws SQLException {
		// Effekte-Properties:
        db.CreateSelect("property_effekt", "e");
        ResultSet rs = db.Select();

		int cnt = 0;
        while (rs.next()) {
			int id = rs.getInt("id");
			Effect fx = null;
			for (Effect maybe : Effect.PROXY) {
				if (maybe.getNummer() == id) {
					fx = maybe;
					break;
				}
			}
            if (fx == null) new SysMsg("Warnung: Effekt-Properties für nicht-existenten Effekt " + id + ": "+ rs.getString("name") + "=" + rs.getString("value"));

			if (fx != null) fx.setProperty(rs.getString("name"), rs.getString("value"));
			cnt ++;
		}

		return cnt;
	}



	public static int loadBefehle(Datenbank db) throws SQLException {
		long start = System.currentTimeMillis(); int cnt = 0;
		
		// Befehle:
		BefehlsSpeicher.getInstance().clear();
		
        db.myQuery = "SELECT * FROM befehle ORDER BY sortierung";
        ResultSet rs = db.Select();

        while (rs.next()) {
			Unit u = Unit.Get(rs.getInt("nummer"));
			try {
				if (u != null) {
                    u.BefehleExperimental.add(u, rs.getString("befehl"));
                    u.Befehle.add(rs.getString("befehl"));
                } else {
					new SysErr("Warnung: Befehl '" + rs.getString("befehl") + "' gehört angeblich zu Einheit[" + Codierung.toBase36(rs.getInt("nummer")) + "], aber die gibt es nicht." );
				}
				// new Debug("Befehl aus der DB gelesen: " + rs.getString("befehl"));
			} catch(IllegalArgumentException ex) {
				new Debug("Unerlaubter Befehl - " + u + ": '" + rs.getString("befehl") + "'");
				new Fehler("'" + rs.getString("befehl") + "' - " + ex.getMessage(), u);
			}
			cnt ++;
        }
		new SysMsg(cnt + " Befehle eingelesen in " + (System.currentTimeMillis() - start) + "ms.");

		return cnt;
	}


	public static int loadMessages(Datenbank db) throws SQLException {
		// Meldungen
		long start = System.currentTimeMillis(); int cnt = 0;

        Message.DeleteAll();
		
        db.myQuery = "SELECT * FROM meldungen ORDER BY id";
        ResultSet rs = db.Select();


        while (rs.next()) {
			// das fügt die Meldung automatisch dem Klassen-internen Cache hinzu
			@SuppressWarnings("unused") // Message landet im Proxy
			Message m = Message.fromResultSet(rs);
			cnt ++;
        }
		new SysMsg(cnt + " Meldungen eingelesen in " + (System.currentTimeMillis() - start) + "ms.");

		return cnt;
	}


	public static int loadNeueSpieler(Datenbank db) throws SQLException {
        NeuerSpieler.PROXY.clear();

        db.CreateSelect("neuespieler", "p");
        ResultSet rs = db.Select();
        while (rs.next()) {
			NeuerSpieler n = NeuerSpieler.fromResultSet(rs);
			NeuerSpieler.PROXY.add(n);
        }

        return NeuerSpieler.PROXY.size();
	}


}
