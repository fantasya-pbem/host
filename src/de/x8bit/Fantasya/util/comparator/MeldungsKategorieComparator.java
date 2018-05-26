package de.x8bit.Fantasya.util.comparator;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Messages.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Dieser Comparator bestimmt über die Reihenfolge der Meldungsarten im NR.
 * @author hb
 */
// TODO in alle Reporte einbauen (naja, wenigstens die NRs)
public class MeldungsKategorieComparator implements Comparator<Class<? extends Message>> {
    public static final Map<Class<? extends Message>, Integer> SortierWerte = new HashMap<Class<? extends Message>, Integer>();
    static {
        SortierWerte.put(Greetings.class, -1000);
        SortierWerte.put(Botschaft.class, -999);
        SortierWerte.put(SysErr.class, -100);
        SortierWerte.put(SysMsg.class, -10);
        SortierWerte.put(Fehler.class, 1);
        SortierWerte.put(Battle.class, 2);
        SortierWerte.put(Magie.class, 3);
        SortierWerte.put(Zauberbuch.class, 4);
        SortierWerte.put(Info.class, 5);
        SortierWerte.put(EchsenNews.class, 6);
        SortierWerte.put(Handelsmeldungen.class, 7);
        SortierWerte.put(Bewegung.class, 8);
        SortierWerte.put(Spionage.class, 9);
    }

    @Override
    public int compare(Class<? extends Message> o1, Class<? extends Message> o2) {
        int v1 = Integer.MAX_VALUE; int v2 = Integer.MAX_VALUE;
        if (SortierWerte.containsKey(o1)) v1 = SortierWerte.get(o1);
        if (SortierWerte.containsKey(o2)) v2 = SortierWerte.get(o2);

        if (v1 < v2) return -1;
        if (v1 > v2) return +1;
        return 0;
    }

}
