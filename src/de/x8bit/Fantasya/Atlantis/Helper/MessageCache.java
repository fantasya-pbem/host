package de.x8bit.Fantasya.Atlantis.Helper;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.util.comparator.MessageComparator;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/** A collection of messages that can also return them by player and/or coordinate.
 * 
 * <p>
 * The cache on one hand provides a collection interface that simply contains all
 * messages in the game. On the other hand, it also has functionality to return
 * all messages that are associated with a certain player, a certain region on
 * the map, or both. This additional functionality is convenient to prepare, e.g.,
 * the various player reports. The information on the location and owning player
 * is taken from the messages themselves. As a consequence, they must not be
 * modified once they are added to the cache.
 * </p>
 * 
 * <p>
 * Messages are not required to belong to a player or a region. Such messages
 * can still be added to the cache, but they can only be retrieved by iterating
 * through the whole cache.
 * </p>
 */

public class MessageCache implements Cache<Message> {
	
	private Set<Message> allObjects = new TreeSet<Message>(new MessageComparator());
	private Map<Integer, Set<Message>> playerMap = new HashMap<Integer, Set<Message>>();
	private Map<Coords, Set<Message>> regionMap = new HashMap<Coords, Set<Message>>();

	/** Adds a message to the cache.
	 * 
	 * @param msg The message to add
	 * @return true if the message was successfully added.
	 * @throws NullPointerException if the parameter is null.
	 */
	@Override
	public boolean add(Message msg) {
		if (msg == null) {
			throw new NullPointerException("May not add a null element to the message cache.");
		}
		
		// add to the player list for faster lookup
		if (msg.getPartei() != null) {
			if (!playerMap.containsKey(msg.getPartei().getNummer())) {
				playerMap.put(msg.getPartei().getNummer(), new TreeSet<Message>(new MessageComparator()));
			}
			
			playerMap.get(msg.getPartei().getNummer()).add(msg);
		}
		
		// add to region map for faster lookup
		if (msg.getCoords() != null) {
			if (!regionMap.containsKey(msg.getCoords())) {
				regionMap.put(msg.getCoords(), new TreeSet<Message>(new MessageComparator()));
			}
			
			regionMap.get(msg.getCoords()).add(msg);
		}
		
		return allObjects.add(msg);
	}

	/** Removes a message from the cache. */
	@Override
	public boolean remove(Object o) {
		Message msg = (Message)o;
		
		if (msg.getPartei() != null) {
			if (playerMap.containsKey(msg.getPartei().getNummer())) {
				playerMap.get(msg.getPartei().getNummer()).remove(o);
			}
		}

		if (msg.getCoords() != null) {
			if (regionMap.containsKey(msg.getCoords())) {
				regionMap.get(msg.getCoords()).remove(o);
			}
		}

		return allObjects.remove(o);
	}

	/** Returns all messages concerning a specific party.
	 * 
	 * @param p the id of the player whose messages are requested.
	 * @return an unmodifiable set with all relevant messages. If none are found,
	 * an empty set is returned.
	 */
	@Override
	public Set<Message> getAll(int p) {
		if (!playerMap.containsKey(p)) {
			return Collections.unmodifiableSet(new HashSet<Message>());
		}
		
		return Collections.unmodifiableSet(playerMap.get(p));
	}

	/** Returns all messages concerning a specific region
	 * 
	 * @param coords the coordinates of the region in question.
	 * @return an unmodifiable set with all relevant messages. If none are found,
	 * an empty set is returned.
	 */
	@Override
	public Set<Message> getAll(Coords coords) {
		if (!regionMap.containsKey(coords)) {
			return Collections.unmodifiableSet(new HashSet<Message>());
		}
		
		return Collections.unmodifiableSet(regionMap.get(coords));
	}

	/** Returns all messages concerning a specific party in a specific region
	 * 
	 * @param coords the region whose messages are requested.
	 * @param p the id of the player whose messages are requested.
	 * @return an unmodifiable set with all relevant messages. If none are found,
	 * an empty set is returned.
	 */
	@Override
	public Set<Message> getAll(Coords coords, int p) {
		Set<Message> retval = new TreeSet<Message>(new MessageComparator());
		
		// we first request all messages at the given coordinate, then loop
		// through them by hand.
		for (Message msg : getAll(coords)) {
			if (msg.getPartei() != null && msg.getPartei().getNummer() == p) {
				retval.add(msg);
			}
		}
		
		return Collections.unmodifiableSet(retval);
	}

	/** Returns the size of the cache. */
	@Override
	public int size() {
		return allObjects.size();
	}

	/** Returns true if no messages are stored. */
	@Override
	public boolean isEmpty() {
		return allObjects.isEmpty();
	}

	/** Removes all messages from the cache. */
	@Override
	public void clear() {
		allObjects.clear();
		playerMap.clear();
		regionMap.clear();
	}

	/** Returns an iterator that iterates over all messages. */
	@Override
	public Iterator<Message> iterator() {
		return allObjects.iterator();
	}

	@Override
	public boolean contains(Object o) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public <T> T[] toArray(T[] a) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean addAll(Collection<? extends Message> c) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}