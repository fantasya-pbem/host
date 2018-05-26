package de.x8bit.Fantasya.Host;

import java.util.ArrayList;

import de.x8bit.Fantasya.Atlantis.Atlantis;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Richtung;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Welt;
import de.x8bit.Fantasya.Atlantis.Helper.Nachfrage;
import de.x8bit.Fantasya.Atlantis.Helper.StartPosition;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Items.Eisen;
import de.x8bit.Fantasya.Atlantis.Items.Eisenschild;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Items.Holzschild;
import de.x8bit.Fantasya.Atlantis.Items.Kettenhemd;
import de.x8bit.Fantasya.Atlantis.Items.Plattenpanzer;
import de.x8bit.Fantasya.Atlantis.Items.Schwert;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Items.Speer;
import de.x8bit.Fantasya.Atlantis.Items.Stein;
import de.x8bit.Fantasya.Atlantis.Kampfposition;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.Debug;
import de.x8bit.Fantasya.Atlantis.Messages.Greetings;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.Atlantis.Regions.*;
import de.x8bit.Fantasya.Atlantis.Skills.*;
import de.x8bit.Fantasya.Atlantis.Units.Ork;
import de.x8bit.Fantasya.Host.EVA.util.NeuerSpieler;
import de.x8bit.Fantasya.Host.Terraforming.AltInsel;
import de.x8bit.Fantasya.Host.Terraforming.ProtoInsel;
import de.x8bit.Fantasya.util.Random;
import java.util.List;

public class NeueSpieler
{
	public NeueSpieler() {
        NeueSpielerAussetzen();
	}

	/** Liste aller Regionen die ein Terraforming über isch ergehen lassen müssen */
	private static ArrayList<Region> terraforming = new ArrayList<Region>();;
	
	/**
	 * setzt alle neuen Spieler aus
	 */
	public static void NeueSpielerAussetzen() {
		for (NeuerSpieler n : NeuerSpieler.PROXY) {
			// jetzt den Spieler anlegen
			Partei f = Partei.Create();
			if (n.getName() == null || n.getName().length() == 0) {
				f.setName("Volk " + f.getNummerBase36());
			}
			else {
				f.setName(n.getName());
			}
			if (n.getDescription() != null && n.getDescription().length() > 0) {
				f.setBeschreibung(n.getDescription());
			}
			f.setEMail(n.getEmail());
			f.setRasse(n.getRasse().getSimpleName());
			f.setNMR(GameRules.getRunde());
			f.setDefaultsteuer(10);
			f.setUserId(n.getUserId());
			f.setOwnerId(n.getUserId() * 10000 + f.getAlter());
			if (n.getPassword() != null && n.getPassword().length() > 0) {
				f.setPassword(n.getPassword());
			}
			Partei.PROXY.add(f);
			new SysMsg("neuen Spieler erzeugt - " + f);

			// *juhu* ... ein neuer Spieler ... der braucht seine Einheiten
			String tarnung = "";
			if (n.getTarnung() != null) tarnung = n.getTarnung().getSimpleName();
			if (n.getInsel() >= 0)	{
				NeueEinheiten(
						FreieRegion(1, n.getInsel()),
						f,
						tarnung,
						n.getHolz(),
						n.getSteine(),
						n.getEisen()
				);

				// so ... nun noch begrüßen
				new Greetings(
						f,
						"Willkommen auf Fantasya - Dein Passwort findest Du in der ZR-Datei, \n" +
						"dies ist die Zugvorlage. Die NR-Datei enthält die Auswertung als \n" +
						"Text. Die CR-Datei ist für Magellan oder andere grafische Clients \n" +
						"gedacht. \n" + 
                        "\n" +
						"Fantasya ist zur Zeit leider noch \"Beta\", das Spiel wird aber soweit \n" +
						"es das Privatleben zulässt weiter entwickelt."
				);

			} else	{
				// es wurde ein Monstervolk ausgesetzt
				// die Einheiten werden entsprechnd in Monster()
				// geplant und verteilt
				// die Spalte "monster" muss noch von Hand auf 1 gesetzt werden !!
			}

		}

		// wer jetzt nicht dabei ist, der ist nicht dabei:
		NeuerSpieler.PROXY.clear();
	}
	
