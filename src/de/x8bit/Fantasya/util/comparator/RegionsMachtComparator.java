package de.x8bit.Fantasya.util.comparator;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author hapebe
 */
public class RegionsMachtComparator implements Comparator<Unit> {

	private static final UnitSortierungComparator usc = new UnitSortierungComparator();
	private Map<Partei, ManpowerRecord> parteien = new HashMap<Partei, ManpowerRecord>();

	public RegionsMachtComparator(Region r) {
		for (Unit u : r.getUnits()) {
			Partei p = Partei.getPartei(u.getOwner());

			if (!parteien.containsKey(p)) {
				ManpowerRecord mpr = new ManpowerRecord(p);
				parteien.put(p, mpr);
			}

			parteien.get(p).addPersonen(u.getPersonen());
		}
	}

	public int compare(Unit u1, Unit u2) {
		Partei p1 = Partei.getPartei(u1.getOwner());
		Partei p2 = Partei.getPartei(u2.getOwner());

		if (parteien.get(p1).getPersonen() > parteien.get(p2).getPersonen()) return -1;
		if (parteien.get(p1).getPersonen() < parteien.get(p2).getPersonen()) return 1;

		return usc.compare(u1, u2);
	}

	@SuppressWarnings("rawtypes")
	private class ManpowerRecord implements Comparable {
		final Partei partei;
		int personen;

		public ManpowerRecord(Partei p) {
			partei = p;
			personen = 0;
		}

		@SuppressWarnings("unused")
		public Partei getPartei() {
			return partei;
		}

		public int getPersonen() {
			return personen;
		}

		public void addPersonen(int anzahl) {
			personen += anzahl;
		}

		public int compareTo(Object o) {
			ManpowerRecord other = (ManpowerRecord) o;

			if (this.getPersonen() < other.getPersonen()) return -1;
			if (this.getPersonen() > other.getPersonen()) return -1;

			return 0;
		}


	}

}
