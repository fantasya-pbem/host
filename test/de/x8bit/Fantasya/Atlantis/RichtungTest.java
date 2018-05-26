package de.x8bit.Fantasya.Atlantis;


import static org.junit.Assert.assertEquals;
import org.junit.Test;


public class RichtungTest {
	@Test
	public void checkThatInversionLooksReasonable() {
		assertEquals("Incorrect inversion of direction.",
				Richtung.Nordwesten, Richtung.Suedosten.invert());
		assertEquals("Incorrect inversion of direction.",
				Richtung.Nordosten, Richtung.Suedwesten.invert());
	}

	@Test
	public void getDirectionFromString() {
		assertEquals("Incorrect direction returned.",
				Richtung.Nordosten, Richtung.getRichtung("No"));
		assertEquals("Incorrect direction returned.",
				Richtung.Osten, Richtung.getRichtung("Osten"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void exceptionThrownOnInvalidString() {
		Richtung.getRichtung("CertainlyNoValidDirection");
	}
}
