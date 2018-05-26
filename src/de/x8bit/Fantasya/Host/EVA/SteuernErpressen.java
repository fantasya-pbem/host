package de.x8bit.Fantasya.Host.EVA;

import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Allianz.AllianzOption;
import de.x8bit.Fantasya.Atlantis.Buildings.Steuerturm;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Skills.Steuereintreiben;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.EVA.util.AnzahlHint;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;
import de.x8bit.Fantasya.Host.EVA.util.DoppelteAusfuehrungException;
import java.util.HashMap;
import java.util.Map;

public class SteuernErpressen extends EVABase
{
	public static final Map<Integer, Integer> ParteiErtrag = new HashMap<Integer, Integer>();
    
    public SteuernErpressen() {
		super("treibe", "Steuern von den Bauern erpressen");
		
		addTemplate("");
		for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
        List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();
        BefehlsMuster bm = null;

        bm = new BefehlsMuster(SteuernErpressen.class, 0, "^(treibe)[n]?([ ]+(\\/\\/).*)?", "t", Art.LANG);
        bm.setKeywords("treibe", "treiben");
        retval.add(bm);

		bm = new BefehlsMuster(SteuernErpressen.class, 11, "^(treibe)[n]?( [0-9]+)([ ]+(\\/\\/).*)?", "t", Art.LANG);
		bm.addHint(new AnzahlHint(1));
        bm.setKeywords("treibe", "treiben");
        retval.add(bm);

        return retval;
    }
	
    @Override
	public void DoAction(Region r, String befehl) {
		List<Einzelbefehl> befehle = BefehlsSpeicher.getInstance().get(this.getClass(), r.getCoords());

		for (Unit u : r.getUnits()) u.wants = 0; // reset für alle, sonst kann aus früheren Phasen was durchkommen.

		int wants = 0;
		for (Einzelbefehl eb : befehle) {
			if (eb.isPerformed()) throw new DoppelteAusfuehrungException(eb.toString());

			// Angaben über die "Akteure" selbst:
			Unit u = eb.getUnit();

			if (u.getGebaeude() > 0) {
				Building building = Building.getBuilding(u.getGebaeude());
				if (building.getClass() == Steuerturm.class) continue; // Einheit steht in einem Steuerturm
			}

			if (u.Talentwert(Steuereintreiben.class) > 0) {
				if (u.getWaffen() == 0)	{
					new Fehler(u + " hat keine Waffen zum Erpressen der Bevölkerung.", u);
					eb.setError();
					continue;
				}
				if (!r.istBewacht(u, AllianzOption.Treiben)) {
					int wunsch = Integer.MAX_VALUE;
					if (eb.getVariante() == 11) wunsch = eb.getAnzahl(); // Variante mit Angabe des Wunsch-Budgets

					// jetzt wird der Wunsch ggf. aufs Können heruntergeschraubt:
					wunsch = Math.min(wunsch, u.Talentwert(Steuereintreiben.class) * u.getWaffen() * 20);

					wants += wunsch;
					u.wants = wunsch;	// falls der Befehl doppelt auftaucht ^^
				} else {
					new Fehler(u + " - die Region wird von Nicht-Alliierten Einheiten bewacht.", u);
					eb.setError();
					continue;
				}
			} else {
				new Fehler(u + " kann die Bauern nicht überzeugen, Steuern schuldig zu sein.", u); // an die Bundesregierung wenden !!
				eb.setError();
				continue;
			}

			eb.setPerformed();
		}


		int steuern = r.getSilber();	// ab 1/20-igstel wird es Steuern erpressen ^^

		double faktor = (wants > steuern ? (double) steuern / (double) wants : 1.0);
		for(Unit u : r.getUnits()) {
			if (u.wants != 0) {
				// wants genauer berechnen ... 
				wants = (int) ((double) u.wants * faktor);
				// ... wenn dabei u.wants == 0 entsteht auf 1 setzen ... Einheit wollte ursprünglich was haben
				// Java rundet ja erfolgreich nach unten ab
				if (wants == 0) wants = 1;
				
				// jetzt noch mit vorhandenem Silber abgleichen
				if (wants > steuern) wants = steuern;
				
				if (u.wants > wants) {
					new Info(u + " kann nur " + wants + " statt " + u.wants + " Silber an Steuern eintreiben.", u);
				} else {
					new Info(u + " treibt " + wants + " Silber an Steuern ein.", u);
				}
				
				steuern -= wants;
				Item it = u.getItem(Silber.class);
				it.setAnzahl(it.getAnzahl() + wants);
				r.setSilber(r.getSilber() - wants);
				u.setEinkommen(u.getEinkommen() + wants);
                
                // Ertrag durch Steuereintreiben aufzeichnen (kommt später ggf. in den Report)
                if (!ParteiErtrag.containsKey(u.getOwner())) ParteiErtrag.put(u.getOwner(), 0);
                ParteiErtrag.put(u.getOwner(), ParteiErtrag.get(u.getOwner()) + wants);
			}
		}
	}

    @Override
    public void DoAction(Einzelbefehl eb) { }
    @Override
	public boolean DoAction(Unit u, String[] befehl) { return false; }
    @Override
	public void PostAction() { }
    @Override
	public void PreAction() { }
	
}
