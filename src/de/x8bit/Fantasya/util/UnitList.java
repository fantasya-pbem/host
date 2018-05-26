package de.x8bit.Fantasya.util;

import de.x8bit.Fantasya.Atlantis.Unit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * In dieser speziellen Liste kann jede Einheit nur einmal enthalten sein.
 * @author hapebe
 */
public class UnitList extends ArrayList<Unit> {
	private static final long serialVersionUID = -440494921605362288L;

	@Override
	public boolean add(Unit u) {
		if (this.contains(u)) {
			throw new RuntimeException("Einheit " + u + " soll in eine Liste aufgenommen werden, ist aber schon drin!");
		}

		return super.add(u);
	}

	@Override
	public void add(int index, Unit u) {
		if (this.contains(u)) {
			throw new RuntimeException("Einheit " + u + " soll in eine Liste aufgenommen werden, ist aber schon drin!");
		}

		super.add(index, u);
	}

	@Override
	public boolean addAll(Collection<? extends Unit> c) {
		for (Unit u : c) {
			this.add(u);
		}
		return true;
	}

	@Override
	public boolean addAll(int index, Collection<? extends Unit> c) {
		int cursor = index;
		for (Unit u : c) {
			this.add(cursor, u);
			cursor ++;
		}
		return true;
	}

	/**
	 * @return Die summierte Anzahl aller Personen in Einheiten dieser Liste.
	 */
	public int getPersonen() {
		int retval = 0;
		for (Unit u : this) {
			if (u != null) retval += u.getPersonen();
		}
		return retval;
	}
	
	/**
	 * @return die Nummer der Partei mit den meisten Personen in dieser Liste, 
	 * oder -1, wenn keine Partei (also keine Einheit) vorhanden ist.
	 */
	public int getGroesstePartei() {
		Map<Integer, UnitList> listen = new HashMap<Integer, UnitList>();
		for (Unit u : this) {
			if (!listen.containsKey(u.getOwner())) listen.put(u.getOwner(), new UnitList());
			UnitList l = listen.get(u.getOwner());
			l.add(u);
		}
		
		int maxN = -1;
		int groesste = -1;
		for (int nr : listen.keySet()) {
			int n = listen.get(nr).getPersonen();
			if (n > maxN) {
				groesste = nr;
				maxN = n;
			}
		}
		
		return groesste;
	}




}
