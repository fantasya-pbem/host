package de.x8bit.Fantasya.util;


import de.x8bit.Fantasya.Atlantis.Atlantis;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import java.util.Collection;


/** Eine Muellkippe, wo erst einmal der ganze Code zum Erfragen freier Nummern
 * abgeladen wurde.
 *
 * Er sieht recht redundant aus, das muss noch maechtig ueberarbeitet werden.
 * Allerdings benoetigt man das Zeug nicht in der Atlantis-Basisklasse.
 */

public class FreieNummern {

	/**
	 * liefert die nächste freie Nummer für das Atlantis-Objekt ... <b>Worst-Case Variante!!</b> 
	 * @param proxy der jeweilige Objekt-Speicher
	 */
	public static int freieNummer(Collection<? extends Atlantis> proxy) {
		if (proxy.isEmpty()) {
			return 1;
		}
		
		if (proxy.iterator().next() instanceof Unit ) {
			return UnitIDPool.getInstance().getFreieNummer();
		}

		for(int wanted = 1; wanted < Codierung.fromBase36("zzzz"); wanted++) {
			boolean free = true;
			for(Atlantis o : proxy) {
				if (!free) break; // nur PROXY abbrechen
				if (o.getNummer() == wanted) free = false;
			}
			// ist frei? - dann zurück damit
			if (free) return wanted;
		}
		new BigError("keine freie Nummer gefunden!");
		return 0;
	}
	
}
