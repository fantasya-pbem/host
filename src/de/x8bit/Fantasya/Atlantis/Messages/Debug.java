package de.x8bit.Fantasya.Atlantis.Messages;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Host.EVA.util.ZATMode;

public class Debug extends Message
{
	/**
	 * Konstruktor für die Instantiierung via Reflection beim Laden aus der DB
	 */
	public Debug() {}

	public Debug(String msg) {
		if (ZATMode.CurrentMode().isDebug()) {
            print(0, msg, Partei.getPartei(0));	// Owner == 0 signalisiert das System ^^
        }
	}

}
