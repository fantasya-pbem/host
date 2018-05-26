package de.x8bit.Fantasya.Host.EVA;

import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Buildings.Cave;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Greifenei;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.Botschaft;
import de.x8bit.Fantasya.Atlantis.Messages.Debug;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.ZATMsg;
import de.x8bit.Fantasya.Atlantis.Regions.Ebene;
import de.x8bit.Fantasya.Atlantis.Regions.Geroellebene;
import de.x8bit.Fantasya.Atlantis.Regions.Gletscher;
import de.x8bit.Fantasya.Atlantis.Regions.Lavastrom;
import de.x8bit.Fantasya.Atlantis.Regions.Moor;
import de.x8bit.Fantasya.Atlantis.Regions.Ozean;
import de.x8bit.Fantasya.Atlantis.Regions.Sandstrom;
import de.x8bit.Fantasya.Atlantis.Regions.Sumpf;
import de.x8bit.Fantasya.Atlantis.Regions.Trockenwald;
import de.x8bit.Fantasya.Atlantis.Regions.Vulkan;
import de.x8bit.Fantasya.Atlantis.Regions.Wald;
import de.x8bit.Fantasya.Atlantis.Regions.Wueste;
import de.x8bit.Fantasya.Atlantis.Regions.aktiverVulkan;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Units.Greif;
import de.x8bit.Fantasya.Atlantis.Welt;
import de.x8bit.Fantasya.Host.EVA.Umwelt.BauernWanderung;
import de.x8bit.Fantasya.Host.EVA.Umwelt.BaumAussaat;
import de.x8bit.Fantasya.Host.EVA.Umwelt.TierWanderung;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.InselVerwaltung;
import de.x8bit.Fantasya.Host.EVA.util.ZATMode;
import de.x8bit.Fantasya.util.Random;
import de.x8bit.Fantasya.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <ul>
 * <li>Unterhalt der Einheiten</li>
 * <li>Wachstum der Resourcen &amp; Bauern</li>
 * <li>Bauern-Migration</li>
 * </ul>
 * @author mogel
 *
 */
public class Environment extends EVABase implements NotACommand {
	/**
	 * Enthält die Anzahl der verhungerten Bauern für jede Region - oder aber
	 * gar keinen Eintrag, wenn keine Hungertoten zu beklagen sind.
	 */
	protected static Map<Region, Integer> VerhungerteBauern = new HashMap<Region, Integer>();
	public static Map<Region, Integer> VerhungerteBauern() { return VerhungerteBauern; }

	/**
	 * Enthält die Anzahl des Bevölkerungszuwachses der Regionen (nur für den Schritt region.Wachstum() gültig,
	 * Hunger etc. bleibt außen vor)
	 */
	public static Map<Region, Integer> NeugeboreneBauern = new HashMap<Region, Integer>();


	public Environment()
	{
		super("Und sie bewegt sich doch! ...... ");
		
		Unterwelt();

        // Haben wir ausgestorbene Völker?
        for (Partei p : Partei.PROXY) {
			if (p.isMonster()) continue;

            int cnt = 0;
            for (Unit u : Unit.CACHE.getAll(p.getNummer())) {
                cnt += u.getPersonen();

                if (cnt > 0) break;
            }
            if (cnt == 0) new Botschaft(null, p, "\n\nDas gesamte Reich ist untergegangen!\n\nDu bist jederzeit herzlich eingeladen, ein neues Volk zu begründen.");
        }
		
		VerhungerteBauern = new HashMap<Region, Integer>();
		try {
			for (Region region : Region.CACHE.values()) {
				region.Wachstum();

				// Unterhalt der jeweiligen anwesenden Einheiten
				for(Unit unit : region.getUnits()) {
					Unterhalt(unit);
					if (GameRules.isSpring()) Greifenei(unit);
				}

				// jede Region die eine Einheit enthält, wird wieder auf 10 gesetzt ... sonst wird subtrahiert
				if (!region.getUnits().isEmpty()) region.setAlter(10); else if (region.getAlter() > 0) region.setAlter(region.getAlter() - 1);

				// wenn die Region ein Ozean ist und einen Namen hat: wech damit!
				if ((region instanceof Ozean) || (region instanceof Lavastrom)) region.setName("");
				if (region instanceof Ozean) if (((Ozean)region).getSturmValue() == -1) Ozean.calcSturmValue();
				// diese Region abschließen
			}

			if (ZATMode.CurrentMode().isDebug()) {
                new Debug("Verhungerte Bauern: ");
                for (Region r : Environment.VerhungerteBauern().keySet()) {
                    new Debug(r + ": " + Environment.VerhungerteBauern().get(r));
                }
            }
		} catch (Exception ex) {
			new BigError(ex);
		}

		new BauernWanderung();
		new TierWanderung();
//		new BaumAussaat();

		// Wald wird zu Ebene ab weniger als 600 Bäumen (und umgekehrt)
		// Trockenwald wird zu Geroellebene ab weniger als 150 Bäumen (und umgekehrt)
		Wald2Ebene();
		
		Cave.NeueRunde(); // Höhlen "altern" - ggf. ändert sich ihr Ausgang auf der anderen Ebene...
	}

