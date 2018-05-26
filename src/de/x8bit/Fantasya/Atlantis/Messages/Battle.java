package de.x8bit.Fantasya.Atlantis.Messages;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Unit;

public class Battle extends Message {
	/**
	 * Konstruktor für die Instantiierung via Reflection beim Laden aus der DB
	 */
	public Battle() {}

	public Battle(String msg, Unit u)
	{
		super();
        print(0, msg, u.getCoords(), u);
	}
	
	public Battle(String msg, Partei p) {
		super();
		print(0, msg, p);
	}

}
