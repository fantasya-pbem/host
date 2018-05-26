package de.x8bit.Fantasya.util.comparator;

import de.x8bit.Fantasya.Atlantis.Item;
import java.util.Comparator;

/**
 *
 * @author hb
 */
public class ItemStueckGewichtComparator implements Comparator<Item> {

    @Override
    public int compare(Item i1, Item i2) {
        if (i1.getGewicht() < i2.getGewicht()) {
            return -1;
        } else if (i1.getGewicht() < i2.getGewicht()) {
            return +1;
        }
        return 0;
    }

}
