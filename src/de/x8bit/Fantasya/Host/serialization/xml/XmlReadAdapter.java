package de.x8bit.Fantasya.Host.serialization.xml;

import de.x8bit.Fantasya.Host.serialization.Adapter;
import de.x8bit.Fantasya.Host.serialization.util.SerializedData;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/** Adapter to read data from xml files.
 *
 * This adapter can be used to read the table information from an xml file. It
 * cannot be used to write data out. Use the XmlWriteAdapter for this. The file
 * is only accessed when you open the adapter(). At that point the whole file
 * is parsed, and its content is kept in memory until the next call to open()
 * or until the adapter is closed() again.
 *
 * Internally, the adapter defers most of the work to the JAXB library that
 * will automagically load a couple of specialized classes from xml.
 */

public class XmlReadAdapter implements Adapter {

	private String filename;
	private Database database;

	/** Constructs a new adapter.
	 *
	 * @param filename  the name of the file where the data is loaded from.
	 * @throws IllegalArgumentException if the filename is invalid.
	 */
	public XmlReadAdapter(String filename) {
		if (filename == null || filename.isEmpty()) {
			throw new IllegalArgumentException("Invalid filename given.");
		}

		this.filename = filename;
	}

	@Override
	public void open() {
		File xmlFile = new File(filename);
		if (!xmlFile.exists()) {
			throw new IllegalStateException("Xml file does not exist!");
		}

		try{
			JAXBContext jc = JAXBContext.newInstance(
					new Class<?>[] {Database.class, Table.class, Row.class, Entry.class});
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			database = (Database) unmarshaller.unmarshal(xmlFile);
		} catch (JAXBException e) {
			throw new IllegalStateException("Error loading xml database.", e);
		}
	}

	@Override
	public void close() {
		database = null;
	}

	@Override
	public SerializedData readData(String table) {
		if (database == null) {
			throw new IllegalStateException("Adapter was not opened.");
		}
		if (!database.getTableNames().contains(table)) {
			return new SerializedData();
		}

		SerializedData data = new SerializedData();
		for (Row row : database.getTable(table).getRows()) {
			Map<String, String> item = new HashMap<String, String>();

			for (Entry entry : row.getEntries()) {
				item.put(entry.getKey(), entry.getValue());
			}

			try {
				data.add(item);
			} catch (IllegalArgumentException e) {
				throw new IllegalStateException("Table " + table + " seems to be bad.", e);
			}
		}

		return data;
	}

	/** This function is explicitely forbidden for this adapter. */
	@Override
	public void writeData(String table, SerializedData data) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
