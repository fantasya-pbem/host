package de.x8bit.Fantasya.Host.EVA.Umwelt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Items.AnimalResource;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Messages.ZATMsg;
import de.x8bit.Fantasya.util.Random;

/**
 * Regelt die Wanderung von Tieren zwischen Regionen.
 * @author hapebe
 */
public final class TierWanderung {

	public final static boolean DEBUG_AUSGABEN = false;

	public TierWanderung() {
		new ZATMsg("Tiere wandern...");

		for (Region r : Region.CACHE.values()) {

			boolean isoliert = true;
			for (Region n : r.getNachbarn()) {
				if (n.istBetretbar(null)) { isoliert = false; break; }
			}
			if (isoliert) continue; // Aus isolierten Regionen wandert kein Tier aus.

			for (Item it : r.getResourcen()) {
				if (!(it instanceof AnimalResource)) continue;
				if (it.getAnzahl() == 0) continue;
				
				AnimalResource tier = (AnimalResource)it;

				int anzahl = 0;
				// TODO in Statistik-Methode wandeln, vgl. Baumsaat in nachbarregionen
				for (int i=0; i<it.getAnzahl(); i++) {
					if (tier.willWandern(r)) anzahl++;
				}
				if (anzahl == 0) {
//					if (ZATMode.CurrentMode().isDebug() && DEBUG_AUSGABEN) {
//						r.setBeschreibung(r.getBeschreibung() + " ### Kein " + tier.getClass().getSimpleName() + " will weg." );
//					}
					continue;
				}

				// alle passenden Nachbarregionen - UND die Ausgangsregion,
				// damit nicht aus der fetten Ebene die Elefanten auf die
				// einzigen beiden Nachbarn marschieren, wenn das Gletscher sind.
				int reizSumme = 0;
				Map<Region, Integer> reizvoll = new HashMap<Region, Integer>();
				int frei = (r.freieArbeitsplaetze() > 0 ? r.freieArbeitsplaetze() : 0);
				frei /= 2; // aber eigentlich wollen die Tiere ja weg!
				reizSumme += frei;
				reizvoll.put(r, frei);
				for (Region n : r.getNachbarn()) {
					if (!n.istBetretbar(null)) continue;
					
					frei = (n.freieArbeitsplaetze() > 0 ? n.freieArbeitsplaetze() : 0);
					if (frei == 0) continue;

					reizSumme += frei;
					reizvoll.put(n, frei);
				}

				// alle übervölkert / unattraktiv? (schnell den WWF holen!)
				if (reizSumme <= 0) continue;

				List<Region> kandidaten = new ArrayList<Region>();
				kandidaten.addAll(reizvoll.keySet());

				Map<Region, Integer> auswanderung = new HashMap<Region, Integer>();

				// alle wandernden Tiere entscheiden sich jetzt für eine Richtung:
				for (int i=0; i < anzahl; i++) {
					boolean found = false;
					int attempts = 0;
					do {
						attempts ++;
						Collections.shuffle(kandidaten);
						Region kandidat = kandidaten.get(0);
						if ((Random.W(reizSumme) - 1) < reizvoll.get(kandidat)) {
							// hier entlang!
							if (!auswanderung.containsKey(kandidat)) auswanderung.put(kandidat, 0);
							auswanderung.put(kandidat, auswanderung.get(kandidat) + 1);
							found = true;
						}
						if (attempts > 100000) throw new RuntimeException("Kein Auswanderungsziel aus " + r + "?");
					} while(!found);
				}

				// so, wer will jetzt doch lieber bleiben?
				auswanderung.remove(r);
//				if (ZATMode.CurrentMode().isDebug() && DEBUG_AUSGABEN) {
//					if (auswanderung.isEmpty()) {
//						r.setBeschreibung(r.getBeschreibung() + " ### Alle " + tier.getClass().getSimpleName() + " wollen doch bleiben." );
//					}
//				}
				if (auswanderung.isEmpty()) continue;

				// jetzt müssen wir es nur noch registrieren:
				for (Region ziel : auswanderung.keySet()) {
					int wirklich = auswanderung.get(ziel);
					Class<? extends Item> c = it.getClass();

					it.setAnzahl(it.getAnzahl() - wirklich );
					ziel.setResource(c, ziel.getResource(c).getAnzahl() + wirklich);
				}

				// neugieriger Programmiere will wissen, was los ist?
//				if (ZATMode.CurrentMode().isDebug() && DEBUG_AUSGABEN) {
//					StringBuilder msg = new StringBuilder();
//
//					msg.append(anzahl).append(" ").append(tier.getClass().getSimpleName()).append(" wollen weg; ");
//
//					List<String> l = new ArrayList<String>();
//					for (Region a : reizvoll.keySet()) {
//						l.add(a + "=" + reizvoll.get(a));
//					}
//					msg.append("Attr: ").append(StringUtils.aufzaehlung(l));
//
//					l = new ArrayList<String>();
//					for (Region a : auswanderung.keySet()) {
//						l.add(a + "=" + auswanderung.get(a));
//					}
//					msg.append("; Auswanderung nach: ").append(StringUtils.aufzaehlung(l));
//
//					r.setBeschreibung(r.getBeschreibung() + " ### " + msg);
//				}

			} // nächste Resource / Tier
		} // nächste Region

	}

}
