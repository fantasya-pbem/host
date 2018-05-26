package de.x8bit.Fantasya.Host.ZAT.Battle;

import java.util.TreeSet;

/**
 *
 * @author hapebe
 */
@SuppressWarnings("serial")
public class GruppenSet extends TreeSet<Gruppe> {

    public int getPersonen() {
        int retval = 0;
        for (Gruppe g : this) {
            retval += g.getPersonen();
        }
        return retval;
    }

    public boolean istAuthentisch() {
        for (Gruppe g : this) {
            if (!g.istAuthentisch()) {
                return false;
            }
        }
        return true;
    }

    public boolean hatAngreifer() {
        for (Gruppe g : this) {
            if (!g.istAngreifer()) {
                return true;
            }
        }
        return false;
    }
    
}
