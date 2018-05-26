package de.x8bit.Fantasya.Host.EVA;

import java.sql.ResultSet;
import java.sql.SQLException;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.DeepDebug;
import de.x8bit.Fantasya.Host.Datenbank;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;

public class SpecialDebug extends EVABase implements NotACommand
{
	
	public boolean DoAction(Unit u, String[] befehl) { return false; }
	public void DoAction(Region r, String befehl) { }
	public void PostAction() { }
	public void PreAction() { }
	
	/**
	 * initialisiert einen besonderen Debug-Schritt
	 * @param step - dieser Schritt wird durchgeführt
	 */
	public SpecialDebug(int step)
	{
		super("SpecialDebug - " + debugStep(step));

		ListResources(new Coords(1, -12, 1));	// 11g
//		ListResources(new Coords(2, -13, 1));	// c7
//		ListResources(new Coords(2, -12, 1));	// c9
//		ListResources(new Coords(3, -12, 1));	// g
//		ListResources(new Coords(4, -12, 1));	// sk
	}
	
	private void ListResources(Coords coords)
	{
		Datenbank db = new Datenbank("SpecialDebug");
		db.myQuery = "SELECT anzahl, resource FROM resourcen WHERE " + coords.Where(false);
		ResultSet rs = db.Select();
		try
		{
			Region region = Region.Load(coords);
			new DeepDebug(" - Resourcen für " + region + " (" + String.valueOf(region.getBauern()) + " Bauern)", coords);
			while(rs.next()) new DeepDebug(" --- " + rs.getString("resource") + " " + String.valueOf(rs.getInt("anzahl")), coords);
		} catch (SQLException e)
		{
			new BigError(e);
		}
		db.Close();
	}
	
	private static String debugStep(int step)
	{
		switch(step)
		{
			case 1: return "Auflistung der Regions-Resourcen - Vorher";
			case 2: return "Auflistung der Regions-Resourcen - Nachher";
		}
		return "Debug-Schritt #" + String.valueOf(step) + " nicht definiert";
	}
	
    @Override
    public void DoAction(Einzelbefehl eb) { }

}
