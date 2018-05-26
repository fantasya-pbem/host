package de.x8bit.Fantasya.util;

import java.util.HashSet;

import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.Debug;

/**
 *
 * @author hapebe
 */
public class UnitIDPool extends HashSet<Integer> {
	private static final long serialVersionUID = -1739937923561939811L;
	// TODO: Die ganze Funktionalität sollte vielleicht Unit.PROXY selbst übernehmen - dafür eine eigene List-Klasse schreiben.

	private final static int VORRAT = 1000;
	private final static int MAX_ID = Codierung.fromBase36("zzzz");

	private static UnitIDPool instance;

	private UnitIDPool() {
	}

	public static UnitIDPool getInstance() {
		if (instance == null) instance = new UnitIDPool();
		return instance;
	}

	public int getFreieNummer() {
		if (this.isEmpty()) {
			int cnt = 0;
			for(int maybeID = 1; maybeID < MAX_ID; maybeID++) {
				if (Unit.Get(maybeID) != null) continue;

				this.add(maybeID);
				cnt ++;

				if (cnt >= VORRAT) break;
			}
			if (this.isEmpty()) {
				new BigError("Keine freie Einheiten-Nummer gefunden!?!");
				return -1;
			}
			new Debug(this.size() + " neue freie Einheiten-Nummern gesucht, erste ID: [" + Codierung.toBase36(this.iterator().next()) + "]" );
		}
//		String debug = "Freie Einheiten-Nummern: ";
//		for (int nummer : this) {
//			debug += Codierung.toBase36(nummer) + " ";
//		}
//		new Debug(debug);

		int retval = this.iterator().next();
		this.remove(retval);
		return retval;
	}

}
