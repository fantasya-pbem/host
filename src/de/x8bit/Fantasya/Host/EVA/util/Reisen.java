package de.x8bit.Fantasya.Host.EVA.util;

import de.x8bit.Fantasya.Atlantis.Allianz.AllianzOption;
import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Buildings.Seehafen;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Messages.Bewegung;
import de.x8bit.Fantasya.Atlantis.Messages.Debug;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.SysErr;
import de.x8bit.Fantasya.Atlantis.Regions.Ozean;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Richtung;
import de.x8bit.Fantasya.Atlantis.Ship;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.EVA.NachUndRoute;
import de.x8bit.Fantasya.Host.EVA.Sortieren;
import de.x8bit.Fantasya.util.Random;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author hapebe
 */
public class Reisen {

	private Reisen() { }

	private static Reisen instance = new Reisen();

	public static List<DurchreiseRecord> durchReisen = new ArrayList<DurchreiseRecord>();

	/**
	 * könnte man auch per GameRules konfigurierbar machen...
	 * wenn true, werden Debug-Ausgaben gemacht.
	 */
	public final static boolean LOG = false;

	public static Reisen getInstance() {
		return instance;
	}

	public static void Ausfuehren(Einzelbefehl eb) {
        // Angaben über die "Akteure" selbst:
        Unit u = eb.getUnit();

        // ist gedacht, um in den check... Routinen das passende Bewegungsverb
		// für die Meldung setzen zu können:
		ReiseVerb reiseVerb = new ReiseVerb("reist");
        List<Region> reise = null;
        if (u.getSchiff() == 0) { // kein Kapitän
            // über Land
            if (u.checkLand(reiseVerb)) reise = Reisen.Laufen(eb);
        } else {
            // Segeln
            reiseVerb.setVerb("schippert");
			if (u.checkSegeln(reiseVerb)) {
                reise = Reisen.Segeln(eb);
            }
        }

        // Reise dokumentieren
        if (reise != null) {
			u.ReiseDoku(reise, reiseVerb.getVerb());
			for(int i = 1; i < reise.size(); i++) {
				Region r = reise.get(i);
				Reisen.RegistriereDurchReise(r, u);
			}

			// im Befehl den Weg erfassen - für etwaige FOLGE-Befehle
			eb.setReise(reise);
		}


		// in der neuen Region sind wir ganz am Ende der Report-Liste:
		u.setSortierung(Integer.MAX_VALUE);
		Sortieren.Normalisieren(Region.Load(u.getCoords()));
	}

