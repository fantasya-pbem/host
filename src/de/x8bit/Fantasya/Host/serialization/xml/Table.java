package de.x8bit.Fantasya.Host.serialization.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/** This class represents a table in the xml file.
 * 
 * A table consists of a list of rows. Since there is no checking inherent in
 * the loading that all rows have the same keys, there is some additional
 * functionality to check this. Also, a table can be converted into a
 * SerializedData instance.
 */

@XmlAccessorType(XmlAccessType.NONE)
class Table {
	
	@XmlElement(name = "name")
	private String name;
	
	@XmlElementWrapper(name = "rows")
	@XmlElement(name = "row")
	private List<Row> rows;
	
	
	/** No-args constructor for Jaxb. */
	public Table() {}
	
	/** Constructor to assemble a table with a given set of Rows. */
	public Table(String name, List<Row> rows) {
		this.name = name;
		this.rows = new ArrayList<Row>(rows);
	}
	
	public String getName() {
		return name;
	}
	
	public List<Row> getRows() {
		return rows;
	}
}