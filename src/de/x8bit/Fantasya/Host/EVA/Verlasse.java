package de.x8bit.Fantasya.Host.EVA;


import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.DoppelteAusfuehrungException;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;
import java.util.HashSet;
import java.util.Set;

public class Verlasse extends EVABase
{
	public Verlasse()
	{
		super("verlasse", "Verlassen von Schiffen und Gebäuden");
		
		addTemplate("");
		for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
        List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();
        BefehlsMuster bm = null;

        bm = new BefehlsMuster(Verlasse.class, 0, "^@?(verlasse)[n]?(( schiff)|( gebaeude)|( gebäude))?([ ]+(\\/\\/).*)?", "v", Art.KURZ);
        Set<String> keywords = new HashSet<String>();
        keywords.add("verlasse");
        keywords.add("verlassen");
        keywords.add("schiff");
        keywords.add("gebaeude");
        keywords.add("gebäude");
        retval.add(bm);
        return retval;
    }

	@Override
	public void DoAction(Region r, String befehl) {
		List<Einzelbefehl> befehle = BefehlsSpeicher.getInstance().get(this.getClass(), r.getCoords());

		for (Einzelbefehl eb : befehle) {
			if (eb.isPerformed()) throw new DoppelteAusfuehrungException(eb.toString());

			Unit u = eb.getUnit();

			// testen wovon wir die Befehlsgewalt für irgendwas haben
			if (u.getSchiff() == 0 && u.getGebaeude() == 0) {
				eb.setError();
				new Fehler(u + " - wir sind weder auf einem Schiff noch in einem Gebäude!", u, u.getCoords());
				continue;
			}

			// Auszug
			u.Leave();

			eb.setPerformed();
		}
	}
	public void PostAction() { }
	public void PreAction() { }
	public boolean DoAction(Unit u, String[] befehl) { return false; }
    public void DoAction(Einzelbefehl eb) { }

	
}
