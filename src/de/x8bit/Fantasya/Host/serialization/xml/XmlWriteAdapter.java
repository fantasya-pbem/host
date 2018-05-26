package de.x8bit.Fantasya.Host.serialization.xml;

import de.x8bit.Fantasya.Host.serialization.Adapter;
import de.x8bit.Fantasya.Host.serialization.util.SerializedData;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

/** Adapter to write out game data to an xml file.
 *
 * This class is compatible with the XmlReadAdapter. You basically open it,
 * write per writeData() all tables that should be written, and on closing the
 * xml file is produced. Opening the adapter again makes it forget all written
 * tables so far. Reading data will principally fail.
 */

public class XmlWriteAdapter implements Adapter {

	private File file;
	private List<Table> tables;

	/** Creates a new adapter.
	 *
	 * @param filename  the name of the file to write out to.
	 * @throws IllegalArgumentException if the filename is invalid.
	 */
	public XmlWriteAdapter(String filename) {
		if (filename == null || filename.isEmpty()) {
			throw new IllegalArgumentException("Invalid filename supplied.");
		}

		file = new File(filename);
	}

	/** {@inheritDocs}
	 *
	 * @throws IllegalStateException if the output file cannot be written to.
	 */
	@Override
	public void open() {
		try {
			file.createNewFile();
		} catch (Exception e) {
			throw new IllegalStateException("Error writing file.", e);
		}

		if (!file.exists() || !file.canWrite()) {
			throw new IllegalStateException("Cannot write to the specified output file.");
		}

		tables = new ArrayList<Table>();
	}

	@Override
	public void close() {
		try {
			JAXBContext jc = JAXBContext.newInstance(
					new Class<?>[] {Database.class, Table.class, Row.class, Entry.class});
			Marshaller marshaller = jc.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(new Database(tables), file);
		} catch (Exception e) {
			throw new IllegalStateException("Error writing out xml.", e);
		}

		tables = null;
	}

	@Override
	public SerializedData readData(String table) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void writeData(String table, SerializedData data) {
		if (tables == null) {
			throw new IllegalStateException("Adapter must be opened before writing.");
		}

		// create another table to add to the list.
		List<Row> rows = new ArrayList<Row>();
		for (Map<String,String> item : data) {
			List<Entry> entries = new ArrayList<Entry>();
			for (String key : item.keySet()) {
				entries.add(new Entry(key, item.get(key)));
			}

			rows.add(new Row(entries));
		}

		tables.add(new Table(table, rows));
	}
}