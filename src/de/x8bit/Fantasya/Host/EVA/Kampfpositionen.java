package de.x8bit.Fantasya.Host.EVA;

import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Kampfposition;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;
import de.x8bit.Fantasya.Host.EVA.util.DoppelteAusfuehrungException;
import java.util.HashSet;
import java.util.Set;

public class Kampfpositionen extends EVABase
{
	public Kampfpositionen()
	{
		super("kaempfe", "ändere Kampfpositionen");
		
		addTemplate("");
		for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
        List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();

        // alle Kampfpositionen auflisten
        Set<String> keywords = new HashSet<String>();
        keywords.add("kaempfe");
        keywords.add("kaempfen");
        keywords.add("kämpfe");
        keywords.add("kämpfen");

        StringBuilder regEx = new StringBuilder();
        regEx.append("(");
        for(Kampfposition kp : Kampfposition.values()) {
            if (regEx.length() > 1) regEx.append("|");
            regEx.append("( " + kp.name().toLowerCase() + ")");
            keywords.add(kp.name().toLowerCase());
        }
        regEx.append("|( vorn)");
        keywords.add("vorn");
        regEx.append(")?"); // KAEMPFE ist auch ganz ohne Parameter erlaubt.

        BefehlsMuster bm = new BefehlsMuster(Kampfpositionen.class, 0, "^@?(kaempfe)[n]?" + regEx + "([ ]+(\\/\\/).*)?", "k", Art.KURZ);
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

			if (eb.getTokens().length == 1) {
				// COMMAND KÄMPFE
				new Info(u + " wird " + Kampfposition.Vorne.name().toLowerCase() + " kämpfen.", u);
				u.setKampfposition(Kampfposition.Vorne);
				eb.setPerformed();
				continue;
			}

			Kampfposition kp = Kampfposition.ordinal(eb.getTokens()[1]);
			if (kp == null)	{
				eb.setError();
				new Fehler("Kampfposition '" + eb.getTokens()[1] + "' ist unbekannt.", u);
				continue;
			} else {
				// COMMAND KÄMPFE <position>
				u.setKampfposition(kp);
				new Info(u + " wird " + kp.name().toLowerCase() + " kämpfen.", u);
			}

			eb.setPerformed();
		}

	}
	
	public boolean DoAction(Unit u, String befehl[]) { return false; }

	public void PostAction() { }
	public void PreAction() { }

    @Override
	public void DoAction(Einzelbefehl eb) { }

}
