package de.x8bit.Fantasya.Host.EVA.Umwelt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Messages.ZATMsg;
import de.x8bit.Fantasya.util.Random;

/**
 * Regelt die Aussaat von Bäumen in Nachbarregionen
 * @author hapebe
 */
public final class BaumAussaat {

	public final static boolean DEBUG_AUSGABEN = false;

	public BaumAussaat() {
		new ZATMsg("Bäume streuen ihre Samen...");

		for (Region r : Region.CACHE.values()) {
			System.out.println("Samen: " + r.getName());

			boolean isoliert = true;
			for (Region n : r.getNachbarn()) {
				if (n.istBetretbar(null)) { isoliert = false; break; }
			}
			if (isoliert) continue; // Aus isolierten Regionen wird kein Wald ausgesät.

			// kann gekürzt werden auf reine Statistik - mogel - 13.01.2013
//			int anzahl = 0;
//			for (int i=0; i<r.getBaum(); i++) {
//				if (Random.W(10000) < 50) anzahl++; // 0,5% z.B. bei 600 ~ 3
//			}
			int anzahl = (int) ((float)r.getBaum() * 0.005f);
			if (anzahl == 0) continue;


			// alle passenden Nachbarregionen
			int reizSumme = 0;
			Map<Region, Integer> reizvoll = new HashMap<Region, Integer>();
			for (Region n : r.getNachbarn()) {
				if (!n.istBetretbar(null)) continue;

				int frei = (n.freieArbeitsplaetze() > 0 ? n.freieArbeitsplaetze() : 0);
				if (frei == 0) continue;

				reizSumme += frei;
				reizvoll.put(n, frei);
			}

			// alle übervölkert / Wald auf dem Rückzug?
			if (reizSumme <= 0) continue;

			List<Region> kandidaten = new ArrayList<Region>();
			kandidaten.addAll(reizvoll.keySet());

			Map<Region, Integer> aussaat = new HashMap<Region, Integer>();

			// alle fliegenden Samen entscheiden sich für eine Richtung:
			for (int i=0; i < anzahl; i++) {
				boolean found = false;
				int attempts = 0;
				do {
					attempts ++;
					Collections.shuffle(kandidaten);
					Region kandidat = kandidaten.get(0);
					if ((Random.W(reizSumme) - 1) < reizvoll.get(kandidat)) {
						// hier entlang!
						if (!aussaat.containsKey(kandidat)) aussaat.put(kandidat, 0);
						aussaat.put(kandidat, aussaat.get(kandidat) + 1);
						found = true;
					}
					if (attempts > 100000) throw new RuntimeException("Kein Aussaat-Kandidat aus " + r + " heraus?");
				} while(!found);
			}

			// jetzt müssen wir es nur noch registrieren:
			for (Region ziel : aussaat.keySet()) {
				int wirklich = aussaat.get(ziel);
				ziel.setResource(Holz.class, ziel.getBaum() + wirklich);
			}

			// neugieriger Programmiere will wissen, was los ist?
//			if (ZATMode.CurrentMode().isDebug() && DEBUG_AUSGABEN) {
//				StringBuilder msg = new StringBuilder();
//
//				msg.append(anzahl).append(" Bäume ausgesät; ");
//
//				List<String> l = new ArrayList<String>();
//				for (Region a : reizvoll.keySet()) {
//					l.add(a + "=" + reizvoll.get(a));
//				}
//				msg.append("Attr: ").append(StringUtils.aufzaehlung(l));
//
//				l = new ArrayList<String>();
//				for (Region a : aussaat.keySet()) {
//					l.add(a + "=" + aussaat.get(a));
//				}
//				msg.append("; Aussaat nach: ").append(StringUtils.aufzaehlung(l));
//
//				r.setBeschreibung(r.getBeschreibung() + " ### " + msg);
//			}

		} // nächste Region

	}

}
