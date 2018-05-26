package de.x8bit.Fantasya.Host.EVA;

import de.x8bit.Fantasya.Atlantis.Messages.Debug;
import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Spell;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Magie;
import de.x8bit.Fantasya.Atlantis.Spell.AttackSpell;
import de.x8bit.Fantasya.Atlantis.Spell.ConfusionSpell;
import de.x8bit.Fantasya.Atlantis.Spell.DefenceSpell;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.Paket;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;
import de.x8bit.Fantasya.Host.EVA.util.DoppelteAusfuehrungException;
import java.util.HashSet;
import java.util.Set;

public class Kampfzauber extends EVABase {

	/**
	 * Key/Name eines Einheiten-Property
	 */
	public final static String ATTACKSPELL = "attackspell";

	/**
	 * Key/Name eines Einheiten-Property
	 */
	public final static String CONFUSIONSPELL = "confusionspell";

	/**
	 * Key/Name eines Einheiten-Property
	 */
	public final static String DEFENCESPELL = "defencespell";

	public Kampfzauber()
	{
		super("kampfzauber", "setzen der Kampfzauber");

		addTemplate("");
		for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
        List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();
        BefehlsMuster bm = null;

		bm = new BefehlsMuster(Kampfzauber.class, 1,
                "^@?(kampfzauber) ((angriff)|(verwirrung)|(verteidigung)) ((nicht)|(kein)|(keiner))([ ]+(\\/\\/).*)?",
                "k", Art.KURZ);
        Set<String> keywords = new HashSet<String>();
        keywords.add("kampfzauber");
        keywords.add("angriff");
        keywords.add("verwirrung");
        keywords.add("verteidigung");
        keywords.add("nicht");
        keywords.add("kein");
        keywords.add("keiner");
        bm.setKeywords(keywords);
        retval.add(bm);

        List<Paket> alp = Paket.getPaket("Spells");
		for(int i = 0; i < alp.size(); i++)
		{
			Spell spell = (Spell) (alp.get(i)).Klasse;
			if (!spell.isBattleSpell()) continue;
			for(String tpl : spell.getTemplates()) {
                // hier lassen wir den Spieler ruhig in die Falle laufen - er soll schon
                // aufpassen, ob ein Spruch für die gewünschte Phase geeignet ist. (?)
                bm = new BefehlsMuster(Kampfzauber.class, 2, "^(kampfzauber) ((angriff)|(verwirrung)|(verteidigung)) " + tpl + "([ ]+(\\/\\/).*)?", "k", Art.KURZ);
                keywords = new HashSet<String>();
                keywords.add("kampfzauber");
                keywords.add("angriff");
                keywords.add("verwirrung");
                keywords.add("verteidigung");
                bm.setKeywords(keywords);
                retval.add(bm);
			}
		}

        return retval;
    }
    
    @Override
	public void PreAction() {
        // prüfen, ob alle gesetzten Kampfzauber auch "legal" sind:
        for (Unit u : Unit.CACHE) {
            if (u.Talentwert(de.x8bit.Fantasya.Atlantis.Skills.Magie.class) == 0) continue;
            
            String[] slots = new String[] {CONFUSIONSPELL, ATTACKSPELL, DEFENCESPELL};
            for (String slot : slots) {
                if (u.hasProperty(slot)) {
	                String spruch = u.getStringProperty(slot);
                    String befehl = spruch;
                    // Toleranz für Legacy:
                    if (!spruch.toUpperCase().startsWith("ZAUBER")) befehl = "ZAUBERE " + spruch;
                    try {
                        @SuppressWarnings("unused")
						Einzelbefehl eb = new Einzelbefehl(u, befehl);
                    } catch (IllegalArgumentException ex) {
                        String wasIsses = "Verwirrungszauber";
                        if (slot.equals(ATTACKSPELL)) wasIsses = "Angriffszauber";
                        if (slot.equals(DEFENCESPELL)) wasIsses = "Verteidigungszauber";
                        new Fehler(u + ": Der " + wasIsses + " '" + befehl + "' ist nicht gültig - bitte einen korrekten " + wasIsses + " setzen.", u);
                        u.removeProperty(slot);
                    }
                }
            }
        }
    }
    
	
	@Override
	public void PostAction() { }


