package de.x8bit.Fantasya.Host.EVA;

import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;
import de.x8bit.Fantasya.Host.EVA.util.DoppelteAusfuehrungException;

public class BewacheAN extends EVABase
{
	public BewacheAN()
	{
		super("bewache", "Bewachen von Regionen aktivieren");
		for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
        List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();

        BefehlsMuster bm = new BefehlsMuster(BewacheAN.class, 0, "^@?(bewache)[n]?([ ]+(\\/\\/).*)?", "b", Art.KURZ);
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

			// darüber schweigen wir...
			if (u.getBewacht()) {
				eb.setPerformed();
				continue;
			}

			if (u.getWaffen() < u.getPersonen()) {
				if (u.getWaffen() == 0)
					new Fehler(u + " fehlt das passende Talent zur Waffe um die Region bewachen zu können", u);
				else
					new Fehler(u + " hat nicht genügend Waffen um die Region zu bewachen.", u, u.getCoords());
				eb.setError();
			} else {
				u.setBewacht(true);
				// Vorsicht: BEWACHE wird nach der Bewegung ausgeführt, daher kann die Region eine andere sein!
				new Info(u + " bewacht jetzt in " + Region.Load(u.getCoords()) + ".", u, u.getCoords());
				eb.setPerformed();
			}
		}
	}
	
	public void PostAction() { }
	public void PreAction() { }
	public boolean DoAction(Unit u, String[] befehl) { return false; }
    public void DoAction(Einzelbefehl eb) { }
}