    @Override
	public boolean DoAction(Unit u, String[] befehl) { return false; }
    @Override
	public void DoAction(Region r, String befehl) { }
    @Override
	public void PostAction() { }
    @Override
	public void PreAction() { }
    @Override
	public void DoAction(Einzelbefehl eb) { }


	
	/**
	 * hier schlüpfen die neuen Greifen
	 * @param unit
	 */
	private void Greifenei(Unit unit)
	{
		// Anzahl der Küken holen
		int anzahl = Random.rnd(0, GameRules.Monster.TIER.Greif.Eclosion()) + 1;
		Item eier = unit.getItem(Greifenei.class);
		if (anzahl > eier.getAnzahl()) anzahl = eier.getAnzahl();
		
		// entweder Item oder Person
		if (unit instanceof Greif) {
			unit.setPersonen(unit.getPersonen() + anzahl);
		} else {
			Item greif = unit.getItem(de.x8bit.Fantasya.Atlantis.Items.Greif.class);
			greif.setAnzahl(greif.getAnzahl() + anzahl);
		}
		
		if (anzahl > 0) {
            if (anzahl == 1) {
                new Info("Ein Greif ist geschlüpft.", unit);
            } else {
                new Info("Es sind " + anzahl + " Greife geschlüpft.", unit);
            }
            eier.setAnzahl(eier.getAnzahl() - anzahl);
        }
		
	}

	/**
	 * Unterhalt der Einheit (normal 10 Silber)
	 * @param unit - diese Einheit
	 */
	private void Unterhalt(Unit unit)
	{
		boolean hunger = unit.actionUnterhalt();
		if (!hunger) {
			int lp = unit.getLebenspunkte();
			
			// Regenerierung mit den halben Hungerpunkten
			lp -= (unit.Hungerpunkte() / 2) * unit.getPersonen();
			if (lp < 0) lp = 0;
			
			unit.setLebenspunkte(lp);
		} else {
			// gehungert wird schon in unit.actionUnterhalt() !!
		}
		
//		// zusätzlicher Unterhalt für Items
//		for(Item item : unit.Items)
//		{
//			if (item.getClass().equals(Kriegselefant.class))
//			{
//				int silber = unit.getItem(Silber.class).getAnzahl();
//				if (silber < unit.getItem(Kriegselefant.class).getAnzahl() * 5)
//				{
//					int rest = silber / 5;
//					new Fehler(unit + " verliert " + (unit.getItem(Kriegselefant.class).getAnzahl() - rest) + " Kriegselefanten durch Unterernährung", unit, unit.getCoords());
//					unit.getItem(Kriegselefant.class).setAnzahl(rest);
//					unit.getItem(Silber.class).setAnzahl(unit.getItem(Silber.class).getAnzahl() - rest * 5);
//				}
//			}
//		}
	}
	
