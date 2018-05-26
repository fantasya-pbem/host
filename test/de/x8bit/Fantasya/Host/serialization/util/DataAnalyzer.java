package de.x8bit.Fantasya.Host.serialization.util;

import java.util.Map;

/** Utility class to enhance the SerializedData class with functions
 * useful for testing.
 * 
 * There is some functionality that does not really have a place in the
 * SerializedData class because it is never used in the normal code.
 * On the other hand, this functionality greatly simplifies the tests.
 * This test-only wrapper adds functionality to determine the size of
 * the SerializedData object and whether it contains certain entries or not.
 */

public class DataAnalyzer {

	private SerializedData data;

	/** Creates a new analyzer.
	 * 
	 * @param data the SerializedData object that is analyzed.
	 * @throws IllegalArgumentException if the data object is null.
	 */
	public DataAnalyzer(SerializedData data) {
		if (data == null) {
			throw new IllegalArgumentException("No data to analyze.");
		}

		this.data = data;
	}

	/** Returns the size of the encapsulated data object. */
	public int size() {
		int retval = 0;
		for (Map<String,String> item : data) {
			retval++;
		}

		return retval;
	}

	/** Returns whether the data object contains the argument as entry. */
	public boolean contains(Map<String,String> item) {
		for (Map<String,String> entry : data) {
			if (entry.equals(item)) {
				return true;
			}
		}

		return false;
	}
}