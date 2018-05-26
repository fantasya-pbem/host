package de.x8bit.Fantasya.Host.ManualTests;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Richtung;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Regions.Berge;
import de.x8bit.Fantasya.Atlantis.Regions.Ebene;
import de.x8bit.Fantasya.Atlantis.Regions.Ozean;
import de.x8bit.Fantasya.Atlantis.Regions.Wald;
import de.x8bit.Fantasya.Atlantis.Skills.Wahrnehmung;
import de.x8bit.Fantasya.Atlantis.Units.Goblin;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.Datenbank;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.Main;
import de.x8bit.Fantasya.Host.EVA.EVABase;
import de.x8bit.Fantasya.Host.EVA.util.BefehleKlassifizieren;
import de.x8bit.Fantasya.Host.EVA.util.EVAFastLoader;
import de.x8bit.Fantasya.Host.EVA.util.EVAFastSaver;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.util.Codierung;

/**
 * Test-Routine fürs Fantasya-Debuggen,
 * lässt sich per Kommandozeilen-Schalter bei Main starten: -testworld
 *
 * Sicherheitshalber wird geprüft, ob der Name der Datenbank ein "test" enthält,
 * d.h. getestet werden DARF NICHT in der echten Spieldatenbank, die DB wird
 * nämlich jedes Mal frisch initialisiert (und vorher geleert).
 *
 * Der komplette Aufruf muss also mindestens so aussehen:
 * Main -datenbank fantasya-test -testworld
 *
 * Konkrete Test-Szenarien sind als eigene Klassen angelegt, wenn es um registrierte
 * Bugs geht, heißen diese "Mantis####" (Nummer des Mantis-Reports).
 *
 * Das Vorgehen zum eigentlichen Test: Normalerweise einen ganz normalen
 * ZAT ausführen (natürlich auf der gleichen DB!) und dann in
 * den Reports (ggf. in der DB - Messages) schauen, ob alles gelaufen
 * ist wie erwartet.
 *
 * @author hapebe
 */
public class TestWorld {

	/** wird in createParteien gesetzt */
	Partei spieler1;
    boolean continueWithZAT;

    public static List<Region> RegionsVorrat = null;

	public TestWorld() {
		EVABase.loadAllEx();

        if (GameRules.getRunde() <= 1) {
            createParteien();
            createBasisEinheiten(spieler1);
        }
        continueWithZAT = true;
	}

	/**
     * <p>Instantiiert die angegebene Testklasse - diese muss von TestBase oder TestSuite abgeleitetet sein.</p>
     * <p>Das Verhalten hängt auch von GameRules.getRunde() ab: Bei "1" wird der Test eingerichtet, in den 
     * nachfolgenden Runden wird jeweils TestBase.verifyTest() aufgerufen.</p>
     * @param args_testworld Name der Testklasse ohne den Prafix 'de.x8bit.Fantasya.Host.ManualTests.'
     */
    public TestWorld(String args_testworld) {
		this();

		if (args_testworld == null) {
			new SysMsg("Keine Test-Klasse für -testworld angegeben!");
			System.exit(0);
		}
		
		TestBase tb = null;
		try {
			tb = (TestBase) Class.forName("de.x8bit.Fantasya.Host.ManualTests." + args_testworld).newInstance();
            new TestMsg ("    " + tb.getClass().getName() + " initialisiert.");
		} catch (InstantiationException ex) {
			new BigError("InstantiationException: " + ex.getMessage());
		} catch (IllegalAccessException ex) {
			new BigError("IllegalAccessException: " + ex.getMessage());
		} catch (ClassNotFoundException ex) {
			new BigError("ClassNotFoundException: " + ex.getMessage());
		}

		if (tb != null) {
			tb.setTestWorld(this);

            if (GameRules.getRunde() <= 1) {
                // Test einrichten:
                try {
					tb.setupTest();
                    // bei dieser Speicher-Strategie müssen die Befehle noch "normiert" werden:
                    for (Unit u : Unit.CACHE) {
                        int i = 0;
                        for (String befehl : u.Befehle) {
                            try {
                                Einzelbefehl eb = new Einzelbefehl(u, u.getCoords(), befehl, i);
                                u.BefehleExperimental.add(eb);
                            } catch (IllegalArgumentException ex) {
                                new Fehler(ex.getMessage(), u);
                                continue;
                            }
                            i ++;
                        }
                    }
                    EVAFastSaver.saveAll(false);
                } catch (RuntimeException ex) {
                    new BigError(ex);
                } catch (SQLException ex) {
                    new BigError(ex);
                }

                new TestMsg("Test-Setup " + args_testworld + " wurde angelegt.");
            } else {
				if (Main.getBFlag("EVA")) try {
					EVAFastLoader.loadAll();
					new BefehleKlassifizieren();
				} catch (SQLException ex) {
					new BigError(ex);
				}

                // wenn die Runde nicht mehr 1 ist, dann die Ergebnisse überprüfen:
                try {
                    boolean success = tb.verifyTest();
                    if (success) {
                        new TestMsg("*** " + tb.getClass().getSimpleName() + " bestanden. ***");
                    } else {
                        new TestMsg("!!! " + tb.getClass().getSimpleName() + " nicht bestanden. !!!");
                    }
                } catch (RuntimeException ex) { new BigError(ex); }
                continueWithZAT = false;
            }
		}

	}

