package de.x8bit.Fantasya.Host.EVA.Umwelt;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Messages.Debug;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.Atlantis.Messages.ZATMsg;
import de.x8bit.Fantasya.Atlantis.Regions.Wald;
import de.x8bit.Fantasya.Host.EVA.Environment;
import de.x8bit.Fantasya.util.Random;

/**
 * Beinhaltet die Regeln und deren Umsetzung zur Ein- und Auswanderung von Bauern zwischen Regionen.
 * @author hapebe
 */
public final class BauernWanderung {

	public final static boolean DEBUG_AUSGABEN = false;
	
	/**
	 * Matrix der Bauern-Migrationsströme:
	 * <code>int anzahlMigranten = BauernWanderung.get(startRegion).get(zielRegion)
	 * </code>
	 */
	protected final static Map<Region, Map<Region, Integer>> BauernWanderung = new HashMap<Region, Map<Region, Integer>>();

	public BauernWanderung() {
		new ZATMsg("Bauern wandern...");

		BauernWandernAus();
	}


	public final void BauernWandernAus() {
		NumberFormat pct = NumberFormat.getPercentInstance();
		pct.setMinimumFractionDigits(2);
		pct.setMaximumFractionDigits(2);

		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMinimumFractionDigits(3);
		nf.setMaximumFractionDigits(3);


//		if (ZATMode.CurrentMode().isDebug() && DEBUG_AUSGABEN) {
//
//			new Debug("Bauern-Auswanderungs-Wahrscheinlichkeiten bei Attraktivitätswert ... :");
//			for (int i=-10; i<=10; i++) {
//				new Debug("att=" + i + " --> " + pct.format(BauernAuswanderWahrscheinlichkeit(i)));
//			}
//			new Debug("Attraktivität einer Zielregion bei Attraktivitätswert-Unterschied ... :");
//			for (int i=-10; i<=10; i++) {
//				new Debug("att=" + i + " --> " + nf.format(BauernAuswanderZielAttraktivitaet(i)));
//			}
//		}

		// eigene Attraktivität aller Region berechnen:
		Map<Region, Double> attraktivitaet = new HashMap<Region, Double>();

		for (Region r : Region.CACHE.values()) {
			if (!r.istBetretbar(null)) continue;

			int bauern = r.getBauern();	if (bauern < 1) bauern = 1;

			double reichtum = Math.log10(BewerteProKopfVermoegen(r));
			if (reichtum < -1) reichtum = -1;
			if (reichtum > 0) reichtum *= 2;


			// Unterschied im Lohn-Niveau:
			int maxNachbarLohn = -1;
			for (Region nachbar : r.getNachbarn()) {
				if (!nachbar.istBetretbar(null)) continue;

				if (nachbar.getLohn() > maxNachbarLohn) maxNachbarLohn = nachbar.getLohn();
			}
			double lohn = (double)(r.getLohn() - maxNachbarLohn);
			lohn /= 2;


			// Unterschied zum größten Wohlstand unter den Nachbarn - Neid:
			double maxNeid = 0;
			for (Region nachbar : r.getNachbarn()) {
				if (!nachbar.istBetretbar(null)) continue;

				double neid = GetBauernNeid(r, nachbar);
				if (neid > maxNeid) maxNeid = neid;
			}
			double neid = maxNeid;


			double arbeit = 0;
			if (r.freieArbeitsplaetze() > 0) arbeit = Math.log10(r.freieArbeitsplaetze());
			arbeit -= 2; // 100 Arbeitsplätze sind die "Norm"


			double freiheit = 0;
			if (bauern < 100) {
				freiheit = 4 - (Math.log10(bauern) * 2);
			}


			double hunger = 0;
			if (Environment.VerhungerteBauern().containsKey(r)) {
				hunger = -5 - Math.log10(Environment.VerhungerteBauern().get(r));
			}

			double a = reichtum + lohn - neid + arbeit + freiheit + hunger;

			attraktivitaet.put(r, a);

//			if (ZATMode.CurrentMode().isDebug() && DEBUG_AUSGABEN) {
//				String lohnEinfluss = "";
//				String neidEinfluss = "";
//				String hungerEinfluss = "";
//				String freiheitsEinfluss = "";
//				if (Math.abs(lohn) > 1e-6) lohnEinfluss = "; Lohn " + nf.format(lohn);
//				if (neid > 1e-6) neidEinfluss = "; Neid " + nf.format(-1 * neid);
//				if (hunger < -1e-6) hungerEinfluss = "; HUNGER " + nf.format(hunger);
//				if (freiheit > 0) freiheitsEinfluss = "; Freiheit " + nf.format(freiheit);
//				r.setBeschreibung(
//						r.getBeschreibung() + " - "
//						+ "Attraktivität für Bauern: Reichtum " + nf.format(reichtum)
//						+ lohnEinfluss
//						+ neidEinfluss
//						+ "; Arbeit " + nf.format(arbeit)
//						+ freiheitsEinfluss
//						+ hungerEinfluss
//						+ " = " + nf.format(a)
//				);
//			}
		}

		// Zweite Runde - die tatsächlichen Auswanderer suchen sich ein Ziel:
		for (Region r : Region.CACHE.values()) {
			if (!r.istBetretbar(null)) continue;

			// gibt es denn irgendeine Nachbarregion, in der es sich (besser) leben lässt?
			// das ist sozusagen das Gegenstück zur Neid-Berechnung (weiter oben)
			double eigeneAttraktivitaet = attraktivitaet.get(r);
			double bestesZiel = Double.NEGATIVE_INFINITY;
			for (Region nachbar : r.getNachbarn()) {
				if (!nachbar.istBetretbar(null)) continue;

				double attr = attraktivitaet.get(nachbar) - eigeneAttraktivitaet;
				if (attr > bestesZiel) bestesZiel = attr;
			}
			// wenn dieser Wert jetzt negativ ist, dann ist es zu Hause doch am schönsten:
			if (bestesZiel < 0)	eigeneAttraktivitaet -= bestesZiel; // also höher

//			if (ZATMode.CurrentMode().isDebug() && DEBUG_AUSGABEN) {
//				String zuhauseIstEsAmSchoensten = "";
//				if (bestesZiel < 0) {
//					zuhauseIstEsAmSchoensten =
//							"; alternativlos " + nf.format(bestesZiel)
//							+ " = " + nf.format(eigeneAttraktivitaet);
//				}
//				r.setBeschreibung(
//						r.getBeschreibung()
//						+ zuhauseIstEsAmSchoensten
//						+ "; Auswanderung zu " + pct.format(BauernAuswanderWahrscheinlichkeit(eigeneAttraktivitaet))
//				);
//			}


			double p = BauernAuswanderWahrscheinlichkeit(eigeneAttraktivitaet);
			int auswanderer = 0;
			for (int i=0; i < r.getBauern(); i++) {
				double schicksal = (double)Random.rnd(0, 1000000);
				if ((schicksal / 1000000d) < p) auswanderer++;
			}

			// bei hohem Auswärtsdrang kommt noch eine pauschale Anzahl dazu:
			// (bspw. hungernde Gletscher räumen...)
			if (p > 0.025d) {
				auswanderer += Random.W(10);
			}
			if (auswanderer > r.getBauern()) auswanderer = r.getBauern();

			List<Region> ziele = new ArrayList<Region>();
			for (Region nachbar : r.getNachbarn()) {
				if (nachbar.istBetretbar(null)) ziele.add(nachbar);
			}

			if (ziele.isEmpty()) {
				new Debug("Keine Auswanderungsziele verfügbar in " + r + " " + r.getCoords() + ".");
				continue;
			}



//			if (ZATMode.CurrentMode().isDebug() && DEBUG_AUSGABEN) {
//				List<String> zielBeschreibung = new ArrayList<String>();
//				for (Region ziel : ziele) {
//					double attrDifferenz = attraktivitaet.get(ziel) - attraktivitaet.get(r);
//
//					// Wenn es im Regionstyp des Ziels weniger Platz als hier gibt, bremst das die Auswander-Freude:
//					int platzAmZiel = ziel.freieArbeitsplaetze() + ziel.benutzteArbeitsplaetze();
//					if (ziel instanceof Wald) platzAmZiel -= 600 * 10;
//					int platzHier = r.freieArbeitsplaetze() + r.benutzteArbeitsplaetze();
//					if (r instanceof Wald) platzHier -= 600 * 10;
//					double groessenQuote =	(double)(platzAmZiel) / (double)(platzHier);
//					if (groessenQuote > 1) groessenQuote = 1;
//
//					double zielWert = BauernAuswanderZielAttraktivitaet(attrDifferenz);
//
//					String desc = ziel + ": " + nf.format(attrDifferenz);
//					desc += " --> " + nf.format(zielWert);
//					if (groessenQuote < 1d) desc += "*" + pct.format(groessenQuote);
//					zielBeschreibung.add(desc);
//				}
//				r.setBeschreibung(
//						r.getBeschreibung()
//						+ " --- es gibt " + auswanderer + " Auswanderer, "
//						+ " mögliche Ziele sind: " + StringUtils.aufzaehlung(zielBeschreibung)
//				);
//			}

			// Ziele als Wahrscheinlichkeiten (Summe = 1) normalisieren:
			double zielWertSumme = 0d;
			Map<Region, Double> zielWerte = new HashMap<Region, Double>();
			for (Region ziel : ziele) {
				double attrDifferenz = attraktivitaet.get(ziel) - attraktivitaet.get(r);
				// new Debug("attrDifferenz = " + attraktivitaet.get(ziel) + " - " + attraktivitaet.get(r) + ", Bauern-Neid: " + GetBauernNeid(r, ziel));

				// noch den Neid bzw. die Abscheu berücksichtigen
				attrDifferenz += GetBauernNeid(r, ziel);


				// Wenn es im Regionstyp des Ziels weniger Platz als hier gibt, bremst das die Auswander-Freude:
				int platzAmZiel = ziel.freieArbeitsplaetze() + ziel.benutzteArbeitsplaetze();
				if (ziel instanceof Wald) platzAmZiel -= 600 * 10;
				int platzHier = r.freieArbeitsplaetze() + r.benutzteArbeitsplaetze();
				if (r instanceof Wald) platzHier -= 600 * 10;

				// new Debug("Platz am Ziel: " + platzAmZiel + ", Platz hier: " + platzHier);
				double groessenQuote =	(double)(platzAmZiel) / (double)(platzHier);
				if (groessenQuote > 1) groessenQuote = 1;

				double zielWert = BauernAuswanderZielAttraktivitaet(attrDifferenz) * groessenQuote;

				// new Debug("Zielwert " + r + " vs " + ziel + " = " + zielWert + " (attrDifferenz=" + attrDifferenz + ")");
				if (zielWert == Double.NaN) throw new RuntimeException("BauernAuswanderZielAttraktivitaet(" + attrDifferenz + ") * " + groessenQuote + " ergibt NaN.");

				zielWerte.put(ziel, zielWert);
				zielWertSumme += zielWert;
				if (zielWertSumme == Double.NaN)  throw new RuntimeException("Summe der Auswanderungswerte ist NaN (aktueller Summand: " + zielWert + ").");
			}
			if (zielWertSumme == 0) {
				if (r.getCoords().getWelt() != 1) {
					new SysMsg("Bauernwanderung aus " + r + " (" + r.getCoords() + ") ist nicht möglich.");
					break;
				}
				throw new RuntimeException("Summe der Auswanderungswerte ist 0.");
			}
			if (zielWertSumme == Double.NaN)  throw new RuntimeException("Summe der Auswanderungswerte ist NaN.");

			// normalisieren:
			for (Region ziel : ziele) {
				zielWerte.put(ziel, zielWerte.get(ziel) / zielWertSumme);
			}

//			if (ZATMode.CurrentMode().isDebug() && DEBUG_AUSGABEN) {
//				List<String> optionsDesc = new ArrayList<String>();
//				for (Region ziel : ziele) {
//					optionsDesc.add(ziel + " " + pct.format(zielWerte.get(ziel)));
//				}
//
//				r.setBeschreibung(
//						r.getBeschreibung()
//						+ " --- " + StringUtils.aufzaehlung(optionsDesc)
//				);
//			}

			// und jetzt: Entscheiden & abhauen!
			Map<Region, Integer> reisen = new HashMap<Region, Integer>();
			for (Region ziel : ziele) { reisen.put(ziel, 0); }

			for (int i=0; i<auswanderer; i++) {
				Region meineWahl = null;
				if (ziele.size() == 1) {
					meineWahl = ziele.get(0);
				} else {
					do {
						Collections.shuffle(ziele);
						int grenze = (int)Math.round(zielWerte.get(ziele.get(0)) * 1000000d);
						if (Random.W(1000000) <= grenze) {
							meineWahl = ziele.get(0);
						} else {
							ziele.remove(0);	// entfernen - hier wollen wir nicht hin
							if (ziele.size() == 1) meineWahl = ziele.get(0);
						}
					} while (meineWahl == null);
				}

				reisen.put(meineWahl, reisen.get(meineWahl) + 1);
			}

			for (Region ziel : reisen.keySet()) {
				if (reisen.get(ziel) == 0) continue;
				SetBauernWanderung(r, ziel, reisen.get(ziel));
			}

//			if (ZATMode.CurrentMode().isDebug() && DEBUG_AUSGABEN) {
//				List<String> desc = new ArrayList<String>();
//				for (Region ziel : reisen.keySet()) {
//					if (reisen.get(ziel) == 0) continue;
//					desc.add(reisen.get(ziel) + " nach " + ziel);
//				}
//
//				r.setBeschreibung(
//						r.getBeschreibung()
//						+ " --- " + StringUtils.aufzaehlung(desc) + "."
//				);
//			}


		} // Ende der zweiten Runde (Auswanderer suchen sich ein Ziel)


		// dritte Runde - gesagt, getan:
		for (Region r : Region.CACHE.values()) {
			if (!r.istBetretbar(null)) continue;

			for (Region nachbar : r.getNachbarn()) {
				int weg = GetBauernWanderung(r, nachbar);
				int her = GetBauernWanderung(nachbar, r);

				int bilanz = her - weg;

				if (bilanz == 0) continue;

				r.setBauern(r.getBauern() + bilanz);
			}
		}


	}


