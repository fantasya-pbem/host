package de.x8bit.Fantasya.Host.EVA;

import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Allianz.AllianzOption;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.Debug;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;
import de.x8bit.Fantasya.Host.EVA.util.DoppelteAusfuehrungException;
import de.x8bit.Fantasya.Host.EVA.util.IDHint;
import de.x8bit.Fantasya.util.Codierung;
import de.x8bit.Fantasya.util.StringUtils;
import java.util.HashSet;
import java.util.Set;

public class Allianzen extends EVABase {
	public Allianzen() {
		super("helfe", "Allianzen setzen");
		
		addTemplate("");
		for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
        BefehlsMuster bm = null;
        List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();

        bm = new BefehlsMuster(Allianzen.class, 1, "^@?(helfe)[n]? [a-z0-9]{1,4}( nicht)?([ ]+(\\/\\/).*)?", "h", Art.KURZ);
        bm.addHint(new IDHint(1));
        bm.setKeywords("helfe", "helfen", "nicht");
        retval.add(bm);

        // alle HELFE-Optionen auflisten
        Set<String> keywords = new HashSet<String>();
        keywords.add("helfe");
        keywords.add("helfen");
        keywords.add("nicht");
        StringBuilder regEx = new StringBuilder();
        regEx.append("(");
        for(AllianzOption ao : AllianzOption.values()) {
            if (regEx.length() > 1) regEx.append("|");
            regEx.append("("
                    + "(" + ao.name().toLowerCase() + ")"
                    + "|(" + ao.name().toLowerCase() + "n)"
                    + ")");
            keywords.add(ao.name().toLowerCase());
        }
        regEx.append("|(ressourcen)");
        regEx.append(")");

        bm = new BefehlsMuster(Allianzen.class, 2, "^@?(helfe)[n]? [a-z0-9]{1,4} " + regEx + "( nicht)?([ ]+(\\/\\/).*)?", "h", Art.KURZ);
        bm.addHint(new IDHint(1));
        bm.setKeywords(keywords);
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
			Partei p = Partei.getPartei(u.getOwner());

            int pid = -1;
            try {
                pid = Codierung.fromBase36(eb.getTargetId());
            } catch(NumberFormatException ex) { new BigError(ex); }
			Partei partner = Partei.getPartei(pid);

            if (partner == null) {
                eb.setError();
                new Fehler("Kann keine Allianz mit " + eb.getTargetId() + " eingehen - diese Partei existiert nicht.", u);
                continue;
            }
            
            if (partner.isMonster()) {
                eb.setError();
                new Fehler("Kann keine Allianz mit " + eb.getTargetId() + " eingehen - ich mag Monster einfach nicht.", u);
                continue;
            }
            
            if (partner.getNummer() == u.getOwner()) {
                eb.setError();
                new Fehler("wie sollte ich eine Allianz mit uns selber eingehen?!", u);
                continue;
            }

            // haben wir überhaupt irgendwo Kontakt?
            if (!p.getBekannteParteien().contains(partner)) {
                new Debug("Bekannte Parteien für " + p + " ("+ eb.getBefehlCanonical() + ")" + StringUtils.aufzaehlung(p.getBekannteParteien()));
                eb.setError();
                new Fehler("Kann keine Allianz mit " + partner + " eingehen - wir haben keinen diplomatischen Kontakt.", u);
                continue;
            }

            // so, es wird ernst:

            // ja oder nein?
            boolean nicht = false;
            if (eb.getTokens()[eb.getTokens().length-1].equalsIgnoreCase("nicht")) nicht = true;

            if (eb.getVariante() == 1) {
                // HELFE ohne Option (ALLES)

				if (nicht) {
					new Info("Allianz für " + partner + " komplett annulliert.", u);
                    p.setAllianz(partner.getNummer(), AllianzOption.Alles, false);
                } else {
                    new Info("Allianz für " + partner + " auf ALLES gesetzt.", u);
                    p.setAllianz(partner.getNummer(), AllianzOption.Alles, true);
                }

                eb.setPerformed();
            }

            if (eb.getVariante() == 2) {
                // HELFE mit Option
                String optionParam = eb.getTokens()[2];
                // Mantis #360 - Ressource UND Resource akzeptieren:
                if (optionParam.toLowerCase().equals("ressourcen")) optionParam = "Resourcen";

                // Option holen
                AllianzOption option = AllianzOption.ordinal(optionParam);
                // nochmal versuchen ohne das letzte Zeichen:
                if (option == null) option = AllianzOption.ordinal(optionParam.substring(0, optionParam.length() - 1));
                if (option == null) {
                    new Fehler("Die Allianz-Option '" + optionParam + "' ist unbekannt.", u);
                    eb.setError();
                    continue;
                }

				if (nicht) {
                    p.setAllianz(partner.getNummer(), option, false);
                    new Info("Allianz für " + partner + " und '" + option.name() + "' annulliert.", u);
                } else {
                    p.setAllianz(partner.getNummer(), option, true);
                    new Info("Allianz für " + partner + " und '" + option.name() + "' gesetzt.", u);
                }

                eb.setPerformed();
            }
        } // nächster Einzelbefehl

    }

    @Override
	public boolean DoAction(Unit u, String befehl[]) { return false; }
    @Override
	public void PostAction() { }
    @Override
	public void PreAction() { }
    @Override
    public void DoAction(Einzelbefehl eb) { }

}
