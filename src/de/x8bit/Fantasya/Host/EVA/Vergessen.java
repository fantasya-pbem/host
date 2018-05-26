package de.x8bit.Fantasya.Host.EVA;

import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.EVA.util.AnzahlHint;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;
import de.x8bit.Fantasya.Host.EVA.util.DoppelteAusfuehrungException;
import de.x8bit.Fantasya.Host.EVA.util.SkillHint;
import de.x8bit.Fantasya.Host.Paket;

public class Vergessen extends EVABase {
	public Vergessen() {
		super("vergessen", "Talente werden vergessen");
		
		addTemplate("");
		for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
		BefehlsMuster bm = null;
        List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();
		
		// Skills - alle Namen auflisten:
        Set<String> skillNames = new HashSet<String>();
        for (Paket p : Paket.getPaket("Skills")) {
            skillNames.addAll(EVABase.getNames(p)); // damit werden auch "ComplexNames" berücksichtigt, bspw. Varianten mit / ohne Umlaut
        }
        // ... und als RegEx formulieren:
        StringBuilder regEx = new StringBuilder();
        regEx.append("(");
        for (String name : skillNames) {
            if (regEx.length() > 1) regEx.append("|");
            regEx.append("(").append(name).append(")");
        }
        regEx.append(")");
		

		bm = new BefehlsMuster(Vergessen.class, 1, "^@?((vergesse)[n]?|(vergiss)) " + regEx + "([ ]+(\\/\\/).*)?", "v", Art.KURZ);
        bm.setKeywords("vergiss", "vergesse", "vergessen");
		bm.addHint(new SkillHint(1));
        retval.add(bm);

		bm = new BefehlsMuster(Vergessen.class, 2, "^@?((vergesse)[n]?|(vergiss)) [1-9]{1}[0-9]{0,5} " + regEx + "([ ]+(\\/\\/).*)?", "v", Art.KURZ);
		bm.addHint(new AnzahlHint(1));
		bm.addHint(new SkillHint(2));
        bm.setKeywords("vergiss", "vergesse", "vergessen");
        retval.add(bm);

        return retval;
    }

	@Override
    public void DoAction(Einzelbefehl eb) {
		if (eb.isPerformed()) throw new DoppelteAusfuehrungException(eb.toString());
		
		Unit u = eb.getUnit();
		Class<? extends Skill> sk = eb.getSkill();
		int vergissTage = u.getSkill(sk).getLerntage();
		
		if (vergissTage == 0) {
			String verb = (u.getPersonen()==1 ? "hat" : "haben");
			new Fehler(u + " " + verb + " gar keine Fähigkeiten in " + sk.getSimpleName() + " zum Vergessen.", u);
			eb.setError();
			return;
		}
		
		if (eb.getVariante() == 2) {
			int wunschVergissTage = eb.getAnzahl() * u.getPersonen();
			if (wunschVergissTage < vergissTage) vergissTage = wunschVergissTage;
		}
		
		u.getSkill(sk).setLerntage(u.getSkill(sk).getLerntage() - vergissTage);
		int meldungsTage = Math.round((float)vergissTage / (float)u.getPersonen());
		String verb = (u.getPersonen()==1 ? "vergisst" : "vergessen");
		new Info(u + " " + verb + " " + meldungsTage + " Lerntage in " + sk.getSimpleName() + ".", u);
		eb.setPerformed();
	}

	@Override
	public void DoAction(Region r, String befehl) {	}
	@Override
	public void PostAction() { }
	@Override
	public void PreAction() { }
	@Override
	public boolean DoAction(Unit u, String[] befehl) { return false; }

}
