package de.x8bit.Fantasya.Host.Reports.util;

import java.util.Comparator;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;

/**
 * @author hb
 */
public class RegionReportComparatorPersonen implements Comparator<Region> {

    Partei p;

    public RegionReportComparatorPersonen(Partei p) {
        this.p = p;
    }

    /**
     *
     * @param r1
     * @param r2
     * @return -1 wenn r1 < r2, 0 wenn r1 == r2, +1 wenn r1 > r2
     */
    @Override
    public int compare(Region r1, Region r2) {
        int n1 = 0; int n2 = 0;
        for (Unit u : r1.getUnits()) if (u.getOwner() == p.getNummer()) n1 += u.getPersonen();
        for (Unit u : r2.getUnits()) if (u.getOwner() == p.getNummer()) n2 += u.getPersonen();

        if (n1 > n2) return -1;
        if (n1 < n2) return +1;

        return (new RegionReportComparatorLNR()).compare(r1, r2);
    }

}
