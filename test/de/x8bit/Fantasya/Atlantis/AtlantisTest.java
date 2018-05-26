package de.x8bit.Fantasya.Atlantis;


import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;


public class AtlantisTest {
	private Atlantis object = new Atlantis();

	private final String stringKey = "someStringKey";
	private final String stringValue = "someStringValue";
	private final String integerKey = "Don't Panic";
	private final int integerValue = 42;

	@Before
	public void setup() {
		object.setProperty(stringKey, stringValue);
		object.setProperty(integerKey, integerValue);
	}

	@Test
	public void testSettingAndGettingOfStringProperty() {
		assertEquals("String property was not stored properly.",
				stringValue, object.getStringProperty(stringKey));
	}

	@Test
	public void testGettingOfStringPropertyWithDefaultVal() {
		assertEquals(stringValue, object.getStringProperty(stringKey, "blah"));
		assertEquals("blah", object.getStringProperty("nonExistent", "blah"));
	}

	@Test
	public void testSettingAndGettingOfIntegerProperty() {
		assertEquals("Integer property not stored properly.",
				integerValue, object.getIntegerProperty(integerKey));
	}

	@Test
	public void testGettingOfIntegerPropertyWithDefaultVal() {
		assertEquals(integerValue, object.getIntegerProperty(integerKey, integerValue-1));
		assertEquals(integerValue-1, object.getIntegerProperty("nonExistant", integerValue-1));
	}
	@Test(expected = IllegalArgumentException.class)
	public void testExceptionOnConvertingStringToIntegerProperty() {
		object.getIntegerProperty(stringKey);
	}

	@Test(expected = IllegalArgumentException.class)
	public void errorOnInvalidStringProperty() {
		object.getStringProperty("some invalid key");
	}

	@Test(expected = IllegalArgumentException.class)
	public void errorOnInvalidIntegerProperty() {
		object.getIntegerProperty("another invalid key");
	}

	@Test(expected = IllegalArgumentException.class)
	public void errorOnSettingStringPropertyWithNullValue() {
		object.setProperty("someKey", null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void errorOnSettingNullKey() {
		object.setProperty(null, "blah");
	}

	@Test(expected = IllegalArgumentException.class)
	public void errorOnSettingNullIntegerKey() {
		object.setProperty(null, 1);
	}

	@Test
	public void testThatListOfPropertiesCanBeObtained() {
		Set<String> properties = object.getProperties();

		assertEquals("Set of properties has incorrect size.", 2, properties.size());
		assertTrue("Property set incorrect.", properties.contains(stringKey));
		assertTrue("Property set incorrect.", properties.contains(integerKey));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void listOfPropertiesCannotBeModified() {
		object.getProperties().clear();
	}

	@Test
	public void checkForPropertiesWorks() {
		assertTrue("hasProperties() works incorrectly.", object.hasProperty(stringKey));
		assertTrue("hasProperties() works incorrectly.", object.hasProperty(integerKey));
		assertFalse("hasProperties() works incorrectly.", object.hasProperty("not existing"));
	}

	@Test
	public void propertiesCanBeRemovedProperly() {
		object.removeProperty(integerKey);
		assertFalse("Property was not removed.", object.hasProperty(integerKey));

		object.removeProperty(stringKey);
		assertFalse("Property was not removed.", object.hasProperty(stringKey));
	}
}
