package de.x8bit.Fantasya.Host.EVA;

import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Allianz.AllianzOption;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Skills.Unterhaltung;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.EVA.util.AnzahlHint;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;
import de.x8bit.Fantasya.Host.EVA.util.DoppelteAusfuehrungException;
import java.util.HashMap;
import java.util.Map;

public class Unterhalten extends EVABase {
    
	public static final Map<Integer, Integer> ParteiErtrag = new HashMap<Integer, Integer>();
    
	public Unterhalten() {
		super("unterhalte", "Unterhalten der Bevölkerung");
		
		addTemplate("");
		for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
        List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();
        BefehlsMuster bm = null;

        bm = new BefehlsMuster(Unterhalten.class, 0, "^(unterhalte)[n]?([ ]+(\\/\\/).*)?", "u", Art.LANG);
        bm.setKeywords("unterhalte", "unterhalten");
        retval.add(bm);

		bm = new BefehlsMuster(Unterhalten.class, 11, "^(unterhalte)[n]?( [0-9]+)([ ]+(\\/\\/).*)?", "u", Art.LANG);
		bm.addHint(new AnzahlHint(1));
        bm.setKeywords("unterhalte", "unterhalten");
        retval.add(bm);

        return retval;
    }

    @Override
	public boolean DoAction(Unit u, String[] befehl) { return false; }
	public void PostAction() { }
	public void PreAction() {
		// die Mengen-Marker aller Einheiten zurücksetzen:
        for (Unit u : Unit.CACHE) {
			u.wants = 0;
		}
	}
	
	public void DoAction(Region r, String befehl) {
		List<Einzelbefehl> befehle = BefehlsSpeicher.getInstance().get(this.getClass(), r.getCoords());

        int wants = 0;
		// alle Unterhalter sammeln
		for (Einzelbefehl eb : befehle) {
			if (eb.isPerformed()) throw new DoppelteAusfuehrungException(eb.toString());

			// Angaben über die "Akteure" selbst:
			Unit u = eb.getUnit();

			u.wants = 0; // reset
            if (u.Talentwert(Unterhaltung.class) > 0) {
                if (!r.istBewacht(u, AllianzOption.Unterhalte)) {
                    int weWant = Integer.MAX_VALUE;
                    if (eb.getVariante() == 11) weWant = eb.getAnzahl(); // Befehlsvariante mit Wunschertrags-Angabe

					// jetzt wird der Wunsch ggf. aufs Können heruntergeschraubt:
					weWant = Math.min(weWant, u.Talentwert(Unterhaltung.class) * u.getPersonen() * 20);

                    wants += weWant;
                    u.wants = weWant;	// falls der Befehl doppelt auftaucht ^^
                } else {
                    new Fehler(u + " - die Region wird von Nicht-Alliierten Einheiten bewacht.", u, u.getCoords());
                    eb.setError();
                }
            } else {
                new Fehler(u + " hat keine Idee wie die Bevölkerung unterhalten werden soll.", u, u.getCoords());
                eb.setError();
            }
            eb.setPerformed();
        }
		

		int unterhalt = r.getSilber() / 20;	// nur 1/20-igstel !!
		double faktor = (wants > unterhalt ? (double) unterhalt / (double) wants : 1.0);

        for(Unit u : r.getUnits()) {
			if (u.wants == 0) continue; // die wollen ja gar nicht unterhalten!

			// new Info("In " + r + " ($" + r.getSilber() + ") kann max. für " + unterhalt + " unterhalten werden. Der U-Faktor ist " + faktor, u);

            
            // wants genauer berechnen ...
            wants = (int) ((double) u.wants * faktor);

            // ... wenn dabei u.wants == 0 entsteht auf 1 setzen ... Einheit wollte ursprünglich was haben
            // Java rundet ja erfolgreich nach unten ab
            if (wants == 0) wants = 1;

            // jetzt noch mit verfügbarem Vermögen abgleichen
            if (wants > unterhalt) wants = unterhalt;

            if (u.wants > wants) {
                new Info(u + " kann nur " + wants + " statt " + u.wants + " Silber mit Unterhaltung verdienen.", u);
            } else {
                new Info(u + " verdient " + wants + " Silber mit Unterhaltung.", u);
            }
            unterhalt -= wants;
            u.getItem(Silber.class).setAnzahl(u.getItem(Silber.class).getAnzahl() + wants);
            r.setSilber(r.getSilber() - wants);
            u.setEinkommen(u.getEinkommen() + wants);
            
            // Ertrag durch Unterhaltung aufzeichnen (kommt später ggf. in den Report)
            if (!ParteiErtrag.containsKey(u.getOwner())) ParteiErtrag.put(u.getOwner(), 0);
            ParteiErtrag.put(u.getOwner(), ParteiErtrag.get(u.getOwner()) + wants);
		}
	}

    @Override
    public void DoAction(Einzelbefehl eb) { }

}
