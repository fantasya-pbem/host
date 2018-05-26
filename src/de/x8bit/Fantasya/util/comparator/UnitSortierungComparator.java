package de.x8bit.Fantasya.util.comparator;

import java.util.Comparator;

import de.x8bit.Fantasya.Atlantis.Unit;

/**
 *
 * @author hb
 */
public class UnitSortierungComparator implements Comparator<Unit> {

    @Override
    public int compare(Unit u1, Unit u2) {
        if (u1.getSortierung() < u2.getSortierung()) return -1;
        if (u1.getSortierung() > u2.getSortierung()) return +1;

		if (u1.sortierGlueck < u2.sortierGlueck) return -1;
		if (u1.sortierGlueck > u2.sortierGlueck) return +1;

        return 0;
    }

}
