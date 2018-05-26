package de.x8bit.Fantasya.Host.ZAT.Battle;

/**
 *
 * @author hapebe
 */
@SuppressWarnings("rawtypes")
public class GruppenPaarung implements Comparable {
    final Gruppe a;
    final Gruppe b;
    int wichtigkeit = 0;

    /**
     * @param a der Angreifer (liefert auch die "Wichtigkeit" dieser Paarung)
     * @param b der Verteidiger
     */
    public GruppenPaarung(Gruppe a, Gruppe b) {
        if ((a == null) || (b == null)) {
            throw new IllegalArgumentException("Eine GruppenPaarung darf keine Gruppe <null> enthalten.");
        }
        this.a = a;
        this.b = b;
        this.wichtigkeit = a.getWichtigkeit();
    }

    public Gruppe getA() {
        return a;
    }

    public Gruppe getB() {
        return b;
    }

    public int getWichtigkeit() {
        return wichtigkeit;
    }

    public void setWichtigkeit(int wichtigkeit) {
        this.wichtigkeit = wichtigkeit;
    }

    public boolean istInvers(GruppenPaarung other) {
        if (other == null) {
            return false;
        }
        if ((this.a.equals(other.b)) && (this.b.equals(other.a))) {
            return true;
        }
        return false;
    }

    public GruppenPaarung invers() {
        GruppenPaarung inv = new GruppenPaarung(getB(), getA());
        inv.setWichtigkeit(this.getWichtigkeit());
        return inv;
    }

    @Override
    public String toString() {
        return "Paar " + a.shortDesc() + " - " + b.shortDesc() + " (w:" + getWichtigkeit() + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GruppenPaarung other = (GruppenPaarung) obj;
        if (!this.a.equals(other.a)) {
            return false;
        }
        if (!this.b.equals(other.b)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + this.a.hashCode();
        hash = 37 * hash + this.b.hashCode();
        return hash;
    }

    @Override
    public int compareTo(Object o) {
        if (o == null) {
            throw new IllegalArgumentException("Kann GruppenPaarung nicht mit <null> vergleichen.");
        }
        if (!(o instanceof GruppenPaarung)) {
            throw new IllegalArgumentException("Kann GruppenPaarung nicht mit " + o.getClass().getCanonicalName() + " vergleichen.");
        }
        GruppenPaarung gp2 = (GruppenPaarung) o;
        int cmpA = this.getA().compareTo(gp2.getA());
        if (cmpA != 0) {
            return cmpA;
        }
        int cmpB = this.getB().compareTo(gp2.getB());
        if (cmpB != 0) {
            return cmpB;
        }
        if (!this.equals(gp2)) {
            throw new IllegalStateException("compare ergibt 0, aber " + this + " und " + gp2 + " sind nicht equal)!");
        }
        return 0;
    }
    
}
