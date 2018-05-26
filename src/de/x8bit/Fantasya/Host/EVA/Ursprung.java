package de.x8bit.Fantasya.Host.EVA;

import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.DoppelteAusfuehrungException;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;

public class Ursprung extends EVABase
{
	
	public Ursprung()
	{
		super("ursprung", "Verschiebe Ursprung der Völker");
		
		addTemplate("");
		for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
        List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();
        BefehlsMuster bm = null;

        bm = new BefehlsMuster(Ursprung.class, 0, "^@?(ursprung) [-]?[0-9]+ [-]?[0-9]+([ ]+(\\/\\/).*)?", "u", Art.KURZ);
        bm.setKeywords("ursprung");
        retval.add(bm);

        return retval;
    }
	
    @Override
	public void DoAction(Region r, String befehl) {
		List<Einzelbefehl> befehle = BefehlsSpeicher.getInstance().get(this.getClass(), r.getCoords());

		for (Einzelbefehl eb : befehle) {
			if (eb.isPerformed()) throw new DoppelteAusfuehrungException(eb.toString());

			Unit u = eb.getUnit();
            Partei p = Partei.getPartei(u.getOwner());
            int dx = 0;
            int dy = 0;
            try	{
                dx = Integer.parseInt(eb.getTokens()[1]);
                dy = Integer.parseInt(eb.getTokens()[2]);
                Coords c = p.getUrsprung();

                p.setUrsprung(new Coords(c.getX() + dx, c.getY() + dy, c.getWelt()));

                new Info("Ursprung wurde um X:" + dx + " / Y:" + dy + " verschoben.", u);
            } catch(Exception ex) {
                eb.setError();
                new Fehler("'" + eb.getBefehlCanonical() + "' - die Koordinaten zur Verschiebung sind fehlerhaft (keine Zahlen?)", u);
            }
            eb.setPerformed();
        }
    }

    @Override
    public boolean DoAction(Unit u, String[] befehl) { return false; }
    @Override
    public void DoAction(Einzelbefehl eb) { }
    @Override
	public void PostAction() { }
    @Override
	public void PreAction() { }


}
