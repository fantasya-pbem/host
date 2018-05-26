package de.x8bit.Fantasya.Host.EVA.util;

import java.text.NumberFormat;

import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;

/**
 *
 * @author hapebe
 */
public class BefehleKlassifizieren {

	public BefehleKlassifizieren() {
		int unitCnt = 0;
		int befehlCnt = 0;
		long befehlsZeichen = 0;
		for (Unit u : Unit.CACHE) {
			for (Einzelbefehl eb : u.BefehleExperimental) {
				BefehlsSpeicher.getInstance().add(eb);
				// new Debug(eb.toString());
				befehlsZeichen += eb.getBefehl().length();
				befehlCnt ++;
			}
			// Nur "weltliche" Einheiten zählen:
			if (u.getCoords().getWelt() != 0) unitCnt ++;
        }
		new SysMsg("BefehlsSpeicher: " + befehlCnt + " gültige Befehle (" + NumberFormat.getNumberInstance().format(befehlsZeichen) + " Zeichen) von " + unitCnt + " Einheiten interpretiert.");
	}

}
