package de.x8bit.Fantasya.Host.EVA;

import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Ship;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.SysErr;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;
import de.x8bit.Fantasya.Host.EVA.util.DoppelteAusfuehrungException;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.IDHint;
import de.x8bit.Fantasya.Host.EVA.util.Reisen;
import de.x8bit.Fantasya.Host.EVA.util.UnitHint;
import de.x8bit.Fantasya.util.Codierung;

/**
 *
 * @author hapebe
 */
public class Folgen extends EVABase {

	public Folgen()	{
		super("folge", "Reisende folgen dem Weg ihrer befohlenen Anführer");

		addTemplate("");
		for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
        List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();
		BefehlsMuster bm = null;

		// FOLGE EINHEIT ...
		bm = new BefehlsMuster(Folgen.class, 1, "^folge(n)? (einheit) [a-z0-9]{1,4}([ ]+(\\/\\/).*)?", "f", Art.LANG);
		bm.addHint(new UnitHint(2));
        bm.setKeywords("folge", "folgen", "einheit");
		retval.add(bm);

		// FOLGE EINHEIT TEMP ...
		bm = new BefehlsMuster(Folgen.class, 1 + EVABase.TEMP, "^folge(n)? (einheit) (temp) [a-z0-9]{1,4}([ ]+(\\/\\/).*)?", "f", Art.LANG);
		bm.addHint(new UnitHint(2));
        bm.setKeywords("folge", "folgen", "einheit");
		retval.add(bm);

		// FOLGE SCHIFF ...
		bm = new BefehlsMuster(Folgen.class, 2, "^folge(n)? (schiff) [a-z0-9]{1,4}([ ]+(\\/\\/).*)?", "f", Art.LANG);
		bm.addHint(new IDHint(2));
        bm.setKeywords("folge", "folgen", "schiff");
		retval.add(bm);


		return retval;
    }

	@Override
	public void PreAction() { }


