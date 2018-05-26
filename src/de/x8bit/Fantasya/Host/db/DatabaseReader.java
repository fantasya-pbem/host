package de.x8bit.Fantasya.Host.db;

import java.sql.ResultSet;
import java.sql.SQLException;

/** Basis-Interface fuer die lesende Datenbank-Verbindung.
  *
  * Sinn und Zweck dieses Interfaces ist es, ein schlankes (!!) Interface fuer
  * die ueblichen Datenbankoperationen zu bieten, die mit lesendem Zugriff
  * einhergehen, vor allem fuer Selects.
  *
  * Das hat mehrere Vorteile: Erstens kann man transparent die zugrundeliegende
  * Klasse austauschen, zweitens muss man sich nicht durch die 1000 Zeilen von
  * Datenbank kaempfen, um eine Datenbank nutzen zu koennen. Drittens kann man
  * mit Interfaces Mock-Objekte erzeugen zum Testen.
  */

public interface DatabaseReader {

	/** Fuehrt einen Select aus, der alle Spalten und Zeilen aus der
	  * angegebenen Tabelle zurueckgibt. */
	public ResultSet selectAll(String table) throws SQLException;

	/** Gibt die mit der letzten Anfrage belegten Resourcen frei.
	  *
	  * Sollte nach jeder Anfrage ausgefuehrt werden.
	  */
	public void cleanup() throws SQLException;
}
