package de.x8bit.Fantasya.Atlantis;

import de.x8bit.Fantasya.util.ComplexName;

/** Interface, das von allen Objekten mit komplexen Namen implementiert wird.
 *
 * ComplexNames werden insbesondere verwendet fuer Umlaute und andere
 * Situationen mit mehrdeutigen Schreibweisen. D.h. Klassen, die dieses
 * Interface implementieren, sind bevorzugt solche, deren Namen vom Spieler
 * in nicht immer eindeutiger Form angegeben werden. Das sind insbesondere
 * alle Objekte, die gebaut werden koennen, also Items, Buildings und Ships.
 */

public interface NamedItem {
	/** Liefert den komplexen Namen fuer das entsprechende Object zurueck.
	 *
	 * In der Regel kann das in einer nicht-statischen Form implementiert
	 * werden. So wie Fantasya zur Zeit programmiert ist, wird diese Funktion
	 * nur extrem selten abgefragt.
	 */
	public ComplexName getComplexName();
}
