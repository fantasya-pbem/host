package de.x8bit.Fantasya.Host.EVA;

import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;

public class Kommentare extends EVABase
{
	public final static int PERMANENT = 0;
	public final static int VOID = 1;

	public Kommentare()	{
		super("//", "Kommentare werden durchgereicht...");

        addTemplate("");
        for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
		BefehlsMuster bm = null;
        List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();

        bm = new BefehlsMuster(Kommentare.class, Kommentare.PERMANENT, "^(\\/\\/).*", "/", Art.KURZ);
		bm.setKeep(true); // "lange" Kommentare - kommen in die nächste Befehlsvorlage
		retval.add(bm);
		
        retval.add(new BefehlsMuster(Kommentare.class, Kommentare.VOID, "^;.*", ";", Art.KURZ));

		return retval;
	}

    @Override
	public void PreAction() {
        // Kommentare generell als "erledigt" markieren.
        for (Einzelbefehl eb : BefehlsSpeicher.getInstance().getAll(this.getClass())) {
            eb.setPerformed();
        }
    }

    @Override
	public void PostAction() { }
	
    @Override
    public boolean DoAction(Unit u, String[] befehl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void DoAction(Region r, String befehl) {
        // nothing to do - tralla!
    }
	
    @Override
	public void DoAction(Einzelbefehl eb) { }

}
