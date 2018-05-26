package de.x8bit.Fantasya.Atlantis.Messages;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Unit;

public class Fehler extends Message
{
	/**
	 * Konstruktor für die Instantiierung via Reflection beim Laden aus der DB
	 */
	public Fehler() {}
	
	public Fehler(String msg) {
		print(0, msg, null);
	}

	public Fehler(String msg, Unit u) {
		this(msg, u, u.getCoords());
	}
	
	public Fehler(String msg, Unit u, Coords c)
	{
		super();
		print(0, msg, c, u);
	}
	
	public Fehler(String msg, Partei owner)
	{
		super();
		print(0, msg, owner);

	}

}
