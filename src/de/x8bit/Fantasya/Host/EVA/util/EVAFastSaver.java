package de.x8bit.Fantasya.Host.EVA.util;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import de.x8bit.Fantasya.Atlantis.Allianz;
import de.x8bit.Fantasya.Atlantis.Allianz.AllianzOption;
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
import de.x8bit.Fantasya.Atlantis.Helper.Nachfrage;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Items.Seide;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.SysErr;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.Atlantis.Regions.Ebene;
import de.x8bit.Fantasya.Atlantis.Skills.Wahrnehmung;
import de.x8bit.Fantasya.Atlantis.Spells.HainDerTausendEichen;
import de.x8bit.Fantasya.Atlantis.Steuer;
import de.x8bit.Fantasya.Host.Datenbank;
import de.x8bit.Fantasya.Host.serialization.MigrationSerializerFactory;
import de.x8bit.Fantasya.Host.serialization.Serializer;
import java.util.HashSet;
import java.util.Set;

/**
 * Absicht dieser Klasse ist nicht weniger, als das gesamte Daten-Objektmodell
 * vollständig und SCHNELL in die MySQL-Datenbank zu schreiben. Der EVAFastSaver
 * soll dabei die Datenbank-Routinen der einzelnen Objekte ersetzen - sie sollen
 * eher nicht entfernt werden, aber funktionsidentisch gedoppelt.
 * @author hapebe
 */
@SuppressWarnings("unused") // hier wird der result des DB-Query unterdrückt
public class EVAFastSaver {

	/**
	 * Anzahl der Objekte, die in EINEM SQL-Statement gemeinsam in die DB geschrieben werden
	 */
	public static final int ROWS_PER_INSERT = 50;
	/**
	 * Intervall (Anzahl) der Meldungen über geschriebene Objekte
	 */
	public static final int LOG_INTV = 1000;

	public static void saveAll(boolean clear) throws SQLException {
		new SysMsg("EVAFastSaver.saveAll...");

		Datenbank db = new Datenbank("EVAFastSaver");

		//-------------------------------------------------------------
		// Hack: Leite das Speichern uebergangsweise zum fertig implementierten Teil
		// des neuen Load/Save-Systems um.
		Serializer newStuff = MigrationSerializerFactory.buildSerializer(db);

		// zur Zeit implementiert:
		// - Parteien.
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
		newStuff.saveAll();
		//-------------------------------------------------------------

		//EVAFastSaver.saveParteien(db, clear);
		//EVAFastSaver.saveAllianzen(db, clear);
		//EVAFastSaver.saveSteuern(db, clear);

//		EVAFastSaver.saveRegionen(db, clear);
//		EVAFastSaver.saveResourcen(db, clear);
//		EVAFastSaver.saveLuxus(db, clear);
//		EVAFastSaver.saveStrassen(db, clear);
//		EVAFastSaver.saveBuildings(db, clear);
//		EVAFastSaver.saveShips(db, clear);

//		EVAFastSaver.saveEinheiten(db, clear);
//		EVAFastSaver.saveItems(db, clear);
//		EVAFastSaver.saveSpells(db, clear);
//		EVAFastSaver.saveTalente(db, clear);
//		EVAFastSaver.saveEinheitenProperties(db);
//		EVAFastSaver.saveKontakte(db, clear);
//		EVAFastSaver.saveEffekte(db, clear);
		EVAFastSaver.saveBefehle(db, clear);

//		EVAFastSaver.saveProperties(db, clear);

//		EVAFastSaver.saveNeueSpieler(db);

		db.Close();
	}

	public static void saveParteien(Datenbank db, boolean clear) {
		String table = "partei";
		db.Truncate(table);
		db.DisableKeys(table);
		int cnt = 1;
		StringBuffer values = new StringBuffer();
		for (Partei p : Partei.PROXY) {
			// Partei 0 kommt nicht in die DB/aus der DB, es gibt sie aber trotzdem:
			if (p.getNummer() == 0) {
				continue;
			}

			// new Debug(p.toString());
			if (values.length() > 0) {
				values.append(", ");
			}
			values.append(Datenbank.MakeInsertValues(p.getDBValues()));

			if ((cnt % EVAFastSaver.ROWS_PER_INSERT) == 0) {
				db.myQuery = EVAFastSaver.makeQuery(table, p.getDBValues(), values);
				// new Debug(db.myQuery);
				int result = db.Update();

				values = new StringBuffer();
			}
			if ((cnt % EVAFastSaver.LOG_INTV) == 0) {
				new SysMsg("EVAFastSaver: " + cnt + " Parteien...");
			}
			cnt++;
		}
		if (values.length() > 0) {
			db.myQuery = EVAFastSaver.makeQuery(table, Partei.PROXY.get(0).getDBValues(), values);
			int result = db.Update();
		}
		db.EnableKeys(table);
		new SysMsg((cnt - 1) + " Parteien fertig.");
	}

