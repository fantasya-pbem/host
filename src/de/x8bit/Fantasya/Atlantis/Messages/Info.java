package de.x8bit.Fantasya.Atlantis.Messages;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;

public class Info extends Message
{
	/**
	 * Konstruktor für die Instantiierung via Reflection beim Laden aus der DB
	 */
	public Info() {}

	public Info(String msg, Unit u) {
		this(msg, u, u.getCoords());
	}
	
	public Info(String msg, Unit u, Coords c)
	{
		super();
		print(0, msg, c, u);
	}
	
    /**
	 * Sendet eine Meldung an eine Partei - ohne Angabe von Einheit oder Region
	 * @param msg
	 * @param partei
	 */
	public Info(String msg, Partei partei) {
        super();
        print(0, msg, partei);
    }

	/**
	 * Sendet eine Nachricht an jeweils eine Einheit jeder anwesenden Partei in der Region.
	 * @param msg Nachricht
	 * @param r Region, in der sich irgendetwas berichtenswertes ereignet hat
	 */
	public Info(String msg, Region r) {
		super();

		print(0, msg, null, r.getCoords());
	}
	
}
