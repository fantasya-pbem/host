package de.x8bit.Fantasya.Host.Terraforming;

import de.x8bit.Fantasya.Atlantis.Coords;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author hb
 */
@SuppressWarnings("rawtypes")
public class CoordRelation implements Comparable {
    final Coords c;
    final int distance;

    public CoordRelation(Coords c) {
        this.c = c;
        this.distance = c.getDistance(new Coords(0,0,c.getWelt()));
    }

    @Override
    public int compareTo(Object o) {
        if (!(o instanceof CoordRelation)) throw new RuntimeException();
        CoordRelation other = (CoordRelation)o;

        if (other.getDistance() > this.getDistance()) return -1;
        if (other.getDistance() < this.getDistance()) return 1;
        return c.compareTo(other.getC());
    }

    public Coords getC() {
        return c;
    }

    public int getDistance() {
        return distance;
    }

	@SuppressWarnings("unchecked")
    public static List<CoordRelation> AllRelations(int welt, int x0, int y0, int x1, int y1) {
        if (x0 > x1) { int temp = x0; x0 = x1; x1 = temp; }
        if (y0 > y1) { int temp = y0; y0 = y1; y1 = temp; }

        List<CoordRelation> retval = new ArrayList<CoordRelation>();
        for (int x = x0; x < x1; x++) {
            for (int y = y0; y < y1; y++) {
                retval.add(new CoordRelation(new Coords(x, y, welt)));
            }
        }

        Collections.sort(retval);
        return retval;
    }
}
