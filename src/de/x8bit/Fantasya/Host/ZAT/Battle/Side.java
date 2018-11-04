package de.x8bit.Fantasya.Host.ZAT.Battle;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Buildings.Burg;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Helper.Kampfzauber;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Kampfposition;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Spell;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Helper.Monster;
import de.x8bit.Fantasya.Atlantis.Messages.Battle;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.Botschaft;
import de.x8bit.Fantasya.Atlantis.Messages.Debug;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.SysErr;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Skills.Magie;
import de.x8bit.Fantasya.Atlantis.Skills.Reiten;
import de.x8bit.Fantasya.Atlantis.Skills.Taktik;
import de.x8bit.Fantasya.Atlantis.Spell.AttackSpell;
import de.x8bit.Fantasya.Host.EVA.Kriege;
import de.x8bit.Fantasya.Host.EVA.util.ZATMode;
import de.x8bit.Fantasya.Host.ZAT.Battle.Effects.BFXBurginsasseAttack;
import de.x8bit.Fantasya.Host.ZAT.Battle.Effects.BFXBurginsasseDefence;
import de.x8bit.Fantasya.Host.ZAT.Battle.Effects.BFXFliegendDefensiv;
import de.x8bit.Fantasya.Host.ZAT.Battle.Effects.BFXFliegendOffensiv;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapon.WeaponType;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapons.ITeureWaffe;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapons.WPegasus;
import de.x8bit.Fantasya.Host.ZAT.Battle.util.DuellAnalyse;
import de.x8bit.Fantasya.Host.ZAT.Battle.util.KampfreportXML;
import de.x8bit.Fantasya.Host.ZAT.Battle.util.KriegerNahkampfComparator;
import de.x8bit.Fantasya.Host.ZAT.Zaubern;
import de.x8bit.Fantasya.util.comparator.ItemStueckGewichtComparator;
import de.x8bit.Fantasya.util.Random;
import de.x8bit.Fantasya.util.StringUtils;
import de.x8bit.Fantasya.util.comparator.UnitSkillComparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * diese Klasse enthält den Aufbau eine Kampfseite (defenders || attackers)
 * @author mogel
 */
public class Side {

    protected final Gefecht gefecht;
    protected String name;
    protected final Map<Partei, Set<Gruppe>> teilnehmer;
    protected final Map<Partei, KampfreportXML> berichte;
    
    /** alle Einheiten, die für diese Seite antreten - damit sie am Ende ggf. 
     * eine Meldung über erlittene Verletzungen und Gefallene erhalten können. */
    protected final Set<Unit> anfangsUnits;
    
    /** Q&D - die Kampfreihe vorne */
    public final List<Krieger> vorne = new ArrayList<Krieger>();
    /** Q&D - Kampfreihe hinten */
    public final List<Krieger> hinten = new ArrayList<Krieger>();
    /** Q&D - diese Einheiten kämpfen nicht freiwillig (Zivilisten) */
    public final List<Krieger> nicht = new ArrayList<Krieger>();
    /**
     * diese Einheiten sind explizit gar nicht beteiligt
     * (obwohl sie zu einer der beteiligten Seiten gehören)
     */
    public final List<Krieger> immun = new ArrayList<Krieger>();
    /** Q&D - der Behälter für alle Items die im Krieg durch tote Einheiten entstehen */
    public List<Item> jackpot = new ArrayList<Item>();

    /**
     * <p>Standard-Konstruktor bei ATTACKIERE - alle Einheiten der Parteien werden verwickelt.</p>
     * <p>Hier wird gleich die Aufteilung der Seiten in die Kampfreihen übernommen</p>
     * @param pParteien - eine Liste aller Parteien für diese Seite
     * @param region - in dieser Region wird gespielt
     */
    public Side(Set<Gruppe> pGruppen, Region region, Gefecht gefecht, String name) {
        this.gefecht = gefecht;
        this.name = name;
        this.teilnehmer = new HashMap<Partei, Set<Gruppe>>();
        this.anfangsUnits = new HashSet<Unit>();
        this.berichte = new HashMap<Partei, KampfreportXML>();
        
        Set<Partei> parteien = new HashSet<Partei>();
        for (Gruppe g : pGruppen) parteien.add(Partei.getPartei(g.getParteiNr()));

        List<Unit> units = new ArrayList<Unit>();
        for (Partei partei : parteien) {
            if (!teilnehmer.containsKey(partei)) teilnehmer.put(partei, new HashSet<Gruppe>());
            if (!berichte.containsKey(partei)) berichte.put(partei, new KampfreportXML(partei));
            
            for (Gruppe g : pGruppen) {
                if (g.getParteiNr() != partei.getNummer()) continue;
                
                teilnehmer.get(partei).add(g);
                
                for (Unit unit : g.getUnits()) {
                    // gibt es da wirklich jemanden ?
                    if (unit.getPersonen() <= 0) {
                        // new Debug("Nein - weil niemand in der Einheit drin ist.");
                        continue;
                    }

                    units.add(unit);
                }
            }
        }
        if (ZATMode.CurrentMode().isDebug()) {
            StringBuffer sb = new StringBuffer();
            sb.append("Folgende Einheiten auf seiten der ").append(name).append(" aufgenommen: ");
            for (Unit u : units) {
                sb.append("\n").append(u).append(" (").append(u.getKampfposition()).append(")");
            }
            new Debug(sb + " (vor Side.aufstellen()).");
        }

        this.aufstellen(units);
    }

