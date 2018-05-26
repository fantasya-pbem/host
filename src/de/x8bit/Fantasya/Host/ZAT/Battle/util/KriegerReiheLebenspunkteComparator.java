package de.x8bit.Fantasya.Host.ZAT.Battle.util;

import de.x8bit.Fantasya.Host.ZAT.Battle.Krieger;
import java.util.Comparator;

/**
 *
 * @author hapebe
 */
public class KriegerReiheLebenspunkteComparator implements Comparator<Krieger> {

	public int compare(Krieger k1, Krieger k2) {
        if (k1.getReihe() > k2.getReihe()) return -1;
        if (k1.getReihe() < k2.getReihe()) return 1;

		if (k1.getLebenspunkte() < k2.getLebenspunkte()) return -1;
		if (k1.getLebenspunkte() > k2.getLebenspunkte()) return 1;
		return 0;
	}

}