	/**
	 * erzeugt für den neuen Spieler die zugehörigen Einheiten ... in F2 wird
	 * <ul>
	 * <li>eine Einheit (10 Personen) mit Treiben <b>&</b> Unterhaltung (je 300 LT) ausgesetzt
	 * <li>eine Einheit (1 Person) mit Wahrnehmung (840 LT) + Steine + Holz (evt. noch Eisen)
	 * <li>drei Einheiten (je 1 Person) in den Nachbarregionen mit je einem Zufallstalent (300 - 640 LT)
	 * </ul>
	 * @param region - die Startregion
	 * @param volk - dieses Volk bekommt die Einheiten
	 * @param tarnung - Rassentarnung bei Echsen
	 * @param holz - Start-Holz
	 * @param steine - Start-Steine
	 * @param eisen - Start-Eisen
	 */
	@SuppressWarnings("unchecked")
	private static void NeueEinheiten(Region region, Partei volk, String tarnung, int holz, int steine, int eisen)
	{
        // Bonus-Faktor für Spätstarter:
        // 1% pro Runde, multiplikativ
        // 1 - 1,00
        // 10 - 1,10
        // 50 - 1,64
        // 100 - 2,70
        // 200 - 7,32
        // 300 - (19,79) --> 9 + sqrt(10,79) = 12,28
        // 400 - (53,52) --> 9 + sqrt(44,52) = 15,67
        double faktor = Math.pow(1.01d, (double)GameRules.getRunde());
        if (faktor > 10d) {
            faktor = 9d + Math.sqrt(faktor - 9d);
        }
		
		// folgende Skills werden bei den Zufallsskills verwendet
		Class<?> skills[] = { 
							Bergbau.class, Steinbau.class, Holzfaellen.class, 
                            Pferdedressur.class, Reiten.class, 
                            Burgenbau.class, Strassenbau.class,
                            Schiffbau.class, Segeln.class, 
                            Wagenbau.class,
                            Handel.class,
                            Ausdauer.class, 
                            Tarnung.class,
							// Religion.class,
                            // Kraeuterkunde.class, 
						 };
		
		
		// Ursprung für Spieler setzen:
		volk.setUrsprung(region.getCoords());
		
		// Wahrnehmung
		Unit u = NeueEinheitErzeugen(region.getCoords(), volk, tarnung, 1);
		u.setSkill(Wahrnehmung.class, 630 + (int)(180d*faktor/1.01d));
		// linear mit der Runde mehr Silber:
        // 1 - 10100
        // 10 - 11000
        // 50 - 15000
        // 100 - 20000
        // 200 - 30000
        // 300 - 40000
        u.setItem(Silber.class, GameRules.getRunde() * 100 + 10000);
		u.setItem(Holz.class, holz);
		u.setItem(Stein.class, steine);
		u.setItem(Eisen.class, eisen);
		u.setKampfposition(Kampfposition.Nicht);
		u.BefehleExperimental.add(u, "LERNE Wahrnehmung");
		
		// Treiben & Unterhaltung
        int n = (int)Math.round(10d * faktor);
		u = NeueEinheitErzeugen(region.getCoords(), volk, tarnung, n);
		u.setSkill(Speerkampf.class, 300 * n);
		u.setSkill(Hiebwaffen.class, 300 * n);
		u.setSkill(Unterhaltung.class, 300 * n);
		u.setSkill(Steuereintreiben.class, 300 * n);
		u.setItem(Speer.class, n);
		u.setItem(Schwert.class, n);
        
        for (int i=0; i < n; i++) {
            if (Random.W(100) <= 10) u.addItem(Holzschild.class, 1);
            if (Random.W(100) <= 10) u.addItem(Kettenhemd.class, 1);
            if (Random.W(100) <= 5) u.addItem(Eisenschild.class, 1);
            if (Random.W(100) <= 5) u.addItem(Plattenpanzer.class, 1);
        }
        
		// Was können wir am besten?
		int topTW = -1; Skill topSkill = null;
		for (Skill skill :  u.getSkills()) {
			if (u.Talentwert(skill) > topTW) {
				topTW = u.Talentwert(skill);
				topSkill = skill;
			}
		}
		u.BefehleExperimental.add(u, "LERNE " + topSkill.getName());
		
		n = (int) (Math.pow(faktor, 0.3333333d) * 3d); // 
        for (int i = 0; i < n; i++)	{
			Coords c = null;
			boolean ende = false;

			// zufällige Region auswählen
			while (!ende) {
				c = region.getCoords().shift(Richtung.random());
				if (Region.Load(c).istBetretbar(null)) ende = true;
			}

			// neue Einheit in dieser Region
			u = NeueEinheitErzeugen(c, volk, tarnung, 1);
			Class<? extends Skill> ichKann = (Class<? extends Skill>) skills[Random.rnd(0, skills.length)];
            
            int niedrigGelernt = (int)(180d * Math.sqrt(faktor));
            int hochGelernt = (int)(360d * Math.sqrt(faktor));
			u.setSkill(ichKann , Random.rnd(niedrigGelernt, hochGelernt));
            
            int arm = (int)(50d * faktor / 1.01d);
            int reich = (int)(151d * faktor / 1.01d);
			u.setItem(Silber.class, Random.rnd(arm, reich));
            
			u.setKampfposition(Kampfposition.Nicht);
			u.BefehleExperimental.add(u, "LERNE " + ichKann.getSimpleName());
		}
	}
	
