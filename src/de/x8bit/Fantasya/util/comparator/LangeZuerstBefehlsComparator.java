package de.x8bit.Fantasya.util.comparator;

import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import java.util.Comparator;

/**
 *
 * @param <T> Sollte immer 'Einzelbefehl' sein.
 * @author hapebe
 */
public class LangeZuerstBefehlsComparator implements Comparator<Einzelbefehl> {

	static SortierungBefehlsComparator sbc = new SortierungBefehlsComparator();

	public int compare(Einzelbefehl eb1, Einzelbefehl eb2) {

		if (eb1.getMuster().getArt() == Art.LANG) {
			if (eb2.getMuster().getArt() == Art.LANG) return sbc.compare(eb1, eb2);
			return -1;
		}

		// eb1 ist nicht lang, es sind auch nicht beide lang.
		if (eb2.getMuster().getArt() == Art.LANG) return +1;

		// keiner von beiden ist lang:
		if (eb1.getMuster().getArt() == Art.MULTILANG) {
			if (eb2.getMuster().getArt() == Art.MULTILANG) return sbc.compare(eb1, eb2);
			return -1;
		}

		// eb1 ist nicht multi-lang, es sind auch nicht beide multi-lang.
		if (eb2.getMuster().getArt() == Art.MULTILANG) return +1;

		// keiner ist lang oder multi-lang:
		return sbc.compare(eb1, eb2);
	}
}