	/**
	 * Wald wird zu einer Ebene ab weniger als 600 Bäumen ... und
	 * umgekehrt zu einem Wald ab 600 Bäumen
	 */
	public final void Wald2Ebene() {
		List<Region> wald2ebene = new ArrayList<Region>();
		List<Region> ebene2wald = new ArrayList<Region>();
		
		List<Region> trocken2geroell = new ArrayList<Region>();
		List<Region> geroell2trocken = new ArrayList<Region>();


		// Kandidaten suchen:
		for (Region region : Region.CACHE.values()) {
			// Wald zu Ebene
			if (region.getClass() == Wald.class) {
				if (region.getResource(Holz.class).getAnzahl() < 600) {
					wald2ebene.add(region);
				}
			}

			// Ebene zu Wald
			if (region.getClass() == Ebene.class) {
				if (region.getResource(Holz.class).getAnzahl() >= 600) {
					ebene2wald.add(region);
				}
			}
			
			// Trockenwald zu Geroellebene
			if (region.getClass() == Trockenwald.class) {
				if (region.getResource(Holz.class).getAnzahl() < 150) {
					trocken2geroell.add(region);
				}
			}
			
			// Geroellebene zu Trockenwald
			if (region.getClass() == Geroellebene.class) {
				if (region.getResource(Holz.class).getAnzahl() >= 150) {
					geroell2trocken.add(region);
				}
			}
		}

		// Umwandeln - Wald -> Ebene
		for (Region region : wald2ebene) {
			Region ebene = region.cloneAs(Ebene.class);

			Region.CACHE.remove(region.getCoords());
			Region.CACHE.put(ebene.getCoords(), ebene);
		}
		
		// Umwandeln - Ebene -> Wald
		for (Region region : ebene2wald) {
			Region wald = region.cloneAs(Wald.class);

			Region.CACHE.remove(region.getCoords());
			Region.CACHE.put(wald.getCoords(), wald);
		}

		// Umwandeln - Trockenwald -> Geroellebene
		for (Region region : trocken2geroell) {
			Region ebene = region.cloneAs(Geroellebene.class);

			Region.CACHE.remove(region.getCoords());
			Region.CACHE.put(ebene.getCoords(), ebene);
		}
		
		// Umwandeln - Geroellebene -> Trockenwald
		for (Region region : geroell2trocken) {
			Region wald = region.cloneAs(Trockenwald.class);

			Region.CACHE.remove(region.getCoords());
			Region.CACHE.put(wald.getCoords(), wald);
		}
	}