	private static double BauernAuswanderWahrscheinlichkeit(double attraktivitaet) {
		double minWanderWahrscheinlichkeit = 0.001d;
		double maxWanderWahrscheinlichkeit = 0.05d;
		double spanne = maxWanderWahrscheinlichkeit - minWanderWahrscheinlichkeit;

		double alpha = -3;
		double beta = 1.8d;
		double p = 1 / (1 + Math.pow(Math.E, - ((alpha - attraktivitaet) / beta)));

		return minWanderWahrscheinlichkeit + p * spanne;
	}

	private static double BauernAuswanderZielAttraktivitaet(double attraktivitaetsUnterschied) {
		double alpha = -3;
		double beta = 1d;

		attraktivitaetsUnterschied *= -1;
		double p = 1 / (1 + Math.pow(Math.E, - ((alpha - attraktivitaetsUnterschied) / beta)));

		return p;
	}


	private static void SetBauernWanderung(Region von, Region nach, int anzahl) {
		if (!BauernWanderung.containsKey(von)) BauernWanderung.put(von, new HashMap<Region, Integer>());

		BauernWanderung.get(von).put(nach, anzahl);
	}

	public static int GetBauernWanderung(Region von, Region nach) {
		if (!BauernWanderung.containsKey(von)) return 0;
		if (!BauernWanderung.get(von).containsKey(nach)) return 0;

		return BauernWanderung.get(von).get(nach);
	}

