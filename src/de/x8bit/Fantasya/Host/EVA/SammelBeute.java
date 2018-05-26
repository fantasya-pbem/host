package de.x8bit.Fantasya.Host.EVA;

import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import java.util.ArrayList;
import java.util.List;

/**
 * Behandelt die Varianten des Befehls SAMMEL ... BEUTE ...
 * @author hb
 */
public class SammelBeute extends EVABase {
    public final static int VAR_ALLES = 1;
    public final static int VAR_TRAGBAR = 2;
    public final static int VAR_NICHTS = 3;

    public SammelBeute()	{
		super("sammel", "Beutegier wird zum Ausdruck gebracht");

		addTemplate("");
		for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
        BefehlsMuster bm = null;
        List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();

        bm = new BefehlsMuster(SammelBeute.class, VAR_NICHTS, "^@?((sammel)|(sammeln)|(sammle))(( keine beute)|( beute nicht)|( nicht beute))([ ]+(\\/\\/).*)?", "s", Art.KURZ);
        bm.setKeywords("sammel", "sammle", "beute", "nicht", "keine");
        retval.add(bm);

        bm = new BefehlsMuster(SammelBeute.class, VAR_TRAGBAR, "^@?((sammel)|(sammeln)|(sammle))(( tragbare beute)|( beute massvoll))([ ]+(\\/\\/).*)?", "s", Art.KURZ);
        bm.setKeywords("sammel", "sammle", "beute", "tragbare", "massvoll");
        retval.add(bm);

        bm = new BefehlsMuster(SammelBeute.class, VAR_ALLES, "^@?((sammel)|(sammeln)|(sammle))(( alle beute)|( beute alles)|( beute))([ ]+(\\/\\/).*)?", "s", Art.KURZ);
        bm.setKeywords("sammel", "sammle", "beute", "nicht", "keine");
        retval.add(bm);

        return retval;
    }


    @Override
    public void PreAction() { }

    @Override
    public boolean DoAction(Unit u, String[] befehl) { return false;  }

    @Override
    public void DoAction(Region r, String befehl) { }

    @Override
    public void DoAction(Einzelbefehl eb) {
        Unit u = eb.getUnit();
        if (eb.getVariante() == VAR_NICHTS) {
            Kriege.BEUTE_MODI.put(u, Kriege.BEUTE_NICHTS);
        } else if (eb.getVariante() == VAR_ALLES) {
            Kriege.BEUTE_MODI.put(u, Kriege.BEUTE_ALLES);
        } else if (eb.getVariante() == VAR_TRAGBAR) {
            Kriege.BEUTE_MODI.put(u, Kriege.BEUTE_TRAGBAR);
        } else {
            throw new IllegalStateException("Unbekannter Modus für SAMMEL BEUTE: " + eb.getVariante());
        }
        eb.setPerformed();
    }

    @Override
    public void PostAction() { }

}
