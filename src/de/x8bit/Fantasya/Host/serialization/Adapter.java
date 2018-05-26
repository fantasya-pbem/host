package de.x8bit.Fantasya.Host.serialization;

import de.x8bit.Fantasya.Host.serialization.util.SerializedData;

/** An Adapter converts the game data from some underlying data source (XML file,
 *  database) into SerializedData objects and back.
 *
 * It provides an important step in making the load/save code independent of the
 * output format through this conversion in the format-independent SerializedData
 * objects. To use it, you always open() it first, read or write the tables that
 * you are interested in, and finally close() it again.
 */

public interface Adapter {

	/** Opens the connection to the underlying data source.
	 *
	 * This function must be called before the adapter can be used. In the case
	 * of a database, it opens the connection to the data base, for xml files, it
	 * opens and reads in the appropriate files. The adapter should accept
	 * multiple calls to this function. Either it then reopens things or just
	 * discards open requests while open.
	 */
	public void open();

	/** Closes the connection to the underlying data source.
	 *
	 * This frees all resources associated with reading in data, and also, e.g.,
	 * closes the connection to a database server. This function must be called
	 * when you are done with an adapter, and the adapter needs to be opened again
	 * before it can be used further.
	 *
	 * @throws IllegalStateException if the adapter was not opened before.
	 */
	public void close();

	/** Reads data from the underlying source.
	 *
	 * Note that if you request a table that cannot be found in the underlying
	 * source, the adapter will silently return an empty SerializedData object.
	 *
	 * @param table The name of the "table" to read from (however it is
	 * implemented in the underlying source)
	 * @return the data converted into a format-independent SerializedData instance.
	 * @throws IllegalStateException if the adapter has not been opened or a read error occurs.
	 */
	public SerializedData readData(String table);

	/** Writes the data to the table in the underlying source.
	 *
	 * @param table The name of the "table" to write to (however it is implemented
	 * in the underlying source)
	 * @throws IllegalStateException if the adapter has not been opened or a write error occurs.
	 */
	public void writeData(String table, SerializedData data);
}