	/**
	 * erstellt nicht zwangsläufig neue Regionen, sondern wägt diese Entscheidung selbst ab.
	 */
	private void neueUnterweltRegionen() {
		int oberweltRegionen = 0;
		int unterweltRegionen = 0;
		for (Region r : Region.CACHE.values()) {
			if (r.getCoords().getWelt() == 1) oberweltRegionen ++;
			if (r.getCoords().getWelt() == -1) unterweltRegionen ++;
		}
		
		if (unterweltRegionen == 0) {
			// Unterwelt neu anlegen:
			new ZATMsg("Die Unterwelt wird neu angelegt.");
			Welt.NeueRegionen(-1);
			
//			// erste Höhlen anlegen:
//			List<Region> oberweltOrte = new ArrayList<Region>();
//			List<Region> unterweltOrte = new ArrayList<Region>();
//			for (Region r : Region.PROXY) {
//				if (!r.istBetretbar(null)) continue;
//				if (r.getClass() == Sandstrom.class) continue; // keine Höhlen auf/in Sandströmen
//				if (r.getCoords().getWelt() == 1) {
//					oberweltOrte.add(r);
//				} else if (r.getCoords().getWelt() == -1) {
//					unterweltOrte.add(r);
//				}
//			}
//			if (oberweltOrte.isEmpty() || unterweltOrte.isEmpty()) {
//				new SysErr("Es gibt entweder keine Ober- oder Unterweltregionen, die sich für Höhlen eignen würden...");
//				return;
//			}
//			
//			List<String> hoehlenArten = new ArrayList<String>();
//			hoehlenArten.add("Erdspalte");
//			hoehlenArten.add("Erdloch");
//			hoehlenArten.add("Grotte");
//			hoehlenArten.add("Sandloch");
//			hoehlenArten.add("Erdriss");
//			hoehlenArten.add("Felsspalte");
//			Collections.shuffle(oberweltOrte);
//			Collections.shuffle(unterweltOrte);
//			Set<Integer> inselnMitHoehle = new HashSet<Integer>();
//			for (int i=0; i<5; i++) {
//				
//				// Oberwelt:
//				int loop = 0;
//				for (boolean okay = false; !okay; ) {
//					Region r = oberweltOrte.get(0);
//					if (!inselnMitHoehle.contains(r.getInselKennung())) {
//						okay = true;
//						
//						String hoehlenArt = hoehlenArten.get(Random.rnd(0, hoehlenArten.size()));
//						if (r.getClass() == Wueste.class) hoehlenArt = "Sandloch";
//						if (r.getClass() == Sumpf.class) hoehlenArt = "Erdloch";
//						if (r.getClass() == Moor.class) hoehlenArt = "Erdloch";
//						if (r.getClass() == Gletscher.class) hoehlenArt = "Felsspalte";
//						if (r.getClass() == Vulkan.class) hoehlenArt = "Felsspalte";
//						if (r.getClass() == aktiverVulkan.class) hoehlenArt = "Felsspalte";
//						
//						Building cave = Building.Create(hoehlenArt, r.getCoords());
//						cave.setSize(Random.W(20) + Random.W(10));
//						inselnMitHoehle.add(r.getInselKennung());
//					} else {
//						Collections.shuffle(oberweltOrte);
//					}
//					if (loop++ > 100000) okay = true; // dann gibt es wohl nicht genügend Inseln
//				}
//				// Unterwelt:
//				loop = 0;
//				for (boolean okay = false; !okay; ) {
//					Region r = unterweltOrte.get(0);
//					if (!inselnMitHoehle.contains(r.getInselKennung())) {
//						okay = true;
//						
//						String hoehlenArt = hoehlenArten.get(Random.rnd(0, hoehlenArten.size()));
//						if (r.getClass() == Wueste.class) hoehlenArt = "Sandloch";
//						if (r.getClass() == Sumpf.class) hoehlenArt = "Erdloch";
//						if (r.getClass() == Moor.class) hoehlenArt = "Erdloch";
//						if (r.getClass() == Gletscher.class) hoehlenArt = "Felsspalte";
//						if (r.getClass() == Vulkan.class) hoehlenArt = "Felsspalte";
//						if (r.getClass() == aktiverVulkan.class) hoehlenArt = "Felsspalte";
//
//						Building cave = Building.Create(hoehlenArt, r.getCoords());
//						cave.setSize(Random.W(20) + Random.W(10));
//						inselnMitHoehle.add(r.getInselKennung());
//					} else {
//						Collections.shuffle(unterweltOrte);
//					}
//					if (loop++ > 100000) okay = true; // dann gibt es wohl nicht genügend Inseln
//				}
//			}
		}
        
        unterweltRegionen = 0;
        for (Region r : Region.CACHE.values()) {
            if (r.getCoords().getWelt() == -1) unterweltRegionen ++;
        }
		float ratio = (float)unterweltRegionen / (float)oberweltRegionen;
        
        // wenn es weniger als als ein Viertel soviel Unter- wie Oberwelt gibt,
        // dann entstehen neue Unterwelt-Inseln.
        int anzahlNeu = Random.W(6) - 2;
        while ((ratio < 0.5f) && (anzahlNeu > 0 )) {
            new ZATMsg("Mehr Regionen für die Unterwelt...");
            Welt.NeueRegionen(-1);

            anzahlNeu --;

            unterweltRegionen = 0;
            for (Region r : Region.CACHE.values()) {
                if (r.getCoords().getWelt() == -1) unterweltRegionen ++;
            }
            ratio = (float)unterweltRegionen / (float)oberweltRegionen;
        }
		
	}
	