	@Override
	public void DoAction(Einzelbefehl eb) {
		if (eb.isPerformed()) throw new DoppelteAusfuehrungException(eb.toString());

		// Angaben über die "Akteure" selbst:
		Unit u = eb.getUnit();
//		Partei p = Partei.getPartei(u.getOwner());

		int variante = eb.getVariante();
		Unit targetUnit = null;
		Ship targetShip = null;

		// ist eine TEMP-Einheit im Spiel?
		if ((eb.getVariante() & EVABase.TEMP) != 0) {
			String tempId = eb.getTargetUnit().toLowerCase();
			if (tempId.startsWith("temp ")) tempId = tempId.substring(5);

			int tempnummer = Unit.getRealNummer(tempId, u);

			if (tempnummer == 0) {
				eb.setError();
				new Fehler(u + " - Temp-Einheit " + eb.getTargetUnit() + " nicht gefunden.", u, u.getCoords());
				return;
			}
			eb.setTargetUnit(Codierung.toBase36(tempnummer));
			variante -= EVABase.TEMP;
			// Einheit ist jetzt mit "normaler" Nummer bekannt
		}

		if (eb.getTargetUnit() != null) {
			targetUnit = Unit.Load(Codierung.fromBase36(eb.getTargetUnit()));
		} else if (eb.getTargetId() != null) {
			if (variante == 2) {
				// FOLGE SCHIFF ...
				targetShip = Ship.Load(Codierung.fromBase36(eb.getTargetId()));
				if (targetShip == null) {
					eb.setError();
					new Fehler(u + " - '" + eb.getBefehl() + "': Schiff " + eb.getTargetId() + " nicht gefunden.", u);
					return;
				}
				if (targetShip.getOwner() != 0) {
					targetUnit = Unit.Load(targetShip.getOwner());
				}
			} else {
				eb.setError();
				new SysErr("Unbekannte FOLGE-Variante ohne Einheit: #" + variante + "; " + eb);
				return;
			}
		} else {
			eb.setError();
			new SysErr("FOLGE ohne Einheit UND ohne Schiff: " + eb);
			return;
		}

		// Wäre (also: War) die Einheit am Ausgangsort sichtbar?
		if ((targetUnit == null) || (!u.couldSeeInRegion(targetUnit, Region.Load(u.getCoords())))) {
			eb.setError();
			if (variante == 1) {
				new Fehler(u + " - '" + eb.getBefehl() + "': Einheit " + eb.getTargetUnit() + " nicht gefunden.", u, u.getCoords());
			} else if (variante == 2) {
				new Fehler(u + " - '" + eb.getBefehl() + "': Schiff " + targetShip + " nicht gefunden.", u, u.getCoords());
			}
			return;
		}

		// die Einheit ist vorhanden und sichtbar
		if (targetUnit.getNummer() == u.getNummer()) {
			eb.setError();
			new Fehler(u + " verbringt den Monat in konzentrierter Selbstbeobachtung und findet heraus: Wer sich selbst folgen möchte, kommt nicht voran.", u);
			return;
		}


		Einzelbefehl vorlaufBefehl = null;
		for (Einzelbefehl maybe : targetUnit.BefehleExperimental) {
			if (maybe.getProzessor() == NachUndRoute.class) {
				vorlaufBefehl = maybe;
				break;
			}
		}
		if (vorlaufBefehl == null) {
			eb.setError();
			if (variante == 1) {
				new Fehler(u + ": " + targetUnit + " ist in diesem Monat nicht gereist.", u);
			} else if (variante == 2) {
				new Fehler(u + ": Auf Schiff " + targetShip + " tut sich diesen Monat nicht viel.", u);
			}
			return;
		}

		List<Region> vorlauf = vorlaufBefehl.getReise();
		if (vorlauf == null) {
			eb.setError();
			if (targetUnit.getOwner() == u.getOwner()) {
				new Fehler(u + ": " + targetUnit + " wollte zwar reisen, ist aber nicht vom Fleck gekommen.", u, u.getCoords());
			} else {
				// TODO: Hier offen legen, dass die Einheit eigentlich reisen wollte?
				new Fehler(u + ": " + targetUnit + " ist in diesem Monat nicht gereist.", u, u.getCoords());
			}
			return;
		}

		if (vorlauf.size() == 0) {
			// Zieleinheit hat scih nicht bewegt - vgl. dessen Meldungen
			eb.setError();
			new Fehler(u + " - '" + eb.getBefehl() + "': Einheit " + eb.getTargetUnit() + " hat sich nicht bewegt.", u);
			return;
		}

		// die Einheit ist gereist.

		// die Einheit ist (war) nicht in der gleichen Region
		if (!u.getCoords().equals(vorlauf.get(0).getCoords())) {
			eb.setError();
			if (variante == 1) {
				new Fehler(u + " - '" + eb.getBefehl() + "': Einheit " + eb.getTargetUnit() + " ist nicht in der gleichen Region.", u);
			} else if (variante == 2) {
				new Fehler(u + " - '" + eb.getBefehl() + "': Schiff " + targetShip + " ist nicht in der gleichen Region.", u);
			}
			return;
		}


		// endlich: Hinterher!!!
		List<Region> weg = new ArrayList<Region>();
		for (Region reg : vorlauf) weg.add(reg);
		// die letzte Region ist nicht im Reise-Bericht enthalten:
		weg.add(Region.Load(targetUnit.getCoords()));

		Einzelbefehl hinterher = new Einzelbefehl(u, eb.getCoords(), Reisen.ErzeugeNachBefehl(weg), eb.getSortRank());

		Reisen.Ausfuehren(hinterher);
		eb.setPerformed();
		// der FOLGE-Befehl hat die Reise ja "verursacht":
		eb.setReise(hinterher.getReise());

		if (eb.getReise() != null) {
			if (variante == 1) {
				if (vorlauf.size() == eb.getReise().size()) {
					new Info(u + " folgt " + targetUnit + " auf die Reise.", u);
				} else {
					new Info(u + " folgt " + targetUnit + " auf die Reise, wird aber abgehängt.", u);
				}
			} else if (variante == 2) {
				if (vorlauf.size() == eb.getReise().size()) {
					new Info(u + " fährt im Konvoi mit " + targetShip + ".", u);
				} else {
					new Info(u + " fährt im Konvoi mit " + targetShip + ", kann aber den Sichtkontakt nicht halten.", u);
				}
			} else {
				new SysErr("Unbekannte FOLGE-Variante #" + variante + " (für Meldung); " + eb);
			}
		}

	}


	@Override
	public void PostAction() {
		// nach dem Wechsel von Regionen ist es ein guter Augenblick, um die Sortierung neu zu berechnen:
		for (Region r : Region.CACHE.values()) {
			Sortieren.Normalisieren(r);
		}
	}





	@Override
	public boolean DoAction(Unit u, String[] befehl) {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public void DoAction(Region r, String befehl) {
		return; // NOP
	}
}
