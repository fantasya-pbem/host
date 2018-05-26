package de.x8bit.Fantasya.util.lang;

import org.junit.Assert;
import org.junit.Test;

public class NonsenseTexterTest {

	@Test
	public void generateNonsenseWord() {
		// simply check that the returned string has a certain length.
		int size = 5;

		String word = NonsenseTexter.makeNonsenseWort(size);
		Assert.assertNotNull("No word was generated.", word);
		Assert.assertTrue("Generated word is too short", word.length() >= size);
	}
}
