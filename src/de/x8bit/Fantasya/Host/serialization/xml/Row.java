package de.x8bit.Fantasya.Host.serialization.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/** A row (in a table) is essentially a list of Entries. */

@XmlAccessorType(XmlAccessType.NONE)
class Row {
	
	@XmlElementWrapper(name = "entries")
	@XmlElement(name = "entry")
	private List<Entry> entries;
	
	/** No-args constructor required by Jaxb. */
	public Row() {}
	
	/** Constructor to initialise a row. */
	public Row(List<Entry> entries) {
		this.entries = new ArrayList<Entry>(entries);
	}
	
	/** Returns the entries. */
	public List<Entry> getEntries() {
		return entries;
	}
}