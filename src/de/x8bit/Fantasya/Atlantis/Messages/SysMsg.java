package de.x8bit.Fantasya.Atlantis.Messages;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;

public class SysMsg extends Message
{
	/**
	 * Konstruktor für die Instantiierung via Reflection beim Laden aus der DB
	 */
	public SysMsg() {}

	/**
	 * erzeugt eine neue Systemmeldung
	 * @param msg - Meldung
	 */
	public SysMsg(String msg)
	{
		this(0, msg);
	}
	/**
	 * erzeugt eine neue Systemmeldung
	 * @param level - DebugLevel
	 * @param msg - Meldung
	 */
	public SysMsg(int level, String msg)
	{
		super();
		print(level, msg, Partei.getPartei(0));	// Owner == 0 signalisiert das System ^^
	}
}
