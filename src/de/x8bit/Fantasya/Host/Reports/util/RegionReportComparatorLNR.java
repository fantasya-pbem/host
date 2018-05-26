package de.x8bit.Fantasya.Host.Reports.util;

import java.util.Comparator;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Region;

/**
 * @author hb
 */
public class RegionReportComparatorLNR implements Comparator<Region> {

    /**
     *
     * @param r1
     * @param r2
     * @return -1 wenn r1 < r2, 0 wenn r1 == r2, +1 wenn r1 > r2
     */
    @Override
    public int compare(Region r1, Region r2) {
        Coords c1 = r1.getCoords();
        Coords c2 = r2.getCoords();
		
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
