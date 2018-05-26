package de.x8bit.Fantasya.Host.Reports.Writer;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.Host.Reports.ReportWriter;

public class ZRWriter extends ReportWriter
{
	public ZRWriter(Partei partei)
	{
		super(partei);
		new SysMsg(" - ZR-Report");
		OpenFile(".zr", "iso-8859-15");
//		OpenFile(".zr", "utf-8");
	}
	
	/**
	 * schreib eine Zeile in den ZR-Report
	 * @param line
	 */
	public void wl(String line)
	{
		try
		{
			file.write(line + "\r\n");
		} catch(Exception ex) { new BigError(ex); }
	}
	
	/**
	 * schreibt eine Leerzeile in der ZR-Report
	 *
	 */
	public void wl()
	{
		wl("");
	}
}