	public static void saveAllianzen(Datenbank db, boolean clear) {
		for (Allianz a : Allianz.getAlle()) {
			if (!a.isValid()) {
				new SysErr("Problem mit Allianz...");
			}
		}

		String table = "allianzen";
		db.Truncate(table);
		db.DisableKeys(table);

		// wird nur für die Spaltennamen in der DB-Tabelle gebraucht:
		Map<String, Object> fields = (new Allianz()).getDBValues(AllianzOption.Gib);

		int cnt = 1;
		StringBuffer values = new StringBuffer();
		for (Partei p : Partei.PROXY) {
			for (int partnerNr : p.getAllianzen().keySet()) {
				Allianz a = p.getAllianz(partnerNr);
				for (AllianzOption ao : AllianzOption.values()) {
					if (ao != AllianzOption.Alles) {
						if (a.getOption(ao)) {
							if (values.length() > 0) {
								values.append(", ");
							}
							values.append(Datenbank.MakeInsertValues(a.getDBValues(ao)));

							if ((cnt % EVAFastSaver.ROWS_PER_INSERT) == 0) {
								db.myQuery = EVAFastSaver.makeQuery(table, fields, values);
								int result = db.Update();

								values = new StringBuffer();
							}
							if ((cnt % EVAFastSaver.LOG_INTV) == 0) {
								new SysMsg("EVAFastSaver: " + cnt + " Allianz-Optionen...");
							}
							cnt++;
						}
					}
				}
			}
		}
		if (values.length() > 0) {
			db.myQuery = EVAFastSaver.makeQuery(table, fields, values);
			int result = db.Update();
		}
		db.EnableKeys(table);
		new SysMsg((cnt - 1) + " Allianz-Optionen fertig.");
	}

	public static void saveSteuern(Datenbank db, boolean clear) {
		String table = "steuern";
		db.Truncate(table);
		db.DisableKeys(table);

		// wird nur für die Spaltennamen in der DB-Tabelle gebraucht:
		Map<String, Object> fields = (new Steuer(0, 0, 0)).getDBValues();

		int cnt = 1;
		StringBuffer values = new StringBuffer();
		for (Partei p : Partei.PROXY) {
			for (Steuer st : p.getSteuern()) {
				// TODO Ist dieses Verhalten erwünscht?
				// if (st.getRate() == p.getDefaultsteuer()) continue;

				if (values.length() > 0) {
					values.append(", ");
				}
				values.append(Datenbank.MakeInsertValues(st.getDBValues()));

				if ((cnt % EVAFastSaver.ROWS_PER_INSERT) == 0) {
					db.myQuery = EVAFastSaver.makeQuery(table, st.getDBValues(), values);
					int result = db.Update();

					values = new StringBuffer();
				}
				if ((cnt % EVAFastSaver.LOG_INTV) == 0) {
					new SysMsg("EVAFastSaver: " + cnt + " Partei-Steuersätze...");
				}
				cnt++;
			}
		}
		if (values.length() > 0) {
			db.myQuery = EVAFastSaver.makeQuery(table, fields, values);
			int result = db.Update();
		}
		db.EnableKeys(table);
		new SysMsg((cnt - 1) + " Partei-Steuersätze fertig.");
	}

	/** speichert die Properties von verschiedenen Objekten */
	public static void saveProperties(Datenbank db, boolean clear) {
//		EVAFastSaver.saveParteienProperties(db, "property_parteien", Partei.class);
//		EVAFastSaver.saveEffekteProperties(db, clear);
//		EVAFastSaver.saveRegionProperties(db);
//		EVAFastSaver.saveBuildingProperties(db, "property_gebaeude", Building.class);
	}

