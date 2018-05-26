package de.x8bit.Fantasya.Atlantis.Helper;

import de.x8bit.Fantasya.Atlantis.Coords;

import java.util.Collection;
import java.util.Set;

/** Interface for caches that can group objects by owning party and/or coordinate.
 *
 * You can query objects by owning player, position, and both.
 */

public interface Cache<T> extends Collection<T> {

	/** Returns all objects belonging to a certain party id. */
	public Set<T> getAll(int p);

	/** Returns all objects at a certain position. */
	public Set<T> getAll(Coords coords);

	/** Returns all objects at a certain position and of a certain party id. */
	public Set<T> getAll(Coords coords, int p);
}
