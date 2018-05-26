package de.x8bit.Fantasya.Host.EVA;

import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;
import de.x8bit.Fantasya.Host.EVA.util.DoppelteAusfuehrungException;

public class Stirb extends EVABase
{
	public Stirb()
	{
		super("stirb", "Selbstmord der Spieler");
		
		addTemplate("");
		for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
        List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();
        BefehlsMuster bm = null;

        bm = new BefehlsMuster(Stirb.class, 0, "^(stirb) (\")?.+(\")?([ ]+(\\/\\/).*)?", "s", Art.KURZ);
        bm.setKeywords("stirb");
        retval.add(bm);
        return retval;
    }
	
	public void DoAction(Region r, String befehl) {
		List<Einzelbefehl> befehle = BefehlsSpeicher.getInstance().get(this.getClass(), r.getCoords());

		for (Einzelbefehl eb : befehle) {
			if (eb.isPerformed()) throw new DoppelteAusfuehrungException(eb.toString());

			// Angaben über die "Akteure" selbst:
			Unit u = eb.getUnit();

			Partei p = Partei.getPartei(u.getOwner());
			if (!eb.getTokens()[1].replace("\"", "").equals(p.getPassword())) {
				new Fehler("Das Passwort für den Selbstmord ist fehlerhaft.", u, u.getCoords());
				eb.setError();
				continue;
			} else {
				new Info("Staatsauflösung wird ausgeführt.", p);
				int anzahl = 0;
				for (Unit maybe : Unit.CACHE.getAll(p.getNummer())) {
					maybe.setPersonen(0);
					anzahl ++;
				}
				new Info(anzahl + " Einheiten wurden entlassen.", p);
				eb.setPerformed();
			}
		}

	}

	
	public void PostAction() { }
	public void PreAction() { }
	public boolean DoAction(Unit u, String[] befehl) { return false; }
    public void DoAction(Einzelbefehl eb) { }

}