	/**
	 * fügt die Ork-Skills beim Aussetzen zu den Einheiten hinzu ... normalerweise werden
	 * die Skills beim Rekrutieren gesetzt ... aber beim Aussetzen fallen die Einheiten einfach
	 * vom Himmel
	 * @param ork - der Ausgesetzt wird
	 */
	private static void addOrkSkills(Unit ork)
	{
		if (ork instanceof Ork)
		{
			Skill s = ork.getSkill(Hiebwaffen.class);
			s.setLerntage(s.getLerntage() + 30 * ork.getPersonen());
			s = ork.getSkill(Speerkampf.class);
			s.setLerntage(s.getLerntage() + 30 * ork.getPersonen());
		}
	}
	
	/**
	 * erzeugt eine komplett neue Einheit beim Aussetzen eines Spielers
	 * @param coords - Koordinaten der Region für diese Einheit
	 * @param volk - für dieses Volk die Einheit erzeugen
	 * @param tarnung - Rassentarnung
	 * @param personen - Anzahl der Person für diese Einheit
	 * @return neue Einheit die Erzeugt wurde
	 */
	private static Unit NeueEinheitErzeugen(Coords coords, Partei volk, String tarnung, int personen)
	{
		Unit u;
		u = Unit.CreateUnit(volk.getRasse(), volk.getNummer(), coords);
		u.setOwner(volk.getNummer());
		u.setPersonen(personen);
		if (volk.getRasse().equalsIgnoreCase("Echse")) {
			// wird nur von Echsen verwendet, sonst ignoriert
			if (tarnung.length() > 0) u.setTarnRasse(tarnung);
		}
		addOrkSkills(u);
		return u;
	}

