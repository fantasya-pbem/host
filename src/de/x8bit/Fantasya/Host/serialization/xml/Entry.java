package de.x8bit.Fantasya.Host.serialization.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/** This is just a data element for key/value pairs. */

@XmlAccessorType(XmlAccessType.NONE)
class Entry {
	
	@XmlElement(name="key")
	private String key;
	
	@XmlElement(name="value")
	private String value;
	
	
	/** Default constructor for Jaxb. */
	public Entry() {}
	
	/** Initialises a new entry. */
	public Entry(String key, String value) {
		this.key = key;
		this.value = value;
	}
	
	/** Returns the name of the key. */
	public String getKey() {
		return key;
	}
	
	/** Returns the value. */
	public String getValue() {
		return value;
	}
}