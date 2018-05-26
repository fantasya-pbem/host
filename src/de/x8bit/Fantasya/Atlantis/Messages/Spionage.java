package de.x8bit.Fantasya.Atlantis.Messages;

import de.x8bit.Fantasya.Atlantis.*;

public class Spionage extends Message {

	public Spionage() { }
	
	public Spionage(String msg, Unit u)
	{
		super();
		print(0, msg, u.getCoords(), u);
	}

}
