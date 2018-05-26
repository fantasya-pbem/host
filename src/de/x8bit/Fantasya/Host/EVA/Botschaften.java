package de.x8bit.Fantasya.Host.EVA;

import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Messages.Botschaft;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;
import de.x8bit.Fantasya.Host.EVA.util.DoppelteAusfuehrungException;
import de.x8bit.Fantasya.Host.EVA.util.IDHint;
import de.x8bit.Fantasya.Host.EVA.util.UnitHint;
import de.x8bit.Fantasya.util.Codierung;
import de.x8bit.Fantasya.util.StringUtils;

public class Botschaften extends EVABase {

    public final static int EINHEITEN_A = 1;
    public final static int EINHEITEN_B = 2;
    public final static int PARTEI = 10;
    public final static int REGION = 20;

	public Botschaften() {
		super("botschaft", "Depeschen senden und Nachrichten verkünden");
		
		addTemplate("");
		for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
        BefehlsMuster bm = null;
        List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();

        bm = new BefehlsMuster(Botschaften.class, EINHEITEN_A + EVABase.TEMP, "^@?(botschaft temp)[a-z0-9]{1,4}( ).*", "b", Art.KURZ);
        bm.addHint(new UnitHint(1));
        bm.setKeywords("botschaft", "einheit");
        retval.add(bm);

        bm = new BefehlsMuster(Botschaften.class, EINHEITEN_A, "^@?(botschaft )[a-z0-9]{1,4}( ).*", "b", Art.KURZ);
        bm.addHint(new UnitHint(1));
        bm.setKeywords("botschaft", "einheit");
        retval.add(bm);


        bm = new BefehlsMuster(Botschaften.class, EINHEITEN_B + EVABase.TEMP, "^@?(botschaft einheit temp)[a-z0-9]{1,4}( ).*", "b", Art.KURZ);
        bm.addHint(new UnitHint(2));
        bm.setKeywords("botschaft", "einheit");
        retval.add(bm);

        bm = new BefehlsMuster(Botschaften.class, EINHEITEN_B, "^@?(botschaft einheit )[a-z0-9]{1,4}( ).*", "b", Art.KURZ);
        bm.addHint(new UnitHint(2));
        bm.setKeywords("botschaft", "einheit");
        retval.add(bm);


        bm = new BefehlsMuster(Botschaften.class, PARTEI, "^@?(botschaft )((partei)|(volk))( )[a-z0-9]{1,4}( ).*", "b", Art.KURZ);
        bm.addHint(new IDHint(2));
        bm.setKeywords("botschaft", "partei", "volk");
		retval.add(bm);

        bm = new BefehlsMuster(Botschaften.class, REGION, "^@?(botschaft )(an )?((region)|(alle))( ).*", "b", Art.KURZ);
        bm.setKeywords("botschaft", "an", "region", "alle");
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
			int variante = eb.getVariante();
            
			if ((variante == EINHEITEN_A) || (variante == EINHEITEN_B)) {
				// ist eine TEMP-Einheit im Spiel?
				if ((variante & EVABase.TEMP) != 0) {
					String tempId = eb.getTargetUnit().toLowerCase();
					if (tempId.startsWith("temp ")) tempId = tempId.substring(5);

					int tempnummer = Unit.getRealNummer(tempId, u);

					if (tempnummer == 0) {
						eb.setError();
						new Fehler(u + " - Temp-Einheit " + eb.getTargetUnit() + " nicht gefunden.", u);
						continue;
					}
					eb.setTargetUnit(Codierung.toBase36(tempnummer));
					variante -= EVABase.TEMP;
				}


				// Zieleinheit holen
				Unit targetUnit = null;
				if (eb.getTargetUnit() != null) {
					targetUnit = Unit.Load(Codierung.fromBase36(eb.getTargetUnit()));
				}

				if (targetUnit == null) {
					eb.setError();
					new Fehler(u + " - Einheit [" + eb.getTargetUnit() + "] nicht gefunden.", u);
					continue;
				} else if (!targetUnit.getCoords().equals(u.getCoords())) {
					eb.setError();
					new Fehler(u + " - Einheit [" + eb.getTargetUnit() + "] nicht gefunden.", u);
					continue;
				} else if (!u.cansee(targetUnit)) {
					eb.setError();
					new Fehler(u + " - Einheit [" + eb.getTargetUnit() + "] nicht gefunden.", u);
					continue;
                }

				String txt = "";
				if (variante == EINHEITEN_A) {
					eb.combineTokens(2, Integer.MAX_VALUE);
					txt = eb.getTokens()[2];
				}
				if (variante == EINHEITEN_B) {
					eb.combineTokens(3, Integer.MAX_VALUE);
					txt = eb.getTokens()[3];
				}

				new Botschaft(u, targetUnit, txt);
				new Info("Botschaft '" + StringUtils.anriss(txt, 40) + "' an " + targetUnit + " überbracht.", u);
				eb.setPerformed();
			} // endif BOTSCHAFT EINHEIT ...

			if (variante == PARTEI) {
				int partei = -1;
				try {
					partei = Codierung.fromBase36(eb.getTargetId());
				} catch (NumberFormatException ex) {}

				if (partei == -1) {
					new Fehler("Empfänger-Partei [" +eb.getTargetId() + "] nicht erkannt.", u);
					eb.setError();
					continue;
				}

				Partei p = Partei.getPartei(partei);
				if (p == null) {
					new Fehler("Empfänger-Partei [" +eb.getTargetId() + "] nicht gefunden.", u);
					eb.setError();
					continue;
				}

				eb.combineTokens(3, Integer.MAX_VALUE);
				String txt = eb.getTokens()[3];

				// vielleicht gibt es getarnte, also lassen wir den Urheber im Unklaren,
				// ob er Leser finden wird.
				new Botschaft(u, p, txt);
				new Info("Aushänge an " + p + " überall angeschlagen: '" + StringUtils.anriss(txt, 40) + "'", u);
				eb.setPerformed();
			}
			
			if (variante == REGION) {
				int txtBeginn = 2;
				if (eb.getTokens()[2].equalsIgnoreCase("an")) txtBeginn = 3;
				eb.combineTokens(txtBeginn, Integer.MAX_VALUE);

				String txt = eb.getTokens()[txtBeginn];
				
				new Botschaft(u, r, txt);
				new Info("Herolde losgeschickt: 'An alle: " + StringUtils.anriss(txt, 40) + "'", u);
				eb.setPerformed();
			}


        } // nächster Einzelbefehl
    }

	
    @Override
	public void PreAction()	{ }
    @Override
	public void PostAction() { }
    @Override
	public boolean DoAction(Unit u, String[] befehl) { return false; }
    @Override
	public void DoAction(Einzelbefehl eb) { }

}
