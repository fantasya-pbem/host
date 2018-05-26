package de.x8bit.Fantasya.Host.Reports.util;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Regions.Chaos;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.util.MapSelection;
import java.util.HashMap;
import java.util.Map;

/**
 * Dient der Bewertung des "Einflusses", den eine Partei auf Fantasya hat. 
 * Dabei wird die Anzahl anwesender Personen (bzw. anwesender max. Trefferpunkte)
 * in den Regionen berücksichtigt, mit einem Einfluss-Radius von 4 (?) Regionen.
 * Gewichtet wird die so erreichte Präsenz mit der Präsenz anderer Parteien 
 * (negativ) und der Anzahl ansässiger Bauern.
 * @author hapebe
 */
final public class EinflussKarte {
	protected final Map<Coords, EinflussRecord> einfluss = new HashMap<Coords, EinflussRecord>();
	
	public EinflussKarte() {
		for (Region r : Region.CACHE.values()) {
			for (Partei p : r.anwesendeParteien()) {
				int n = 0;
				// 2012-08-14 - max. Trefferpunkte statt Personenzahl: Libellen sind gar nicht so einflussreich...
                for (Unit u : r.getUnits(p.getNummer())) n += u.maxLebenspunkte(); // u.getPersonen();
				registerEinfluss(r.getCoords(), p, n);
			}
		}
	}
	
	protected final void registerEinfluss(Coords c, Partei p, int personen) {
		// die Region selbst:
		addRegionsEinfluss(c, p, personen);
		
		MapSelection ms = new MapSelection();
		ms.add(c);
		
		for (int i=0; i<4; i++) {
			// und, mit abnehmendem Gewicht, die Nachbar-"Ringe"
			personen /= 3;
			if (personen == 0) break;
			
			MapSelection kontur = ms.getAussenKontur();
			for (Coords n : kontur.asList()) {
				addRegionsEinfluss(n, p, personen);
			}
			
			ms.addAll(kontur.asList());
		}
	}
	
	/**
	 * Diese Methode addiert Einfluss auf genau eine Region - das ist NICHT
	 * die Methode, um "von außen" die Daten der Spielwelt mitzuteilen!
	 * @param c
	 * @param p
	 * @param personen 
	 */
	protected void addRegionsEinfluss(Coords c, Partei p, int personen) {
		if (!einfluss.containsKey(c)) {
			Region r = Region.Load(c);
			if ((r == null) || (r instanceof Chaos)) return;
			einfluss.put(c, new EinflussRecord(r.getBauern()));
		}	
		EinflussRecord er = einfluss.get(c);
		if (!er.anwesende.containsKey(p)) er.anwesende.put(p, 0);
		er.anwesende.put(p, er.anwesende.get(p) + personen);
	}
	
	public float getEinfluss(Coords c, Partei p) {
		if (!einfluss.containsKey(c)) return 0f;
		EinflussRecord er = einfluss.get(c);
		if (!er.anwesende.containsKey(p)) return 0f;
		int n = er.anwesende.get(p);
		if (n == 0) return 0f;
		
		float alle = (float)er.getSummeAnwesende();
		float ratio = (float)n / alle;
		// ratio *= ratio; // also bspw. 0.81 bei 90%, 0.25 bei 50%, 0.01 bei 10%
		// float masse = (float)Math.sqrt(alle);
		float masse = alle;
		
		return ratio * masse * ((float)er.getBauern() + 100);
	}
	
	public float getGlobalenEinfluss(Partei p) {
		float summe = 0f;
		for (Region r : Region.CACHE.values()) {
			summe += getEinfluss(r.getCoords(), p);
		}
		return summe;
	}
	
	private class EinflussRecord {

		public EinflussRecord(int bauern) {
			this.bauern = bauern;
			this.anwesende = new HashMap<Partei, Integer>();
		}
		
		final int bauern;
		final Map<Partei, Integer> anwesende;

		public int getBauern() {
			return bauern;
		}
		
		public int getSummeAnwesende() {
			int retval = 0;
			for (Partei p : anwesende.keySet()) {
				retval += anwesende.get(p);
			}
			return retval;
		}
	}
	
}
