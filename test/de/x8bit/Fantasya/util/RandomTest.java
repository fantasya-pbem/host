package de.x8bit.Fantasya.util;

import static org.junit.Assert.*;
import org.junit.Test;

/** Very basic test only, otherwise it easily becomes too complicated. */

public class RandomTest {

	@Test
	public void checkThatRandomReturnsProperRandomValues() {
		int min = 10;
		int max = 100;

		for (int i = 0; i < 1000; i++) {
			int randomNumber = Random.rnd(min, max);
			assertTrue("Random number outside range.",
				randomNumber >= min && randomNumber < max);
		}
	}

	@Test
	public void checkThatDiceThrowingWorksAsExpected() {
		int sides = 20;

		for (int i = 0; i < 1000; i++) {
			int randomNumber = Random.W(sides);
			assertTrue("Dice throwing does not work properly.",
					randomNumber >= 1 && randomNumber <= sides);
		}
	}
}