	private static double BewerteProKopfVermoegen(Region r) {
		int bauern = r.getBauern();	if (bauern < 1) bauern = 1;
		int silber = r.getSilber(); if (silber < 1) silber = 1;
		if (bauern < 21) {
			// pro-Kopf-Vermögen ist da nicht sonderlich verlässlich...
			double log = Math.log(bauern + 1) / Math.log(21);
			silber = (int)Math.round((double)silber * log * log);
			// 1 => 5%, 2 => 13%, 3 => 21%, ... 10 => 62%, 19 => 94%
			if (silber < 1) silber = 1;
		}

		return (double)silber / (double)bauern;
	}

	public static double GetBauernNeid(Region hier, Region dort) {
		double silberProKopfDort = BewerteProKopfVermoegen(dort);
		double silberProKopfHier = BewerteProKopfVermoegen(hier);

		// sollte ohnehin sichergestellt sein, aber um auf jeden Fall Ärger mit dem Logarithmus zu vermeiden:
		if (silberProKopfDort < 0.1) silberProKopfDort = 0.1;
		if (silberProKopfHier < 0.1) silberProKopfHier = 0.1;

		double neidfaktor = silberProKopfDort / silberProKopfHier;
		double neid = Math.log10(neidfaktor);
		if (neid < -3) neid = -3;
		if (neid > 3) neid = 3;
		neid *= 2;

		return neid;
	}

}
