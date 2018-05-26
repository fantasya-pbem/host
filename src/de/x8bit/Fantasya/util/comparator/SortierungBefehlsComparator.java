package de.x8bit.Fantasya.util.comparator;

import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import java.util.Comparator;

/**
 * Sortiert Einzelbefehle nach der Einheiten-Sortierung,
 * innerhalb der gleichen Einheit nach Befehls-Sortierung
 * @author hapebe
 */
public class SortierungBefehlsComparator implements Comparator<Einzelbefehl> {

	static UnitSortierungComparator usc = new UnitSortierungComparator();

	public int compare(Einzelbefehl eb1, Einzelbefehl eb2) {
		if (eb1.getUnit().getNummer() == eb2.getUnit().getNummer()) {
			if (eb1.getSortRank() < eb2.getSortRank()) return -1;
			if (eb1.getSortRank() > eb2.getSortRank()) return +1;
			return 0;
		}

		return usc.compare(eb1.getUnit(), eb2.getUnit());
	}

}