	protected void createBasisEinheiten(Partei p) {
		// geeignete Heimat-Regionen finden - sollte es eigentlich rein zufällig geben:
		List<Region> list = this.getAlleRegionen();
		list = this.nurBetretbar(list);
		list = this.nurTerrain(list, Ebene.class);
		list = this.nurNachbarVon(list, Ozean.class);
		list = this.nurNachbarVon(list, Wald.class);
		list = this.nurNachbarVon(list, Berge.class);
		list = this.nurUnbewohnte(list);
		if (list.isEmpty()) {
			new BigError(new RuntimeException("Ooops - keine Startregion für " + p + " gefunden."));
		}

		Region ebene = list.get(0);
		Region wald = null;
		Region berge = null;
		for (Region r:ebene.getNachbarn()) {
			if (r.getTyp().equalsIgnoreCase(Wald.class.getSimpleName())) wald = r;
			if (r.getTyp().equalsIgnoreCase(Berge.class.getSimpleName())) berge = r;
		}
		if ((wald == null) || (berge == null)) {
			new BigError(new RuntimeException("Nicht die passenden Regionen gefunden?"));
		}

		getAlleRegionen().remove(ebene);
		getAlleRegionen().remove(wald);
		getAlleRegionen().remove(berge);

		// Ursprung für Spieler "korrigieren"
		p.setUrsprung(ebene.getCoords());

		// Chef:
		Unit u = Unit.CreateUnit("Mensch", p.getNummer(), ebene.getCoords());
		u.setPersonen(1);
		u.setSkill(Wahrnehmung.class, 840);
		u.setItem(Silber.class, GameRules.getRunde() * 100 + 10000);
		u.setName("Chef");

		// betroffene Region aus dem Cache entfernen:
			//System.out.println("PROXY before: "+Region.PROXY.size());
		if (!Main.getBFlag("EVA")) Region.CACHE.remove(ebene.getCoords());
			//System.out.println("PROXY after: "+Region.PROXY.size());

	}

	/**
	 * erzeugt drei Parteien: 0 für den Spielleiter, 1 als "normales" Volk und 'dark' als Monster-Partei.
	 */
	protected void createParteien() {
		Datenbank db = new Datenbank("TestWorld - Basic Create");
		GameRules.Load();

//		// Partei 0 - für den "Lord of Atlantis"
//		// TODO Partei.Nummer ohne AUTO_INCREMENT anlegen, dann klappt's mit der 0
//		if (false) {
//			Partei f = createPartei(Unit.class);
//			// zu 0 ändern:
//			if (!Main.getBFlag("EVA")) {
//				db.CreateUpdate("partei");
//				db.AddFirstSet("nummer", 0);
//				db.AddNextSet("id", Codierung.toBase36(0));
//				db.AddFirstWhere("nummer", f.getNummer());
//				db.Update();
//			} else {
//				f.setNummer(0);
//				f.setName("Lords of Atlantis");
//				f.setMonster(1);
//			}
//			f = Partei.getPartei(0);
//			new SysMsg("Partei 0 erzeugt - " + f.toString());
//		}

		// jetzt den 1. Spieler anlegen
		Partei f = createPartei(Mensch.class);
		f.setName("Test-Boss");
		f.setEMail("mail1");
		f.setRasse("Mensch");
		f.setNMR(GameRules.getRunde());
		f.setDefaultsteuer(10);
		f.setPassword("online");
		spieler1 = f;
		new SysMsg("neuen Spieler erzeugt - " + f.toString());

		// ...und noch 'dark' !
		Partei schonDa = Partei.getPartei(Codierung.fromBase36("dark"));
		if (schonDa == null) {
			f = createPartei(Goblin.class);
			f.setName("Monster");
			// zu 'dark' ändern:
			if (!Main.getBFlag("EVA")) {
				db.CreateUpdate("partei");
				db.AddFirstSet("nummer", Codierung.fromBase36("dark"));
				db.AddNextSet("monster", 1);
				db.AddNextSet("id", "dark");
				db.AddFirstWhere("nummer", f.getNummer());
				db.Update();
			} else {
				f.setNummer(Codierung.fromBase36("dark"));
				f.setMonster(1);
			}
			f = Partei.getPartei(Codierung.fromBase36("dark"));
			new SysMsg("Monster-Partei erzeugt - " + f.toString());
		}

		db.Close();

		if (Main.getBFlag("EVA")) try {
			EVAFastSaver.saveAll(false);
		} catch (SQLException ex) {
			new BigError(ex);
		}
	}

