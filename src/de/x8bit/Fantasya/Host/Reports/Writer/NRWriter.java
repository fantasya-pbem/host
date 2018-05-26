package de.x8bit.Fantasya.Host.Reports.Writer;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.SysErr;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.Host.Reports.ReportWriter;
import java.util.StringTokenizer;

public class NRWriter extends ReportWriter
{
	/** Breite des NR in Zeichen */
	public int NRWidth = 75;
	
	/** Breite der vorderen Spalte */
	public int NRFront = 1;	// original Breite von V1
	
	/** klassischer c'tor */
	public NRWriter(Partei partei) {
        this(partei, ".nr", "iso-8859-15");  // TODO utf8
	}

    /** ctor */
	public NRWriter(Partei partei, String extension, String encoding) {
		super(partei);
		new SysMsg(" - NR-Report");
		
		// passendes File öffnen
		OpenFile(extension, encoding);
	}
	
	/**
	 * schreibt eine Meldung in den NR ... ist die Meldung größer als
	 * eine Zeile breit ist, so wird die Zeile umgebrochen vor dem letzten
	 * Wort was nicht mehr in die Zeile passen würde
	 * @param msg - Meldung die ggf. umgebrochen wird
	 */
	public void wl(String msg) {
        StringTokenizer tokenizer = new StringTokenizer(msg, "\n", true);
        // w_raw(tokenizer.countTokens() + " tokens");

		while (tokenizer.hasMoreTokens()) {
            String s = tokenizer.nextToken();
            
            while(s.length() > NRWidth - NRFront) {
                int last = NRWidth - NRFront;
                while(!s.substring(last, last + 1).equals(" ")) {
                    last--;
                    if (last < 1) {
                        w_raw(s);
                        new SysErr("in der Meldung ist das erste Wort größer als $NRWidth - '" + s + "'");
                        return;
                    }
                }
                // Teil der Meldung schreiben
                w_raw(s.substring(0, last) + "\r\n");

                // das Geschriebene "löschen"
                s = s.substring(last + 1);
            }
            w_raw(s);
        } // next token

        // ganz am Ende gibt es eine neue Zeile gratis
        w_raw("\r\n");
		
	}

	/**
	 * schreibt eine Meldung radikal (ohne Formatierung) in den Report
	 * @param msg - die Meldung der Meldungen
	 */
	public void w_raw(String msg) {
		try	{
			file.write(addWS(NRFront, msg) /* + "\r\n" */);
		} catch(Exception ex) { new BigError(ex); }
	}
	
	/**
	 * Schreibt eine Meldung direkt zentriert in den NR ... dazu werden
	 * Whitspace entsprechende vorne angehängt.
     * Am linken Rand können zusätzlich Zeichen geschrieben werden.
     * @param left Zeichen am linken Rand
	 * @param center Text, der zentriert geschrieben werden soll
	 */
	public void leftAndCenter(String left, String center) {
        int padding = (NRWidth - center.length()) / 2;
        padding -= left.length();
        
		w_raw(left + addWS(padding, center));

        // am Ende gibt es eine neue Zeile gratis
        w_raw("\r\n");
	}

	/**
	 * schreibt die Meldung direkt zentriert in den NR ... dazu werden
	 * Whitspace entsprechende vorne angehängt
	 * @param msg
	 */
	public void center(String msg) {
		w_raw(addWS((NRWidth - msg.length()) / 2, msg)); // bei wl() werden die WS wieder gelöscht -_-

        // ganz am Ende gibt es eine neue Zeile gratis
        w_raw("\r\n");
	}
	
	/**
	 * fügt WS an den String an (am Anfang natürlich)
	 * @param count - wieviel
	 * @param msg - eigentliche Meldung
	 * @return Meldung mit den WS
	 */
	private String addWS(int count, String msg)	{
		StringBuilder sb = new StringBuilder();
		
		if (count > 0) for(int i = 0; i < count; i++) sb.append(" ");
		sb.append(msg); // ergibt durchaus Müll wenn der String zu lang ist
		
		return sb.toString();
	}
	
}