    private void aufstellen(List<Unit> units) {
        for (Unit unit : units) {
            // eine Kopie (den Ausgangszustand der Einheiten) in das "Register" aufnehmen:
            try {
                this.anfangsUnits.add((Unit)unit.clone());
            } catch (CloneNotSupportedException ex) {
                new BigError(ex);
            }
		
			int burgBonus = 0;
			if (unit.getGebaeude() != 0) {
				Building b = Building.getBuilding(unit.getGebaeude());
				if (b instanceof Burg) {
					if (unit.imGebaeude()) {
                        burgBonus = ((Burg)b).getKampfBonus();
                        new Debug("Einheit " + unit + " bekommt wegen " + b + " einen Burgenbonus von +" + burgBonus + ".");
                    } else {
                        new Debug("Einheit " + unit + " bekommt KEINEN Burgenbonus für " + b + ", passt nicht ins Gebäude.");
                    }
				}
			}

            int maxLebenspunkte = unit.maxLebenspunkte() / unit.getPersonen();
			
			List<Krieger> einheitsKrieger = new ArrayList<Krieger>();
			
            // getKrieger aus der Einheit erstellen
            for (int i = 0; i < unit.getPersonen(); i++) {
                Krieger krieger = Factory(unit, i + 1); // *juhu* ein Krieger

                krieger.setTrefferpunkte(maxLebenspunkte);// soviel kann er max.
				
				if (burgBonus > 0) {
					krieger.addEffect(new BFXBurginsasseAttack(burgBonus));
					krieger.addEffect(new BFXBurginsasseDefence(burgBonus));
				}

                // der wird jetzt noch der passenden Reihe hinzugefügt
                if (unit.getKampfposition().equals(Kampfposition.Vorne) || unit.getKampfposition().equals(Kampfposition.Aggressiv)) {
                    vorne.add(krieger);
                    krieger.setReihe(Krieger.REIHE_VORNE);
                } else if (unit.getKampfposition().equals(Kampfposition.Hinten) || unit.getKampfposition().equals(Kampfposition.Fliehe)) {
                    hinten.add(krieger);
                    krieger.setReihe(Krieger.REIHE_HINTEN);
                } else {
                    if (!getGefecht().getExplizitAngegriffene().contains(unit)) {
                        immun.add(krieger);
                        krieger.setReihe(Krieger.REIHE_IMMUN);
                    } else {
                        nicht.add(krieger);
                        krieger.setReihe(Krieger.REIHE_NICHT);
                    }

                    // TODO: Wenn keiner der Gegner diese KÄMPFE-NICHT-Einheit sehen kann, ist sie ebenfalls immun
                }
				
				einheitsKrieger.add(krieger);
            } // nächster getKrieger
			
            // Lebenpunkte festlegen - zufällig auf die Krieger aufteilen:
			for (int i=0; i < unit.getLebenspunkte(); i++) {
				Krieger irgendeiner = null; int j=0;
				do {
					irgendeiner = einheitsKrieger.get(Random.rnd(0, einheitsKrieger.size()));
					if (++j > 10000000) throw new IllegalStateException("Konnte erlittene Schadenspunkte bei Einheit " + unit + " nicht auf die einzelnen Krieger aufteilen!");
				} while ((irgendeiner.getLebenspunkte() + 1) >= irgendeiner.getTrefferpunkte());
				irgendeiner.setLebenspunkte(irgendeiner.getLebenspunkte() + 1);
			}
            
//            if (ZATMode.CurrentMode().isDebug()) {
//                StringBuilder sb = new StringBuilder();
//                sb.append("Alle Krieger von " + unit + " nach dem Aufstellen:");
//                for (Krieger k : einheitsKrieger) {
//                    sb.append("\n" + k.detailedToString(false));
//                }
//                new Debug(sb.toString());
//            }
			
        } // nächste Einheit

        // 3. Reihe ggf. an die Front ... Zivilisten, arme Schweine
        if (vorne.isEmpty() && hinten.isEmpty()) {
            if (!nicht.isEmpty()) {
                zivilVerteidigung();
                gefecht.meldung(name + ": Es sind keine Kämpfer zum Schutz der Bevölkerung vorhanden - die Zivilisten müssen kämpfen.", true);
            }
        }
    }

    public Gefecht getGefecht() {
        return gefecht;
    }

    public Set<Partei> getParteien() {
        return teilnehmer.keySet();
    }

    public boolean containsUnit(Unit u) {
        for (Partei p : teilnehmer.keySet()) {
            for (Gruppe g : teilnehmer.get(p)) {
                for (Unit maybe : g.getUnits()) {
                    if (maybe.getPersonen() <= 0) continue;
                    
                    if (maybe.getNummer() == u.getNummer()) return true;
                }
            }
        }
        return false;
    }

    /**
     * @return Anzahl aller Personen, die auf dieser Seite stehen (egal in welcher Reihe, AUSSER immune Einheiten)
     */
    public int getPersonen() {
        return vorne.size() + hinten.size() + nicht.size();
    }

