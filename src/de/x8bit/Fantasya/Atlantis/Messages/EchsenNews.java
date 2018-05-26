package de.x8bit.Fantasya.Atlantis.Messages;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Unit;

public class EchsenNews extends Message
{
	/**
	 * Konstruktor für die Instantiierung via Reflection beim Laden aus der DB
	 */
	public EchsenNews() {}

	public EchsenNews(String msg, Unit u)
	{
		super();
		print(0, msg, u.getCoords(), u);
	}
	
	public EchsenNews(String msg, Partei partei)
	{
		super();
		print(0, msg, partei);
	}
	
}