	/**
	 * @param db
	 * @param clear
	 */
//	public static void saveParteienProperties(Datenbank db, String table, Class<? extends Atlantis> objektTyp) {
//		db.Truncate(table);
//		db.DisableKeys(table);
//
//		int cnt = 1;
//		Map<String, Object> fields = Properties.getDBFields(objektTyp);
//
//		StringBuffer values = new StringBuffer();
//		for (Partei p : Partei.PROXY) {
//			for (String prop : p.getProperties()) {
//				if (p.getStringProperty(prop) == null) {
//					continue; // null-Werte werden nicht gespeichert
//				}
//				Map<String, Object> valueMap = p.getPropertys().getDBValues(p, prop);
//				if (valueMap == null) {
//					continue;
//				}
//
//				if (values.length() > 0) {
//					values.append(", ");
//				}
//
//				// parteien_property hat einen extra Primary Key
//				valueMap.put("id", cnt);
//				values.append(Datenbank.MakeInsertValues(valueMap));
//
//				if ((cnt % EVAFastSaver.ROWS_PER_INSERT) == 0) {
//					db.myQuery = EVAFastSaver.makeQuery(table, fields, values);
//					int result = db.Update();
//
//					values = new StringBuffer();
//				}
//
//				if ((cnt % EVAFastSaver.LOG_INTV) == 0) {
//					new SysMsg("EVAFastSaver: " + cnt + " Parteien-Properties...");
//				}
//				cnt++;
//			}
//		}
//		if (values.length() > 0) {
//			db.myQuery = EVAFastSaver.makeQuery(table, fields, values);
//			int result = db.Update();
//		}
//
//		db.EnableKeys(table);
//		new SysMsg((cnt - 1) + " Parteien-Properties fertig.");
//	}
//
//	/**
//	 * @param db
//	 * @param clear
//	 */
//	public static void saveBuildingProperties(Datenbank db, String table, Class<? extends Atlantis> objektTyp) {
//		db.Truncate(table);
//		db.DisableKeys(table);
//
//		int cnt = 1;
//		Map<String, Object> fields = Properties.getDBFields(objektTyp);
//
//		StringBuffer values = new StringBuffer();
//		for (Building b : Building.PROXY) {
//			if (b.getPropertys() == null) {
//				continue;
//			}
//			for (String prop : b.getPropertys().getPropertys()) {
//				Map<String, Object> valueMap = b.getPropertys().getDBValues(prop);
//				if (valueMap == null) {
//					continue;
//				}
//
//				if (values.length() > 0) {
//					values.append(", ");
//				}
//
//				values.append(Datenbank.MakeInsertValues(valueMap));
//
//				if ((cnt % EVAFastSaver.ROWS_PER_INSERT) == 0) {
//					db.myQuery = EVAFastSaver.makeQuery(table, fields, values);
//					int result = db.Update();
//
//					values = new StringBuffer();
//				}
//
//				if ((cnt % EVAFastSaver.LOG_INTV) == 0) {
//					new SysMsg("EVAFastSaver: " + cnt + " Gebäude-Properties...");
//				}
//				cnt++;
//			}
//		}
//		if (values.length() > 0) {
//			db.myQuery = EVAFastSaver.makeQuery(table, fields, values);
//			int result = db.Update();
//		}
//
//		db.EnableKeys(table);
//		new SysMsg((cnt - 1) + " Gebäude-Properties fertig.");
//	}

	// ------- Regionen ---------
	public static void saveRegionen(Datenbank db, boolean clear) {
		Set<Region> checkSet = new HashSet<Region>();
		for (Region r : Region.CACHE.values()) {
			if (checkSet.contains(r)) {
				new BigError(new RuntimeException("Regions.CACHE enthält eine Dopplung:" + r + " " + r.getCoords().xy()));
			}
			checkSet.add(r);
		}


		String table = "regionen";
		db.Truncate(table);
		db.DisableKeys(table);
		int cnt = 1;
		StringBuffer values = new StringBuffer();
		for (Region r : Region.CACHE.values()) {
			if (values.length() > 0) {
				values.append(", ");
			}
			values.append(Datenbank.MakeInsertValues(r.getDBValues()));

			if ((cnt % EVAFastSaver.ROWS_PER_INSERT) == 0) {
				db.myQuery = EVAFastSaver.makeQuery(table, r.getDBValues(), values);
				int result = db.Update();

				values = new StringBuffer();
			}
			if ((cnt % EVAFastSaver.LOG_INTV) == 0) {
				new SysMsg("EVAFastSaver: " + cnt + " Regionen...");
			}
			cnt++;
		}
		if (values.length() > 0) {
			db.myQuery = EVAFastSaver.makeQuery(table, new Ebene().getDBValues(), values);
			int result = db.Update();
		}
		db.EnableKeys(table);
		new SysMsg((cnt - 1) + " Regionen fertig.");
		if (clear) {
			Region.CACHE.clear();
		}
	}