	@Override
	public void DoAction(Region r, String befehl) {
		List<Einzelbefehl> befehle = BefehlsSpeicher.getInstance().get(this.getClass(), r.getCoords());

		for (Einzelbefehl eb : befehle) {
			if (eb.isPerformed()) throw new DoppelteAusfuehrungException(eb.toString());

            Unit u = eb.getUnit();
            int variante = eb.getVariante();

            if (u.Talentwert(de.x8bit.Fantasya.Atlantis.Skills.Magie.class) == 0) {
                eb.setError();
                new Fehler(u + " hat keine Ahnung von Magie.", u);
                continue;
            }

            if (variante == 2) {
                // ab Token 2 bis zum vorletzten Token ist der Zauber:
                eb.combineTokens(2, eb.getTokens().length - 2);
                
                Spell spell = FindSpell(eb.getTokens()[2]);
                // richtigen Spell suchen
                if (spell == null) {
                    eb.setError();
                    new Fehler(u + " kann den Zauber '" + eb.getTokens()[2] + "' nicht zaubern.", u);
                    continue;
                }
                if (spell.isBattleSpell() == false) {
                    eb.setError();
                    new Fehler("" + spell + " ist kein Kampfzauber.", u);
                    continue;
                }
                int wunschStufe = u.Talentwert(de.x8bit.Fantasya.Atlantis.Skills.Magie.class);
                try {
                    // das letzte könnte/sollte/müsste die Wunsch-Stufe sein:
                    wunschStufe = Integer.parseInt(eb.getTokens()[eb.getTokens().length-1]);
                } catch (NumberFormatException ex) {
                    // dann eben nicht.
                }

                // Spell setzen
                String modus = eb.getTokens()[1].toLowerCase();
                eb.combineTokens(2, 99);
                String params = eb.getTokens()[2];
                if (modus.equals("angriff")) SetAttack(u, spell, params, wunschStufe);
                if (modus.equals("verwirrung")) SetConfusion(u, spell, params, wunschStufe);
                if (modus.equals("verteidigung")) SetDefence(u, spell, params, wunschStufe);

                eb.setPerformed();
            }

            if (variante == 1) {
                // Kampfzauber deaktivieren

                String modus = eb.getTokens()[1].toLowerCase();
                if (modus.equals("angriff")) SetAttack(u, null, "", -1);
                if (modus.equals("verwirrung")) SetConfusion(u, null, "", -1);
                if (modus.equals("verteidigung")) SetDefence(u, null, "", -1);

                eb.setPerformed();
            }
        }
    }


	@Override
	public boolean DoAction(Unit u, String[] b) { return false; }
	
	private void SetAttack(Unit unit, Spell spell, String params, int wunschStufe)
	{
        if (spell == null) {
			unit.removeProperty(Kampfzauber.ATTACKSPELL);
            return;
        }

        if (!(spell instanceof AttackSpell)) {
			new Fehler("'" + spell + "' ist kein Angriffszauber.", unit);
		} else {
			new Magie(unit + " setzt den Angriffszauber " + spell + " auf Stufe " + wunschStufe + ".", unit);
            new Debug("unit.setProperty(Kampfzauber.ATTACKSPELL, " + params + ");");
			unit.setProperty(Kampfzauber.ATTACKSPELL, params);
		}
	}
	
	private void SetDefence(Unit unit, Spell spell, String params, int wunschStufe)
	{
		if (spell == null) {
			unit.removeProperty(Kampfzauber.DEFENCESPELL);
            return;
        }

		if (!(spell instanceof DefenceSpell))
		{
			new Fehler("'" + spell + "' ist kein Verteidigungszauber.", unit);
		} else
		{
			new Magie(unit + " setzt den Verteidigungszauber " + spell + " auf Stufe " + wunschStufe + ".", unit);
            new Debug("unit.setProperty(Kampfzauber.DEFENCESPELL, " + params + ");");
			unit.setProperty(Kampfzauber.DEFENCESPELL, params);
		}
	}
	
	private void SetConfusion(Unit unit, Spell spell, String params, int wunschStufe)
	{
		if (spell == null) {
			unit.removeProperty(Kampfzauber.CONFUSIONSPELL);
            return;
        }

		if (!(spell instanceof ConfusionSpell))
		{
			new Fehler("'" + spell + "' ist kein Verwirrungszauber.", unit);
		} else
		{
			new Magie(unit + " setzt den Verwirrungszauber " + spell + " auf Stufe " + wunschStufe + ".", unit);
            new Debug("unit.setProperty(Kampfzauber.CONFUSIONSPELL, " + params + ");");
			unit.setProperty(Kampfzauber.CONFUSIONSPELL, params);
		}
	}
	
	/** sucht den richtigen Spruch */
	private Spell FindSpell(String spruch)
	{
		for(Paket p : Paket.getPaket("Spells"))	{
			Spell spell = (Spell) p.Klasse;
			if (spell.getName().equalsIgnoreCase(spruch)) return spell;
		}
		
		return null;
	}

    @Override
	public void DoAction(Einzelbefehl eb) { }

}