    /**
     * @param p Partei, auf die die Zählung beschränkt werden soll
     * @param kp Kampfposition (-reihe), auf die die Zählung beschränkt werden soll - oder null für alle Positionen
     * @return Anzahl der beteiligten Personen / Krieger
     */
    public int getPersonen(Gruppe g, Kampfposition kp) {
        List<Krieger> alle = null;
        if (kp == null) {
            alle = new ArrayList<Krieger>();
            alle.addAll(this.vorne);
            alle.addAll(this.hinten);
            alle.addAll(this.nicht);
        } else {
            if (kp.equals(Kampfposition.Aggressiv) || kp.equals(Kampfposition.Vorne)) {
                alle = this.vorne;
            }
            if (kp.equals(Kampfposition.Hinten)) {
                alle = this.hinten;
            }
            if (kp.equals(Kampfposition.Nicht) || kp.equals(Kampfposition.Fliehe)) {
                alle = this.nicht;
            }
            if (kp.equals(Kampfposition.Immun)) {
                alle = this.immun;
            }
        }

        if (alle == null) {
            new SysErr("Side.getPersonen(g, kp) mit unerwarteter Kampfposition aufgerufen: " + kp);
            return 0;
        }

        int cnt = 0;
        for (Unit u : g.getUnits()) {
            for (Krieger k : alle) {
                if (k.getUnit().getNummer() == u.getNummer()) {
                    cnt++;
                }
            }
        }
        return cnt;
    }

    /** 
     * räumt die beteiligten Einheiten auf / stellt sie wieder her
     */
    public void CleanUp() {
        // gefecht.meldung("Jackpot von " + this.beschreibeTeilnehmer() + ":" + StringUtils.aufzaehlung(this.jackpot), false);

        List<Krieger> veteranen = new ArrayList<Krieger>();
        veteranen.addAll(vorne);
        veteranen.addAll(hinten);
        veteranen.addAll(nicht);
        veteranen.addAll(immun);

        CleanUp(veteranen);

        // Items aus dem Jackpot übergeben
        // new Debug("Der Jackpot von " + this.beschreibeTeilnehmer() + " enthält: " + StringUtils.aufzaehlung(jackpot) + ".");

        // Jackpot konsolidieren (gleiche Item-Klassen zusammenlegen):
        Map<Class<? extends Item>, Integer> zusammen = new HashMap<Class<? extends Item>, Integer>();
        for (Item item : jackpot) {
            if (!zusammen.containsKey(item.getClass())) zusammen.put(item.getClass(), 0);
            zusammen.put(item.getClass(), zusammen.get(item.getClass()) + item.getAnzahl());
        }
        jackpot.clear();
        for (Class<? extends Item> clazz : zusammen.keySet()) {
            Item item = null;
            try {
                item = clazz.newInstance();
            } catch (Exception ex) {
                new BigError(ex);
            }
            item.setAnzahl(zusammen.get(clazz));
            jackpot.add(item);
        }
        // new Debug("Der Jackpot von " + this.beschreibeTeilnehmer() + " enthält: " + StringUtils.aufzaehlung(jackpot) + ".");
        
        
        // was hält bzw. wer überlebt eigentlich?
        for (Item item : jackpot) {
            int anzahl = 0;
            for (int i = 0; i < item.getAnzahl(); i++) {
                if (item.surviveBattle()) {
                    anzahl++;
                }
            }
            item.setAnzahl(anzahl);
        }
        // new Debug("Der Jackpot von " + this.beschreibeTeilnehmer() + " enthält: " + StringUtils.aufzaehlung(jackpot) + ".");


        List<Unit> traeger = new ArrayList<Unit>();
        List<Unit> sammler = new ArrayList<Unit>();
        for (Partei p : this.getParteien()) {
            for (Unit u : this.getUnits(p)) {
                int m = beuteModus(u);
                if (m == Kriege.BEUTE_NICHTS) {
                    // wir halten uns da raus.
                } else if (m == Kriege.BEUTE_TRAGBAR) {
                    // nur wenn wir überhaupt noch was nehmen könnten.
                    new Debug(u + " sammelt Beute, aber nur soviel sie tragen können (" + (u.gesamteFreieKapazitaet(false) / 100) + " GE).");
                    if (u.gesamteFreieKapazitaet(false) > 0) {
                        traeger.add(u);
                    }
                } else if (m == Kriege.BEUTE_ALLES) {
                    sammler.add(u);
                }
            }
        }

        // die besten Taktiker bekommen zuerst, aber auf dem gleichen TW soll es Zufall sein:
        Collections.shuffle(traeger);
        Collections.shuffle(sammler);

        Collections.sort(traeger, new UnitSkillComparator(Taktik.class));
        Collections.sort(sammler, new UnitSkillComparator(Taktik.class));

        // niedrigstes Stückgewicht zuerst - d.h. Silber ---- zuletzt Steine / Pferde / Elefanten
        Collections.sort(jackpot, new ItemStueckGewichtComparator());


        Map<Unit, List<String>> beuteMeldungen = new HashMap<Unit, List<String>>();
        { // als Block, damit nicht die unit-Variable hinterher irgendwo versehentlich rumgeistert...
            Unit unit = null;
            int unitIdx = 0;
            boolean nurWennTragbar = true;
            if (!traeger.isEmpty()) {
                unit = traeger.get(unitIdx);
            }
            if ((unit == null) && (!sammler.isEmpty())) {
                unit = sammler.get(0);
                nurWennTragbar = false;
            }

            Item item = null;
            int itemIdx = 0;
            if (!jackpot.isEmpty()) {
                item = jackpot.get(itemIdx);
            }

            while ((unit != null) && (item != null)) {
                if (item.getAnzahl() <= 0) {
                    itemIdx++;
                    if (jackpot.size() > itemIdx) {
                        item = jackpot.get(itemIdx);
                    } else {
                        // nichts mehr da.
                        item = null;
                        break;
                    }
                }

                int aufsammeln = item.getAnzahl();
                boolean wirHabenGenug = false;
                if (nurWennTragbar) {
                    int maxTragbar = unit.gesamteFreieKapazitaet(false) / item.getGewicht();
    //                new Battle(
    //                        "Es gibt " + item.getAnzahl() + " " + item.getName() + " (" + item.getGewicht() + " GE/100 pro Stück) zu erbeuten. "
    //                        + unit + " hat " + unit.geFreieKapazitaet(false) + " freie Kapazität "
    //                        + "und kann bis zu " + maxTragbar + " Stück aufsammeln.",
    //                        unit
    //                );
                    if (maxTragbar <= aufsammeln) {
                        aufsammeln = maxTragbar;
                        wirHabenGenug = true;
    //                    new Battle(unit + ": Wir haben genug!", unit);
                    }
                }

                int hatSchon = unit.getItem(item.getClass()).getAnzahl();
                unit.setItem(item.getClass(), hatSchon + aufsammeln);
                if (aufsammeln > 0) {
                    if (!beuteMeldungen.containsKey(unit)) beuteMeldungen.put(unit, new ArrayList<String>());
                    beuteMeldungen.get(unit).add(aufsammeln + " " + item.getName());
                }

                item.setAnzahl(item.getAnzahl() - aufsammeln);

                if (wirHabenGenug) {
                    unitIdx++;
                    if (traeger.size() > unitIdx) {
                        unit = traeger.get(unitIdx);
                    } else if (!sammler.isEmpty()) {
                        // okay, jetzt die, die sich mit allem zuschütten wollen:
                        unit = sammler.get(0);
                        nurWennTragbar = false;
                    } else {
                        // keiner will mehr.
                        unit = null;
                        break;
                    }
                }
            } // nächstes Item und/oder nächste Unit
        } // Ende des Blocks

        // wenn's jetzt noch keine hat, dann bleibt's halt liegen:
        jackpot.clear();
        
        for (Unit u : beuteMeldungen.keySet()) {
            new Battle(u + " sammelt " + StringUtils.aufzaehlung(beuteMeldungen.get(u))  + " auf.", u);
        }
        
        
        // Meldungen über Gefallene:
        gefallenenMeldungen();
    }

