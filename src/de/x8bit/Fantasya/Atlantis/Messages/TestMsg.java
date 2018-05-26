package de.x8bit.Fantasya.Atlantis.Messages;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Host.Main;

public class TestMsg extends Message
{
	/**
	 * Konstruktor für die Instantiierung via Reflection beim Laden aus der DB
	 */
	public TestMsg() {}

	/**
	 * erzeugt eine neue Testmeldung - gedacht für halb-automatisches Testen
     * via package ManualTests
	 * @param msg - Meldung
	 */
	public TestMsg(String msg)
	{
		super();
		if (Main.getBFlag("DEBUG") || Main.getBFlag("debug")) {
            print(0, msg, Partei.getPartei(0));	// Owner == 0 signalisiert das System ^^
        }
	}
}
