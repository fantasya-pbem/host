package de.x8bit.Fantasya.Host.EVA.util;

import java.util.ArrayList;

import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.EVA.EVABase;
import de.x8bit.Fantasya.Host.EVA.TempEinheiten;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;
import java.util.List;

/**
 * <p>Befehlsliste für jeweils eine Einheit und eine Runde. Die Liste verweigert
 * die Aufnahme mehrerer langer Befehle bzw. gemischter multi-langer (HANDEL)
 * und anderer langer Befehle.</p>
 * <p>Ausnahme: Innerhalb von MACHE TEMP xyz .. ENDE sind lange Befehle erstmal
 * erlaubt; auch mehrere werden nicht automatisch verworfen. Diese Überprüfung
 * muss dann beim Kopieren der Befehle in die eigene Befehlsliste der
 * TEMP-Einheit erfolgen.</p>
 * @author hb
 */
public class BefehlsListe extends ArrayList<Einzelbefehl> {
	private static final long serialVersionUID = 8858843017049903540L;

    protected String fehlerBeschreibung = null;

    public void add(Unit u, String text) {
        Einzelbefehl eb = new Einzelbefehl(u, u.getCoords(), text, this.size());

        this.add(eb);
    }

    @Override
    public boolean add(Einzelbefehl eb) {
        super.add(eb);

        if (!this.isValid()) {
            this.remove(eb);
            throw new IllegalArgumentException(eb.getUnit() + " - " + this.fehlerBeschreibung);
        }

        return true;
    }

    /**
	 * Die Methode setzt ggf. (return false) eine entsprechende fehlerBeschreibung
	 * @see Befehlsliste.getFehlerBeschreibung()
	 * @return true, wenn maximal ein langer Befehl (oder eine Sorte multi-langer Befehle) vorhanden ist.
	 */
	public boolean isValid() {
        if (this.countLangeBefehle() > 1) {
            fehlerBeschreibung = " es ist schon ein langer Befehl vorhanden (" + this.ersterLangerBefehl().getBefehlCanonical() + ").";
            return false;
        }
        if (this.containsLangenBefehl() && this.containsMultiLangenBefehl()) {
            fehlerBeschreibung = " es ist schon ein langer Befehl vorhanden.";
            return false;
        }
        // jetzt noch sicherstellen, dass alle multi-langen Befehle zum gleichen Prozessor gehören:
        if (this.countMultiLangeBefehle() > 1) {
            Class<? extends EVABase> multiLangerProzessor = null;
            for(Einzelbefehl eb : this) {
                if (eb.getArt().equals(Art.MULTILANG)) {
                    if (multiLangerProzessor == null) {
                        multiLangerProzessor = eb.getProzessor();
                    } else {
                        if (eb.getProzessor() != multiLangerProzessor) {
                            fehlerBeschreibung = " hat schon einen langen Befehl.";
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

	/**
	 * @return eine Beschreibung des Grundes, warum die Liste nicht gültig ist (wäre), nachdem eine add-Operation versucht wurde - oder null, wenn kein Fehler aufgetreten ist.
	 */
	public String getFehlerBeschreibung() {
		return fehlerBeschreibung;
	}

	public Einzelbefehl last() {
		return this.get(this.size() - 1);
	}

	/**
	 * entfernt lange und multi-lange Befehle aus dieser Liste.
	 * Wird bspw. von der KI verwendet.
	 */
	public void removeLangeBefehle() {
		List<Einzelbefehl> copy = new ArrayList<Einzelbefehl>(this);
		for (Einzelbefehl eb : copy) {
			if (eb.getArt().equals(Art.LANG)) remove(eb);
			if (eb.getArt().equals(Art.MULTILANG)) remove(eb);
		}

        if (!this.isValid()) {
            throw new IllegalArgumentException(this.fehlerBeschreibung);
        }
	}

    public boolean containsLangenBefehl() {
        return (countLangeBefehle() > 0);
    }

    private int countLangeBefehle() {
        int retval = 0;
        boolean inTempEinheit = false;

        for (Einzelbefehl eb : this) {
			// TODO ZAT-Hack - Wenn EVA steht, muss das eher als Exception geworfen werden.
			if (eb.getProzessor() == null) continue;

            if (eb.getProzessor() == TempEinheiten.class) {
				// MACHE TEMP?
				if (eb.getMuster().getVariante() == 0 + EVABase.TEMP) { inTempEinheit = true; continue; }
				// ENDE?
				if (eb.getMuster().getVariante() == 1) { inTempEinheit = false; continue; }
			}
			// in einer TEMP-Definition ist erstmal alles erlaubt.
			if (inTempEinheit) continue;

            if (eb.getArt().equals(Art.LANG)) retval++;
        }
        return retval;
    }

    public boolean containsMultiLangenBefehl() {
        return (countMultiLangeBefehle() > 0);
    }

    private int countMultiLangeBefehle() {
        int retval = 0;
        boolean inTempEinheit = false;

        for (Einzelbefehl eb : this) {
			// TODO ZAT-Hack - Wenn EVA steht, muss das eher als Exception geworfen werden.
			if (eb.getProzessor() == null) continue;
            
			if (eb.getProzessor() == TempEinheiten.class) {
				// MACHE TEMP?
				if (eb.getMuster().getVariante() == 0 + EVABase.TEMP) { inTempEinheit = true; continue; }
				// ENDE?
				if (eb.getMuster().getVariante() == 1) { inTempEinheit = false; continue; }
			}
			// in einer TEMP-Definition ist erstmal alles erlaubt.
			if (inTempEinheit) continue;

            if (eb.getArt().equals(Art.MULTILANG)) retval++;
        }
        return retval;
    }

    private Einzelbefehl ersterLangerBefehl() {
        boolean inTempEinheit = false;

        for (Einzelbefehl eb : this) {
			// TODO ZAT-Hack - Wenn EVA steht, muss das eher als Exception geworfen werden.
			if (eb.getProzessor() == null) continue;

            if (eb.getProzessor() == TempEinheiten.class) {
				// MACHE TEMP?
				if (eb.getMuster().getVariante() == 0 + EVABase.TEMP) { inTempEinheit = true; continue; }
				// ENDE?
				if (eb.getMuster().getVariante() == 1) { inTempEinheit = false; continue; }
			}
			// in einer TEMP-Definition ist erstmal alles erlaubt.
			if (inTempEinheit) continue;

            if (eb.getArt().equals(Art.LANG)) return eb;
        }
        return null;
    }

}
