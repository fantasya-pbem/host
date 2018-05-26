package de.x8bit.Fantasya.Atlantis.Messages;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;

public class SysErr extends Message
{
	/**
	 * Konstruktor für die Instantiierung via Reflection beim Laden aus der DB
	 */
	public SysErr() {}

	/**
	 * erzeugt eine neue Fehlermmeldung
	 * @param msg - Meldung
	 */
	public SysErr(String msg)
	{
		this(0, msg);
	}
	
	/**
	 * erzeugt eine neue Fehlermeldung
	 * @param level - DebugLevel
	 * @param msg - Meldung
	 */
	public SysErr(int level, String msg)
	{
		super();
		print(level, msg, Partei.getPartei(0));	// Owner == 0 signalisiert das System ^^
	}
}
