package de.x8bit.Fantasya.Atlantis.Helper;

import de.x8bit.Fantasya.Atlantis.Atlantis;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Messages.SysErr;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/** Stores a set of Atlantis objects, and returns them grouped by location
 *  and/or player.
 *
 * Essentially a collection of Atlantis objects. It does some additional
 * checks when adding new elements, and allows more tailored access to them.
 * Objects stored in a cache must have a valid coordinate, id, and owner id.
 *
 * Note that the cache hands out internal sets when requested. For this reason,
 * all getters return immutable sets or iterators.
 */

public class MapCache<T extends Atlantis> implements Cache<T> {

	/** A list of all objects in the cache. */
	protected Set<T> allObjects = new HashSet<T>();
	/** A mapping from ids to objects. */
	private Map<Integer, T> idMap = new HashMap<Integer, T>();
	/** A mapping from player id to all elements. */
	private Map<Integer, Set<T>> playerMap = new HashMap<Integer, Set<T>>();
	/** A mapping from region id to all elements. */
	private Map<Coords, Set<T>> regionMap = new HashMap<Coords, Set<T>>();

	/** Adds another element to the cache.
	 *
	 * @param element the new element to add
	 * @return true if the cache was changed as a result of the adding.
	 * @throws IllegalArgumentException if another object with this id already
	 * exists in the cache.
	 * @throws NullPointerException if the argument is null.
	 */
	@Override
	public boolean add(T element) {
		if (element == null) {
			throw new NullPointerException("Cache does not allow null elements.");
		}
		if (idMap.containsKey(element.getNummer())) {
			throw new IllegalArgumentException("Object with this id already exists.");
		}

		if (!playerMap.containsKey(element.getOwner())) {
			playerMap.put(element.getOwner(), new HashSet<T>());
		}
		if (!regionMap.containsKey(element.getCoords())) {
			regionMap.put(element.getCoords(), new HashSet<T>());
		}
		
		idMap.put(element.getNummer(), element);
		playerMap.get(element.getOwner()).add(element);
		regionMap.get(element.getCoords()).add(element);
		return allObjects.add(element);
	}

	/** Removes an element from the cache.
	 *
	 * @param o the element to remove
	 * @return whether the element was successfully removed.
	 * @throws NullPointerException if the argument was null.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object o) {
		if (o == null) {
			throw new NullPointerException("Cache does not allow null elements.");
		}

		T element = (T) o;

		if (!allObjects.contains(element)) {
			return false;
		}

		idMap.remove(element.getNummer());
		if (playerMap.get(element.getOwner()) != null) {
			playerMap.get(element.getOwner()).remove(element);
		} else {
			new SysErr("NULL-Entry in de.x8bit.Fantasya.Atlantis.Helper.MapCache.remove(MapCache.java:86):" + element.getClass().getName() + " (" + element.toString() + ")");
		}
		if (regionMap.get(element.getCoords()) != null) {
			regionMap.get(element.getCoords()).remove(element);
		} else {
			new SysErr("NULL-Entry in de.x8bit.Fantasya.Atlantis.Helper.MapCache.remove(MapCache.java:91):" + element.getClass().getName() + " (" + element.toString() + ")");
		}
		return allObjects.remove(element);
	}

	/** Returns the cached element with the given id or null if no element
	 *  was found.
	 */
	public T get(int id) {
		return idMap.get(id);
	}

	/** Get a set of all elements belonging to a certain player. */
	@Override
	public Set<T> getAll(int owner) {
		if (!playerMap.containsKey(owner)) {
		return Collections.unmodifiableSet(new HashSet<T>());
		}
		return Collections.unmodifiableSet(playerMap.get(owner));
	}

	/** Get a set of all elements at a certain coordinate. */
	@Override
	public Set<T> getAll(Coords coords) {
		if (!regionMap.containsKey(coords)) {
			return Collections.unmodifiableSet(new HashSet<T>());
		}
		return Collections.unmodifiableSet(regionMap.get(coords));
	}

	/** Get the set of all elements at a certain coordinate belonging to a certain player.*/
	@Override
	public Set<T> getAll(Coords coords, int owner) {
		// fish out the elements owned by the specified player by hand.
		Set<T> retval = new HashSet<T>();

		for (T item : getAll(coords)) {
			if (item.getCoords().equals(coords) && item.getOwner() == owner) {
				retval.add(item);
			}
		}

		return Collections.unmodifiableSet(retval);
	}

	@Override
	public int size() {
		return allObjects.size();
	}

	@Override
	public boolean isEmpty() {
		return allObjects.isEmpty();
	}

	@Override
	public void clear() {
		allObjects.clear();
		idMap.clear();
		playerMap.clear();
		regionMap.clear();
	}

	@Override
	public boolean contains(Object o) {
		if (o == null) {
			throw new NullPointerException("No null elements allowed.");
		}
		return allObjects.contains(o);
	}

	@Override
	public Iterator<T> iterator() {
		return Collections.unmodifiableCollection(allObjects).iterator();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object o : c) {
			if (!contains(o)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		for (T element : c) {
			add(element);
		}

		return true;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean changed = false;

		for (Object element : c) {
			changed = remove(element) || changed;
		}

		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public Object[] toArray() {
		return allObjects.toArray();
	}

	@SuppressWarnings("hiding")
	@Override
	public <T> T[] toArray(T[] a) {
		return allObjects.toArray(a);
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 59 * hash + (this.allObjects != null ? this.allObjects.hashCode() : 0);
		return hash;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (!(o instanceof Collection)) {
			return false;
		}
		Collection c = (Collection) o;

		if (c.size() != size()) {
			return false;
		}

		for (T item : allObjects) {
			if (!c.contains(item)) {
				return false;
			}
		}

		return true;
	}
}
