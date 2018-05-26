package de.x8bit.Fantasya.Host.db;

import de.x8bit.Fantasya.Host.serialization.util.SerializedData;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/** Converts a result set into a SerializedData object.
 *
 * The point of this class is to strip all the database-related magic from
 * the actual data. ResultSet is database-specific, while SerializedData only
 * holds key/value pairs. Putting the code in a separate class allows easier
 * testing.
 */

public class ResultSetConverter {

	/** Creates a SerializedData object based on the content of the result set. */
	public SerializedData convert(ResultSet rs) throws SQLException {
		if (rs == null) {
			throw new IllegalArgumentException("Need a result set to conver.");
		}

		SerializedData output = new SerializedData();
		Map<String, String> keys = new HashMap<String, String>();

		// figure out the keys of the result set
		ResultSetMetaData meta = rs.getMetaData();
		for (int i = 0; i < meta.getColumnCount(); i++) {
			keys.put(meta.getColumnLabel(i), "");
		}

		// Now, for each row, create a new map, fill it with the contents of the
		// result set, and add it to the output.
		while (rs.next()) {
			Map<String, String> item = new HashMap<String, String>(keys);
			for (String key : item.keySet()) {
				item.put(key, rs.getString(key));
			}

			output.add(item);
		}

		return output;
	}
}