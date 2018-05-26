package de.x8bit.Fantasya.Host.ZAT.Battle;

import de.x8bit.Fantasya.Atlantis.Unit;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author hapebe
 */
public class KriegerCounter {

	private Map<Integer, Integer> cnt = new HashMap<Integer, Integer>();

	public KriegerCounter() {

	}

	public void count(Krieger k) {
		Unit unit = k.getUnit();
		this.count(unit);
	}

	public void count(Unit unit) {
		this.count(unit.getNummer());
	}

	public void count(int unitNr) {
		if (cnt.containsKey(unitNr)) {
			int value = cnt.get(unitNr);
			value ++;
			cnt.put(unitNr, value);
		} else {
			cnt.put(unitNr, 1);
		}
	}

    public boolean isEmpty() {
        if (cnt.size() == 0) return true;
        return false;
    }

    public int getCount(Unit u) {
        if (!cnt.containsKey(u.getNummer())) return 0;
        return cnt.get(u.getNummer());
    }

	public String getReportPhrase() {
		if (cnt.size() == 0) return "niemand";
		
		StringBuffer sb = new StringBuffer();

		int einheiten = cnt.size();
		int i = 0;
		for (int unitNr : cnt.keySet()) {
			if (i > 0) {
				if (i == einheiten - 1) {
					sb.append(" und ");
				} else {
					sb.append(", ");
				}
			}
			i++;

			Unit u = Unit.Load(unitNr);
			int personen = cnt.get(unitNr);

			if (personen < u.getPersonen()) {
				sb.append(personen + " von " + u);
			} else {
				sb.append(u.toString());
			}
		}

		return sb.toString();
	}

    public Map<Integer, String> getReportTokens() {
        Map<Integer, String> retval = new HashMap<Integer, String>();
		for (int unitNr : cnt.keySet()) {
			Unit u = Unit.Load(unitNr);
			int personen = cnt.get(unitNr);

			if (personen < u.getPersonen()) {
				retval.put(unitNr, personen + " von " + u);
			} else {
				retval.put(unitNr, u.toString());
			}
        }
        return retval;
    }

}