    private void gefallenenMeldungen() {
        // den jeweiligen Meldungsemfänger bestimmen:
        Map<Partei, Unit> chefs = new HashMap<Partei, Unit>();
        for (Partei p : getParteien()) {
            Unit chef = besterTaktiker(new HashSet<Unit>(getUnits(p)));
            if (chef != null) chefs.put(p, chef);
        }
        
        Map<Partei, Integer> summeGefallener = new HashMap<Partei, Integer>();
        Map<Partei, List<String>> ausgeloeschte = new HashMap<Partei, List<String>>();
        for (Unit vorher : anfangsUnits) {
            Unit jetzt = Unit.Load(vorher.getNummer());
            if (vorher.getPersonen() > jetzt.getPersonen()) {
                Partei p = Partei.getPartei(jetzt.getOwner());
                int gefallene = vorher.getPersonen() - jetzt.getPersonen();
                
                // die Gesamtbilanz entsprechend erhöhen:
                if (!summeGefallener.containsKey(p)) summeGefallener.put(p, 0);
                summeGefallener.put(p, summeGefallener.get(p) + gefallene);
                
                // die Einheit (oder deren Chef) informieren:
                if (jetzt.getPersonen() > 0) {
                    if (jetzt.getPersonen() == 1) {
                        new Info(jetzt + " trauert um die " + gefallene + " gefallenen Kollegen.", jetzt);
                    } else {
                        new Info(jetzt + " haben " + gefallene + " Gefallene zu beklagen.", jetzt);
                    }
                } else {
                    // die Einheit wurde ausgelöscht - das sagen wir dem Chef:
                    if (!ausgeloeschte.containsKey(p)) ausgeloeschte.put(p, new ArrayList<String>());
                    ausgeloeschte.get(p).add(jetzt + " (" + vorher.getPersonen() + " " + vorher.getRassenName() + ")");
                }
            }
        }

        for (Partei p : ausgeloeschte.keySet()) {
            if (chefs.get(p) != null) {
                new Info(StringUtils.aufzaehlung(ausgeloeschte.get(p)) + " sind komplett aufgerieben worden.", chefs.get(p));
            } else {
                // oha, keiner ist übrig...
                Set<Unit> ehemalige = new HashSet<Unit>();
                for (Unit u : anfangsUnits) {
                    if (u.getOwner() == p.getNummer()) ehemalige.add(u);
                }
                
                Region r = getGefecht().getRegion();
                Coords my = p.getPrivateCoords(r.getCoords());
                new Botschaft(null, p, "In " + r + " " + my + " sind unsere Kämpfer (" + StringUtils.aufzaehlung(ehemalige) + ") komplett besiegt worden!");
            }
        }
        
        for (Partei p : summeGefallener.keySet()) {
            if (chefs.get(p) != null) {
                new Info("Insgesamt " + summeGefallener.get(p) + " von unserem Volk sind im Kampf gefallen.", chefs.get(p));
            } else {
                // oha, keiner ist übrig...
                new Botschaft(null, p, "Dabei sind insgesamt " + summeGefallener.get(p) + " von unserem Volk im Kampf gefallen.");
            }
        }
        
    }
    