	public static void saveResourcen(Datenbank db, boolean clear) {
		String table = "resourcen";
		db.Truncate(table);
		db.DisableKeys(table);
		int cnt = 1;
		StringBuffer values = new StringBuffer();
		Coords c = null;
		for (Region r : Region.CACHE.values()) {
			c = r.getCoords();
			for (Item res : r.getResourcen()) {
				if (values.length() > 0) {
					values.append(", ");
				}
				values.append(Datenbank.MakeInsertValues(res.getResourceDBValues(c)));

				if ((cnt % EVAFastSaver.ROWS_PER_INSERT) == 0) {
					db.myQuery = EVAFastSaver.makeQuery(table, res.getResourceDBValues(c), values);
					int result = db.Update();

					values = new StringBuffer();
				}
				if ((cnt % EVAFastSaver.LOG_INTV) == 0) {
					new SysMsg("EVAFastSaver: " + cnt + " Resourcen...");
				}
				cnt++;
			}
		}
		if (values.length() > 0) {
			db.myQuery = EVAFastSaver.makeQuery(table, (new Holz()).getResourceDBValues(c), values);
			int result = db.Update();
		}
		db.EnableKeys(table);
		new SysMsg((cnt - 1) + " Resourcen fertig.");
	}

	public static void saveLuxus(Datenbank db, boolean clear) {
		String table = "luxus";
		db.Truncate(table);
		db.DisableKeys(table);
		int cnt = 1;
		StringBuffer values = new StringBuffer();
		Coords c = null;
		for (Region r : Region.CACHE.values()) {
			c = r.getCoords();
			for (Nachfrage n : r.getLuxus()) {
				if (values.length() > 0) {
					values.append(", ");
				}
				values.append(Datenbank.MakeInsertValues(n.getDBValues(c)));

				if ((cnt % EVAFastSaver.ROWS_PER_INSERT) == 0) {
					db.myQuery = EVAFastSaver.makeQuery(table, n.getDBValues(c), values);
					int result = db.Update();

					values = new StringBuffer();
				}
				if ((cnt % EVAFastSaver.LOG_INTV) == 0) {
					new SysMsg("EVAFastSaver: " + cnt + " Luxuspreise...");
				}
				cnt++;
			}
		}
		if (values.length() > 0) {
			db.myQuery = EVAFastSaver.makeQuery(table, (new Nachfrage(Seide.class, 1)).getDBValues(c), values);
			int result = db.Update();
		}
		db.EnableKeys(table);
		new SysMsg((cnt - 1) + " Luxuspreise fertig.");
	}

	public static void saveStrassen(Datenbank db, boolean clear) {
		if (Region.CACHE.isEmpty()) {
			return; // keine Regionen, keine Straßen.
		}
		String table = "strassen";
		db.Truncate(table);
		db.DisableKeys(table);

		Map<String, Object> fields = new Ebene().getStrassenDBValues(Richtung.Nordosten);

		int cnt = 1;
		StringBuffer values = new StringBuffer();
		for (Region r : Region.CACHE.values()) {
			for (Richtung richtung : Richtung.values()) {
				if (r.getStrassensteine(richtung) > 0) {
					if (values.length() > 0) {
						values.append(", ");
					}
					values.append(Datenbank.MakeInsertValues(r.getStrassenDBValues(richtung)));

					if ((cnt % EVAFastSaver.ROWS_PER_INSERT) == 0) {
						db.myQuery = EVAFastSaver.makeQuery(table, fields, values);
						int result = db.Update();

						values = new StringBuffer();
					}
					if ((cnt % EVAFastSaver.LOG_INTV) == 0) {
						new SysMsg("EVAFastSaver: " + cnt + " Straßenteile...");
					}
					cnt++;
				}
			}
		}
		if (values.length() > 0) {
			db.myQuery = EVAFastSaver.makeQuery(table, fields, values);
			int result = db.Update();
		}
		db.EnableKeys(table);
		new SysMsg((cnt - 1) + " Straßenteile fertig.");
	}

