package de.x8bit.Fantasya.Atlantis.Messages;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;

/**
 * diese Meldung bekommt jeder Spieler, der neu angefangen hat  
 * @author mogel
 */
public class Greetings extends Message
{
	/**
	 * Konstruktor für die Instantiierung via Reflection beim Laden aus der DB
	 */
	public Greetings() {}

	public Greetings(Partei owner, String message)
	{
        super();
        
		print(0, message, owner);
	}
}
