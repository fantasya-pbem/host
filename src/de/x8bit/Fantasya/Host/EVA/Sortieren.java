package de.x8bit.Fantasya.Host.EVA;

import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;
import de.x8bit.Fantasya.Host.EVA.util.DoppelteAusfuehrungException;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.UnitHint;
import de.x8bit.Fantasya.util.Codierung;
import de.x8bit.Fantasya.util.comparator.UnitSortierungComparator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author hapebe
 */
public class Sortieren extends EVABase {

	public Sortieren() {
		super("sortiere", "Sortieren von Einheiten u.a.");

		addTemplate("");
		for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
        BefehlsMuster bm = null;
		List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();

        bm = new BefehlsMuster(Sortieren.class, 1, "^@?(sortiere)n? (vor) [a-z0-9]{1,4}([ ]+(\\/\\/).*)?", "s", Art.KURZ);
        bm.setKeywords("sortiere", "sortieren", "vor");
		bm.addHint(new UnitHint(2));
		retval.add(bm);

        bm = new BefehlsMuster(Sortieren.class, 2, "^@?(sortiere)n? ((nach)|(hinter)) [a-z0-9]{1,4}([ ]+(\\/\\/).*)?", "s", Art.KURZ);
		bm.addHint(new UnitHint(2));
        bm.setKeywords("sortiere", "sortieren", "nach", "hinter");
		retval.add(bm);

        bm = new BefehlsMuster(Sortieren.class, 11, "^@?(sortiere)n? ((vorn)|(vorne)|(anfang)|(erste)|(erster)|(erstes))([ ]+(\\/\\/).*)?", "s", Art.KURZ);
        Set<String> keywords = new HashSet<String>();
        keywords.add("sortiere");
        keywords.add("sortieren");
        keywords.add("vorn");
        keywords.add("vorne");
        keywords.add("anfang");
        keywords.add("erste");
        keywords.add("erster");
        keywords.add("erstes");
        bm.setKeywords(keywords);
		retval.add(bm);

        bm = new BefehlsMuster(Sortieren.class, 12, "^@?(sortiere)n? ((ende)|(hinten)|(letzte)|(letzter)|(letztes))([ ]+(\\/\\/).*)?", "s", Art.KURZ);
        keywords = new HashSet<String>();
        keywords.add("sortiere");
        keywords.add("sortieren");
        keywords.add("ende");
        keywords.add("hinten");
        keywords.add("letzte");
        keywords.add("letzter");
        keywords.add("letztes");
        bm.setKeywords(keywords);
		retval.add(bm);

        return retval;
    }



	@Override
	public void DoAction(Region r, String befehl) {
		List<Einzelbefehl> befehle = BefehlsSpeicher.getInstance().get(this.getClass(), r.getCoords());

		for (Einzelbefehl eb : befehle) {
			if (eb.isPerformed()) throw new DoppelteAusfuehrungException(eb.toString());

			// Angaben über die "Akteure" selbst:
			Unit u = eb.getUnit();
			Partei p = Partei.getPartei(u.getOwner());
			Unit targetUnit = null;

			// auf jeden Fall:
			Sortieren.Normalisieren(p, r);

			// vor oder nach - es gibt eine Bezugseinheit
			if ((eb.getVariante() == 1) || (eb.getVariante() == 2)) {
				targetUnit = Unit.Load(Codierung.fromBase36(eb.getTargetUnit()));
				if (targetUnit == null) {
					eb.setError();
					new Fehler("'" + eb.getBefehlCanonical() + "' - Einheit nicht gefunden.", u, u.getCoords());
					continue;
				}
				if (!targetUnit.getCoords().equals(u.getCoords())) {
					eb.setError();
					if (targetUnit.getOwner() == u.getOwner()) {
						new Fehler("SORTIERE - Einheit " + targetUnit + " ist nicht hier.", u, u.getCoords());
					} else {
						new Fehler("'" + eb.getBefehlCanonical() + "' - Einheit nicht gefunden.", u, u.getCoords());
					}
					continue;
				}
				if (targetUnit.getOwner() != u.getOwner()) {
					eb.setError();
					if (u.cansee(targetUnit)) {
						new Fehler("SORTIERE - Einheit " + targetUnit + " gehört nicht zu uns.", u, u.getCoords());
					} else {
						new Fehler("'" + eb.getBefehlCanonical() + "' - Einheit nicht gefunden.", u, u.getCoords());
					}
					continue;
				}
			}

			// vor
			if (eb.getVariante() == 1) {
				u.setSortierung(targetUnit.getSortierung() - 5);
			}
			// hinter
			if (eb.getVariante() == 2) {
				u.setSortierung(targetUnit.getSortierung() + 5);
			}

			// Anfang
			if (eb.getVariante() == 11) {
				u.setSortierung(5);
			}
			// Ende
			if (eb.getVariante() == 12) {
				u.setSortierung(Integer.MAX_VALUE);
			}

			// ganz ordentlich:
			Sortieren.Normalisieren(p, r);

			eb.setPerformed();
		}
	}

	@Override
	public boolean DoAction(Unit u, String[] befehl) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void DoAction(Einzelbefehl eb) { }

	@Override
	public void PreAction() { }

	@Override
	public void PostAction() { }


	/**
	 * normalisiert die Sortierreihenfolge der Einheiten aller Parteien in der Region
	 * @param r
	 */
	public static void Normalisieren(Region r) {
		for (Partei p : r.anwesendeParteien()) {
			Sortieren.Normalisieren(p, r);
		}
	}


	/**
	 * Stellt eine wohldefinierte Reihenfolge der "Sortierung" der Einheiten her.
	 * Die erste Einheit erhält den Wert 10, dann wird jeweils um 10 erhöht.
	 * Eine vorher gesetzte Sortierung wird beachtet.
	 * @param p nur die Einheiten dieser Partei werden normalisiert
	 * @param r nur in dieser Region wird normalisiert
	 */
	public static void Normalisieren(Partei p, Region r) {
		// TODO brauchen wir das überhaupt noch, wo wir jetzt mit TreeSet<Unit> arbeiten?

		List<Unit> meine = new ArrayList<Unit>(r.getUnits(p));
		Collections.sort(meine, new UnitSortierungComparator());

		int i = 1;
		for (Unit u : meine) {
			u.setSortierung(i * 10);
			i ++;
		}

//		new Debug("Normalisierte Einheiten von " + p + " in " + r + ": ");
//		for (Unit u : r.getUnits(p)) {
//			new Debug(" -- " + u.getSortierung() + ": " + u);
//		}
	}

}