	public static void saveBuildings(Datenbank db, boolean clear) {
		String table = "gebaeude";
		db.Truncate(table);
		db.DisableKeys(table);
		int cnt = 1;
		StringBuffer values = new StringBuffer();
		for (Building b : Building.PROXY) {
			if (values.length() > 0) {
				values.append(", ");
			}
			values.append(Datenbank.MakeInsertValues(b.getDBValues()));

			if ((cnt % EVAFastSaver.ROWS_PER_INSERT) == 0) {
				db.myQuery = EVAFastSaver.makeQuery(table, b.getDBValues(), values);
				int result = db.Update();

				values = new StringBuffer();
			}
			if ((cnt % EVAFastSaver.LOG_INTV) == 0) {
				new SysMsg("EVAFastSaver: " + cnt + " Gebäude...");
			}
			cnt++;
		}
		if (values.length() > 0) {
			db.myQuery = EVAFastSaver.makeQuery(table, Building.PROXY.iterator().next().getDBValues(), values);
			int result = db.Update();
		}
		db.EnableKeys(table);
		new SysMsg((cnt - 1) + " Gebäude fertig.");
	}

	public static void saveShips(Datenbank db, boolean clear) {
		String table = "schiffe";
		db.Truncate(table);
		db.DisableKeys(table);
		int cnt = 1;
		StringBuffer values = new StringBuffer();
		for (Ship s : Ship.PROXY) {
			if (values.length() > 0) {
				values.append(", ");
			}
			values.append(Datenbank.MakeInsertValues(s.getDBValues()));

			if ((cnt % EVAFastSaver.ROWS_PER_INSERT) == 0) {
				db.myQuery = EVAFastSaver.makeQuery(table, s.getDBValues(), values);
				int result = db.Update();

				values = new StringBuffer();
			}
			if ((cnt % EVAFastSaver.LOG_INTV) == 0) {
				new SysMsg("EVAFastSaver: " + cnt + " Schiffe...");
			}
			cnt++;
		}
		if (values.length() > 0) {
			db.myQuery = EVAFastSaver.makeQuery(table, Ship.PROXY.get(0).getDBValues(), values);
			int result = db.Update();
		}
		db.EnableKeys(table);
		new SysMsg((cnt - 1) + " Schiffe fertig.");
	}

	public static void saveEinheiten(Datenbank db, boolean clear) {
		String table = "einheiten";
		db.Truncate(table);
		db.DisableKeys(table);
		int cnt = 1;
		StringBuffer values = new StringBuffer();

		Unit prototyp = null;
		for (Unit u : Unit.CACHE) {
			if (prototyp == null) {
				prototyp = u;
			}

			if (values.length() > 0) {
				values.append(", ");
			}
			values.append(Datenbank.MakeInsertValues(u.getDBValues()));

			if ((cnt % EVAFastSaver.ROWS_PER_INSERT) == 0) {
				db.myQuery = EVAFastSaver.makeQuery(table, u.getDBValues(), values);
				int result = db.Update();

				values = new StringBuffer();
			}
			if ((cnt % EVAFastSaver.LOG_INTV) == 0) {
				new SysMsg("EVAFastSaver: " + cnt + " Einheiten...");
			}
			cnt++;
		}
		if (values.length() > 0) {
			db.myQuery = EVAFastSaver.makeQuery(table, prototyp.getDBValues(), values);
			db.Update();
		}
		db.EnableKeys(table);
		new SysMsg((cnt - 1) + " Einheiten fertig.");
	}

	public static void saveItems(Datenbank db, boolean clear) {
		String table = "items";
		db.Truncate(table);
		db.DisableKeys(table);
		int cnt = 1;
		StringBuffer values = new StringBuffer();
		Unit unit = null;
		for (Unit u : Unit.CACHE) {
			for (Item it : u.getItems()) {
				if (it.getAnzahl() == 0) {
					continue;
				}

				if (values.length() > 0) {
					values.append(", ");
				}
				values.append(Datenbank.MakeInsertValues(it.getItemDBValues(u)));

				if ((cnt % EVAFastSaver.ROWS_PER_INSERT) == 0) {
					db.myQuery = EVAFastSaver.makeQuery(table, it.getItemDBValues(u), values);
					int result = db.Update();

					values = new StringBuffer();
				}
				if ((cnt % EVAFastSaver.LOG_INTV) == 0) {
					new SysMsg("EVAFastSaver: " + cnt + " Gegenstände...");
				}
				cnt++;
			}
			unit = u;
		}
		if (values.length() > 0) {
			db.myQuery = EVAFastSaver.makeQuery(table, (new Holz()).getItemDBValues(unit), values);
			int result = db.Update();
		}
		db.EnableKeys(table);
		new SysMsg((cnt - 1) + " Gegenstände fertig.");
	}

