package de.x8bit.Fantasya.Host.EVA;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.Soziologie;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author hapebe
 */
public class VolksZaehlung extends EVABase implements NotACommand {

	public final static int MODUS_T0 = 1;
	public final static int MODUS_T1 = 2;

	public static int Modus = MODUS_T0;

	Datensatz daten;

	public VolksZaehlung(int modus) {
		super("Volkszählung " + (Modus==MODUS_T0?"(vorher)":"") + (Modus==MODUS_T1?"(nachher)":""));

		VolksZaehlung.Modus = modus;
		
		this.DoAction(null); // auf diese Art, weil wir noch die statische Variable setzen müssen.

		Soziologie.Vz.put(VolksZaehlung.Modus, this);
	}

	/**
	 * Hier findet die eigentliche Zählung statt. Als "ID" dieser Zählung wird
	 * der aktuelle Wert von VolksZaehlung.Modus verwendet.
	 */
	@Override
	public void DoAction(Einzelbefehl eb) {
		daten = new Datensatz();

		for (Partei p : Partei.PROXY) {
			int nEinheiten = 0;
			int nPersonen = 0;
			int talentTage = 0;
			int einkommen = 0;
			int vermoegen = 0;

			Map<Coords, Integer> bewohner = new HashMap<Coords, Integer>();
            Collection<Unit> units = Unit.CACHE.getAll(p.getNummer());
			if (p.getNummer() == 0) {
				units = Unit.CACHE;
			}
			for (Unit u : units) {
				Coords c = u.getCoords();
                
                nEinheiten ++;
				nPersonen += u.getPersonen();
				for (Skill sk : u.getSkills()) {
					talentTage += sk.getLerntage();
				}
				einkommen += u.getEinkommen();
				vermoegen += u.getItem(Silber.class).getAnzahl();
                
                if (bewohner.get(c) == null) bewohner.put(c, 0);
                bewohner.put(c, bewohner.get(c) + u.getPersonen());
			}

			daten.einheiten.put(p.getNummer(), nEinheiten);
			daten.personen.put(p.getNummer(), nPersonen);
			daten.talentTage.put(p.getNummer(), talentTage);
			daten.einkommen.put(p.getNummer(), einkommen);
			daten.vermoegen.put(p.getNummer(), vermoegen);
            
            // Mittelpunkt / "Schwerpunkt" des Volkes bestimmen:
            double summeX = 0d; double summeY = 0d;
            // new Debug("Schwerpunkt von " + p);
            for (Coords c : bewohner.keySet()) {
                // new Debug(bewohner.get(c) + " Bewohner bei " + c);
                summeX += (double)(bewohner.get(c) * c.getX());
                summeY += (double)(bewohner.get(c) * c.getY());
            }
            double n = (double)nPersonen;
            // new Debug("sx=" + summeX + "; sy=" + summeY + "; N=" + n);
            double mx = summeX / n;
            double my = summeY / n;
            Coords mc = new Coords((int)Math.round(mx), (int)Math.round(my), 1);
            daten.setSchwerpunkt(p, mc);
            // new Debug("mx=" + mx + "; my=" + my + "; c=" + daten.getSchwerpunkt(p));
		}
	}

	@Override
	public void PreAction() { }

	@Override
	public void PostAction() { }

	@Override
	public boolean DoAction(Unit u, String[] befehl) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void DoAction(Region r, String befehl) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public Datensatz getDaten() {
		return daten;
	}


	public class Datensatz {
		final Map<Integer, Integer> einheiten = new HashMap<Integer, Integer>();
		final Map<Integer, Integer> personen = new HashMap<Integer, Integer>();
		final Map<Integer, Integer> talentTage = new HashMap<Integer, Integer>();
		final Map<Integer, Integer> einkommen = new HashMap<Integer, Integer>();
		final Map<Integer, Integer> vermoegen = new HashMap<Integer, Integer>();
		final Map<Integer, Coords> schwerpunkte = new HashMap<Integer, Coords>();
        
        public Coords getSchwerpunkt(Partei p) {
            return schwerpunkte.get(p.getNummer());
        }

        public void setSchwerpunkt(Partei p, Coords schwerpunkt) {
            schwerpunkte.put(p.getNummer(), schwerpunkt);
        }

		public Map<Integer, Integer> getEinheiten() {
			return einheiten;
		}

		public Map<Integer, Integer> getEinkommen() {
			return einkommen;
		}

		public Map<Integer, Integer> getPersonen() {
			return personen;
		}

		public Map<Integer, Integer> getTalentTage() {
			return talentTage;
		}

		public Map<Integer, Integer> getVermoegen() {
			return vermoegen;
		}


	}
}
