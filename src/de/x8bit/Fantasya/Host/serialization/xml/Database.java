package de.x8bit.Fantasya.Host.serialization.xml;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/** Represents a data base (list of tables) in the XML file.
 *
 * This is actually the class that is loaded from the XML file.
 */

@XmlRootElement(name = "database")
@XmlAccessorType(XmlAccessType.NONE)
class Database {

	@XmlElementWrapper(name = "tables")
	@XmlElement(name = "table")
	List<Table> tables;


	/** No-args constructor for Jaxb. */
	public Database() {}

	/** Creates the database with the given list of tables. */
	public Database(List<Table> tables) {
		this.tables = new ArrayList<Table>(tables);
	}

	/** Returns the table for the given name or throws an exception. */
	public Table getTable(String name) {
		for (Table table : tables) {
			if (table.getName().equals(name)) {
				return table;
			}
		}

		throw new IllegalArgumentException("Table with given name does not exist.");
	}

	public Set<String> getTableNames() {
		Set<String> names = new HashSet<String>();

		for (Table table : tables) {
			names.add(table.getName());
		}

		return names;
	}
}