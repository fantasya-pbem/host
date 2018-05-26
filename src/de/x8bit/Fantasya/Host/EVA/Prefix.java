package de.x8bit.Fantasya.Host.EVA;

import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;
import de.x8bit.Fantasya.Host.EVA.util.DoppelteAusfuehrungException;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;

public class Prefix extends EVABase
{
	
	public Prefix()
	{
		super("praefix", "Präfix festlegen");
		
		addTemplate("");
		for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
        List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();
        BefehlsMuster bm = null;

        bm = new BefehlsMuster(Prefix.class, 0, "^@?((praefix)|(präfix)|(prefix)) (\")?(.*)(\")?([ ]+(\\/\\/).*)?", "p", Art.KURZ);
        bm.setKeywords("praefix", "prefix", "präfix");
        retval.add(bm);

        bm = new BefehlsMuster(Prefix.class, 1, "^@?((praefix)|(präfix)|(prefix))[ ]*((\\/\\/).*)?", "p", Art.KURZ);
        bm.setKeywords("praefix", "prefix", "präfix");
        retval.add(bm);

        return retval;
    }
	
	@Override
	public boolean DoAction(Unit u, String[] befehl) { return false; }
	
	@Override
	public void PostAction() { }
	
	@Override
	public void PreAction() { }
	
	@Override
	public void DoAction(Region r, String befehl) {
		List<Einzelbefehl> befehle = BefehlsSpeicher.getInstance().get(this.getClass(), r.getCoords());

		for (Einzelbefehl eb : befehle) {
			if (eb.isPerformed()) throw new DoppelteAusfuehrungException(eb.toString());

			// Angaben über die "Akteure" selbst:
			Unit u = eb.getUnit();

            if (eb.getVariante() == 0) {
				// new Debug("Präfix: " + eb.getTokens()[1].replace("\"", ""));
				u.setPrefix(eb.getTokens()[1].replace("\"", ""));
				eb.setPerformed();
			}

			if (eb.getVariante() == 1) {
				// Präfix entfernen
				u.setPrefix("");
				eb.setPerformed();
			}
        }
    }

    @Override
    public void DoAction(Einzelbefehl eb) { }

}
