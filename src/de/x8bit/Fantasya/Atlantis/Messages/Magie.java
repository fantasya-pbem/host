package de.x8bit.Fantasya.Atlantis.Messages;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Unit;

public class Magie extends Message
{
	/**
	 * Konstruktor für die Instantiierung via Reflection beim Laden aus der DB
	 */
	public Magie() {}

	public Magie(String msg, Unit u)
	{
		super();
		print(0, msg, u.getCoords(), u);
	}
	
}
