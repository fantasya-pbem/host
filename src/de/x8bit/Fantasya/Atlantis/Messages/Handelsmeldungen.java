package de.x8bit.Fantasya.Atlantis.Messages;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Unit;

public class Handelsmeldungen extends Message
{
	/**
	 * Konstruktor für die Instantiierung via Reflection beim Laden aus der DB
	 */
	public Handelsmeldungen() {}

	public Handelsmeldungen(String msg, Unit u)
	{
		super();
		print(0, msg, u.getCoords(), u);
	}
	
}