	public static void saveSpells(Datenbank db, boolean clear) {
		String table = "spells";
		db.Truncate(table);
		db.DisableKeys(table);
		int cnt = 1;
		StringBuffer values = new StringBuffer();
		Unit unit = null;
		for (Unit u : Unit.CACHE) {
			if (u.getSpells().isEmpty()) {
				continue;
			}

			for (Spell s : u.getSpells()) {
				if (values.length() > 0) {
					values.append(", ");
				}
				values.append(Datenbank.MakeInsertValues(s.getDBValues(u)));

				if ((cnt % EVAFastSaver.ROWS_PER_INSERT) == 0) {
					db.myQuery = EVAFastSaver.makeQuery(table, s.getDBValues(u), values);
					int result = db.Update();

					values = new StringBuffer();
				}
				if ((cnt % EVAFastSaver.LOG_INTV) == 0) {
					new SysMsg("EVAFastSaver: " + cnt + " Zaubersprüche...");
				}
				cnt++;
			}
			unit = u;
		}
		if (values.length() > 0) {
			db.myQuery = EVAFastSaver.makeQuery(table, (new HainDerTausendEichen()).getDBValues(unit), values);
			int result = db.Update();
		}
		db.EnableKeys(table);
		new SysMsg((cnt - 1) + " Zaubersprüche fertig.");
	}

	public static void saveTalente(Datenbank db, boolean clear) {
		String table = "skills";
		db.Truncate(table);
		db.DisableKeys(table);
		int cnt = 1;
		StringBuffer values = new StringBuffer();
		Unit unit = null;
		for (Unit u : Unit.CACHE) {
			for (Skill sk : u.getSkills()) {
				if (sk.getLerntage() == 0) {
					continue;
				}

				if (values.length() > 0) {
					values.append(", ");
				}
				values.append(Datenbank.MakeInsertValues(sk.getDBValues(u)));

				if ((cnt % EVAFastSaver.ROWS_PER_INSERT) == 0) {
					db.myQuery = EVAFastSaver.makeQuery(table, sk.getDBValues(u), values);
					int result = db.Update();

					values = new StringBuffer();
				}
				if ((cnt % EVAFastSaver.LOG_INTV) == 0) {
					new SysMsg("EVAFastSaver: " + cnt + " Talente...");
				}
				cnt++;
			}
			unit = u;
		}
		if (values.length() > 0) {
			db.myQuery = EVAFastSaver.makeQuery(table, (new Wahrnehmung()).getDBValues(unit), values);
			int result = db.Update();
		}
		db.EnableKeys(table);
		new SysMsg((cnt - 1) + " Talente fertig.");
	}

	/**
	 * @param db
	 * @param clear
	 */
//	public static void saveEinheitenProperties(Datenbank db) {
//		String table = "property_einheiten";
//		db.Truncate(table);
//		db.DisableKeys(table);
//		int cnt = 1;
//		StringBuffer values = new StringBuffer();
//		Map<String, Object> fields = Properties.getDBFields(Unit.class);
//		for (Unit u : Unit.CACHE) {
//			if (u.getPropertys() == null) {
//				continue;
//			}
//			for (String p : u.getPropertys().getPropertys()) {
//				Map<String, Object> valueMap = u.getPropertys().getDBValues(p);
//				if (valueMap == null) {
//					continue;
//				}
//
//				if (values.length() > 0) {
//					values.append(", ");
//				}
//				values.append(Datenbank.MakeInsertValues(valueMap));
//
//				if ((cnt % EVAFastSaver.ROWS_PER_INSERT) == 0) {
//					db.myQuery = EVAFastSaver.makeQuery(table, fields, values);
//					int result = db.Update();
//
//					values = new StringBuffer();
//				}
//
//				if ((cnt % EVAFastSaver.LOG_INTV) == 0) {
//					new SysMsg("EVAFastSaver: " + cnt + " Einheiten-Properties...");
//				}
//				cnt++;
//			}
//		}
//		if (values.length() > 0) {
//			db.myQuery = EVAFastSaver.makeQuery(table, fields, values);
//			int result = db.Update();
//		}
//		db.EnableKeys(table);
//		new SysMsg((cnt - 1) + " Einheiten-Properties fertig.");
//	}

