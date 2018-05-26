package de.x8bit.Fantasya.Atlantis.Helper;

import de.x8bit.Fantasya.Atlantis.Atlantis;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Units.Elf;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class SortedCacheTest {
	private Cache<Atlantis> cache = new SortedCache<Atlantis>();
	private List<Unit> units = new ArrayList<Unit>();
	private Unit chosen = new Elf();

	private Random rng = new Random();

	@Before
	public void setup() {
		Coords coords = new Coords(1,2,3);

		for (int i = 1; i < 10; i++) {
			Unit entry = new Elf();
			entry.setNummer(rng.nextInt(Integer.MAX_VALUE));
			entry.setOwner(5);
			entry.setCoords(coords);
			entry.setSortierung(i);

			units.add(entry);
			chosen = entry;
		}

		List<Unit> tmp = new ArrayList<Unit>(units);
		while (!tmp.isEmpty()) {
			int i = rng.nextInt(tmp.size());
			cache.add(tmp.get(i));
			tmp.remove(i);
		}


		// just make sure that the tests do not fail because of wrong setup...
		// we expect the entries in this.units to be sorted.
		for (int i = 0; i < units.size()-2; i++) {
			assertTrue("Setup list is not properly ordered.",
					units.get(i).compareTo(units.get(i+1)) < 0);
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void addRequiresAComparableItem() {
		// Atlantis should not be comparable
		cache.add(new Atlantis());
	}

	@Test
	public void addStillAddsObjectsThough() {
		cache.clear();
		cache.add(units.get(0));
		assertTrue("Object was not added to cache.", cache.contains(units.get(0)));
	}

	@Test
	public void iterationIsThroughSortedSet() {
		// check that the sorting order is preserved under the random adding.
		int index = 0;
		for (Atlantis entry : cache) {
			assertEquals("Elements were not properly ordered.",
					units.get(index), entry);
			index++;
		}

		assertEquals("Wrong number of elements in iteration.",
				units.size(), index);
	}

	@Test
	public void getAllOwnedReturnsProperlySortedSet() {
		int index = 0;
		for (Atlantis entry : cache.getAll(chosen.getOwner())) {
			assertEquals("Elements were not properly ordered.",
					units.get(index), entry);
			index++;
		}

		assertEquals("Wrong number of elements in iteration.",
				units.size(), index);
	}

	@Test
	public void getAllAtCoordsReturnsProperlySortedSet() {
		int index = 0;
		for (Atlantis entry : cache.getAll(chosen.getCoords())) {
			assertEquals("Elements were not properly ordered.",
					units.get(index), entry);
			index++;
		}

		assertEquals("Wrong number of elements in iteration.",
				units.size(), index);
	}

	@Test
	public void getAllForCoordsAndOwnerReturnsSortedSet() {
		int index = 0;
		for (Atlantis entry : cache.getAll(chosen.getCoords(), chosen.getOwner())) {
			assertEquals("Elements were not properly ordered.",
					units.get(index), entry);
			index++;
		}

		assertEquals("Wrong number of elements in iteration.",
				units.size(), index);
	}

	@Test
	public void sortingIsInstantaneousWithoutCache() {
		// give the last elements the lowest order
		// and check that the cache updates this on the fly.
		chosen.setSortierung(0);

		// we test all cases
		Collection<Collection<Atlantis>> allCases = new ArrayList<Collection<Atlantis>>();
		allCases.add(cache);
		allCases.add(cache.getAll(chosen.getOwner()));
		allCases.add(cache.getAll(chosen.getCoords()));
		allCases.add(cache.getAll(chosen.getCoords(), chosen.getOwner()));

		// first entry should be the one with the lowest sorting order now.
		for (Collection<Atlantis> collection : allCases) {
			for (Atlantis entry : collection) {
				if (entry.equals(chosen)) {
					break;
				} else {
					fail("Elements were not reordered on the fly.");
				}
			}
		}
	}

	@Test
	public void multipleIteratorsWorkFine() {
		for (Atlantis item : cache) {
			for (Atlantis item2 : cache) {
				// this used to throw an exception because I was too quick
				// with trying to cache things.
			}
		}
	}
}