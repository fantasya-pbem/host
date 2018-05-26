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
import de.x8bit.Fantasya.Host.EVA.util.UnitHint;
import de.x8bit.Fantasya.util.Codierung;
import de.x8bit.Fantasya.util.StringUtils;

public class Kontaktierungen extends EVABase {

    public final static int PERMANENT = 2;
    
	public Kontaktierungen()
	{
		super("kontaktiere", "Kontakte zu anderen Einheiten");
		
		addTemplate("");
		for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
        BefehlsMuster bm = null;
        List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();

        bm = new BefehlsMuster(Kontaktierungen.class, 1 + EVABase.TEMP, "^@?(kontaktiere)[n]?( temp)[a-z0-9]{1,4}([ ]+(\\/\\/).*)?", "k", Art.KURZ);
        bm.addHint(new UnitHint(1));
        bm.setKeywords("kontaktiere", "kontaktieren");
        retval.add(bm);

        bm = new BefehlsMuster(Kontaktierungen.class, 1, "^@?(kontaktiere)[n]? [a-z0-9]{1,4}([ ]+(\\/\\/).*)?", "k", Art.KURZ);
        bm.addHint(new UnitHint(1));
        bm.setKeywords("kontaktiere", "kontaktieren");
        retval.add(bm);


        bm = new BefehlsMuster(Kontaktierungen.class, PERMANENT + EVABase.TEMP, "^@?(kontaktiere)[n]?( temp)[a-z0-9]{1,4} (permanent)([ ]+(\\/\\/).*)?", "k", Art.KURZ);
        bm.addHint(new UnitHint(1));
        bm.setKeywords("kontaktiere", "kontaktieren", "permanent");
		bm.setKeep(true);
        retval.add(bm);

        bm = new BefehlsMuster(Kontaktierungen.class, PERMANENT, "^@?(kontaktiere)[n]? [a-z0-9]{1,4} (permanent)([ ]+(\\/\\/).*)?", "k", Art.KURZ);
        bm.addHint(new UnitHint(1));
        bm.setKeywords("kontaktiere", "kontaktieren", "permanent");
		bm.setKeep(true);
        retval.add(bm);

        
        return retval;
    }
	
	@Override
	public void DoAction(Region r, String befehl) {
		List<Einzelbefehl> befehle = BefehlsSpeicher.getInstance().get(this.getClass(), r.getCoords());

		for (Einzelbefehl eb : befehle) {
			if (eb.isPerformed()) throw new DoppelteAusfuehrungException(eb.toString());

			// Angaben über die "Akteure" selbst:
			Unit u = eb.getUnit();
            Unit targetUnit = null;

			@SuppressWarnings("unused") // Bug in Eclipse - wird weiter unten verwendet
			int variante = eb.getVariante();
            
			// ist eine TEMP-Einheit im Spiel?
			if ((eb.getVariante() & EVABase.TEMP) != 0) {
				String tempId = eb.getTargetUnit().toLowerCase();
				if (tempId.startsWith("temp ")) tempId = tempId.substring(5);

				int tempnummer = Unit.getRealNummer(tempId, u);

				if (tempnummer == 0) {
					eb.setError();
                    new Fehler("TEMP-Einheit " + eb.getTargetUnit() + " nicht gefunden.", u);
					continue;
				}
				eb.setTargetUnit(Codierung.toBase36(tempnummer));
				variante -= EVABase.TEMP;
			}


			// Zieleinheit holen
            if (eb.getTargetUnit() != null) {
				targetUnit = Unit.Load(Codierung.fromBase36(eb.getTargetUnit()));
			}

            if (targetUnit == null) {
                eb.setError();
                new Fehler("Einheit " + eb.getTargetUnit() + " nicht gefunden.", u);
                continue;
            }

            if (!targetUnit.getCoords().equals(u.getCoords())) {
                eb.setError();
                new Fehler("Einheit " + eb.getTargetUnit() + " nicht gefunden.", u);
                continue;
            }

            // Meldungen werden jetzt zusammengefasst in PostAction() ausgegeben:
            // new Info("Wir haben diesen Monat volles Vertrauen in " + targetUnit + ".", u);
            if (!u.Kontakte.contains(targetUnit.getNummer())) {
                u.Kontakte.add(targetUnit.getNummer());
            }

            eb.setPerformed();
        }
    }

	@Override
	public void PostAction() { 
        for (Unit u : Unit.CACHE) {
            if (u.Kontakte.isEmpty()) continue;
            List<String> namen = new ArrayList<String>();
            for (int nr : u.Kontakte) {
                Unit partner = Unit.Load(nr);
                if (partner != null) namen.add(partner.toString());
            }
            if (namen.isEmpty()) continue;
            
            String verb = "haben";
            if (u.getPersonen() == 1) verb = "hat";
            new Info(verb + " diesen Monat volles Vertrauen in " + StringUtils.aufzaehlung(namen) + ".", u);
        }
    }

    
    // in der ZAT-Version: löscht alle alten Kontakte aus der vorherigen Runde
	@Override
	public void PreAction()	{ }
	@Override
	public boolean DoAction(Unit u, String[] befehl) { return false; }
	@Override
	public void DoAction(Einzelbefehl eb) { }

}
