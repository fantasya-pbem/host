package de.x8bit.Fantasya.Host.EVA.util;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Helper.RegionsSicht;
import de.x8bit.Fantasya.util.StringUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author hapebe
 */
@SuppressWarnings("serial")
public class Atlas extends HashMap<Coords, RegionsSicht> {

    /**
     * nimmt die RegionsSicht nur dann auf, wenn sie neu ist; oder neuer als die vorhandene.
     * @param key
     * @param value
     * @return (siehe HashMap; gibt aber auch den vorhandenen Wert zurück, wenn der NICHT verdrängt wurde)
     */
    @Override
    public RegionsSicht put(Coords key, RegionsSicht value) {
        if (containsKey(key)) {
            if (value.getRunde() > get(key).getRunde()) {
                return super.put(key, value);
            }
        } else {
            return super.put(key, value);
        }

        return get(key);
    }
    
    @Override
    public String toString() {
        return "Atlas mit " + size() + " Regionen: " + asCodes();
    }
    
    public String asCodes() {
        List<String> eintraege = new ArrayList<String>();
        for (RegionsSicht rs : values()) {
            eintraege.add(rs.toCode());
        }
        return StringUtils.join(eintraege, " ");
    }

    
    
}
