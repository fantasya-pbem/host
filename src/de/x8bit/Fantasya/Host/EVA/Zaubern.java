package de.x8bit.Fantasya.Host.EVA;

import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Spell;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.SysErr;
import de.x8bit.Fantasya.Atlantis.Messages.ZATMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Magie;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.Paket;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;
import de.x8bit.Fantasya.Host.EVA.util.DoppelteAusfuehrungException;
import de.x8bit.Fantasya.Host.EVA.util.SpellHint;
import de.x8bit.Fantasya.Host.EVA.util.ZATMode;
import de.x8bit.Fantasya.util.Random;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;



public class Zaubern extends EVABase {

	// Berechnung für MaxAura bzw. MaxMana -> sqr(1-ln(1/x))*ln(x)+x
	
	public Zaubern() {
		super("zauber", "Zaubersprüche ausführen");
		
		addTemplate("");
		for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
        BefehlsMuster bm = null;
		List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();

		// Zauber - alle Namen auflisten:
        List<String> spellPatterns = new ArrayList<String>();
        for (Paket p : Paket.getPaket("Spells")) {
            Spell spell = (Spell)p.Klasse;
            for (String pattern : spell.getTemplates()) spellPatterns.add(pattern);
        }
        // ... und zusammenfassen:
        StringBuilder regEx = new StringBuilder();
        regEx.append("(");
        for (String muster : spellPatterns) {
            if (regEx.length() > 1) regEx.append("|");
            regEx.append("(" + muster + ")");
        }
        regEx.append(")");

        bm = new BefehlsMuster(Zaubern.class, 0, "^(zauber)[en]? " + regEx + "([ ]+(\\/\\/).*)?", "z", Art.LANG);
		bm.addHint(new SpellHint(1));
        bm.setKeywords("zauber", "zaubere", "zaubern");
		retval.add(bm);

        return retval;
    }
	
	public void PreAction() { }
	