	public Partei createPartei(Class<? extends Unit> rasse) {
		Partei f = Partei.Create();
		f.setName("Partei " + f.getNummer());
		f.setEMail("none");
		f.setRasse(rasse.getSimpleName());
		f.setNMR(GameRules.getRunde());
		f.setDefaultsteuer(10);
		Partei.PROXY.add(f);
		return f;
	}

	public synchronized List<Region> getAlleRegionen() {
		if (TestWorld.RegionsVorrat == null) {
			if (Main.getBFlag("EVA")) {
				TestWorld.RegionsVorrat = Collections.synchronizedList(new ArrayList<Region>());
				TestWorld.RegionsVorrat.addAll(Region.CACHE.values());
			} else {
				TestWorld.RegionsVorrat = Collections.synchronizedList(new ArrayList<Region>());
				try {
					Datenbank db = new Datenbank("TestWorld - alle Regionen suchen");
					db.CreateSelect("regionen", "r");
					ResultSet rs = db.Select();
					while(rs.next()) {
						int x = rs.getInt("koordx");
						int y = rs.getInt("koordy");
						int welt = rs.getInt("welt");

						TestWorld.RegionsVorrat.add(Region.Load(x, y, welt));
					}
					db.Close();
				} catch (Exception ex) {
					new BigError("SQL-Fehler: "+ex.getMessage());
				}
			}
		}

        return TestWorld.RegionsVorrat;
	}

	public synchronized  List<Region> nurBetretbar(List<Region> in) {
		List<Region> retval = new ArrayList<Region>();
		for (Region r:in) {
			if (r.istBetretbar(null)) retval.add(r);
		}
		return retval;
	}

	public synchronized  List<Region> nurUnbewohnte(List<Region> in) {
		List<Region> retval = new ArrayList<Region>();
		for (Region r:in) {
			if (r.getUnits().isEmpty()) retval.add(r);
		}
		return retval;
	}

	public synchronized List<Region> nurTerrain(List<Region> in, Class<? extends Region> type) {
		List<Region> retval = new ArrayList<Region>();
		for (Region r:in) {
			if (r.getTyp().equalsIgnoreCase(type.getSimpleName())) retval.add(r);
		}
		return retval;
	}

	/**
	 * liefert alle Regionen die Nachbarn von type sind
	 * @param in - eine Regionsliste
	 * @param type - dieser Type ist der Ursprung
	 * @return
	 */
	public List<Region> nurNachbarVon(List<Region> in, Class<?> type) {
		List<Region> retval = new ArrayList<Region>();
		for (Region r:in) {
			for (Region nachbar:r.getNachbarn()) {
				if (nachbar.getTyp().equalsIgnoreCase(type.getSimpleName())) {
					retval.add(r);
					break;
				}
			}
		}
		return retval;
	}

	/**
	 * liefert die Richtung in der eine Nachbarregion liegt
	 * @param source - von dieser Region gehts los
	 * @param destinationregiontype - das ist die gesuchte Nachbarregion
	 * @return die gefundene Richtung
	 */
	public Richtung getRichtung(Region source, Class<?> destinationregiontype)
	{
		for(Richtung r : Richtung.values())
		{
			Region nachbar = Region.Load(source.getCoords().shift(r));
			if (nachbar.getClass().equals(destinationregiontype)) return r;
		}
		return null;
	}
	
	public Partei getSpieler1() {
        if (spieler1 == null) {
			if (Main.getBFlag("EVA")) {
				for (Partei maybe : Partei.PROXY) {
					if (maybe.getName().equals("Test-Boss")) {
						spieler1 = maybe;
						break;
					}
				}
			} else {
				// DB-ZAT
				spieler1 = Partei.getPartei(1);
			}
		}
		return spieler1;
	}

    public boolean shouldContinueWithZAT() {
        return continueWithZAT;
    }

    public void setContinueWithZAT(boolean continueWithZAT) {
        this.continueWithZAT = continueWithZAT;
    }


}
