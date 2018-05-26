package de.x8bit.Fantasya.Host.ZAT.Battle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Debug;
import de.x8bit.Fantasya.util.StringUtils;

/**
 * <p> Jedes Instanz dieser Klasse beschreibt einen "Typ" Krieger, der sich im
 * Kampf exakt gleich verhält - also in alle relevanten Parametern gleich ist. </p>
 * <p>Berücksichtigt werden: Rasse, "Trefferpunkte" (also maximal überlebbarer Schaden),
 * aktive Waffe UND dazugehöriger Talentwert, sämtliche aktive Rüstung, aktive Effekte,
 * unter denen der Krieger steht.</p>
 * <p>NICHT berücksichtigt werden: Die tatsächlichen "Lebenspunkte", also der
 * erlittene Schaden - die "Trefferpunkte", also der maximal überlebbare
 * Schaden wird sehr wohl berücksichtigt.</p>
 * @author hb
 */
public class KriegerTyp {

    private static Map<KriegerTyp, KriegerTyp> Typen = new HashMap<KriegerTyp,KriegerTyp>();

    final int trefferpunkte;
    final List<BattleEffects> fx;
    final List<Weapon> equipment;
    final Unit unit;

    private KriegerTyp(Unit unit, List<BattleEffects> fx, List<Weapon> equipment) {
        this.unit = unit;
        this.trefferpunkte = unit.maxLebenspunkte() / unit.getPersonen();
        this.fx = Collections.unmodifiableList(fx);
        this.equipment = Collections.unmodifiableList(equipment);
    }

    /**
     * @param k Krieger, dessen Typ bestimmt werden soll
     * @return Krieger-Typ - garantiert nicht null.
     */
    public static KriegerTyp getInstance(Krieger k) {
        return KriegerTyp.getInstance(k.getUnit(), k.getEffects(), k.weapons);
    }

    /**
     * @param unit Einheit eines Kriegers - dient zur Bestimmung von Rasse und maxLebenspunkte
     * @param trefferpunkte
     * @param fx
     * @param equipment
     * @return
     */
    public static KriegerTyp getInstance(Unit unit, List<BattleEffects> fx, List<Weapon> equipment) {
        KriegerTyp query = new KriegerTyp(unit, fx, equipment);
        KriegerTyp singleton = Typen.get(query);
        if (singleton != null) return singleton;

        Typen.put(query, query);
        return query;
    }

//    public String beschreibeAusruestung() {
//        StringBuffer retval = new StringBuffer();
//        for (Weapon w : this.getEquipment()) {
//            if (retval.length() > 0) retval.append(" ");
//            retval.append("[").append(w.beschreibeKurz()).append("]");
//        }
//        return retval.toString();
//    }

    /**
     * Eine "Code-Tabelle" für alle derzeit (!) bekannten KriegerTypen.
     * @return
     */
    public static String AusruestungsLegende() {
        StringBuilder sb = new StringBuilder();
        Map<String, String> legende = new HashMap<String,String>();
        for (KriegerTyp kt : Typen.keySet()) {
            new Debug("Ausrüstung? - " + kt.toString());
            for (Weapon w : kt.getEquipment()) {
                String code = w.kurzCode();
                if (!legende.containsKey(code)) {
                    legende.put(code, w.toString());
                }
            }
        }

        for (String code : legende.keySet()) {
            sb.append(String.format("%-5s", code)).append(" ").append(legende.get(code)).append("\n");
        }

        return sb.toString();
    }

    public List<Weapon> getEquipment() {
        return equipment;
    }

    public List<BattleEffects> getFx() {
        return fx;
    }

    public String getRasse() {
        return unit.getRasse();
    }

    public Unit getUnit() {
        return unit;
    }

    public int getTrefferpunkte() {
        return trefferpunkte;
    }


    @Override
    public int hashCode() {
        int hash = 7;
        if (this.unit != null) {
            hash = 17 * hash + this.unit.getRasse().hashCode();
        }
        hash = 17 * hash + this.trefferpunkte;
        hash = 17 * hash + (this.fx != null ? this.fx.size() : 0);
        hash = 17 * hash + (this.equipment != null ? this.equipment.size() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof KriegerTyp)) return false;
        KriegerTyp other = (KriegerTyp)o;

        if (!this.getRasse().equals(other.getRasse())) return false;
        if (this.getTrefferpunkte() != other.getTrefferpunkte()) return false;
        if (this.getEquipment().size() != other.getEquipment().size()) return false;
        if (this.getFx().size() != other.getFx().size()) return false;


        // wir können schon mal davon ausgehen, dass es gleich viel Ausrüstung gibt:
        List<Weapon> hisWeapons = new ArrayList<Weapon>();
        hisWeapons.addAll(other.getEquipment());
        for (Weapon my : this.getEquipment()) {
            if (my.getUnit().getNummer() != this.unit.getNummer()) {
                throw new RuntimeException("Angabe zur Einheit stimmt in KriegerTyp.equals() nicht überein: " + my.getUnit() + " vs. " + this.unit);
            }

            Weapon match = null;
            for (Weapon his : hisWeapons) {
                if (my.equals(his)) {
                    // Talentwerte vergleichen - auch, wenn (vielleicht nur bei einem Partner!) gar kein Talent benötigt wird:
                    int myTW = -1; int hisTW = -1;
                    if (my.neededSkill() != Skill.class) myTW = my.getUnit().Talentwert(my.neededSkill());
                    if (his.neededSkill() != Skill.class) hisTW = his.getUnit().Talentwert(his.neededSkill());
                    
                    if (myTW != hisTW) return false;

                    match = his;
                    break;
                }
            }
            // wenn es keine gleiche Ausrüstung gibt:
            if (match == null) return false;

            // ansonsten: aus der Liste entfernen, falls noch so ein Dings bei "mir" vorhanden ist.
            hisWeapons.remove(match);
        }


        // wir können davon ausgehen, dass es gleich viele Effekte gibt:
        List<BattleEffects> hisFX = new ArrayList<BattleEffects>();
        hisFX.addAll(other.getFx());
        for (BattleEffects my : this.getFx()) {
            BattleEffects match = null;
            for (BattleEffects his : hisFX) {
                if (my.equals(his)) {
                    match = his;
                    break;
                }
            }
            // wenn es keinen gleichen Effekt gibt:
            if (match == null) return false;

            // ansonsten: aus der Liste entfernen, falls noch so ein Effekt bei "mir" vorhanden ist.
            hisFX.remove(match);
        }

        // wir haben alles versucht - aber es gibt einfach keinen Unterschied!
        return true;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append(this.getRasse());
        sb.append(" (").append(getTrefferpunkte()).append("HP)");
        if (!this.getEquipment().isEmpty()) {
            List<String> parts = new ArrayList<String>();
            for (Weapon w : this.getEquipment()) {
                String wBeschreibung = w.toString();
                if ((w.neededSkill() != null) && (w.neededSkill() != Skill.class)) {
                    wBeschreibung += " (T" + this.unit.Talentwert(w.neededSkill()) + ")";
                }
                parts.add(wBeschreibung);
            }
            sb.append(" mit ").append(StringUtils.aufzaehlung(parts));
        }
        if (!this.getFx().isEmpty()) {
            sb.append(" unter dem Einfluss von ").append(StringUtils.aufzaehlung(getFx()));
        }

        return sb.toString();
    }

}