	/**
	 * das Segeln von Schiffen über den großen, weiten Ozean
	 * @param eb - Bewegungsbefehl (NACH oder ROUTE)
	 * @return die Liste der durchreisten Regionen, außer die letzte (aktuelle)
	 */
	private static List<Region> Segeln(Einzelbefehl eb) {
		Unit u = eb.getUnit();
		// mit diesem Schiff soll gesegelt werden:
		Ship ship = Ship.Load(u.getSchiff());

		// Startregion
		Region r = Region.Load(u.getCoords());

		// jede einzelne angegebene Richtung abklappern ... außer bei einem Fehler
		List<Region> bewegung = new ArrayList<Region>();
        boolean pausiert = false;
		while (r != null) {
			// new Debug("befehl: " + befehl[1]);
            if (LOG) new Debug("Bewegungspunkte(zu Wasser) von " + u + "@" + u.getCoords().xy() + ": " + u.getBewegungspunkte());

            // aktuelle Region speichern
			bewegung.add(r);

			// wenn es nicht mehr weiter gehen soll:
			if (pausiert) break;

			List<String> auftraege = new ArrayList<String>();
			for (int i=1; i<eb.getTokens().length; i++) auftraege.add(eb.getTokens()[i]);

			if ((eb.getVariante() == NachUndRoute.BEFEHL_NACH_KOORDS) || (eb.getVariante() == NachUndRoute.BEFEHL_ROUTE_KOORDS)) {
				auftraege = ZieleZuSchritten(eb); // quasi ein "Schattenbefehl" - die konkrete Umsetzung des Koordinatenauftrags
			}


			// Bewachen aufheben
			for(Unit unit : ship.getUnits())	{
				if (unit.getBewacht()) {
					unit.setBewacht(false);
					new Bewegung(unit + ": Die Bewachung in " + r + " wurde durch die Abreise aufgehoben.", unit);
				}
			}

            // Richtung holen
			Richtung richtung = Richtung.getRichtung(auftraege.get(0));
			
			// Abriften durch Sturm
			if (r instanceof Ozean) {
				Ozean o = (Ozean)r;
				int value = ((Ozean) r).getSturmValue();
				int monat[] = new int[] { 1, 2, 1, 1, 0, 0, 0, 0, 1, 2, 3, 2 };
				value += monat[GameRules.getJahreszeit()];
				if (Random.rnd(0, 100) < value) {
					new Bewegung(u + " wird beim Segeln durch den Sturm abgetrieben.", u);
					richtung = richtung.randomNachbar();
				}
			}
			
			// Bewegung ausführen
            // new Debug("Richtung: " + richtung);
			if (richtung != null) {
				boolean cansail = true;

				// wenn an Land, dann Küste beachten
				if (r.istBetretbar(null) && ship.getKueste() != null) {
					if (!richtung.equals(ship.getKueste()))	{
						new Fehler("Das Schiff " + ship + " ankert an der Küste im " + ship.getKueste().name() + " und kann daher nicht nach " + richtung.name() + " fahren.", u);
						cansail = false;
					}
				}

                // gibt es einen Seehafen in der Zielregion?
                Region ziel = Region.Load(r.getCoords().shift(richtung));
                if (ziel.istBetretbar(null)) {
                    for (Building b : ziel.getBuildings()) {
                       if (b instanceof Seehafen) {
                           Unit hafenmeister = Unit.Load(b.getOwner());
                           if (hafenmeister == null) {
                               new Fehler("Der Seehafen in " + ziel + " hat keinen Hafenmeister, das Anlegen scheitert.", u);
                        	   cansail = false;
                        	   continue;
                           }
                           if (hafenmeister.getOwner() == u.getOwner()) continue; // eigene Partei ist okay.
                           Partei other = Partei.getPartei(hafenmeister.getOwner());
                           if (!other.hatAllianz(u.getOwner(), AllianzOption.Kontaktiere)) {
                                new Fehler("Der Seehafen in " + ziel + " lässt uns nicht anlegen.", u);
                                cansail = false;
                           }
                       }
                    }
                }
                if (!cansail) break;
                r = r.Movement(richtung, ship);
				if (r != null) {
					pausiert = !Rotieren(eb);
				} else {
					// da hat es dann aus irgendeinem Grund nicht geklappt mit diesem Reise-Schritt:
					break;
				}
				
			} else {
				// es handelt sich um PAUSE (?)
                Rotieren(eb);
				break; // es wurde keine neue Region angereist.
			}


			// mögliche Küste speichern
			if (r.istBetretbar(null)) ship.setKueste(richtung.invert()); else ship.setKueste(null);


            if ((u.getBewegungspunkte() <= 0) && (!pausiert) && (eb.getTokens().length > 1)) {
                // hat keine Bewegungspunkte mehr, will noch was, und das ist keine PAUSE...
                // new Bewegung(u + " schafft es bis nach " + r + ".", u);
                pausiert = true;
            }

			if (eb.getTokens().length <= 1) pausiert = true;
		}

        // wenn nur noch das Schlüsselwort da ist (NACH / ROUTE), dann ganz löschen.
        if (eb.getTokens().length == 1) {
			eb.setTokens(new String[]{});
			eb.setKeep(false);
		}

		// ggf. die letzte Region entfernen,
		if (bewegung.size() > 0) {
			// nämlich wenn es die aktuelle ist:
			if (bewegung.get(bewegung.size() - 1).equals(Region.Load(u.getCoords()))) {
				bewegung.remove(bewegung.size() - 1);
			}
		}
		
		return bewegung;
	}

