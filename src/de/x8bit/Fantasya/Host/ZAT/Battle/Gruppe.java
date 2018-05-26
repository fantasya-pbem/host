package de.x8bit.Fantasya.Host.ZAT.Battle;

import de.x8bit.Fantasya.Atlantis.Kampfposition;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.util.Codierung;
import de.x8bit.Fantasya.util.StringUtils;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author hapebe
 */
@SuppressWarnings("rawtypes")
public class Gruppe implements Comparable {
    protected final int parteiNr;
    protected final int tarnParteiNr;
    protected final Set<Unit> units = new HashSet<Unit>();
    boolean istAngreifer;
    boolean istVerteidiger;
    boolean istHelfer;
    boolean istVerbuendeter;

    public Gruppe(int parteiNr, int tarnParteiNr) {
        this.parteiNr = parteiNr;
        this.tarnParteiNr = tarnParteiNr;
    }

    public int getParteiNr() {
        return parteiNr;
    }

    public int getTarnParteiNr() {
        return tarnParteiNr;
    }

    public Set<Unit> getUnits() {
        return units;
    }
    
    /**
     * @return die größte Einheit in dieser Gruppe
     */
    public Unit getHauptUnit() {
        int maxN = Integer.MIN_VALUE;
        Unit retval = null;
        for (Unit u : getUnits()) {
            if (u.getPersonen() > maxN) {
                retval = u;
                maxN = u.getPersonen();
            }
        }
        return retval;
    }

    public int getPersonen() {
        int retval = 0;
        for (Unit u : getUnits()) {
            retval += u.getPersonen();
        }
        return retval;
    }

    /**
     * @return eine Kennzahl für den Einfluss eines möglichen Angriffswunsches bei der Bestimmung der effektiven Angreifer und Verteidiger (für den Fall sich widersprechender Angriffswünsche zwischen mehreren Gruppen)
     */
    public int getWichtigkeit() {
        int retval = 0;
        for (Unit u : getUnits()) {
            if (u.getKampfposition().equals(Kampfposition.Nicht)) {
                continue;
            }
            if (u.getKampfposition().equals(Kampfposition.Fliehe)) {
                continue;
            }
            if (u.getKampfposition().equals(Kampfposition.Immun)) {
                continue;
            }
            retval += u.maxLebenspunkte();
        }
        return retval;
    }

    public boolean istAngreifer() {
        return istAngreifer;
    }

    public void setAngreifer(boolean istAngreifer) {
        this.istAngreifer = istAngreifer;
    }

    public boolean istVerteidiger() {
        return istVerteidiger;
    }

    public void setVerteidiger(boolean istVerteidiger) {
        this.istVerteidiger = istVerteidiger;
    }

    public boolean istHelfer() {
        return istHelfer;
    }

    public void setHelfer(boolean istHelfer) {
        this.istHelfer = istHelfer;
    }

    public boolean istVerbuendeter() {
        return istVerbuendeter;
    }

    public void setVerbuendeter(boolean istVerbuendeter) {
        this.istVerbuendeter = istVerbuendeter;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Gruppe)) {
            return false;
        }
        Gruppe other = (Gruppe) obj;
        if (this.getParteiNr() == other.getParteiNr() && (this.getTarnParteiNr() == other.getTarnParteiNr())) {
            return true;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + this.parteiNr;
        hash = 97 * hash + this.tarnParteiNr;
        return hash;
    }

    @Override
    public int compareTo(Object o) {
        if (o == null) {
            throw new IllegalStateException("Kann eine Gruppe nicht mit <null> vergleichen.");
        }
        if (!(o instanceof Gruppe)) {
            throw new IllegalStateException("Kann eine Gruppe nicht mit " + o.getClass().getCanonicalName() + " vergleichen.");
        }
        Gruppe g2 = (Gruppe) o;
        if (this.getParteiNr() < g2.getParteiNr()) {
            return -1;
        }
        if (this.getParteiNr() > g2.getParteiNr()) {
            return +1;
        }
        if (this.getTarnParteiNr() < g2.getTarnParteiNr()) {
            return -1;
        }
        if (this.getTarnParteiNr() > g2.getTarnParteiNr()) {
            return +1;
        }
        if (this.equals(g2)) {
            return 0;
        }
        throw new IllegalStateException("HashCodes von " + this + " und " + g2 + " sind gleich, aber equals() liefert false.");
    }

    public String beschreibeFuerPartei(Partei p) {
        if (istAuthentisch()) {
            return Partei.getPartei(getParteiNr()).toString();
        } else {
            if (p.getNummer() == 0) {
                return "Gruppe von (" + Partei.getPartei(parteiNr) + " getarnt als) " + Partei.getPartei(tarnParteiNr);
            }
            if (getTarnParteiNr() == 0) {
                if (this.getParteiNr() == p.getNummer()) {
                    String desc = "eine Gruppe von uns, die ihre Herkunft verschweigt";
                    if (getUnits().size() == 1) {
                        desc = getHauptUnit() + " (verheimlicht, zu uns zu gehören)";
                    } else {
                        desc += " (um " + getHauptUnit() + ")";
                    }
                    return desc;
                }
                String desc = "eine Gruppe von K\u00e4mpfern unklarer Herkunft";
                if (getUnits().size() == 1) {
                    desc = getHauptUnit() + " (ungewisser Herkunft)";
                } else {
                    desc += " (um " + getHauptUnit() + ")";
                }
                return desc;
            } else {
                if (this.getParteiNr() == p.getNummer()) {
                    String desc = "eine Gruppe von uns, die sich als Angeh\u00f6rige von " + Partei.getPartei(this.getTarnParteiNr()) + " ausgibt";
                    if (getUnits().size() == 1) {
                        desc = getHauptUnit() + " (geben sich als Angehörige von " + Partei.getPartei(this.getTarnParteiNr()) + " aus)";
                    } else {
                        desc += " (um " + getHauptUnit() + ")";
                    }
                    return desc;
                }
                if (this.getTarnParteiNr() == p.getNummer()) {
                    String desc = "eine Gruppe von Betr\u00fcgern, die sich als Angeh\u00f6rige unseres Volkes ausgeben";
                    if (getUnits().size() == 1) {
                        desc = getHauptUnit() + " (Betrüger, die behaupten zu uns zu gehören)";
                    } else {
                        desc += " (um " + getHauptUnit() + ")";
                    }
                    return desc;
                }
                // TODO: Allianzen berücksichtigen - wenn sie sich als Alliierte ausgeben, es aber nicht sind --> melden.
                return Partei.getPartei(tarnParteiNr).toString();
            }
        }
    }

    public String microDesc() {
        return "[" + Codierung.toBase36(getParteiNr()) + "][" + Codierung.toBase36(getTarnParteiNr()) + "]";
    }

    public String shortDesc() {
        StringBuilder sb = new StringBuilder();
        sb.append(istAngreifer() ? "A" : "");
        sb.append(istVerteidiger() ? "V" : "");
        sb.append(istHelfer() ? "H" : "");
        sb.append(istVerbuendeter() ? "L" : ""); // L wie aLliiert.
        sb.append(sb.length() > 0 ? " " : "");
        sb.append("Grp. ");
        sb.append(microDesc());
        return sb.toString();
    }

    @Override
    public String toString() {
        return shortDesc() + "(" + StringUtils.aufzaehlung(getUnits()) + ")";
    }

    /**
     * @return true, wenn Partei und Tarnpartei übereinstimmen
     */
    public boolean istAuthentisch() {
        return getParteiNr() == getTarnParteiNr();
    }
    
}