    /**
     *  macht aus den verbleibenden Kriegern wieder komplette Einheiten
     */
    private void CleanUp(List<Krieger> reihe) {
        Set<Unit> units = new HashSet<Unit>();
        for (Krieger krieger : reihe) {
            units.add(krieger.getUnit());
        }

        for (Unit u : units) {
            u.setLebenspunkte(0);
            u.setPersonen(0);
        }

        for (Krieger krieger : reihe) {
            Unit unit = krieger.getUnit();
            // Lebenspunkte verrechnen
            unit.setLebenspunkte(unit.getLebenspunkte() + krieger.getLebenspunkte());
            unit.setPersonen(unit.getPersonen() + 1);
        }
    }

    /**
     * überprüft ob das Item genau einmal vorhanden ist und gibt dann true zurück
     * @param unit - diese Einheit
     * @param item - diese Item soltle vorhanden sein 
     * @return TRUE wenn vorhanden
     */
    private boolean checkItem(Unit unit, Class<? extends Item> item) {
        return checkItem(unit, item, 1);
    }

    /**
     * überprüft ob das Item genau X-mal vorhanden ist und gibt dann true zurück
     * @param unit - diese Einheit
     * @param item - dieses Item sollte vorhanden sein
     * @param anzahl - so viele Items müssen vorhanden sein 
     * @return TRUE wenn vorhanden
     */
    private boolean checkItem(Unit unit, Class<? extends Item> item, int anzahl) {
        if (unit.getItem(item).getAnzahl() >= anzahl) {
            return true;
        }
        return false;
    }