	/**
	 * Bewegung über Land
	 * @param eb - kompletter Bewegungsbefehl
	 * @return die Liste der durchreisten Regionen, außer die letzte (aktuelle)
	 */
	private static List<Region> Laufen(Einzelbefehl eb) {
		Unit u = eb.getUnit();
		// Startregion
		Region r = Region.Load(u.getCoords());
		// alle betretenen Regionen, außer der letzten - d.h. der hinterher aktuellen
		List<Region> bewegung = new ArrayList<Region>();

        boolean pausiert = false;
		while (r != null) {
			// new Debug("befehl: " + befehl[1]);
            if (LOG) new Debug("Bewegungspunkte(zu Land) von " + u + "@" + u.getCoords().xy() + ": " + u.getBewegungspunkte());

            // aktuelle Region speichern
			bewegung.add(r);

			// wenn es nicht mehr weiter gehen soll:
			if (pausiert) break;

			List<String> auftraege = new ArrayList<String>();
			for (int i=1; i<eb.getTokens().length; i++) auftraege.add(eb.getTokens()[i]);

			// bei Befehlen mit Zielkoordinaten:
			if ((eb.getVariante() == NachUndRoute.BEFEHL_NACH_KOORDS) || (eb.getVariante() == NachUndRoute.BEFEHL_ROUTE_KOORDS)) {
				auftraege = ZieleZuSchritten(eb); // quasi ein "Schattenbefehl" - die konkrete Umsetzung des Koordinatenauftrags
				if(auftraege.isEmpty()) break;
			}

			// new Debug("Richtungs-Aufträge: " + StringUtils.aufzaehlung(auftraege));

            // Richtung holen und Bewegung ausführen
			Richtung richtung = Richtung.getRichtung(auftraege.get(0));
            // new Debug("Richtung: " + richtung);
			if (richtung != null) {
                // wenn irgendwas nicht geht, dann gibt es hier null zurück:
				r = r.Movement(richtung, u);
				if (r != null) {
					// hat geklappt!
					pausiert = !Rotieren(eb);
				} else {
					// nej.
					break;
				}
			} else {
				// es handelt sich um PAUSE (?)
                Rotieren(eb);
				break; // es wurde keine neue Region angereist.
			}

            if ((u.getBewegungspunkte() <= 0) && (!pausiert) && (eb.getTokens().length > 1)) {
                // hat keine Bewegungspunkte mehr, will noch was, und das ist keine PAUSE...
                new Bewegung(u + " schafft es bis nach " + r + ".", u);
                break;
            }

			// jetzt sind wir auf jeden Fall eine Region gereist - Bewachen aufheben:
			if (u.getBewacht()) {
				u.setBewacht(false);
				new Bewegung(u + ": Die Bewachung in " + r + " wurde durch die Abreise aufgehoben.", u);
			}

			if (eb.getTokens().length <= 1) pausiert = true;
		}

        // wenn nur noch das Schlüsselwort da ist (NACH / ROUTE), dann ganz löschen.
        if (eb.getTokens().length == 1) {
			eb.setTokens(new String[]{});
			eb.setKeep(false);
		}

		// ggf. die letzte Region entfernen,
		if (bewegung.size() > 0) {
			// nämlich wenn es die aktuelle ist:
			if (bewegung.get(bewegung.size() - 1).equals(Region.Load(u.getCoords()))) {
				bewegung.remove(bewegung.size() - 1);
			}
		}

		return bewegung;
	}

