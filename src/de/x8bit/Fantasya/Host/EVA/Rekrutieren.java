package de.x8bit.Fantasya.Host.EVA;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.EVA.util.AnzahlHint;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;

public class Rekrutieren extends EVABase
{
	public Rekrutieren()
	{
		super("rekrutiere", "Rekrutieren von Personen");
		
		addTemplate("");
		for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
		BefehlsMuster bm = null;
        List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();

        bm = new BefehlsMuster(Rekrutieren.class, 0, "^@?(rekrutiere)[n]? [1-9][0-9]*([ ]+(\\/\\/).*)?", "r", Art.KURZ);
		bm.addHint(new AnzahlHint(1));
		retval.add(bm);
		
        return retval;
    }
	
	public boolean DoAction(Unit u, String[] befehl) { return false; }
	public void PreAction() { }
	public void PostAction() { }
	
	public void DoAction(Region r, String befehl) {
		int wants = 0;	// die Summe aller benötigten Rekruten für diese Region
		
		List<Einzelbefehl> befehle = BefehlsSpeicher.getInstance().get(this.getClass(), r.getCoords());

		for (Einzelbefehl eb : befehle) {
            Unit u = eb.getUnit();
			u.wants = 0; // reset
			Partei p = Partei.getPartei(u.getOwner());

            int anzahl = eb.getAnzahl();
            // new Debug("Rekrutieren: " + u + " will " + anzahl + " Personen.");


            // gibt es "störende" Bewacher (Nicht-Alliierte)?
            Set<Partei> bewacher = r.getBewacherParteien(u);
            // Gibt es jetzt noch hartnäckige Wächter?
            if (!bewacher.isEmpty()) {
                eb.setError();
                new Fehler (u + " kann niemanden rekrutieren: Die Bewacher " + r.getBewacherPhrase(bewacher) + " lassen das nicht zu.", u);
                continue;

				// TODO Meldungen auch an die Bewacher geben.
            }

			if (!p.isMonster()) {
				// besteht die Einheit aus Migranten?
				if (!p.getRasse().equalsIgnoreCase(u.getRasse())) {
					eb.setError();
					new Fehler ("Einheit " + u + " kann niemanden rekrutieren: Sie ist nur zu Gast bei uns.", u);
					continue;
				}
			}

            // Anzahl auf das Verfügbare Silber anpassen
            int silber = u.getItem(Silber.class).getAnzahl();
            if (silber < u.getRekrutierungsKosten() * anzahl) {
                new Fehler (u + " hat nicht genügend Silber zum Rekrutieren von " + anzahl + " Personen.", u, u.getCoords());
                anzahl = silber / u.getRekrutierungsKosten();
            }

            wants += anzahl; // und Wunsch merken
            u.wants += anzahl; // für mehrere Rekrutiere Befehle
		}
		
		// und nun rekrutieren
		int rekruten = r.Rekruten();
		double faktor = (wants > rekruten ? (double) rekruten / (double) wants : 1.0);


		for (Einzelbefehl eb : befehle) {
            Unit u = eb.getUnit();
        
			// jetzt testen ob die Einheiten was haben will
			if (u.wants > 0) {
				// wants genauer berechnen ... 
				wants = (int) ((double) u.wants * faktor);
				
				// ... wenn dabei u.wants == 0 entsteht auf 1 setzen ... Einheit wollte ursprünglich was haben
				// Java rundet ja erfolgreich nach unten ab
				if (wants <= 0) wants = 1;
				
				// jetzt noch mit vorhanden Rekruten abgleichen
				if (wants > rekruten) wants = rekruten;
				
				// Info an den Spieler
			
				if (u.Rekrutieren(wants)) {
					if (wants < u.wants) {
						new Info(u + " kann nur " + wants + " statt " + u.wants + " Personen rekrutieren.", u);
					} else if (wants > 0) {
						new Info(u + " rekrutiert " + wants + " Personen.", u);
					}
				} else {
					new Fehler(u + " kann niemand rekrutieren.", u);
				}

				// Rekruten berechen
				rekruten -= wants;
			}

            eb.setPerformed();
		}
	}


    @Override
    public void DoAction(Einzelbefehl eb) { }

}
