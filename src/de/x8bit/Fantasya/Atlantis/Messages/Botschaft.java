package de.x8bit.Fantasya.Atlantis.Messages;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;

public class Botschaft extends Message
{
	/**
	 * Konstruktor für die Instantiierung via Reflection beim Laden aus der DB
	 */
	public Botschaft() {}

	public Botschaft(Unit von, Unit an, String msg) {
		super();
		print(0, "Botschaft von " + von + ": " + msg, an.getCoords(), an);
	}
	
	/**
	 * Botschaft an eine Partei in einer Region
	 * @param von Absender
	 * @param an Empfänger-Partei
	 * @param msg Nachricht
	 */
	public Botschaft(Unit von, Partei an, String msg) {
		super();
		String absender = null;
        if (von != null) {
            absender = Partei.getPartei(von.getOwner()).toString();
            if (von.getTarnPartei() != von.getOwner()) {
                if (von.getTarnPartei() == 0) {
                    absender = "irgendwem";
                } else {
                    Partei tp = Partei.getPartei(von.getTarnPartei());
                    if (tp == null) {
                        // ?
                        absender = "irgendwem";
                    } else {
                        absender = tp.toString();
                    }
                }
            }
        } else {
            // von == null  -  das sind Botschaften vom Spiel and den Spieler
        }
        
        if (von != null) {
			print(0, "Überall sind Aushänge von " + absender + " an " + an + " angenagelt: " + msg,
					Partei.getPartei(0), von.getCoords());
        } else {
            // von == null  -  das sind Botschaften vom Spiel and den Spieler:
            print(0, msg, an);
        }
	}

	/**
	 * Sendet eine Nachricht an jeweils eine Einheit jeder anwesenden Partei in der Region.
	 * @param von Absender der Botschaft
	 * @param msg Nachricht
	 * @param r Region, in der zum Diplomatendinner geladen wird.
	 */
	public Botschaft(Unit von, Region r, String msg) {
		super();
		String absender = Partei.getPartei(von.getOwner()).toString();
        if (von.getTarnPartei() != von.getOwner()) {
            if (von.getTarnPartei() == 0) {
                absender = "irgendwem";
            } else {
                Partei tp = Partei.getPartei(von.getTarnPartei());
                if (tp == null) {
                    // ?
                    absender = "irgendwem";
                } else {
                    absender = tp.toString();
                }
            }
        }

		// for (Partei p : r.anwesendeParteien()) {
			// txt, (keine!) partei, (keine!) unit, coords:
//			print(0, "Herolde von " + absender + " verkünden: 'An alle: " + msg + "'",
//					Partei.getPartei(0), r.getCoords());
		// }
        
        for(Partei p : r.anwesendeParteien()) {
        	if (p.getNummer() != von.getOwner()) {
        		print(0, "Herolde von " + absender + " verkünden: 'An alle, " + msg + "'", p, r.getCoords());
        	}
        }
	}
	
}