	/**
	 * Aktive Vulkane richten Schaden an, außerdem brechen hie und da neue 
	 * Vulkane aus und bestehende erlöschen.
	 */
	private void vulkanismus() {
		
		List<Region> schlafendeVulkane = new ArrayList<Region>();
		List<Region> aktiveVulkane = new ArrayList<Region>();


		// Kandidaten suchen:
		for (Region region : Region.CACHE.values()) {
			if (region.getClass() == Vulkan.class) schlafendeVulkane.add(region);
			if (region.getClass() == aktiverVulkan.class) aktiveVulkane.add(region);
		}

		new ZATMsg("Vulkanismus in " + (schlafendeVulkane.size() + aktiveVulkane.size()) + " Regionen, davon " + aktiveVulkane.size() + " mit aktiven Vulkanen...");
		
		// Ausbruch?
		for (Region region : schlafendeVulkane) {
			if (Random.W(36) < 36) continue;
			
			// Ausbruch!
			Region inferno = region.cloneAs(aktiverVulkan.class);

			Region.CACHE.remove(region.getCoords());
			Region.CACHE.put(inferno.getCoords(), inferno);
			
			new ZATMsg("In " + inferno.getName() + " bricht ein Vulkan aus.");
			
			for (Unit u : inferno.getUnits()) {
				int opfer = 0;
				for (int i = 0; i < u.getPersonen(); i++) {
					if (Random.W(6) < 4) opfer ++;
				}
				if (opfer > 0) {
					float talentFaktor = 1f - (float)opfer / (float)u.getPersonen();
					if (opfer == u.getPersonen()) {
						if (opfer == 1) {
							new Info(u + " ist beim Ausbruch des Vulkans ums leben gekommen.", Partei.getPartei(u.getOwner()));
						} else {
							new Info("Alle von " + u + " sind beim Ausbruch des Vulkans ums Leben gekommen.", Partei.getPartei(u.getOwner()));
						}
					} else {
						new Info(u + " - " + opfer + " von uns sind beim Ausbruch des Vulkans ums Leben gekommen.", u);
					}
					u.setPersonen(u.getPersonen() - opfer);
					for (Skill sk : u.getSkills()) {
						sk.setLerntage(Math.round((float)sk.getLerntage() * talentFaktor));
					}
					u.setLebenspunkte(u.maxLebenspunkte() / 2);
				}
			}
		}
		
		// Ende des Ausbruchs? Sonst: Weiterer Vulkanschaden
		for (Region region : aktiveVulkane) {
			if (Random.W(12) == 12) {
				// Der Vulkan erlischt:
				Region erloschen = region.cloneAs(Vulkan.class);

				Region.CACHE.remove(region.getCoords());
				Region.CACHE.put(erloschen.getCoords(), erloschen);
				
				new ZATMsg("Der Vulkan in " + erloschen.getName() + " erlischt.");
			} else {
				for (Unit u : region.getUnits()) {
					int opfer = 0;
					for (int i = 0; i < u.getPersonen(); i++) {
						if (Random.W(6) < 4) opfer ++;
					}
					if (opfer > 0) {
						float talentFaktor = 1f - (float)opfer / (float)u.getPersonen();
						if (opfer == u.getPersonen()) {
							if (opfer == 1) {
								new Info(u + " ist beim Ausbruch des Vulkans ums leben gekommen.", Partei.getPartei(u.getOwner()));
							} else {
								new Info("Alle von " + u + " sind beim Ausbruch des Vulkans ums Leben gekommen.", Partei.getPartei(u.getOwner()));
							}
						} else {
							new Info(u + " - " + opfer + " von uns sind beim Ausbruch des Vulkans ums Leben gekommen.", u);
						}
						u.setPersonen(u.getPersonen() - opfer);
						for (Skill sk : u.getSkills()) {
							sk.setLerntage(Math.round((float)sk.getLerntage() * talentFaktor));
						}
						u.setLebenspunkte(u.maxLebenspunkte() / 2);
					}
				}
			}
		}

	}
	
