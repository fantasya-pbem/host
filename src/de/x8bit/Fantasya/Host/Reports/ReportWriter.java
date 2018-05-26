package de.x8bit.Fantasya.Host.Reports;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.EVA.util.InselVerwaltung;
import de.x8bit.Fantasya.Host.EVA.util.InselVerwaltung.ParteiReportDaten;

/**
 * @author  mogel
 */
public class ReportWriter
{

	/**
	 * @uml.property  name="partei"
	 * @uml.associationEnd  
	 */
	protected Partei partei;
	// -- alt - 18.07.2012 -- protected Writer file;
	protected BufferedWriter file;
    
    protected ParteiReportDaten parteiReportDaten;
	
	public ReportWriter(Partei partei) {
        if (partei == null) throw new NullPointerException("ReportWriter soll für Partei null erstellt werden...");
		this.partei = partei;

        parteiReportDaten = InselVerwaltung.getInstance().getParteiReportDaten(partei);
	}

	/**
	 * öffnete ein File zum schreiben der Reports
	 * @param extension - ".cr" oder ".nr" oder ".zr" oder ".xml" oder was auch immer
	 * @param codierung
	 */
	protected void OpenFile(String extension, String codierung)
	{
		try
		{
			// -- alt - 18.07.2012 - file = new PrintWriter("reporte/" + GameRules.getRunde() + "-" + partei.getNummerBase36() + extension, codierung);
			file = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("reporte/" + GameRules.getRunde() + "-" + partei.getNummerBase36() + extension), codierung));
		} catch(Exception ex)
		{
			new BigError(ex);
		}
	}
	
	/**
	 * schließt das File wieder
	 */
	public void CloseFile()
	{
		// und die ganze Sache beenden
		try
		{
			file.write("");
			file.flush();
			file.close();
		} catch(Exception ex) { new BigError(ex); }
	}
	
	/**
	 * liefert alle Regionen wo eine Einheit der Partei steht - berücksichtigt Regeln zur Regionssichtbarkeit bspw. nach Alter
	 * @return detailliert sichtbare Regionen, die dieser Spieler <strong>kennt</strong>
	 */
	public List<Region> Regionen() {
		return parteiReportDaten.getRegionen();
	}

	
	/**
	 * liefert alle Nachbarregionen - berücksichtigt Regeln zur Regionssichtbarkeit bspw. nach Alter
	 * @return alle Nachbarregionen, die dieser Spieler <strong>kennt</strong>
	 */
	public List<Region> Nachbarn() {
		return parteiReportDaten.getNachbarn();
	}
}