	public void PostAction() {
		// Mana + Aura nehmen zu:
		for (Unit unit : Unit.CACHE) {
			int tw = unit.Talentwert(Magie.class);
			if (tw == 0) continue;

			int max = unit.getMaxAura();
			int a = unit.getCoords().getWelt() > 0 ? unit.getAura() : unit.getMana();

			// TODO muss noch etwas verbessert werden ... steigt zu schnell
			int zuwachs = Random.rnd(1, tw + 1);
			if (a + zuwachs > max) zuwachs = max - a;

			if (zuwachs > 0) {
				if (unit.getCoords().getWelt() > 0) {
					new Info(unit + " regeneriert " + zuwachs + " Punkte Aura.", unit);
					unit.setAura(a + zuwachs);
				} else {
					new Info(unit + " regeneriert " + zuwachs + " Punkte Mana.", unit);
					unit.setMana(a + zuwachs);
				}
			}
		}

		if (ZATMode.CurrentMode().isDebug()) {
			new ZATMsg("Zauber-Tabelle ausgeben...");
			
			try {
				File file = new File("temp/Zauber-Tabelle.csv");

				Writer out = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(file), "UTF8"));

				out.append("Name\t");
				out.append("Stufe\t");
				out.append("Element\t");
				out.append("Syntax\t");
				out.append("ConfusionSpell\t");
				out.append("AttackSpell\t");
				out.append("DefenceSpell\t");
				out.append("\r\n");

				for (Paket p : Paket.getPaket("Spells")) {
					Spell sp = (Spell)p.Klasse;

					out.append(sp.getName()).append("\t");
					out.append(Integer.toString(sp.getStufe())).append("\t");
					out.append(sp.getElementar().toString()).append("\t");
					out.append(sp.getSpruch()+"\t");
					out.append((sp instanceof Spell.ConfusionSpell)?"1":"0").append("\t");
					out.append((sp instanceof Spell.AttackSpell)?"1":"0").append("\t");
					out.append((sp instanceof Spell.DefenceSpell)?"1":"0").append("\t");
					out.append("\r\n");
				}

				out.flush();
				out.close();

			} catch (UnsupportedEncodingException e) {
				new SysErr("Kann Zauber-Tabelle nicht schreiben: " + e.getMessage());
			} catch (IOException e) {
				new SysErr("Kann Zauber-Tabelle nicht schreiben: " + e.getMessage());
			}
			
		}

	}


	public void DoAction(Region r, String befehl) {
		List<Einzelbefehl> befehle = BefehlsSpeicher.getInstance().get(this.getClass(), r.getCoords());

		for (Einzelbefehl eb : befehle) {
			if (eb.isPerformed()) throw new DoppelteAusfuehrungException(eb.toString());

			// Angaben über die "Akteure" selbst:
			Unit u = eb.getUnit();

			Spell spell = null;
			try {
				spell = eb.getSpell().newInstance();
			} catch (InstantiationException ex) {
				new BigError(ex);
			} catch (IllegalAccessException ex) {
				new BigError(ex);
			}
			if (spell == null) {
				// Spell nicht gefunden
				new Fehler(u + " - der Zauberspruch '" + eb.getTokens()[1] + "' wurde nicht gefunden.", u);
				eb.setError();
				continue;
			}

			// Kampfzauber (?) und FirstSpells werden hier NICHT ausgeführt
			
			// TODO spell.isFirstSpell() - deren Behandlung muss natürlich anders werden!
			if (spell.isFirstSpell()) continue;
            
			if (!spell.canUsedBy(u)) {
				new Fehler(u + " ist verwirrt.", u);
                eb.setError();
				continue;
			}

			// Spruch ausführen
			Execute(u, spell, eb.getTokens());
			eb.setPerformed();
		}
	}
	
	/** sucht den richtigen Spruch */
	public static Spell FindSpell(String spruch)
	{
		for(Paket p : Paket.getPaket("Spells"))
		{
			Spell spell = (Spell) p.Klasse;
			if (spell.getName().toLowerCase().equalsIgnoreCase(spruch)) return spell;
		}
		
		return null;
	}
	
	/**
	 * bereitet den Zauberpsruch vor ... überprüft also ob die VOrrausetzungen stimmen ... 
	 * wie Aura/Mana - Stufe ... Aura bzw. Mana muss vom Spruch abgezogen werden !! 
	 * @param u - diese Einheit zaubert
	 * @param spell - diesen Spruch
	 * @param param - der Spruch einmal zerlegt in seine Parameter
	 */
	private void Execute(Unit u, Spell spell, String [] param)
	{
		if (spell == null) return;
		
		// Talent prüfen
		if (u.Talentwert(de.x8bit.Fantasya.Atlantis.Skills.Magie.class) < spell.getStufe())
		{
			new Fehler(u + " kann den Spruch nicht mehr zaubern, es fehlt am nötigen Talent.", u);
			return;
		}
		
		// Mana / Aura prüfen
		if (!spell.isOrcus())
		{
			// Aura benötigt
			if (u.getAura() < spell.getStufe())
			{
				new Fehler(u + " fehlt genügend Aura um den Spruch zaubern zu können.", u);
				return;
			}
		} else
		{
			// Mana benötigt
			if (u.getMana() < spell.getStufe())
			{
				new Fehler(u + " fehlt genügend Mana um den Spruch zaubern zu können.", u);
				return;
			}
		}
		
		// Spell ausführen
		int stufe = spell.ExecuteSpell(u, param); // merken mit welcher Stufe gezaubert wurde
		int verbrauch = stufe * spell.getStufe();
		if (verbrauch > 0) {
			if (spell.isOrcus())
			{
				u.setMana(u.getMana() - verbrauch);
				new Info(u + " hat " + verbrauch + " Punkte Mana verbraucht.", u);
			} else
			{
				u.setAura(u.getAura() - verbrauch);
				new Info(u + " hat " + verbrauch + " Punkte Aura verbraucht.", u);
			}
		}
	}

	public boolean DoAction(Unit u, String[] b) { return false; }
    public void DoAction(Einzelbefehl eb) { }

}