	/**
	 * liefert eine freie Region für einen neuen Spieler ... dazu wird die InselKennung
	 * verwendet 
	 * @param welt - die Welt wo gesucht werden soll
	 * @param insel - aussetzen auf dieser Insel erzwingen
	 * @return eine freie Region
	 */
	public static Region FreieRegion(int welt, int insel) {
		Region r = null;
		List<Region> nachbarn = null;
		List<Region> regions = null;
		
		new SysMsg("Berechne Startregion für neuen Spieler");

		
		// Konvention: nur freie, besiedelbare Regionen, die mindestens 3 ebensolche Nachbarn haben.
		if (GameRules.GetOption(GameRules.NEUE_INSEL_METHODE).equalsIgnoreCase("April2011")) {
            // alternative Methode, um Startregionen zu holen:
			regions = HoleRegionenApril2011();
		} else {
			// Standard & default:
			regions = HoleRegionen(insel);
		}

		// Zufällige Region auswählen ... diese Region ist definitiv noch frei
		r = regions.get(Random.rnd(0, regions.size()));
		nachbarn = GuteNachbarRegionen(r);


		// Inselkennung merken ... ist wichtig
		// -- unused -- int inselkennung = r.getInselKennung();

		// Terraforming ... d.h. 2 Nachbarregionen werden zu einem Wald und
		// einem Berg ... hier werden auch Ozeane wieder geändert
		// die aktuelle Region wird zur Ebene
		{
			int d1 = Random.rnd(0, nachbarn.size());
			int d2 = d1;
			while (d1 == d2) d2 = Random.rnd(0, nachbarn.size());

			// -- Ebene
			Region or = Region.Load(r.getCoords());

			Region hr = or.cloneAs(Ebene.class);
			hr.RenameRegion();
			hr.setAlter(10);
			hr.setBauern(0); hr.setSilber(0); hr.getResourcen().clear();
			Class<? extends Item> luxus = hr.getProduce();
			Nachfrage n = hr.getNachfrage(luxus);
			hr.Init();
			hr.setNachfrage(luxus, n.getNachfrage());
			terraforming.add(hr);
			Region.CACHE.put(hr.getCoords(), hr);
			new SysMsg(3, "Terraforming (E) für " + hr);

			// -- Wald
			or = Region.Load(nachbarn.get(d1).getCoords());

			hr = or.cloneAs(Wald.class);
			hr.RenameRegion();
			hr.setAlter(9);
			hr.setBauern(0); hr.setSilber(0); hr.getResourcen().clear();
			luxus = hr.getProduce();
			n = hr.getNachfrage(luxus);
			hr.Init();
			hr.setNachfrage(luxus, n.getNachfrage());
			terraforming.add(hr);
			Region.CACHE.put(hr.getCoords(), hr);
			new SysMsg(3, "Terraforming (W) für " + hr);

			// -- Berg
			or = Region.Load(nachbarn.get(d2).getCoords());

			hr = or.cloneAs(Berge.class);
			hr.RenameRegion();
			hr.setAlter(9);
			hr.setBauern(0); hr.setSilber(0); hr.getResourcen().clear();
			luxus = hr.getProduce();
			n = hr.getNachfrage(luxus);
			hr.Init();
			hr.setNachfrage(luxus, n.getNachfrage());
			terraforming.add(hr);
			Region.CACHE.put(hr.getCoords(), hr);
			new SysMsg(3, "Terraforming (B) für " + hr);
		}

		// alle Regionen als bekannt & betreten markieren
		r.setAlter(10);
		for(Region n : r.getNachbarn())	n.setAlter(9);

		// neue Region zurück liefern
		return r;
	}
	
	/**
	 * überprüft alle Nachbarregionen ob diese schon Betreten wurden ... wenn
	 * ja, dann wird die Region ebenfalls als betreten markiert
	 * @param r - die überprüft werden soll
	 * @return true wenn Region zum Aussetzen geeignet
	 */
	public static List<Region> GuteNachbarRegionen(Region r) {
		new SysMsg(2, "überprüfe Region - " + r + r.getCoords());
		
		List<Region> nachbarn = new ArrayList<Region>();
		// erstmal alle freien Nachbarn zählen ... dazu muss
		// das Alter < 1 sein und die Region muss betretbar sein ...
		// beim Aussetzen wird das Alter der Region auf 9 gesetzt ... daher
		// kann hier auf kleiner 1 getestet werden ... die Region ist also
		// theoretisch eine Runde lang noch nicht betreten worden
		for (Region n : r.getNachbarn()) {
			// new Debug("Nachbar - " + (Atlantis) n + " (Alter: " + n.getAlter() + ")");
			new SysMsg(4, "Nachbar - " + (Atlantis) n + " (Alter: " + n.getAlter() + ")");
			if ((n.getAlter() <= 0) && (n.istBetretbar(null))) nachbarn.add(n);
		}

		return nachbarn;
	}
	
