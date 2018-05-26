package de.x8bit.Fantasya.Host.EVA;

import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.DoppelteAusfuehrungException;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;

public class Website extends EVABase
{
	
	public Website()
	{
		super("website", "Ändere Werbung (Website) der Spieler");
		
		addTemplate("");
		for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
        List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();
        BefehlsMuster bm = null;

        bm = new BefehlsMuster(Website.class, 0, "^@?(website) .+([ ]+(\\/\\/).*)?", "w", Art.KURZ);
        bm.setKeywords("website");
        retval.add(bm);

        bm = new BefehlsMuster(Website.class, 0, "^@?(homepage) .+([ ]+(\\/\\/).*)?", "h", Art.KURZ);
        bm.setKeywords("homepage");
        retval.add(bm);

        return retval;
    }

	
	public void PostAction() { }
	public void PreAction() { }

	@Override
	public void DoAction(Region r, String befehl) {
		List<Einzelbefehl> befehle = BefehlsSpeicher.getInstance().get(this.getClass(), r.getCoords());

		for (Einzelbefehl eb : befehle) {
			if (eb.isPerformed()) throw new DoppelteAusfuehrungException(eb.toString());

			Unit u = eb.getUnit();
            Partei p = Partei.getPartei(u.getOwner());

            if (eb.getTokens().length < 2) {
                new Info("Keine Website angeben, lösche die alte.", u, u.getCoords());
                p.setWebsite("");
            } else {
                if (eb.getTokens()[1].equalsIgnoreCase("nicht")) {
                    new Info("Lösche die alte Website.", u, u.getCoords());
                    p.setWebsite("");
                } else {
                    p.setWebsite(eb.getTokens()[1]);
                    new Info("Website auf '" + eb.getTokens()[1] + "' geändert, keine Überprüfung des Protokolls", u, u.getCoords());
                }
            }

            eb.setPerformed();
        }
    }

    @Override
    public boolean DoAction(Unit u, String[] befehl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void DoAction(Einzelbefehl eb) { }

}
