package de.x8bit.Fantasya.Host.serialization.db;

import de.x8bit.Fantasya.Host.serialization.Adapter;
import de.x8bit.Fantasya.Host.serialization.util.SerializedData;
import de.x8bit.Fantasya.util.StringUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/** Adapter for reading data from / writing to a database.
 *
 * The adapter requires a properly set up database object to communicate with the
 * actual data source. Tables are accepted in more or less all formats as long as
 * the individual cell entries can be cast into strings.
 */

public class DatabaseAdapter implements Adapter {

	private Database db;

	/** Constructs a new adapter.
	 *
	 * @param db   the Database object to communicate with the database.
	 */
	public DatabaseAdapter(Database db) {
		if (db == null) {
			throw new IllegalArgumentException("Must supply a valid database.");
		}

		this.db = db;
	}

	/** Initialises the connection to the database.
	 *
	 * @throws RuntimeException if any error occurs.
	 */
	@Override
	public void open() {
		try {
			db.open();
		} catch (SQLException ex) {
			throw new RuntimeException("Error opening the database.", ex);
		}
	}

	/** Closes the connection to the database.
	 *
	 * @throws IllegalStateException if the adapter was never opened.
	 * @throws RuntimeException if any error occurs.
	 */
	@Override
	public void close() {
		if (!db.isOpen()) {
			throw new IllegalStateException("Adapter was not opened.");
		}

		try {
			db.close();
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		}
	}

	/** Reads in all data from the table with the given name and returns it
	 *  in an easier to parse form.
	 *
	 * @param table   the name of the table to be read in.
	 * @return the whole table data as a SerializedData object or an empty
	 * object if the table does not exist.
	 * @throws RuntimeException if an error occurs during database access.
	 */
	@Override
	public SerializedData readData(String table) {
		try {
			if (!db.hasTable(table)) {
				return new SerializedData();
			}

			ResultSet result = db.getTableData(table);
			ResultSetMetaData rsInfo = result.getMetaData();

			// convert the data into a SerializedData object.
			SerializedData converted = new SerializedData();
			while (result.next()) {
				Map<String, String> item = new HashMap<String, String>();

				for (int i = 1; i <= rsInfo.getColumnCount(); i++) {
					item.put(rsInfo.getColumnName(i), result.getString(i));
				}
				converted.add(item);
			}

			return converted;
		} catch (Exception ex) {
			throw new RuntimeException("Error accessing database.", ex);
		}
	}

	/** Writes the data to the given table.
	 *
	 * If the table exists, it is truncated before writing out data.
	 *
	 * @param table   the name of the table to write the data to.
	 * @param data    the data to write into the table.
	 */
	@Override
	public void writeData(String table, SerializedData data) {
		try {
			// create a new table if required.
			if (!db.hasTable(table)) {
				String creationCommand = "CREATE TABLE " + table + " (";
				for (String key : data.keySet()) {
					creationCommand += key + " text,";
				}
				creationCommand = creationCommand.substring(0, creationCommand.length()-1) + ")";
				creationCommand += " ENGINE=InnoDB CHARSET=utf8;";
				db.executeCommand(creationCommand);
			} else {
				db.executeCommand("TRUNCATE " + table + ";");
			}
			
			StringBuilder keys = new StringBuilder();
			StringBuilder vals = new StringBuilder();

			for (Map<String,String> item : data) {
				keys.delete(0, keys.length());
				vals.delete(0, vals.length());

				for (String key : item.keySet()) {
//					String valString = item.get(key).replace("'", "\\'");
					String valString = StringUtils.checkValue(item.get(key));
					keys.append(",").append(key);
					vals.append(",'").append(valString).append("'");
				}

				keys.replace(0, 1, "(").append(")");
				vals.replace(0, 1, "(").append(")");

				String command = "INSERT INTO " + table + " " + keys.toString()
						+ " VALUES " + vals.toString() + ";";
				db.executeCommand(command);
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}