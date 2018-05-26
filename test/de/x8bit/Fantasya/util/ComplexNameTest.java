package de.x8bit.Fantasya.util;

import java.util.Set;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;


public class ComplexNameTest {

	private String singular = "Singular string";
	private String plural = "Plural string";
	private String[] aliases = {"Alias1", "Alias2", "Alias3"};
	private ComplexName name;

	@Before
	public void setup() {
		name = new ComplexName(singular, plural, aliases);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructoThrowsExceptionOnNullSingularForm() {
		new ComplexName(null, plural, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorThrowsExceptionOnNullPluralForm() {
		new ComplexName(singular, null, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorThrowsExceptionOnNullAliases() {
		String[] badAlias = {"blah", null};
		new ComplexName(singular, plural, badAlias);
	}

	@Test
	public void canBeSetUpWithoutAliases() {
		new ComplexName(singular, plural, null);
	}

	@Test
	public void complexNameMatchesAllSuppliedStrings() {
		assertTrue("Singular form must be matched.", name.matches(singular));
		assertTrue("Plural form must be matches.", name.matches(plural));
		for (String s : aliases) {
			assertTrue("Aliases must be matched.", name.matches(s));
		}
	}

	@Test
	public void complexNameDoesNotMatchOtherStrings() {
		assertFalse("Complex name must not match other strings", name.matches("DefinitelyNotInThere"));
	}

	@Test
	public void returnCorrectFormOfName() {
		assertEquals("Complex name must return singular form for single element.",
				singular, name.getName(1));
		assertEquals("Complex name must return singular form for no element.",
				singular, name.getName(0));
		assertEquals("Complex name must return plural form for multiple elements.",
				plural, name.getName(2));
	}

	@Test(expected=IllegalArgumentException.class)
	public void failOnNegativeCounts() {
		name.getName(-1);
	}

	@Test(expected=UnsupportedOperationException.class)
	public void aliasesCannotBeChanged() {
		Set<String> aliases = name.getAliases();
		aliases.clear();
	}
}
