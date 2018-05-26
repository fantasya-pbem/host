package de.x8bit.Fantasya.Atlantis.Helper;

import de.x8bit.Fantasya.Atlantis.Atlantis;
import de.x8bit.Fantasya.Atlantis.Coords;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/** A cache that sorts its content.
 *
 * It seems as if some code assumes that the content of the cache is sorted.
 * I think this is bad behavior (the code should do it itself), but for the
 * sake of compatibility, this special cache is introduced. It only works
 * for comparable classes (i.e., for units), and will sort all data on-the-fly
 * before returning it. That is, it does not hold any internal caches on purpose!
 */

public class SortedCache<T extends Atlantis> extends MapCache<T> {

	@Override
	public boolean add(T object) {
		if (!(object instanceof Comparable)) {
			throw new IllegalArgumentException("Sorting works only with comparables.");
		}

		return super.add(object);
	}

	@Override
	public Iterator<T> iterator() {
		return new TreeSet<T>(this.allObjects).iterator();
	}

	@Override
	public Set<T> getAll(int owner) {
		return new TreeSet<T>(super.getAll(owner));
	}

	@Override
	public Set<T> getAll(Coords coords) {
		return new TreeSet<T>(super.getAll(coords));
	}

	@Override
	public Set<T> getAll(Coords coords, int owner) {
		return new TreeSet<T>(super.getAll(coords, owner));
	}
}