	public static List<Region> HoleRegionenApril2011() {
		new Debug("NeueSpieler.HoleRegionen [April2011-Variante]");

		Region irgendeine = null;
        for (Region maybe : Region.CACHE.values()) {
            if (maybe.getCoords().getWelt() > 0) { irgendeine = maybe; break; }
        }
		if (irgendeine == null) {
			new BigError(new NullPointerException("Es gibt noch keine Regionen in der Oberwelt - diesen Status unterstützt die April2011-Methode in dieser Phase nicht."));
		}

		List<Region> kandidaten = new ArrayList<Region>();
		do {
			kandidaten.clear();

			// jedesmal wieder die komplette (möglicherweise ja gewachsene) Welt betrachten:
			ProtoInsel vorhanden = new AltInsel(irgendeine.getCoords().getWelt());
			for (StartPosition sp : vorhanden.findeStartPositionen()) {
				kandidaten.add(Region.Load(sp.getZentrum()));
			}

			if (kandidaten.size() < 5) {
				Welt.NeueRegionen(1); // Oberwelt vergrößern
			}
		} while(kandidaten.size() < 5);
		
		// wird z.B. von Monstern gebraucht:
		int inselkennungSpieler = GameRules.getInselkennungSpieler();
		for (Region k : kandidaten) {
			if (k.getInselKennung() > inselkennungSpieler) {
				inselkennungSpieler = k.getInselKennung();
			}
		}
		GameRules.setInselkennungSpieler(inselkennungSpieler);
		
		// wird z.B. von Monstern gebraucht:
		int inselkennungErzeugung = 0;
		for (Region r : Region.CACHE.values()) {
			if (r.getInselKennung() > inselkennungErzeugung) inselkennungErzeugung = r.getInselKennung();
		}
		GameRules.setInselkennungErzeugung(inselkennungErzeugung);
		

		return kandidaten;
	}

	/**
	 * holt die aktuellen Regionen aus der DB ... also die die Abhängig sind von
	 * inselkennung.spieler und inselkennung.erzeugung
	 * @param inselkennung - diese Insel absuchen, quasi diese Insel für den Spieler erzwingen
	 * @return alle verfügbaren Regionen
	 */
	public static ArrayList<Region> HoleRegionen(int inselkennung) {
		ArrayList<Region> regions = null;
		
		// Inselkennung aus DB oder erzwungen (!=0)
		int insel = inselkennung;
		if (inselkennung == 0) {
			insel = GameRules.getInselkennungSpieler();
			new SysMsg(4, " - verwende System-Wert für Insel (" + insel + ")");
		} else {
			new SysMsg(4, " - erzwinge Insel " + insel);
		}

		for(boolean ende=false; !ende; ) {
			regions = new ArrayList<Region>();

			for (Region r : Region.CACHE.values()) {
				if (r.getCoords().getWelt() != 1) continue;
				
				if (r instanceof Ozean) continue;
				if (r.getInselKennung() != insel) continue;
				if (r.getAlter() != 0) continue;
				if (!r.istBetretbar(null)) continue;

				// hier muss auch schon getestet werden, ob es genügend "gute Nachbarn" gibt
				List<Region> guteNachbarn = GuteNachbarRegionen(r);
				if (guteNachbarn.size() < 3) continue;

				regions.add(r);
				new SysMsg(3, "Besiedlungsreife Region " + r + " mit " + guteNachbarn.size() + " üppigen Nachbarregionen.");
			}

			// neue Inselkennung, wenn zu wenige Regionen vorhanden
			if (regions.size() < 5)	{
				if (GameRules.getInselkennungErzeugung() <= insel) {
					// keine freien Regionen mehr vorhanden => einfach neue Regionen erstellen (also einen neuen Block machen)
					Welt.NeueRegionen(1);
				} else {
					// verdammt ... keine Region mehr auf der Insel frei ... also die Nächste ansteuern
					insel++;

					// nur speichern wenn die Inselkennung nicht erwzungen wurde ... sonst werden die
					// anderen Inseln beim nächsten Spieler überprungen
					if (inselkennung == 0) GameRules.setInselkennungSpieler(insel);
					new SysMsg(3, "ändere " + GameRules.INSELKENNUNG_SPIELER + " auf " + insel);
				}
			} else {
				// wir haben mindestens 5 Regionen zur Auswahl:
				ende = true;
			}
		}

		new SysMsg(4, regions.size() + " Regionen geladen");
		return regions;
	}
	
}
