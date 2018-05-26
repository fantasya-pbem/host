package de.x8bit.Fantasya.Host.Reports.util;

import java.util.Comparator;

import de.x8bit.Fantasya.Atlantis.Coords;

/**
 * @author hb
 */
public class CoordComparatorLNR implements Comparator<Coords> {

    /**
     *
     * @param c1
     * @param c2
     * @return -1 wenn r1 < r2, 0 wenn r1 == r2, +1 wenn r1 > r2
     */
    @Override
    public int compare(Coords c1, Coords c2) {
		// Oberwelt zuerst:
		if (c1.getWelt() < c2.getWelt()) return +1;
		if (c1.getWelt() > c2.getWelt()) return -1;

        if (c1.getY() > c2.getY()) return -1;
        if (c1.getY() < c2.getY()) return +1;

        if (c1.getX() < c2.getX()) return -1;
        if (c1.getX() > c2.getX()) return +1;

        return 0;
    }

}
