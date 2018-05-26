package de.x8bit.Fantasya.Host.EVA;

import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.EVA.util.AnzahlHint;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.DoppelteAusfuehrungException;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.IDHint;
import de.x8bit.Fantasya.Host.EVA.util.UnbekannteBefehlsVarianteException;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;
import de.x8bit.Fantasya.util.Codierung;

public class Steuerrate extends EVABase
{
	public void PreAction() { }
	public void PostAction() { }	
	
	public Steuerrate()
	{
		super("steuern", "Steuern festsetzen und eintreiben");
		
		addTemplate("");
        for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
		BefehlsMuster bm = null;
        List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();

		// Variante 1: Generell
        bm = new BefehlsMuster(Steuerrate.class, 1, "^@?(steuer)(n)? ((100)|([0-9]{1,2}))([ ]+(\\/\\/).*)?", "s", Art.KURZ);
		bm.addHint(new AnzahlHint(1));
        bm.setKeywords("steuer", "steuern");
		retval.add(bm);

		// Variante 2: Für eine einzelne Partei
        bm = new BefehlsMuster(Steuerrate.class, 2, "^@?(steuer)(n)? ((100)|([0-9]{1,2})) [a-z0-9]{1,4}([ ]+(\\/\\/).*)?", "s", Art.KURZ);
		bm.addHint(new AnzahlHint(1));
		bm.addHint(new IDHint(2));
        bm.setKeywords("steuer", "steuern");
		retval.add(bm);

		return retval;
    }

	/** die Steuerrate festlegen */
	public void DoAction(Region r, String befehl) {
		List<Einzelbefehl> befehle = BefehlsSpeicher.getInstance().get(this.getClass(), r.getCoords());

		for (Einzelbefehl eb : befehle) {
			if (eb.isPerformed()) throw new DoppelteAusfuehrungException(eb.toString());

			int rate = eb.getAnzahl();
			Unit u = eb.getUnit();
			Partei p = Partei.getPartei(u.getOwner());

			// STEUERN 45
			if (eb.getVariante() == 1) {
				new Info("Allgemeine Steuerrate für andere Völker wird auf " + rate + "% festgelegt.", u, u.getCoords());

				p.setDefaultsteuer(rate);

			// STEUERN 45 abc
			} else if (eb.getVariante() == 2) {
				int nummer = Codierung.fromBase36(eb.getTargetId());
				if (nummer != 0) {
					Partei other = Partei.getPartei(nummer);
					if (other != null) {

						p.setSteuern(nummer, rate);

						new Info("Steuerrate für " + other + " wird auf " + rate + "% festgelegt.", u, u.getCoords());
					} else {
					}
				} else {
					new Fehler("Das Volk mit der Nummer '0' können wir nicht besteuern.", u, u.getCoords());
				}
				
			} else {
				throw new UnbekannteBefehlsVarianteException(eb.getBefehlCanonical());
			}
			eb.setPerformed();
		}
	}

	/** die Steuerrate festlegen */
	public boolean DoAction(Unit u, String[] befehl) { return false; }

    @Override
    public void DoAction(Einzelbefehl eb) { }

}
