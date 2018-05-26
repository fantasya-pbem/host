package de.x8bit.Fantasya.Atlantis.Messages;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Message;

public class DeepDebug extends Message
{
	/**
	 * Konstruktor für die Instantiierung via Reflection beim Laden aus der DB
	 */
	public DeepDebug() {}

	public DeepDebug(String msg, Coords c)
	{
		super();
		print(0, msg, null, c);
	}
}