	private void sandstroeme() {
		List<Region> alle = new ArrayList<Region>();
		for (Region r : Region.CACHE.values()) {
			if (r.getClass() == Sandstrom.class) alle.add(r);
		}
		
		new ZATMsg("Treibsand in " + alle.size() + " Regionen...");
		for (Region r : alle) {
			// erst einmal versinken alle Gebäude-Baustellen:
			for (Building b : r.getBuildings()) {
				Building.PROXY.remove(b);
				
				if (b.getOwner() != 0) b.setOwner(0);
				b.setSize(0);
				for (Unit u : b.getUnits()) {
					u.setGebaeude(0);
					
					String regionsName = r.toString() + " " + Partei.getPartei(u.getOwner()).getPrivateCoords(r.getCoords());
					new Info(b + " versinkt spurlos im Treibsand von " + regionsName + ".", u);
				}
				
				Building.PROXY.add(b);
			}
			
			// dann nehmen die Einheiten Schaden:
			for (Unit u : r.getUnits()) {
				int opfer = 0;
				for (int i = 0; i < u.getPersonen(); i++) {
					if (!u.canFly()) {
						if (Random.W(6) < 3) opfer ++;
					} else {
						if (Random.W(6) == 1) opfer ++;
					}
				}
				
				if (opfer > 0) {
					String regionsName = r.toString() + " " + Partei.getPartei(u.getOwner()).getPrivateCoords(r.getCoords());
					
					float talentFaktor = 1f - (float)opfer / (float)u.getPersonen();
					if (opfer == u.getPersonen()) {
						if (opfer == 1) {
							new Info(u + " ist im Treibsand von " + regionsName + " versunken.", Partei.getPartei(u.getOwner()));
						} else {
							new Info(u + " sind im Treibsand von " + regionsName + " verschollen.", Partei.getPartei(u.getOwner()));
						}
					} else {
						List<String> verloreneItems = new ArrayList<String>();
						for (Item it : u.getItems()) {
							if (it.getAnzahl() == 0) continue;
							int verloren = 0;
							for (int i=0; i < it.getAnzahl(); i++) {
								if (Random.W(10000) > talentFaktor * 10000) verloren ++;
							}
							if (verloren > 0) {
								int alteAnzahl = it.getAnzahl();
								it.setAnzahl(verloren);
								verloreneItems.add(it.toString());
								it.setAnzahl(alteAnzahl - verloren);
							}
						}
						String itemMsg = "";
						if (!verloreneItems.isEmpty()) itemMsg = " " + StringUtils.aufzaehlung(verloreneItems) + " wurden mitgerissen.";
						new Info(u + " - " + opfer + " von uns sind in den Treibsand von " + regionsName + " geraten und verschwunden." + itemMsg, u);
					}
                    
					u.setPersonen(u.getPersonen() - opfer);
					for (Skill sk : u.getSkills()) {
						sk.setLerntage(Math.round((float)sk.getLerntage() * talentFaktor));
					}
                    u.setLebenspunkte((int)Math.floor((float)u.getLebenspunkte() * talentFaktor));
				}
			}
		}
	}
    