	public static void saveKontakte(Datenbank db, boolean clear) {
		String table = "kontakte";
		db.Truncate(table);
		db.DisableKeys(table);
		int cnt = 1;
		StringBuffer values = new StringBuffer();

		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("einheit", "0");
		fields.put("partner", "0");

		for (Unit u : Unit.CACHE) {
			fields.put("einheit", u.getNummer());
			for (int other : u.Kontakte) {
				if (values.length() > 0) {
					values.append(", ");
				}

				fields.put("partner", other);

				values.append(Datenbank.MakeInsertValues(fields));

				if ((cnt % EVAFastSaver.ROWS_PER_INSERT) == 0) {
					db.myQuery = EVAFastSaver.makeQuery(table, fields, values);
					int result = db.Update();

					values = new StringBuffer();
				}
				if ((cnt % EVAFastSaver.LOG_INTV) == 0) {
					new SysMsg("EVAFastSaver: " + cnt + " Kontakte...");
				}
				cnt++;
			}
		}
		if (values.length() > 0) {
			db.myQuery = EVAFastSaver.makeQuery(table, fields, values);
			int result = db.Update();
		}
		db.EnableKeys(table);
		new SysMsg((cnt - 1) + " Kontakte fertig.");
	}

	/**
	 * Diese Routine speichert die &quot;Kopfdaten&quot; der Einheiten-
	 * Effekte - NUR der Einheiten-Effekte.
	 * @param db
	 * @param clear
	 */
	public static void saveEffekte(Datenbank db, boolean clear) {
		String table = "effekt_einheiten";
		db.Truncate(table);
		db.DisableKeys(table);
		int cnt = 1;
		Map<String, Object> fields = null;
		StringBuffer values = new StringBuffer();
		Unit unit = null;
		for (Unit u : Unit.CACHE) {
			for (Effect e : u.getEffects()) {
				// Besonderheit bei den Effekten: Sie entscheiden selbst, ob sie noch speicher-würdig sind.
				if (e.toDestroy()) {
					continue;
				}

				if (fields == null) {
					fields = e.getDBValues(u);
				}

				if (values.length() > 0) {
					values.append(", ");
				}
				values.append(Datenbank.MakeInsertValues(e.getDBValues(u)));

				if ((cnt % EVAFastSaver.ROWS_PER_INSERT) == 0) {
					db.myQuery = EVAFastSaver.makeQuery(table, e.getDBValues(u), values);
					int result = db.Update();

					values = new StringBuffer();
				}

				if ((cnt % EVAFastSaver.LOG_INTV) == 0) {
					new SysMsg("EVAFastSaver: " + cnt + " Einheiten-Effekte...");
				}
				cnt++;
			}
			unit = u;
		}
		if (values.length() > 0) {
			db.myQuery = EVAFastSaver.makeQuery(table, fields, values);
			int result = db.Update();
		}
		db.EnableKeys(table);
		new SysMsg((cnt - 1) + " Einheiten-Effekte fertig.");
	}

	/**
	 * Diese Routine schreibt die Properties aller Effekte in die Datenbank, sofern
	 * die Effekte in Effect.PROXY registriert sind - das sollten sie also
	 * @param db
	 * @param clear
	 */
//	public static void saveEffekteProperties(Datenbank db, boolean clear) {
//		if (Effect.PROXY.size() > 0) {
//			// ich setze voraus, dass alle Einheiten-Effekt-Properties in die selbe
//			// Tabelle geschrieben werden...
//			String table = "property_effekt";
//			db.Truncate(table);
//			db.DisableKeys(table);
//
//			Map<String, Object> fields = null;
//			int cnt = 1;
//			StringBuffer values = new StringBuffer();
//
//			for (Effect e : Effect.PROXY) {
//				// Besonderheit bei den Effekten: Sie entscheiden selbst, ob sie noch speicher-würdig sind.
//				if (e.toDestroy()) {
//					continue;
//				}
//
//				for (String p : e.getPropertys().getPropertys()) {
//					fields = e.getPropertys().getDBValues(p);
//					if (fields == null) {
//						continue;
//					}
//
//					if (values.length() > 0) {
//						values.append(", ");
//					}
//
//					values.append(Datenbank.MakeInsertValues(fields));
//
//					if ((cnt % EVAFastSaver.ROWS_PER_INSERT) == 0) {
//						db.myQuery = EVAFastSaver.makeQuery(table, fields, values);
//						int result = db.Update();
//
//						values = new StringBuffer();
//					}
//
//					if ((cnt % EVAFastSaver.LOG_INTV) == 0) {
//						new SysMsg("EVAFastSaver: " + cnt + " Effekte-Properties...");
//					}
//					cnt++;
//				}
//			}
//
//			if (values.length() > 0) {
//				db.myQuery = EVAFastSaver.makeQuery(table, fields, values);
//				int result = db.Update();
//			}
//			db.EnableKeys(table);
//			new SysMsg((cnt - 1) + " Effekte-Properties fertig.");
//		}
//	}
//
//	public static void saveRegionProperties(Datenbank db) {
//		String table = "property_regionen";
//		db.Truncate(table);
//		db.DisableKeys(table);
//
//		Map<String, Object> fields = null;
//		Map<String, Object> fieldsPrototype = null;
//		int cnt = 1;
//		StringBuffer values = new StringBuffer();
//
//		for (Region r : Region.CACHE.values()) {
//			if (r.getPropertys() == null) {
//				continue; // Hot-Fix ... die Tabelle wird beim Debuggen nicht richtig initialisiert
//			}
//			for (String p : r.getPropertys().getPropertys()) {
//				fields = r.getPropertys().getDBValues(p);
//				if (fields == null) {
//					continue;
//				} else {
//					if (fieldsPrototype == null) {
//						fieldsPrototype = fields;
//					}
//				}
//
//				if (values.length() > 0) {
//					values.append(", ");
//				}
//
//				values.append(Datenbank.MakeInsertValues(fields));
//
//				if ((cnt % EVAFastSaver.ROWS_PER_INSERT) == 0) {
//					db.myQuery = EVAFastSaver.makeQuery(table, fields, values);
//					int result = db.Update();
//					values = new StringBuffer();
//				}
//				if ((cnt % EVAFastSaver.LOG_INTV) == 0) {
//					new SysMsg("EVAFastSaver: " + cnt + " Region-Properties...");
//				}
//				cnt++;
//			}
//		}
//
//		if (values.length() > 0) {
//			db.myQuery = EVAFastSaver.makeQuery(table, fieldsPrototype, values);
//			int result = db.Update();
//		}
//		db.EnableKeys(table);
//		new SysMsg((cnt - 1) + " Region-Properties fertig.");
//	}