    /**
     * erstellt einen neuen getKrieger aus der Einheit ... in der Einheit werden dann
     * aber auch die entsprechenden Items/Personen abgezogen ...
     * Talente werden berücksichtigt - die Einheit benutzt bevorzugt die Waffen,
     * für die sie das höchste Talent hat.
     * @param unit
     * @param index
     * @return
     */
    private Krieger Factory(Unit unit, int index) {
        Krieger krieger = new Krieger(unit);

        // dieser getKrieger ist der ...te aus seiner Einheit
        krieger.setIndex(index);

        TreeMap<Integer, Weapon> frontWaffen = new TreeMap<Integer, Weapon>();
        TreeMap<Integer, Weapon> schussWaffen = new TreeMap<Integer, Weapon>();
        TreeMap<Integer, Weapon> reitTiere = new TreeMap<Integer, Weapon>();
        TreeMap<Integer, Weapon> ruestungen = new TreeMap<Integer, Weapon>();
        TreeMap<Integer, Weapon> schilde = new TreeMap<Integer, Weapon>();

        // alles mögliche an Ausrüstung / Waffen einsammeln:
        List<Weapon> equipment = new ArrayList<Weapon>();
        for (Item it : unit.getItems()) {
            if (it.getAnzahl() < 1) {
                continue; // davon haben wir keine (mehr)...
            }
            Weapon w = Weapon.FromItem(unit, it);
            if (w == null) {
                // new Debug("Waffe ist null für Item " + it.getName());
                continue;
            }

            equipment.add(w);
        }
        // magisches?
        if (unit.Talentwert(Magie.class) > 0) {
        }


        for (Weapon w : equipment) {
            int talentwert = 0;
            if ((w.neededSkill() != null) && (w.neededSkill() != Skill.class)) {
                talentwert = unit.Talentwert(w.neededSkill());
            }

            int nAngriffe = 0;
            if (w instanceof ITeureWaffe) {
                // ggf. nicht die Kosten "abbuchen"
                nAngriffe = ((ITeureWaffe)w).numberOfAttacksNurZurInfo();
            } else {
                nAngriffe = w.numberOfAttacks();
            }

            if ((w.getWeaponType() == WeaponType.Nahkampf) || (w.getWeaponType() == WeaponType.Distanzkampf)) {
                
                frontWaffen.put(Math.round(talentwert * w.AverageDamageValue() * nAngriffe + talentwert), w);
            }

            if (w.istFernkampfTauglich()) {
                schussWaffen.put(Math.round(talentwert * w.AverageDamageValue() * nAngriffe + talentwert), w);
            }

            if ((w.getWeaponType() == WeaponType.Tier)) {
                // das heißt, ein Pferd (oder andere Tiere, mit denen sich nicht direkt angreifen lässt) ist immer die letzte Wahl.
                reitTiere.put(talentwert * nAngriffe + talentwert, w);
            }

            if ((w.getWeaponType() == WeaponType.Panzer)) {
                int wert = Math.round(w.AverageBlockValue() * 100);
                ruestungen.put(wert, w);
            }

            if ((w.getWeaponType() == WeaponType.Schild)) {
                int wert = Math.round(w.AverageBlockValue() * 100);
                schilde.put(wert, w);
            }
        }

        // maximal eine Frontwaffe - höchster Wert (TW * durchschnittl. Schaden):
        Map.Entry<Integer, Weapon> entry = frontWaffen.lastEntry();
        if (entry != null) {
            if (entry.getKey() > 0) {
                Weapon w = entry.getValue();
                Class<? extends Item> it = w.getUrsprungsItem();
                if (checkItem(unit, it)) {
                    unit.addItem(it, -1);
                    krieger.weapons.add(w);
                }
            } else {
                if (DuellAnalyse.AKTIV) DuellAnalyse.I().message(unit, "könnte eine Frontwaffe " + entry.getValue() + " benutzen, aber hat kein Talent dafür.");
                new Debug(krieger + " könnte eine Frontwaffe " + entry.getValue() + " benutzen, aber er hat kein Talent dafür.");
            }
        }

        // maximal eine Fernkampfwaffe - wir versuchen es mit dem höchsten Talentwert:
        entry = schussWaffen.lastEntry();
        if (entry != null) {
            if (entry.getKey() > 0) {
                Weapon w = entry.getValue();
                Class<? extends Item> it = w.getUrsprungsItem();
                if (checkItem(unit, it)) {
                    unit.addItem(it, -1);
                    krieger.weapons.add(w);
                }
            } else {
                if (DuellAnalyse.AKTIV) DuellAnalyse.I().message(unit, "könnte eine Fernkampfwaffe " + entry.getValue() + " benutzen, aber hat kein Talent dafür.");
                new Debug(krieger + " könnte eine Schusswaffe " + entry.getValue() + " benutzen, aber er hat kein Talent dafür.");
            }
        }

        // maximal ein Tier - wir versuchen es mit dem höchsten Talentwert:
        entry = reitTiere.lastEntry();
        if (entry != null) {
            if (entry.getKey() > 0) {
                Weapon w = entry.getValue();
                Class<? extends Item> it = w.getUrsprungsItem();
                if (checkItem(unit, it)) {
                    unit.addItem(it, -1);
                    krieger.weapons.add(w);
                }
            } else {
                if (DuellAnalyse.AKTIV) DuellAnalyse.I().message(unit, "könnte ein Tier " + entry.getValue() + " führen, aber hat kein Talent dafür.");
                new Debug(krieger + " könnte ein Tier " + entry.getValue() + " führen, aber er hat kein Talent dafür.");
            }
        }

        // maximal eine Rüstung - wir versuchen es mit dem höchsten durchschnittlich geblockten Schaden:
        entry = ruestungen.lastEntry();
        if (entry != null) {
            if (entry.getKey() > 0) {
                Weapon w = entry.getValue();
                Class<? extends Item> it = w.getUrsprungsItem();
                if (checkItem(unit, it)) {
                    unit.addItem(it, -1);
                    krieger.weapons.add(w);
                }
            } else {
                new Debug(krieger + " könnte eine Rüstung " + entry.getValue() + " benutzen, aber Fantasya hält diese Rüstung für unnütz.");
            }
        }

        // maximal ein Schild - wir versuchen es mit dem höchsten durchschnittlich geblockten Schaden:
        entry = schilde.lastEntry();
        if (entry != null) {
            if (entry.getKey() > 0) {
                Weapon w = entry.getValue();
                Class<? extends Item> it = w.getUrsprungsItem();
                if (checkItem(unit, it)) {
                    unit.addItem(it, -1);
                    krieger.weapons.add(w);
                }
            } else {
                new Debug(krieger + " könnte einen Schild " + entry.getValue() + " benutzen, aber Fantasya hält das für unnütz.");
            }
        }

        // Monster haben zusätzliche eigene Waffen ... die sind aber nicht
        // öffentlich verfügbar und daher einfach da
        if (Monster.class.isInstance(unit)) {
            ((Monster) unit).Factory(krieger);
        }

        if (unit.Talentwert(Magie.class) > 0) {
            CheckMageWeapons(krieger);
        }

        for (Weapon w : krieger.weapons) {
            if (w instanceof WPegasus) {
                if (!krieger.getUnit().getRasse().equalsIgnoreCase("troll")) {
                    int bonus = krieger.getUnit().Talentwert(Reiten.class) - 2;
                    if (bonus > 0) {
                        krieger.addEffect(new BFXFliegendOffensiv(bonus));
                        krieger.addEffect(new BFXFliegendDefensiv(bonus));
                    }
                }
            }
        }

        return krieger;
    }