    /**
     * erfasst die bestehenden Höhlen und lässt ggf. neue entstehen
     */
    private void hoehlenEntstehen() {
        // Oberwelt:
        List<Cave> bestehende = Cave.GetCaves(1);
        InselVerwaltung iv = InselVerwaltung.getInstance();
        iv.karteVerarbeiten();
        
        Set<Integer> inselnMitHoehlen = new HashSet<Integer>();
        for (Cave c : bestehende) inselnMitHoehlen.add(iv.getInselNummer(c.getCoords()));
        
        Set<Integer> inselnOhneHoehlen = new HashSet<Integer>();
        inselnOhneHoehlen.addAll(iv.getExistierendeInseln());
        // Inseln mit bestehenden Höhlen aussortieren:
        inselnOhneHoehlen.removeAll(inselnMitHoehlen);
        
        Set<Integer> loeschSet = new HashSet<Integer>();
        // Unterwelt und Ozeane etc. aussortieren:
        for (int inselId : inselnOhneHoehlen) {
            for (Coords c : iv.getInselCoords(inselId)) {
                if (c.getWelt() < 0) { 
                    loeschSet.add(inselId);
                    continue;
                }
                Region r = Region.Load(c);
                if (r instanceof Ozean) {
                    loeschSet.add(inselId);
                    continue;
                }
            }
        }
        for (int inselId : loeschSet) {
            inselnOhneHoehlen.remove(inselId);
        }
        
        // jetzt sind nur noch Oberwelt-Landinseln in inselnOhneHoehlen:
        int grosseInselnOhneHoehle = 0;
        for (int inselId : inselnOhneHoehlen) {
            if (iv.getInsel(inselId).istUnterwelt()) continue; // es interessieren nur die Oberweltinseln...
            if (iv.getInselCoords(inselId).size() > 19) grosseInselnOhneHoehle++;
        }
        if (grosseInselnOhneHoehle > 0) new ZATMsg("Es gibt " + grosseInselnOhneHoehle + " große (>19 Regionen) Oberwelt-Inseln ohne Höhlen.");
        
        for (int i=0; i < grosseInselnOhneHoehle; i++) {
            // nur wenn W10 == 1 :
            if (Random.W(10) != 1) continue;
            
            new ZATMsg(" - Grabe neues Höhlenpaar...");
            
            Map<Coords, Integer> hoehlenChanceOberwelt = new HashMap<Coords, Integer>();
            for (Region r : Region.CACHE.values()) {
                Coords c = r.getCoords();
                if (c.getWelt() <= 0) continue;
                if (!r.istBetretbar(null)) continue;

                // Entfernung zur nächsten bestehenden Höhle bestimmen:
                int minDistance = Integer.MAX_VALUE;
                for (Cave cave : Cave.GetCaves(1)) {
                    if (cave.getCoords().getWelt() <= 0) continue;
                    int d = c.getDistance(cave.getCoords());
                    if (d < minDistance) minDistance = d;
                }

                if (minDistance > 16) minDistance = 16;
                hoehlenChanceOberwelt.put(c, (int)Math.pow(2, minDistance));
                
                // Inseln ohne Höhlen, aber mit mindestens 12 Regionen haben bessere Chancen:
                if (inselnOhneHoehlen.contains(iv.getInselNummer(c))) {
                    if (iv.getInsel(iv.getInselNummer(c)).getCoords().size() > 12) {
                        hoehlenChanceOberwelt.put(c, hoehlenChanceOberwelt.get(c) * 3);
                    }
                }
            }
            // Entstehungs-Wahrscheinlichkeiten von Höhlen für alle Oberwelt-Regionen berechnet
            if (hoehlenChanceOberwelt.isEmpty()) throw new IllegalStateException("Keine geeignete Oberwelt-Region für neue Höhlen gefunden.");
            
            Map<Coords, Integer> hoehlenChanceUnterwelt = new HashMap<Coords, Integer>();
            for (Region r : Region.CACHE.values()) {
                Coords c = r.getCoords();
                if (c.getWelt() >= 0) continue;
                if (!r.istBetretbar(null)) continue;
                if (r instanceof Sandstrom) continue;

                // Entfernung zur nächsten bestehenden Höhle bestimmen:
                int minDistance = Integer.MAX_VALUE;
                for (Cave cave : Cave.GetCaves(-1)) {
                    if (cave.getCoords().getWelt() >= 0) continue;
                    int d = c.getDistance(cave.getCoords());
                    if (d < minDistance) minDistance = d;
                }

                if (minDistance > 16) minDistance = 16;
                hoehlenChanceUnterwelt.put(c, (int)Math.pow(2, minDistance));
            }
            // Entstehungs-Wahrscheinlichkeiten von Höhlen für alle Unterwelt-Regionen berechnet
            if (hoehlenChanceUnterwelt.isEmpty()) throw new IllegalStateException("Keine geeignete Unterwelt-Region für neue Höhlen gefunden.");
            
            int summeOberweltChancen = 0; int summeUnterweltChancen = 0; 
            for (Coords c : hoehlenChanceOberwelt.keySet()) summeOberweltChancen += hoehlenChanceOberwelt.get(c);
            for (Coords c : hoehlenChanceUnterwelt.keySet()) summeUnterweltChancen += hoehlenChanceUnterwelt.get(c);
            
            StringBuilder sb = new StringBuilder();
            sb.append("Höhlenpaar: ");
            
            // die "Gewinner" in Ober- und Unterwelt bestimmen:
            Coords ausgangOben = null; Coords ausgangUnten = null;
            List<Coords> kandidatenOben = new ArrayList<Coords>(hoehlenChanceOberwelt.keySet());
            Collections.shuffle(kandidatenOben);
            while (Random.W(summeOberweltChancen) > hoehlenChanceOberwelt.get(kandidatenOben.get(0))) {
                Collections.shuffle(kandidatenOben);
            }
            ausgangOben = kandidatenOben.get(0);
            sb.append("p-Level(oben) = ").append(hoehlenChanceOberwelt.get(kandidatenOben.get(0)));
            sb.append(", ");
            List<Coords> kandidatenUnten = new ArrayList<Coords>(hoehlenChanceUnterwelt.keySet());
            Collections.shuffle(kandidatenUnten);
            while (Random.W(summeUnterweltChancen) > hoehlenChanceUnterwelt.get(kandidatenUnten.get(0))) {
                Collections.shuffle(kandidatenUnten);
            }
            ausgangUnten = kandidatenUnten.get(0);
            sb.append("p-Level(unten) = ").append(hoehlenChanceUnterwelt.get(kandidatenUnten.get(0)));
            sb.append(";  ");

            
			List<String> hoehlenArten = new ArrayList<String>();
			hoehlenArten.add("Erdspalte");
			hoehlenArten.add("Erdloch");
			hoehlenArten.add("Grotte");
			hoehlenArten.add("Sandloch");
			hoehlenArten.add("Erdriss");
			hoehlenArten.add("Felsspalte");
            
            Region r = Region.Load(ausgangOben);
            String hoehlenArt = hoehlenArten.get(Random.rnd(0, hoehlenArten.size()));
            if (r.getClass() == Wueste.class) hoehlenArt = "Sandloch";
            if (r.getClass() == Sumpf.class) hoehlenArt = "Erdloch";
            if (r.getClass() == Moor.class) hoehlenArt = "Erdloch";
            if (r.getClass() == Gletscher.class) hoehlenArt = "Felsspalte";
            if (r.getClass() == Vulkan.class) hoehlenArt = "Felsspalte";
            if (r.getClass() == aktiverVulkan.class) hoehlenArt = "Felsspalte";
            Building cave = Building.Create(hoehlenArt, r.getCoords());
            cave.setSize(Random.W(20) + Random.W(10));
            sb.append(cave).append(" in ").append(r).append(" ").append(r.getCoords()).append(", Inselkennung ").append(r.getInselKennung());
            sb.append(" und ");
            
            r = Region.Load(ausgangUnten);
            hoehlenArt = hoehlenArten.get(Random.rnd(0, hoehlenArten.size()));
            if (r.getClass() == Wueste.class) hoehlenArt = "Sandloch";
            if (r.getClass() == Sumpf.class) hoehlenArt = "Erdloch";
            if (r.getClass() == Moor.class) hoehlenArt = "Erdloch";
            if (r.getClass() == Gletscher.class) hoehlenArt = "Felsspalte";
            if (r.getClass() == Vulkan.class) hoehlenArt = "Felsspalte";
            if (r.getClass() == aktiverVulkan.class) hoehlenArt = "Felsspalte";
            cave = Building.Create(hoehlenArt, r.getCoords());
            cave.setSize(Random.W(20) + Random.W(10));
            sb.append(cave).append(" in ").append(r).append(" ").append(r.getCoords()).append(", Inselkennung ").append(r.getInselKennung());
            
            new Debug(sb.toString());
            
            // neues Höhlenpaar fertig!
        }
        
        
        
    }
	
	/**
	 * Entwicklung der Unterwelt und die Verbindungen / das Gleichgewicht mit 
	 * der Oberwelt.
	 */
	private void Unterwelt() {
		String s = GameRules.GetOption(GameRules.UNTERWELT_AKTIV);
		if (Integer.parseInt(s) == 0) {
			new ZATMsg("Überspringe die Entwicklung der Unterwelt (Option " + GameRules.UNTERWELT_AKTIV + "=" + s + ").");
			return;
		}
		
		neueUnterweltRegionen();
		vulkanismus();
		sandstroeme();
        hoehlenEntstehen();
	}

}
