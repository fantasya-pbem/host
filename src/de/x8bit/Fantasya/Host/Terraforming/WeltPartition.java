package de.x8bit.Fantasya.Host.Terraforming;

import de.x8bit.Fantasya.Host.Terraforming.ProtoInsel;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Region;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Jede Instanz dieser Klasse steht für einen Teil einer Weltkarte, der von
 * möglichen anderen Teilen vollständig getrennt ist - also durch Chaos bzw.
 * undefinierte Regionen
 * @author hapebe
 */
@SuppressWarnings("rawtypes")
public class WeltPartition extends TreeMap<Integer, Map<Integer, Integer>> implements Comparable {
	private static final long serialVersionUID = -5578107036551084015L;

	private WeltPartition() {
		
	}

	@SuppressWarnings("unchecked")
	public static List<WeltPartition> Partitioniere(ProtoInsel insel) {
		List<WeltPartition> partitionen = new ArrayList<WeltPartition>();

		WeltPartition zuKlaeren = new WeltPartition();
		for (Region r : insel.alleRegionen()) zuKlaeren.add(r.getCoords());

		while (!zuKlaeren.isEmpty()) {
			WeltPartition aktuell = new WeltPartition();

			Coords seed = zuKlaeren.getCoords().get(0);
			
			// die komplette Partition:
			aktuell.add(seed);

			// diejenigen Koordinaten, die noch "wachsen" können, d.h. die
			// möglicherweise noch Nachbarn haben, die auch zu 'aktuell'
			// gehören könnten.
			Set<Coords> aktive = new HashSet<Coords>();
			aktive.add(seed);

			// die Startkoordinate braucht nicht mehr geklärt zu werden,
			// sie gehört auf jeden Fall zu 'aktuell'
			zuKlaeren.remove(seed);

			for (boolean gewachsen = true; gewachsen; ) {
				gewachsen = false;
				Set<Coords> neueAktive = new HashSet<Coords>();

				for (Coords c : aktive) {
					for (Coords n : c.getNachbarn()) {
						if (!zuKlaeren.contains(n)) continue;
						if (aktuell.contains(n)) continue;

						// gotcha!
						gewachsen = true;
						aktuell.add(n);
						neueAktive.add(n);
						zuKlaeren.remove(n);
					}
					
				}
				aktive = neueAktive;
			}

			partitionen.add(aktuell);
		}

		Collections.sort(partitionen);
		return partitionen;
	}

	public void add(Coords c) {
		int x = c.getX();
		int y = c.getY();
		this.add(x, y);
	}

	public void add(int x, int y) {
		if (this.get(x) == null) this.put(x, new TreeMap<Integer, Integer>());
		this.get(x).put(y, 1);
	}

	public boolean contains(Coords c) {
		return contains(c.getX(), c.getY());
	}

	public boolean contains(int x, int y) {
		if (this.get(x) == null) return false;
		if (this.get(x).get(y) == null) return false;
		return true;
	}

	public void remove(Coords c) {
		int x = c.getX();
		int y = c.getY();
		this.remove(x, y);
	}

	public void remove(int x, int y) {
		if (this.get(x) == null) return;
		this.get(x).remove(y);
		if (this.get(x).isEmpty()) this.remove(x);
	}

	public List<Coords> getCoords() {
		List<Coords> retval = new ArrayList<Coords>();
		for (int x : this.keySet()) {
			for (int y : this.get(x).keySet()) {
				retval.add(new Coords(x, y, 0));
			}
		}
		return retval;
	}

	public List<Coords> getGrenzen() {
		List<Coords> retval = new ArrayList<Coords>();
		for (Coords c : this.getCoords()) {
			for (Coords n : c.getNachbarn()) {
				if (!this.contains(n)) {
					retval.add(c);
					break;
				}
			}
		}
		return retval;
	}


    public Coords getMittelpunkt() {
		List<Coords> coords = this.getCoords();

        int sx = 0; int sy = 0;
        int swelt = 0;
        for (Coords c : coords) {
            sx += c.getX();
            sy += c.getY();
            swelt += c.getWelt();
        }

        return new Coords(
                (int)Math.round((double)sx / (double)coords.size()),
                (int)Math.round((double)sy / (double)coords.size()),
                (int)Math.round((double)swelt / (double)coords.size())
                );
    }





	public int compareTo(Object o) {
		if (!(o instanceof WeltPartition)) throw new UnsupportedOperationException("Kann nur WeltPartitionen untereinander vergleichen.");
		WeltPartition wp2 = (WeltPartition)o;

		if (wp2.getCoords().size() > this.getCoords().size()) return 1;
		if (wp2.getCoords().size() < this.getCoords().size()) return -1;
		return 0;
	}

}
