package de.x8bit.Fantasya.Host.Reports.Writer;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.Host.Reports.ReportWriter;

public class CRWriter extends ReportWriter
{
	public CRWriter(Partei partei)
	{
		super(partei);
		new SysMsg(" - CR-Report");
		
		// passendes File öffnen
		OpenFile(".cr", "utf-8");
	}
		
	/**
	 * schreib eine Zeile in den Report
	 * @param line
	 */
	public void wl(String line)
	{
		try
		{
			file.write(line + "\n");
		} catch(Exception ex) { new BigError(ex); }
	}
	
	/**
	 * schreibt eine Zeile in den Report
	 * <br><br>
	 * wl("Fantasya", "Spiel")
	 * <br><br><i>liefert im CR</i><br><br>
	 * "Fantasya";Spiel
	 * <br>
	 * <br>
	 * @param content - String der geschrieben werden soll
	 * @param comment - Kommentar/Tag dazu
	 */
	public void wl(String content, String comment)
	{
		wl("\"" + content + "\";" + comment);
	}
	
	/**
	 * schreibt eine Zeile in den Report
	 * <br><br>
	 * wl(3, "Runde")
	 * <br><br><i>liefert im CR</i><br><br>
	 * 3;Runde
	 * <br>
	 * <br>
	 * @param content - Zahl die geschrieben werden soll
	 * @param comment - Kommentar/Tag dazu
	 */
	public void wl(int content, String comment)
	{
		wl(content + ";" + comment);
	}	
	
	/**
	 * schreibt eine Zeile in den Report
	 * <br><br>
	 * wl(3, "Runde")
	 * <br><br><i>liefert im CR</i><br><br>
	 * 3;Runde
	 * <br>
	 * <br>
	 * @param content - Zahl die geschrieben werden soll
	 * @param comment - Kommentar/Tag dazu
	 */
	public void wl(long content, String comment)
	{
		wl(content + ";" + comment);
	}	
}
