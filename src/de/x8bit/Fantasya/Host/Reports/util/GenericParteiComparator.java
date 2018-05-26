package de.x8bit.Fantasya.Host.Reports.util;

import de.x8bit.Fantasya.Atlantis.Partei;
import java.util.Comparator;
import java.util.Map;

/**
 *
 * @author hapebe
 */
public class GenericParteiComparator implements Comparator<Partei>{
	
	final Map<Partei, Float> ranking;

	public GenericParteiComparator(Map<Partei, Float> ranking) {
		this.ranking = ranking;
	}

	@Override
	public int compare(Partei p1, Partei p2) {
		if (ranking.get(p1) < ranking.get(p2)) return -1;
		if (ranking.get(p1) > ranking.get(p2)) return +1;
		return 0;
	}
	
}
