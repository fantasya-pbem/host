package de.x8bit.Fantasya.Host.ZAT.Battle.util;

import de.x8bit.Fantasya.Host.ZAT.Battle.Krieger;
import java.util.Comparator;

/**
 * Sortiert Krieger mit einer Nahkampf-Waffe vor Krieger ohne.
 * @author hb
 */
public class KriegerNahkampfComparator implements Comparator<Krieger> {

    @Override
    public int compare(Krieger k1, Krieger k2) {
        if (k1.hatNahkampfWaffe() && (!k2.hatNahkampfWaffe())) return +1;
        if (k2.hatNahkampfWaffe()) return -1;
        return 0;
    }

}
