package de.x8bit.Fantasya.Atlantis;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.util.Codierung;

/**
 * alles erbt von Object => alles in Fantasya erbt von Atlantis ^^ zusätzlich werden noch einige Statische Methoden angeboten
 * @author  mogel
 */
// TODO:
// * properties vereinfachen
//   - implementiere spezielle Loader fuer die einzelnen Atlantis-Objekte.
//     Diese laden/speichern die Properties explizit
//   - nutze die neuen Loader, migriere zu dem neuen Property-Schema, loesche obsoleten Code
// * Name und Beschreibung kann doch eigentlich woanders hin?
//   (nicht alle Atlantisobjekte benoetigen einen Namen/Beschreibung)
//   - genaugenommen gilt das auch fuer den Owner
//   => nicht alle Objekte muessen von Atlantis ableiten; speziell
//      Skills und Items benoetigen (fast) keine dieser Funktionen
// * SysMsg ist aeusserst haesslich; bei Fehlern sollte eine Exception geworfen
//   und notfalls die ganze Ausfuehrung abgebrochen werden. Ebenso BigError.
//   Entweder der ZAT laeuft durch oder macht einen Epic Fail, keine Halb-Fehler.
public class Atlantis {

	/** Handgesetzter Name des Objektes ("Sammler", "Mein Schiff" etc.) */
	private String Name = this.getClass().getSimpleName();
	/** Beschreibung des Objektes ("sammelt Beeren") */
	private String Beschreibung = "";
	/** Koordinaten für dieses Objekt. */
	private Coords coords = new Coords(0,0,0);
	/** Eigentümer des Objectes (genauer: dessen ID); 0 fuer keinen Besitzer. */
	private int Owner;
	/** Nummer (Id) des Objektes */
	private int nummer;
	/** Zusaetzliche Properties fuer dieses Objekt. */
	Map<String,String> properties = new HashMap<String,String>();


	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

	public String getBeschreibung() {
		return Beschreibung;
	}

	public void setBeschreibung(String beschreibung) {
		Beschreibung = beschreibung;
	}

	/**
	 * der Eigentümer des Objekts
	 * <ul>
	 * <li>bei Einheiten das Volk</li>
	 * <li>bei Gebäuden und Schiffen der Kapitän bzw. Gebäudebesitzer</li>
	 * </ul>
	 * @return
	 */
	public int getOwner() {
		return Owner;
	}

	public void setOwner(int owner) {
		Owner = owner;
	}
    
    /**
     * @param p
     * @return TRUE, wenn dieses Atlantis-Objekt den Owner p hat (also zur Partei p gehört)
     */
    public boolean istVon(Partei p) {
        return (this.getOwner() == p.getNummer());
    }

	public Coords getCoords() {
		return coords;
	}

	public void setCoords(Coords value) {
		coords = new Coords(value);
	}

	/** Gibt die Nummer (Id) des Objektes zurueck. */
	public int getNummer() {
		return nummer;
	}

	/** Liefert die Nummer in Base36-Kodierung. */
	public String getNummerBase36() {
		return Codierung.toBase36(nummer);
	}

	/** Setzt die Nummer (id) des Objektes. */
	public void setNummer(int nummer) {
		if (nummer >= 0) {
			this.nummer = nummer;
		} else {
			new SysMsg("negative Nummer");
		}
	}

	/** Typ des Objektes */
	public String getTyp() {
		return this.getClass().getSimpleName();
	}

	/** liefert die Kennnung für das Atlantis-Objekt */
	@Override
	public String toString() {
		return getName() + " [" + getNummerBase36() + "]";
	}


	// Property handling


	/** Sets a property of the object.
	  *
	  * @param key the name of the property
	  * @param value the value of the property.
	  */
	public void setProperty(String key, String value) {
		if (value == null || key == null) {
			throw new IllegalArgumentException("Cannot set property with null key or value.");
		}

		properties.put(key, value);
	}

	/** Sets a property with an integer value. */
	public void setProperty(String key, int value) {
		setProperty(key, ((Integer)value).toString());
	}

	/** Returns an unmodifiable set of all properties. */
	public Set<String> getProperties() {
		return Collections.unmodifiableSet(properties.keySet());
	}

	/** Returns whether property exists or not. */
	public boolean hasProperty(String key) {
		return properties.containsKey(key);
	}

	/** Removes a property. */
	public void removeProperty(String key) {
		properties.remove(key);
	}
	
	/** Clears properties. */
	public void clearProperties() {
		properties.clear();
	}

	/** Returns a property as string.
	  *
	  * @param key name of the property.
	  * @throws IllegalArgumentException if the property does not exist.
	  */
	public String getStringProperty(String key) {
		if (!properties.containsKey(key)) {
			throw new IllegalArgumentException("Property " + key + " not found.");
		}

		return properties.get(key);
	}

	/** Return the property value if it exists or a default value.
	 *
	 * @param key     the name of the property
	 * @param def     the default value to use if the property does not exist.
	 */
	public String getStringProperty(String key, String def) {
		if (hasProperty(key)) {
			return getStringProperty(key);
		}

		return def;
	}

	/** Returns a property as integer.
	  *
	  * @param key name of the property.
	  * @throws IllegalArgumentException if the property is not an integer
	  */
	public int getIntegerProperty(String key) {
		return Integer.decode(getStringProperty(key));
	}

	/** Return the property value if it exists or a default value.
	 *
	 * @param key     the name of the property
	 * @param def     the default value to use if the property does not exist.
	 * @throws IllegalArgumentException if the property exists, but is no integer.
	 */
	public int getIntegerProperty(String key, int def) {
		if (hasProperty(key)) {
			return getIntegerProperty(key);
		}

		return def;
	}
}
