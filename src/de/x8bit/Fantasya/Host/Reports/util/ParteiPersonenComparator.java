package de.x8bit.Fantasya.Host.Reports.util;

import de.x8bit.Fantasya.Atlantis.Partei;
import java.util.Comparator;

/**
 *
 * @author hapebe
 */
public class ParteiPersonenComparator implements Comparator<Partei>{

	@Override
	public int compare(Partei p1, Partei p2) {
		int n1 = p1.getPersonen();
		int n2 = p2.getPersonen();
		
		if (n1 < n2) return -1;
		if (n1 > n2) return +1;
		return 0;
	}
	
}
