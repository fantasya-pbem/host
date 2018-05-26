package de.x8bit.Fantasya.Atlantis.Helper;

import de.x8bit.Fantasya.Atlantis.Atlantis;
import de.x8bit.Fantasya.Atlantis.Coords;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class MapCacheTest {

	private MapCache<Atlantis> cacheWithObject = new MapCache<Atlantis>();
	private MapCache<Atlantis> emptyCache = new MapCache<Atlantis>();
	private List<Atlantis> elements = new ArrayList<Atlantis>();

	private Atlantis object = new Atlantis();
	private Atlantis object2 = new Atlantis();
	private Atlantis objectWithSameId = new Atlantis();
	private Atlantis objectWithSameOwner = new Atlantis();
	private Atlantis objectWithSamePosition = new Atlantis();

	@Before
	public void setup() {
		Coords c1 = new Coords(1, 2, 1);
		Coords c2 = new Coords(5, 4, 1);

		object.setNummer(1);
		object2.setNummer(2);
		objectWithSameOwner.setNummer(3);
		objectWithSamePosition.setNummer(4);

		object.setOwner(1);
		object2.setOwner(2);
		objectWithSameOwner.setOwner(1);
		objectWithSamePosition.setOwner(3);

		object.setCoords(c1);
		object2.setCoords(c2);
		objectWithSameOwner.setCoords(c2);
		objectWithSamePosition.setCoords(c1);

		// and an invalid object for a change...
		objectWithSameId.setNummer(object.getNummer());
		objectWithSameId.setOwner(5);
		objectWithSameId.setCoords(c1);

		// for testing multiple xxxAll().
		elements.add(object);
		elements.add(object2);

		// fill the standard cache already with an object
		cacheWithObject.add(object);
	}

	@Test
	public void addReturnsTrueIfCacheIsModified() {
		// i.e., always returns true.
		assertTrue("Adding does not return true.", emptyCache.add(object));
	}

	@Test(expected = NullPointerException.class)
	public void cacheExpectsNonNullElements() {
		cacheWithObject.add(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void cacheExpectsUniqueIds() {
		cacheWithObject.add(objectWithSameId);
	}

	@Test
	public void isEmptyWorksProperly() {
		assertTrue("Initial cache must be marked empty.", emptyCache.isEmpty());

		emptyCache.add(object);
		assertFalse("Cache is no longer empty.", emptyCache.isEmpty());
	}

	@Test
	public void sizeReturnsSizeOfCache() {
		assertEquals("Initial size of cache must be 0.", 0, emptyCache.size());

		emptyCache.add(object);
		assertEquals("Size is wrong.", 1, emptyCache.size());

		emptyCache.add(object2);
		assertEquals("Size is wrong.", 2, emptyCache.size());
	}

	@Test
	public void cacheIsClearedProperly() {
		cacheWithObject.add(object2);
		cacheWithObject.clear();

		assertTrue("Cache was not cleared properly.", cacheWithObject.isEmpty());

		// if this fails, not everything was cleared.
		cacheWithObject.add(object);
	}

	@Test(expected = NullPointerException.class)
	public void nullElementsCannotBeRemoved() {
		cacheWithObject.remove(null);
	}

	@Test
	public void elementsCanBeRemoved() {
		assertTrue("Removal did not return true.", cacheWithObject.remove(object));
		assertTrue("Removal did not remove element.", cacheWithObject.isEmpty());
		assertFalse("Removal did not indicate failure.", cacheWithObject.remove(object));
	}

	@Test
	public void elementsCanBeQueried() {
		assertTrue("Contains() does not return true.", cacheWithObject.contains(object));
		assertFalse("Contains() does not return false.", cacheWithObject.contains(object2));
	}

	@Test(expected = NullPointerException.class)
	public void nullIsNeverContained() {
		cacheWithObject.contains(null);
	}

	@Test
	public void containingOfMultipleElementsCanBeTested() {
		cacheWithObject.add(object2);

		assertTrue("Elements are not recognized properly.", cacheWithObject.containsAll(elements));

		elements.add(objectWithSameId);
		assertFalse("Elements are not recognized properly.", cacheWithObject.containsAll(elements));
	}

	@Test
	public void multipleElementsAreAddedProperly() {
		assertTrue("Cache did not return true on adding.", emptyCache.addAll(elements));
		assertTrue("Elements were not added properly.", emptyCache.containsAll(elements));
	}

	@Test
	public void multipleElementsAreRemovedProperly() {
		cacheWithObject.add(object2);

		assertTrue("Removal was not marked as success.", cacheWithObject.removeAll(elements));
		assertTrue("Elements were not removed.", cacheWithObject.isEmpty());

		assertFalse("Removal was not marked as failure.", cacheWithObject.removeAll(elements));
		cacheWithObject.add(object);
		assertTrue("Removal was not marked as success.", cacheWithObject.removeAll(elements));
	}

	@Test
	public void iteratorAllowsIteratingOverCache() {
		cacheWithObject.add(object2);

		boolean foundObject = false;
		boolean foundObject2 = false;

		for (Atlantis element : cacheWithObject) {
			if (!foundObject && element == object) {
				foundObject = true;
			}
			else if (!foundObject2 && element == object2) {
				foundObject2 = true;
			}
			else {
				fail();
			}
		}

		assertTrue("Iterator was not usable.", foundObject && foundObject2);
	}

	@Test
	public void gettingObjectsByIdWorks() {
		assertEquals("Object was correctly retrieved.",
				object, cacheWithObject.get(object.getNummer()));
		assertNull("Non-null object was retrieved on wrong id.",
				cacheWithObject.get(object2.getNummer()));
	}

	@Test
	public void gettingObjectByPlayerIdWorks() {
		cacheWithObject.add(objectWithSameOwner);
		cacheWithObject.add(object2);

		Set<Atlantis> items = cacheWithObject.getAll(object.getOwner());

		assertEquals("Incorrect size of objects for given owner.", 2, items.size());
		assertTrue("Returned set was not correct.", items.contains(object));
		assertTrue("Returned set was not correct.", items.contains(objectWithSameOwner));
	}

	@Test
	public void gettingObjectByCoordinateWorks() {
		cacheWithObject.add(objectWithSamePosition);
		cacheWithObject.add(object2);

		Set<Atlantis> items = cacheWithObject.getAll(object.getCoords());

		assertEquals("Incorrect size of objects at given position.", 2, items.size());
		assertTrue("Returned set was incorrect.", items.contains(object));
		assertTrue("Returned set was incorrect.", items.contains(objectWithSamePosition));
	}

	@Test
	public void gettingObjectByCoordinateAndPlayerIdWorks() {
		cacheWithObject.add(objectWithSamePosition);
		cacheWithObject.add(object2);

		Set<Atlantis> items = cacheWithObject.getAll(object.getCoords(), object.getOwner());
		assertEquals("Incorrect size of returned set.", 1, items.size());
		assertTrue("Returned set was incorrect.", items.contains(object));
	}

	@Test
	public void removingAnObjectRemovesAllReferences() {
		// we try to use the getAll() functions to retrieve a removed object.
		// just be sure it is cleared out of the cache completely.
		cacheWithObject.remove(object);

		assertNull("Object was not properly removed.",
				cacheWithObject.get(object.getNummer()));
		assertTrue("Object was not properly removed.",
				cacheWithObject.getAll(object.getCoords()).isEmpty());
		assertTrue("Object was not properly removed.",
				cacheWithObject.getAll(object.getOwner()).isEmpty());
		assertTrue("Object was not properly removed.",
				cacheWithObject.getAll(object.getCoords(), object.getOwner()).isEmpty());
	}

	@Test
	public void clearingRemovesAllReferences() {
		// we try to use the getAll() functions to retrieve a removed object.
		// just be sure it is cleared out of the cache completely.
		cacheWithObject.clear();

		assertNull("Object was not properly removed.",
				cacheWithObject.get(object.getNummer()));
		assertTrue("Object was not properly removed.",
				cacheWithObject.getAll(object.getCoords()).isEmpty());
		assertTrue("Object was not properly removed.",
				cacheWithObject.getAll(object.getOwner()).isEmpty());
		assertTrue("Object was not properly removed.",
				cacheWithObject.getAll(object.getCoords(), object.getOwner()).isEmpty());
	}

	@Test
	public void equalityTestingWorks() {
		cacheWithObject.add(object2);

		Set<Atlantis> equalSet = new HashSet<Atlantis>();
		equalSet.add(object);
		equalSet.add(object2);

		Set<Atlantis> nonequalSet = new HashSet<Atlantis>();
		nonequalSet.add(object);

		Set<Atlantis> otherNonequalSet = new HashSet<Atlantis>();
		otherNonequalSet.add(object);
		otherNonequalSet.add(objectWithSameOwner);

		assertFalse("Equality matches null object.", cacheWithObject.equals(null));
		assertFalse("Equality matches wrong class.", cacheWithObject.equals(5));
		assertFalse("Equality matches wrong collection.", cacheWithObject.equals(nonequalSet));
		assertFalse("Equality matches wrong collection.", cacheWithObject.equals(otherNonequalSet));
		assertTrue("Equality fails to mach correct collection.", cacheWithObject.equals(equalSet));
	}

	@Test
	public void conversionToArrayIsPossible() {
		cacheWithObject.add(object2);

		Object[] uncastArray = cacheWithObject.toArray();
		Object[] castArray = cacheWithObject.toArray(new Atlantis[1]);

		assertEquals("Cast and uncast array are not equal.", uncastArray, castArray);
		assertEquals("Wrong array size.", cacheWithObject.size(), castArray.length);
		for (Object entry : castArray) {
			assertTrue("Wrong output entry.", cacheWithObject.contains(entry));
		}
	}


	// check that all returned Sets are unmodifiable


	@Test(expected = UnsupportedOperationException.class)
	public void emptyOwnedSetIsUnmodifiable() {
		emptyCache.getAll(object.getOwner()).clear();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void emptyLocationSetIsUnmodifiable() {
		emptyCache.getAll(object.getCoords()).clear();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void emptyLocationOwnedSetIsUnmodifiable() {
		emptyCache.getAll(object.getCoords(), object.getOwner()).clear();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void fullOwnedSetIsUnmodifiable() {
		cacheWithObject.getAll(object.getOwner()).clear();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void fullLocationSetIsUnmodifiable() {
		cacheWithObject.getAll(object.getCoords()).clear();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void fullLocationOwnedSetIsUnmodifiable() {
		cacheWithObject.getAll(object.getCoords(), object.getOwner()).clear();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void iteratorDoesNotAllowMutation() {
		cacheWithObject.iterator().next();
		cacheWithObject.iterator().remove();
	}
}
