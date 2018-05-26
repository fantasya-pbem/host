package de.x8bit.Fantasya.Host.EVA;

import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;
import de.x8bit.Fantasya.Host.EVA.util.DoppelteAusfuehrungException;

public class BewacheAUS extends EVABase
{
	public BewacheAUS()
	{
		super("bewache", "Bewachen von Regionen deaktivieren");
		
		addTemplate("");
		for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
        List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();

        BefehlsMuster bm = new BefehlsMuster(BewacheAUS.class, 0, "^@?(bewache)[n]? (nicht)([ ]+(\\/\\/).*)?", "b", Art.KURZ);
        bm.setKeywords("bewache", "bewachen", "nicht");
        retval.add(bm);
        
        return retval;
    }
	
	public void DoAction(Region r, String befehl) {
		List<Einzelbefehl> befehle = BefehlsSpeicher.getInstance().get(this.getClass(), r.getCoords());

		for (Einzelbefehl eb : befehle) {
			if (eb.isPerformed()) throw new DoppelteAusfuehrungException(eb.toString());

			// Angaben über die "Akteure" selbst:
			Unit u = eb.getUnit();

			// COMMAND BEWACHE NICHT
			if (u.getBewacht() == false) {
				new Fehler(u + " hat bisher gar nichts bewacht.", u, u.getCoords());
				eb.setError();
				continue;
			}

			u.setBewacht(false);
			new Info(u + " bewacht die Region " + r + " NICHT mehr.", u, u.getCoords());
			eb.setPerformed();
		}
	}

	public void PostAction() { }
	public void PreAction() { }
	public boolean DoAction(Unit u, String[] befehl) { return false; }
    public void DoAction(Einzelbefehl eb) { }
}
