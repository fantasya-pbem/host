package de.x8bit.Fantasya.Host.Reports.util;

import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Unit;
import java.util.Comparator;

/**
 *
 * @author hapebe
 */
public class ParteiSilberComparator implements Comparator<Partei>{

	@Override
	public int compare(Partei p1, Partei p2) {
		int n1 = 0;
		for (Unit u : p1.getEinheiten()) n1 += u.getItem(Silber.class).getAnzahl();
		int n2 = 0;
		for (Unit u : p2.getEinheiten()) n2 += u.getItem(Silber.class).getAnzahl();
		
		if (n1 < n2) return -1;
		if (n1 > n2) return +1;
		return 0;
	}
	
}