    /**
     * überprüft die magischen Waffen eines Zauberers ... also Angriffszauber und Verteidigungszauber
     * @param mage - der Zauberer
     */
    private void CheckMageWeapons(Krieger krieger) {
        if (krieger.unit.hasProperty(Kampfzauber.ATTACKSPELL)) {
            // zaubern
            // TODO Hier auch Einzelbefehl() nutzen
	        String as = krieger.unit.getStringProperty(Kampfzauber.ATTACKSPELL);
            String befehl[] = RecreateCommand(as.split("\\ "));
            Spell spell = Zaubern.FindSpell(befehl[0]);
            if (spell != null && spell.canUsedBy(krieger.unit)) {
                ((AttackSpell) spell).ExecuteSpell(krieger, befehl);
            }
        }
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
                    // ein kleines Zwischenstück ... zb. "der" oder "tausend" bei "Hain der tausend Eichen" 
                    // braucht nix gemacht werden
                }
            } else {
                als.add(current.toString());
                current = null;
            }
        }

        String[] b = new String[als.size()];
        for (int i = 0; i < als.size(); i++) {
            b[i] = als.get(i);
        }
        return b;
    }

    /**
     * gibt den besten Taktiker zurück
     * @return bester Taktiker der Seite
     */
    public Unit besterTaktiker() {
        Unit taktiker = null;	// der aktuell beste Taktiker

        Set<Unit> tapfere = new HashSet<Unit>();
        for (Krieger k : vorne) {
            tapfere.add(k.getUnit());
        }
        for (Krieger k : hinten) {
            tapfere.add(k.getUnit());
        }

        // Front ... der taktiker ist pauschal erstmal der beste
        taktiker = besterTaktiker(tapfere);
        if (taktiker != null) {
            return taktiker;
        }

        // bisher keine Taktiker ... d.h. das alle möglichen Einheiten
        // in der letzten Reihen stehen ... Angriff auf Zivilisten
        // *bäh* - der spielt ja wie ich
        // wenn das jetzt auch null war ... dann wurde die Partei bereits ausgelöscht
        // in dieser Region
        Set<Unit> andere = new HashSet<Unit>();
        for (Krieger k : nicht) {
            andere.add(k.getUnit());
        }
        return besterTaktiker(andere);

        // Taktiker die nicht kämpfen haben pech ... außer die ersten beiden Reihen sind leer
        // das wurde dann aber schon vorher geklärt
    }

    /**
     * liefert den besten Taktiker
     * @param anfangsUnits - mögliche Einheiten
     * @return der beste Taktiker (durchaus auch eine Einheit TW == 0 wenn kein Taktiker vohanden ist)
     */
    private Unit besterTaktiker(Set<Unit> units) {
        Unit taktiker = null; // der Beste der Besten

        for (Unit unit : units) {
            if (unit.getPersonen() == 0) continue;
            
            if (taktiker == null) {
                taktiker = unit;
            } else if (taktiker.Talentwert(Taktik.class) < unit.Talentwert(Taktik.class)) {
                taktiker = unit;
            }
        }

        return taktiker;
    }

    /**
     * @param partei
     * @return die Liste aller (hier noch vorhandenen!) Einheiten einer Partei
     */
    public List<Unit> getUnits(Partei partei) {
        Set<Unit> units = new HashSet<Unit>();

        for (Krieger k : this.vorne) {
            if (k.getUnit().getOwner() == partei.getNummer()) {
                units.add(k.getUnit());
            }
        }
        for (Krieger k : this.hinten) {
            if (k.getUnit().getOwner() == partei.getNummer()) {
                units.add(k.getUnit());
            }
        }
        for (Krieger k : this.nicht) {
            if (k.getUnit().getOwner() == partei.getNummer()) {
                units.add(k.getUnit());
            }
        }

        List<Unit> retval = new ArrayList<Unit>();
        retval.addAll(units);
        return retval;
    }

    /**
     * @param partei
     * @return die Liste aller (hier noch vorhandenen!) Einheiten einer Partei
     */
    public List<Unit> getUnits(Gruppe gruppe) {
        Set<Unit> units = new HashSet<Unit>();

        for (Unit maybe : gruppe.getUnits()) {
            for (Krieger k : this.vorne) {
                if (k.getUnit().getNummer() == maybe.getNummer()) {
                    units.add(k.getUnit());
                }
            }
            for (Krieger k : this.hinten) {
                if (k.getUnit().getNummer() == maybe.getNummer()) {
                    units.add(k.getUnit());
                }
            }
            for (Krieger k : this.nicht) {
                if (k.getUnit().getNummer() == maybe.getNummer()) {
                    units.add(k.getUnit());
                }
            }
        }

        List<Unit> retval = new ArrayList<Unit>();
        retval.addAll(units);
        return retval;
    }


    /**
     * liefert die getKrieger zu einer Einheit (wozu auch immer ich das benötige)
     * @param unit - diese Einheit und deren getKrieger
     * @return die ArrayListe mit den Kriegern
     */
    public List<Krieger> getKrieger(Unit unit) {
        List<Krieger> alk = new ArrayList<Krieger>();

        for (Krieger k : vorne) {
            if (k.getUnit().getNummer() == unit.getNummer()) {
                alk.add(k);
            }
        }
        for (Krieger k : hinten) {
            if (k.getUnit().getNummer() == unit.getNummer()) {
                alk.add(k);
            }
        }
        for (Krieger k : nicht) {
            if (k.getUnit().getNummer() == unit.getNummer()) {
                alk.add(k);
            }
        }

        return alk;
    }

    /**
     * @return eine Liste ALLER lebenden Krieger dieser Seite.
     */
    public List<Krieger> getKrieger() {
        List<Krieger> alk = new ArrayList<Krieger>();
        alk.addAll(vorne);
        alk.addAll(hinten);
        alk.addAll(nicht);
        alk.addAll(immun);
        return alk;
    }

    /**
     * @return true, wenn diese Seite wenigstens einen aktiven Krieger hat.
     */
    public boolean istLebendig() {
        if (!vorne.isEmpty()) {
            return true;
        }
        if (!hinten.isEmpty()) {
            return true;
        }
        if (!nicht.isEmpty()) {
            return true;
        }
        // immune Einheiten spielen hier keine Rolle!
        return false;
    }

    /**
     * Problem ... der Gegner hat uns überrannt ... daher müssen jetzt alle aus den
     * hinteren Reihen nach vorne
     * @param anzahl - soviele Hinterbänkler müssen an die Front
     * @return die aufgerückten getKrieger, zwecks Meldung
     */
    public KriegerCounter frontVerstaerken(int anzahl) {
        List<Krieger> kandidaten = new ArrayList<Krieger>();
        kandidaten.addAll(hinten);
        Collections.shuffle(kandidaten);

        // Krieger mit Nahkampfwaffe rücken zuerst auf:
        Collections.sort(kandidaten, new KriegerNahkampfComparator());

        KriegerCounter kc = new KriegerCounter();

        while (vorne.size() < anzahl) {
            if (hinten.isEmpty()) {
                break;
            }

            Krieger k = hinten.get(0);
            hinten.remove(0);

            k.setReihe(Krieger.REIHE_VORNE);
            vorne.add(k);

            kc.count(k);
        }

        return kc;
    }

    /**
     * dies wird nur einmal ausgeführt ... nämlich dann wenn am Anfang die Seiten
     * geklärt werden und in den ersten beiden Reihen keine getKrieger stehen ... dann
     * wandern die Hasenfüße nach vorne auf die Schlachtbank
     */
    protected void zivilVerteidigung() {
        for (Krieger k : nicht) {
            new Debug("Zivilverteidigung: " + k + " geht an die Front.");
            k.setReihe(Krieger.REIHE_VORNE);
        }

        vorne.addAll(nicht);
        nicht.clear();
    }

    public KampfreportXML bericht(int nr) {
        return bericht(Partei.getPartei(nr));
    }

    public KampfreportXML bericht(Partei p) {
        return berichte.get(p);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * @return die Gruppe, wenn es nur genau eine Gruppe in dieser Side gibt - sonst (null).
     */
    public Gruppe wennEinzelGruppe() {
        Gruppe retval = null;
        int cnt = 0;
        for (Partei p : teilnehmer.keySet()) {
            for (Gruppe g : teilnehmer.get(p)) {
                cnt ++;
                retval = g;
            }
        }
        
        if (cnt != 1) return null;
        
        return retval;
    }

    public String beschreibeTeilnehmer(Partei betrachter) {
        Gruppe einzeln = wennEinzelGruppe();
        if (einzeln != null) return einzeln.beschreibeFuerPartei(betrachter);

        Set<String> gruppenNamen = new HashSet<String>();
        for (Partei p : teilnehmer.keySet()) {
            for (Gruppe g : teilnehmer.get(p)) gruppenNamen.add(g.beschreibeFuerPartei(betrachter));
        }
        return StringUtils.aufzaehlung(gruppenNamen);
    }

    /**
     * @return Beschreibung für Debug-Zwecke (!)
     */
    public String describe() {
        StringBuilder sb = new StringBuilder();

        sb.append("VORNE:\n");
        sb.append(beschreibeReihe(vorne));
        sb.append("\n");

        sb.append("HINTEN:\n");
        sb.append(beschreibeReihe(hinten));
        sb.append("\n");

        sb.append("NICHT:\n");
        sb.append(beschreibeReihe(nicht));
        sb.append("\n");

        sb.append("IMMUN:\n");
        sb.append(beschreibeReihe(immun));
        sb.append("\n");

        return sb.toString();
    }

    public String beschreibeReihe(List<Krieger> krieger) {
        KriegerCounter kc = new KriegerCounter();
        List<Unit> units = new ArrayList<Unit>();
        for (Krieger k : krieger) {
            if (!units.contains(k.getUnit())) {
                units.add(k.getUnit());
            }
            kc.count(k);
        }

        List<String> parts = new ArrayList<String>();
        for (Unit u : units) {
            StringBuilder part = new StringBuilder();
            part.append("\t" + kc.getCount(u) + " von " + u);
            for (String prop : u.getProperties()) {
                part.append(" (" + prop + "=" + u.getStringProperty(prop) + ")");
            }
            parts.add(part.toString());
        }

        return StringUtils.aufzaehlung(parts);
    }

    /**
     * @param u Die fragliche Einheit
     * @return &quot;Beutemodus&quot; als int-Konstante Kriege.BEUTE_*** (Default: BEUTE_TRAGBAR)
     */
    private int beuteModus(Unit u) {
        if (Kriege.BEUTE_MODI.containsKey(u)) {
            return Kriege.BEUTE_MODI.get(u);
        }
        return Kriege.BEUTE_ALLES;
    }

    public Map<Partei, Set<Gruppe>> getTeilnehmer() {
        return teilnehmer;
    }
    
    
}
