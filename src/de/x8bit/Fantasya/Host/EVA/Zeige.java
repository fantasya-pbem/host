package de.x8bit.Fantasya.Host.EVA;

import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Spell;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.Zauberbuch;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;
import de.x8bit.Fantasya.Host.EVA.util.DoppelteAusfuehrungException;

public class Zeige extends EVABase
{
	
	public Zeige()
	{
		super("zeige", "zeigen von Zauberbüchern und diversen Dingen");
		
		addTemplate("");
		for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
        List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();
        BefehlsMuster bm = null;

        bm = new BefehlsMuster(Zeige.class, 1, "^@?(zeige)[n]? (zauberbuch)([ ]+(\\/\\/).*)?", "z", Art.KURZ);
        bm.setKeywords("zeige", "zeigen", "zauberbuch");
        retval.add(bm);

        bm = new BefehlsMuster(Zeige.class, 0, "^@?(zeige)[n]? .+", "z", Art.KURZ);
        bm.setKeywords("zeige", "zeigen");
        retval.add(bm);
        return retval;
    }
	
	public boolean DoAction(Unit u, String[] befehl) { return false; }
	
	public void PostAction() { }
	public void PreAction() { }
	public void DoAction(Region r, String befehl) {
		List<Einzelbefehl> befehle = BefehlsSpeicher.getInstance().get(this.getClass(), r.getCoords());

		for (Einzelbefehl eb : befehle) {
			if (eb.isPerformed()) throw new DoppelteAusfuehrungException(eb.toString());

			// Angaben über die "Akteure" selbst:
			Unit u = eb.getUnit();

			if (eb.getVariante() == 1) ZeigeZauberbuch(u);

			if (eb.getVariante() == 0) {
				new Info("ZEIGE fehlgeschlagen für: '" + eb.getBefehlCanonical() + "'", Partei.getPartei(0));
				new Fehler("ZEIGE ist derzeit nur für 'ZAUBERBUCH' verfügbar - für alles andere bitte in der Bibliothek nachschauen: http://www.fantasya-pbem.de/bibliothek/", u, u.getCoords());
				eb.setError();
			}

			eb.setPerformed();
		}
	}
	
	public void ZeigeZauberbuch(Unit unit) {
		for(Spell spell : unit.getSpells()) new Zauberbuch(spell, unit);
	}
	
    @Override
    public void DoAction(Einzelbefehl eb) { }

}
