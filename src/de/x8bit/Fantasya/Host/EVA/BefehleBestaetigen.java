package de.x8bit.Fantasya.Host.EVA;

import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.EVA.Lehren.LehrenRecord;
import de.x8bit.Fantasya.Host.EVA.util.AnzahlHint;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;
import de.x8bit.Fantasya.Host.EVA.util.DoppelteAusfuehrungException;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;

/**
 *
 * @author hapebe
 */
public class BefehleBestaetigen extends EVABase {

	public static final int BESTAETIGT_BIS = 1;
	
	public BefehleBestaetigen() {
		super("bestätigt", "Befehle fertig bestätigen");
		
		addTemplate("");
		for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
		String comment = "([ ]+(\\/\\/).*)?";
		
        BefehlsMuster bm = null;
        List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();

        bm = new BefehlsMuster(BefehleBestaetigen.class, BESTAETIGT_BIS, "^@?(bestätigt|bestaetigt) [1-9]{1}[0-9]{0,5}" + comment, "b", Art.KURZ);
        bm.addHint(new AnzahlHint(1));
        bm.setKeywords("bestätigt", "bestaetigt");
        retval.add(bm);

        bm = new BefehlsMuster(BefehleBestaetigen.class, BESTAETIGT_BIS, "^@?(bestätigt|bestaetigt) bis [1-9]{1}[0-9]{0,5}" + comment, "b", Art.KURZ);
        bm.addHint(new AnzahlHint(2));
        bm.setKeywords("bestätigt", "bestaetigt", "bis");
        retval.add(bm);

       
        return retval;
    }
	
	@Override
	public void DoAction(Einzelbefehl eb) {
		if (eb.isPerformed()) throw new DoppelteAusfuehrungException(eb.toString());
		
		eb.setKeep(true);
		
		if (eb.getVariante() == BefehleBestaetigen.BESTAETIGT_BIS) {
			if (eb.getAnzahl() > GameRules.getRunde()) {
				eb.getUnit().setTag("ejcOrdersConfirmed", 1);
			} else {
				// der gilt also nicht mehr:
				if (GameRules.getRunde() > eb.getAnzahl()) {
					new Fehler("Befehls-Bestätigung war schon Anfang dieses Monats abgelaufen (" + eb.getBefehlCanonical() + ").", eb.getUnit());
					eb.setKeep(false);
				} else {
					new Info("Befehls-Bestätigung ist abgelaufen (" + eb.getBefehlCanonical() + ").", eb.getUnit());
				}
			}

			eb.setPerformed();
		}
	}
	
	/**
	 * Alle Einheiten mit einem LEHRE-Befehl werden bestätigt, wenn
	 * ALLE ihre Schülereinheiten ebenfalls bestätigt sind.
	 */
	public static void LehrerBestaetigen() {
        // alle "LehrenRecords" holen:
        for (int lehrerId : Lehren.Unterricht.keySet()) {
			Unit lehrer = Unit.Load(lehrerId);
			if (lehrer == null) continue; // gibt es nicht mehr!
			
            List<LehrenRecord> recs = Lehren.Unterricht.get(lehrerId);
			if (recs.isEmpty()) continue; // lehrt niemanden - das kann nicht so gewollt sein?
			
            boolean bestaetigen = true;
			for (LehrenRecord lr : recs) {
                int schuelerId = lr.getSchueler().getNummer();
				Unit u = Unit.Load(schuelerId);
				
				// gibt es die Einheit überhaupt noch?
				if (u == null) { bestaetigen = false; break; }
				
				// und hat sie eine Bestätigung?
				if (u.getTag("ejcOrdersConfirmed") == null) { bestaetigen = false; break; }
            }
			
			if (bestaetigen) {
				lehrer.setTag("ejcOrdersConfirmed", 1);
			}
        }
	}
	
	
	@Override
	public boolean DoAction(Unit u, String[] befehl) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void DoAction(Region r, String befehl) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void PreAction() { }

	@Override
	public void PostAction() {	}
	
}
