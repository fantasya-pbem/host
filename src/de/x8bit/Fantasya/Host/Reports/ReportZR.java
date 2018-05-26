package de.x8bit.Fantasya.Host.Reports;

import java.util.SortedSet;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.BuildInformation;
import de.x8bit.Fantasya.Host.Main;
import de.x8bit.Fantasya.Host.Reports.Writer.ZRWriter;

/**
 * Zugvorlage
 * @author  mogel
 */
public class ReportZR
{
	/**
	 * @uml.property  name="writer"
	 * @uml.associationEnd  
	 */
	private ZRWriter writer;

	public ReportZR(Partei partei)
	{
		
		writer = new ZRWriter(partei);
		
		// Header schreiben
//		writer.wl("FANTASYA " + Atlantis.toBase36(partei.getNummer()) + " \"" + partei.getPassword() + "\"");
		// gelöscht mit EVA ... getPassword liefert nur noch den Hash aus der DB - das bringt aber nix für die Befehlsvorlage
		writer.wl("FANTASYA " + partei.getNummerBase36() + " \"" + "" + "\"");
		writer.wl();
		writer.wl("; bitte setze Dein Passwort aus der Anmeldung für die Webseite");
		writer.wl("; ein, damit Deine Befehle richtig zugeordnet werden können");
		writer.wl();
		writer.wl();
		writer.wl();
		
		// EVA-Version:

		// writer.Regionen() sollte schon in der gewünschten Sortierung für den Spieler vorliegen -
		// TODO SORTIERE REGIONEN xxx
		for (Region r : writer.Regionen()) {
			SortedSet<Unit> meine = r.getUnits(partei.getNummer());
			if (meine.isEmpty()) continue;

			ReportRegion(r, partei);
			for (Unit u : meine) u.SaveZR(writer);
		}
		
		// Footer schreiben
		writer.wl();
		writer.wl();
		writer.wl("NÄCHSTER");
		
		// Advertisment
		writer.wl();
		writer.wl();
		writer.wl();
		writer.wl("; Befehle gehen an " + Main.GameID + "@fantasya-pbem.de");
		writer.wl("; mit dem Betreff \"Fantasya Befehle\"");
		writer.wl("; Build-ID: " + BuildInformation.getBuild());
		
		// und beenden
		writer.CloseFile();
	}
	
	private void ReportRegion(Region region, Partei partei)	{
		Coords prvC = partei.getPrivateCoords(region.getCoords());

		writer.wl();
		writer.wl();
		writer.wl("; ---------------------------------------------------------------");
		writer.wl("; Region " + region + " (" + prvC.getX() + ", " + prvC.getY() + ")");
		writer.wl();
		writer.wl();
	}
}