	public static void saveBefehle(Datenbank db, boolean clear) {
		String table = "befehle";
		db.Truncate(table);
		db.DisableKeys(table);

		Map<String, Object> fields = null;
		int cnt = 1;
		StringBuffer values = new StringBuffer();
		for (Unit u : Unit.CACHE) {
			if (u.getCoords().getWelt() == 0) {
				continue; // virtuelle Einheiten haben keine Befehle! Schtonk!
			}
			for (Einzelbefehl eb : u.BefehleExperimental) {
				if (values.length() > 0) {
					values.append(", ");
				}

				fields = eb.getDBValues(u);
				values.append(Datenbank.MakeInsertValues(fields));

				if ((cnt % EVAFastSaver.ROWS_PER_INSERT) == 0) {
					db.myQuery = EVAFastSaver.makeQuery(table, fields, values);
					int result = db.Update();

					values = new StringBuffer();
				}
				if ((cnt % EVAFastSaver.LOG_INTV) == 0) {
					new SysMsg("EVAFastSaver: " + cnt + " Befehle...");
				}
				cnt++;
			}
		}
		if (values.length() > 0) {
			db.myQuery = EVAFastSaver.makeQuery(table, fields, values);
			int result = db.Update();
		}
		db.EnableKeys(table);
		new SysMsg((cnt - 1) + " Befehle fertig.");
	}

	public static void saveNeueSpieler(Datenbank db) {
		String table = "neuespieler";
		db.Truncate(table);
		db.DisableKeys(table);

		Map<String, Object> fields = NeuerSpieler.getDBFields();

		int cnt = 1;
		StringBuffer values = new StringBuffer();
		for (NeuerSpieler n : NeuerSpieler.PROXY) {
			if (values.length() > 0) {
				values.append(", ");
			}
			values.append(Datenbank.MakeInsertValues(n.getDBValues()));

			if ((cnt % EVAFastSaver.ROWS_PER_INSERT) == 0) {
				db.myQuery = EVAFastSaver.makeQuery(table, fields, values);
				// new Debug(db.myQuery);
				int result = db.Update();

				values = new StringBuffer();
			}
			if ((cnt % EVAFastSaver.LOG_INTV) == 0) {
				new SysMsg("EVAFastSaver: " + cnt + " neue Spieler...");
			}
			cnt++;
		}
		if (values.length() > 0) {
			db.myQuery = EVAFastSaver.makeQuery(table, fields, values);
			int result = db.Update();
		}
		db.EnableKeys(table);
		new SysMsg((cnt - 1) + " neue Spieler fertig.");
	}

	public static String makeQuery(String table, Map<String, Object> fields, StringBuffer values) {
		return "INSERT INTO " + table + " " + Datenbank.MakeInsertInto(fields) + " VALUES " + values + ";";
	}
}