	/**
	 * @param eb
	 * @return Richtungs-Codes, die den Weg zu den Koordinaten beschreiben, die in eb angegeben sind.
	 */
	private static List<String> ZieleZuSchritten(Einzelbefehl eb) {

		Unit u = eb.getUnit();
		Partei p = Partei.getPartei(u.getOwner());
		Coords cursor = u.getCoords();

		// alle Zielangaben (Coords / "PAUSE") einsammeln:
		List<String> zielAngaben = new ArrayList<String>();
		for (int i=1; i<eb.getTokens().length; i++) zielAngaben.add(eb.getTokens()[i]);

		// die Zielkoordinaten in konkrete Schritte umwandeln:
		List<String> schritte = new ArrayList<String>();
		for (String token : zielAngaben) {
			if (token.equalsIgnoreCase("pause")) {
				schritte.add("pause");
				continue;
			}

			Coords pvtEtappenZiel = Coords.fromString(token);
			if (pvtEtappenZiel == null) {
				eb.setError();
				new Fehler(u + " - habe die Koordinaten " + token + " nicht verstanden.", u);
				return schritte;
			}
			// pvtEtappenZeil kann jetzt (nicht mehr null sein, wenn wir überhaupt hier landen:
			Coords etappenZiel = p.getGlobalCoords(pvtEtappenZiel);

			int loops = 0;
			while(!(cursor.equals(etappenZiel))) {
				Richtung wohin = cursor.getRichtungNach(etappenZiel);
				schritte.add(wohin.getShortcut());
				cursor = cursor.shift(wohin);

				if (++loops > 100000) throw new RuntimeException("Keine Route gefunden nach " + loops + " Iterationen:" + u + ", '" + eb + "'");
			}

			// cursor ist jetzt gleich etappenZiel - also auf zu neuen Horizonten.
		}

		return schritte;
	}

	public static String ErzeugeNachBefehl(List<Region> reise) {
		if ((reise == null) || reise.size() < 2) {
			new SysErr("Reisen.ErzeugeNachBefehl für eine nichtvorhandene Reise! (" + reise + ")");
			return "";
		}

		StringBuilder sb = new StringBuilder();
		sb.append("NACH");

		for (int i=1; i<reise.size(); i++) {
			Coords from = reise.get(i - 1).getCoords();
			Coords to = reise.get(i).getCoords();
			sb.append(" ").append(from.getRichtungNach(to).getShortcut());
		}

		return sb.toString();
	}

	public static void RegistriereDurchReise(Region r, Unit u) {
		Reisen.durchReisen.add(Reisen.getInstance().new DurchreiseRecord(r.getCoords(), u.getNummer()));
	}

//	/**
//	 * Rotiert den Route-Befehl
//	 * @param befehl - ROUTE o o PAUSE w w PAUSE / NACH o no o no PAUSE
//	 */
//	private static String[] rotateMovement(String[] befehl, boolean kreislauf) {
//		String alt = befehl[1];
//		for(int i = 1; i< befehl.length - 1; i++) befehl[i] = befehl[i + 1];
//		befehl[befehl.length - 1] = alt;
//
//		if (!kreislauf) {
//			String[] kuerzer = new String[befehl.length - 1];
//			for (int i=0; i<kuerzer.length; i++) {
//				kuerzer[i] = befehl[i];
//			}
//			return kuerzer;
//		}
//
//		return befehl;
//	}

