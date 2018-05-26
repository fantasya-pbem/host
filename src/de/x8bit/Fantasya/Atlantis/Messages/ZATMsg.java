package de.x8bit.Fantasya.Atlantis.Messages;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;

public class ZATMsg extends Message
{
	/**
	 * Konstruktor für die Instantiierung via Reflection beim Laden aus der DB
	 */
	public ZATMsg() {}

	public ZATMsg(String msg)
	{
		super();
		// new SysMsg("[" + msg + "] RUBS:" + Region.PROXY.size() + "/" + Unit.PROXY.size() + "/" + Building.PROXY.size() + "/" + Ship.PROXY.size());
		
//		int count = Datenbank.Select("SELECT count(*) FROM gebaeude g where nummer=606", 0);
//		if (count == 0) new BigError("verloren vor ZAT-Schritt '" + msg + "'");
		
		// letzten ZAT-Schritt für BigError() merken
		lastMessage = msg;
		
		// durch System reichen
		print(0, msg, Partei.getPartei(0));
	}
}
