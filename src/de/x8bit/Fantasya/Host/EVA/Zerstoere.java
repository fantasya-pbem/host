package de.x8bit.Fantasya.Host.EVA;

import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Richtung;
import de.x8bit.Fantasya.Atlantis.Ship;
import de.x8bit.Fantasya.Atlantis.Skills.Wahrnehmung;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;
import de.x8bit.Fantasya.Host.EVA.util.DoppelteAusfuehrungException;
import java.util.HashSet;
import java.util.Set;

public class Zerstoere extends EVABase
{
	public Zerstoere() {
		super("zerstoere", "Zerstörungen von Gebäuden");
		
		addTemplate("");
		for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
        BefehlsMuster bm = null;
        List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();

        bm = new BefehlsMuster(Zerstoere.class, 1, "^(zerstoere)[n]?([ ]+(\\/\\/).*)?", "z", Art.LANG);
        Set<String> keywords = new HashSet<String>();
        keywords.add("zerstoere");
        keywords.add("zerstoeren");
        keywords.add("zerstöre");
        keywords.add("zerstören");
        bm.setKeywords(keywords);
        retval.add(bm);

        bm = new BefehlsMuster(Zerstoere.class, 2, "^(zerstoere)[n]? (strasse) (nw|no|o|so|sw|w)([ ]+(\\/\\/).*)?", "z", Art.LANG);
        keywords.add("zerstoere");
        keywords.add("zerstoeren");
        keywords.add("zerstöre");
        keywords.add("zerstören");
        keywords.add("strasse");
        bm.setKeywords(keywords);
        retval.add(bm);

        return retval;
    }
	
	public void DoAction(Region r, String befehl) {
		List<Einzelbefehl> befehle = BefehlsSpeicher.getInstance().get(this.getClass(), r.getCoords());

		for (Einzelbefehl eb : befehle) {
			if (eb.isPerformed()) throw new DoppelteAusfuehrungException(eb.toString());

			// Angaben über die "Akteure" selbst:
			Unit u = eb.getUnit();
            
            // ZERSTÖRE
            if (eb.getVariante() == 1) {
                // testen wovon wir die Befehlsgewalt haben
                Building b = null;
                if (u.getGebaeude() != 0) b = Building.getBuilding(u.getGebaeude());
                Ship s = null;
                if (u.getSchiff() != 0) s = Ship.Load(u.getSchiff());

                if ((b == null) && (s == null)) {
                    new Fehler("ZERSTÖRE - Wir sind weder in einem Gebäude noch an Bord eines Schiffs.", u, u.getCoords());
                    eb.setError();
                    continue;
                }
                
                if (b != null) {
                    if (b.getOwner() != u.getNummer()) {
                        new Fehler("ZERSTÖRE - Wir haben nicht das Kommando über " + b + ".", u, u.getCoords());
                        eb.setError();
                        continue;
                    }
                    b.Zerstoere(u);
					eb.setPerformed();
                }

                if (s != null) {
                    if (s.getOwner() != u.getNummer()) {
                        new Fehler("ZERSTÖRE - Wir haben nicht das Kommando über " + s + ".", u, u.getCoords());
                        eb.setError();
                        continue;
                    }
                    s.Zerstoere(u);
					eb.setPerformed();
                }
            }

            // ZERSTÖRE STRASSE <richtung>
            if (eb.getVariante() == 2) {
				Richtung richtung = Richtung.getRichtung(eb.getTokens()[2]);

				if (richtung == null) {
					new Fehler("Welche Straße soll zerstört werden? (Richtung '" + eb.getTokens()[2] + "'?)", u);
					eb.setError(); continue;
				}

				int strassenSteine = r.getStrassensteine(richtung);
				if (strassenSteine == 0) {
					new Fehler("In Richtung " + richtung.toString() + " gibt es hier keine Straße.", u);
					eb.setError(); continue;
				}

				// gibt es "störende" Bewacher (Nicht-Alliierte)?
				Set<Partei> bewacher = r.getBewacherParteien(u);
				if (!bewacher.isEmpty()) {
					eb.setError();
					new Fehler (u + " kann nichts zerstören: Die Bewacher " + r.getBewacherPhrase(bewacher) + " lassen das nicht zu.", u);
					continue;

					// TODO Meldungen auch an die Bewacher geben.
				}

				// nothing's gonna stop us now:
				int zerstoerung = u.getPersonen();
				if (zerstoerung > strassenSteine) zerstoerung = strassenSteine;

				r.setStrassensteine(richtung, strassenSteine - zerstoerung);
				new Info(u + " zerstört die Straße in Richtung " + richtung.toString() + " um " + zerstoerung + " Punkte.", u);
				eb.setPerformed();

				// Anwesende benachrichtigen, sofern sie mit Wahrnehmung gesegnet sind
				for (Partei p : r.anwesendeParteien()) {
					if (p.getNummer() == u.getOwner()) continue;
					for (Unit beobachter : r.getUnits(p.getNummer())) {
						if (beobachter.Talentwert(Wahrnehmung.class) > 0) {
							new Info(u + " zerstört die Straße in Richtung " + richtung.toString() + " um " + zerstoerung + " Punkte.", beobachter);
							break;
						}
					}
				}
            }


            new SysMsg("TODO Gebäude nicht sofort zerstören - Talentwert wird nicht beachtet");
        }
    }


	public void PostAction() { }
	public void PreAction() { }
    public void DoAction(Einzelbefehl eb) { }
	public boolean DoAction(Unit u, String[] befehl) { return false; }
}