	/**
	 * Wenn das jeweils erste Ziel eines Bewegungs-Befehls erreicht wurde,
	 * kümmert sich diese Methode darum, den Befehl für die mögliche Weiterreise
	 * vorzubereiten. Wenn keine Weiterreise gewünscht oder möglich ist, wird 
	 * false zurückgegeben.
	 * @param eb
	 */
	private static boolean Rotieren(Einzelbefehl eb) {
		if (eb.getTokens().length == 1) throw new RuntimeException("Hier ist ein Bewegungsbefehl ohne Parameter angekommen: " + eb);

		// hier ist die betreffende Einheit gerade:
		Coords aufenthalt = eb.getUnit().getCoords();

		String debug = "ZieleRotieren: " + eb.getBefehlCanonical() + " >>> ";

        // wenn ROUTE, dann Kreislauf
        boolean kreislauf =
				(eb.getVariante() == NachUndRoute.BEFEHL_ROUTE_RICHTUNG)
				|| (eb.getVariante() == NachUndRoute.BEFEHL_ROUTE_KOORDS);


		// die Ziele verarbeiten
		String erstesZiel = eb.getTokens()[1];
		if (erstesZiel.equalsIgnoreCase("pause")) {
			if (LOG) new Debug(debug + eb.getBefehlCanonical() + " - diesen Monat ausruhen (PAUSE als erste Aktion).");
			return false;
		}
		
		if ((eb.getVariante() == NachUndRoute.BEFEHL_NACH_RICHTUNG)
				|| (eb.getVariante() == NachUndRoute.BEFEHL_ROUTE_RICHTUNG)) {
			// Wenn der Befehl Richtungsangaben enthält, immer rotieren:
			Reisen.BefehlsTokensVerschieben(eb, kreislauf);
		} else if ((eb.getVariante() == NachUndRoute.BEFEHL_NACH_KOORDS)
			|| (eb.getVariante() == NachUndRoute.BEFEHL_ROUTE_KOORDS)) {
			// bei Zielkoordinaten nur dann verschieben, falls wir das erste Ziel erreicht haben:
			Coords ziel = Coords.fromString(erstesZiel);
			Partei p = Partei.getPartei(eb.getUnit().getOwner());
			ziel = p.getGlobalCoords(ziel);
			if (aufenthalt.equals(ziel)) {
				if (LOG) new Debug(eb.getUnit() + " - Zielkoordinate " + ziel + " erreicht.");
				Reisen.BefehlsTokensVerschieben(eb, kreislauf);
			}
		} else {
			throw new IllegalStateException("Unerwartete Befehlsvariante " + eb.getVariante() + ".");
		}
		if (eb.getTokens().length <= 1) {
			eb.setTokens(new String[] {eb.getTokens()[0]});
			if (LOG) new Debug(debug + eb.getBefehlCanonical() + " - angekommen.");
			return false;
		}

		// wenn am Anfang keine PAUSE vorn war, ist jetzt eine PAUSE vorn?
		if (eb.getTokens()[1].equalsIgnoreCase("pause")) {
			Reisen.BefehlsTokensVerschieben(eb, kreislauf);

			if (LOG) new Debug(debug + eb.getBefehlCanonical() + " - und Pause machen.");
			return false; // diese Runde nicht weiterreisen
		}


		if (LOG) new Debug(debug + eb.getBefehlCanonical() + " - und weiter geht's.");
		return true; // die Reise soll noch (in dieser Runde) weitergehen
	}

	private static void BefehlsTokensVerschieben(Einzelbefehl eb, boolean kreislauf) {
		String erstesZiel = eb.getTokens()[1];
		if ("pause".equals(erstesZiel)) erstesZiel = "PAUSE";

		int neueLaenge = eb.getTokens().length - (kreislauf?0:1); // wenn keine "Kreislauf", dann wird der Befehl insgesamt um ein Token kürzer

		String[] newTokens = new String[neueLaenge];
		newTokens[0] = eb.getTokens()[0]; // Verb unbesehen übernehmen

		// alle Tokens ab 2 um eine Position nach vorn verschieben:
		for (int i=2; i<eb.getTokens().length; i++) newTokens[i-1] = eb.getTokens()[i];

		if (kreislauf) newTokens[newTokens.length - 1] = erstesZiel; // ggf. das erreichte Ziel wieder hinten anhängen.

		eb.setTokens(newTokens);
	}


	public class DurchreiseRecord {
		final Coords c;
		final int unitId;

		public DurchreiseRecord(Coords c, int unitId) {
			this.c = c;
			this.unitId = unitId;
		}

		public Coords getCoords() {
			return c;
		}

		public int getUnitId() {
			return unitId;
		}
	}


}
