package de.x8bit.Fantasya.Host.db;

import java.sql.SQLException;
import java.util.Map;

/** Basis-Interface fuer die schreibende Datenbank-Verbindung.
  *
  * Sinn und Zweck dieses Interfaces ist es, ein schlankes Interface fuer
  * die ueblichen Datenbankoperationen zu bieten, die mit schreibendem Zugriff
  * einhergehen, d.h., Truncates und Insertions.
  *
  * Das hat im Wesentlichen dieselben Vorteile wie der DatabaseReader.
  */

public interface DatabaseWriter {

	/** Fuegt die angegebenen Werte in die Tabelle ein. 
	  *
	  * @param table  der Name der Tabelle, wo eingefuegt wird.
	  * @param values ein Satz von Name-Wert-Paaren fuer die Werte, die eingefuegt werden sollen.
	  */
	public void insert(String table, Map<String,String> values) throws SQLException;

	/** Fuehrt eventuell gecachte Befehle aus. */
	public void update();
	
	/** Loescht den Inhalt der Tabelle. */
	public void truncate(String table) throws SQLException;
}
