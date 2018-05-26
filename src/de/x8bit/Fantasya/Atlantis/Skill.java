package de.x8bit.Fantasya.Atlantis;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import de.x8bit.Fantasya.Host.EVA.EVABase;
import de.x8bit.Fantasya.Host.Paket;

/**
 * @author  mogel
 */
public abstract class Skill extends Atlantis implements NamedItem
{
	private int lerntage;
	public int getLerntage() { return lerntage; }
	public void setLerntage(int value) { lerntage = value; }

	protected static Map<String, Class<? extends Skill>> SKILL_KEY;

	/**
	 * Eine Einheit unternimmt einen Lern-Versuch - das kann beliebige Konsequenzen haben, auch noch fehlschlagen.
	 * @param u Diese Einheit soll lernen.
	 */
	public String Lernen(Unit u) {
		// default-lernen
		setLerntage(getLerntage() + u.getPersonen() * 30 + u.getLehrtage());
        String verb = "lernen";
        if (u.getPersonen() == 1) verb = "lernt";
		return verb + " " + this.getClass().getSimpleName() + ".";
	}


	/**
	 * @return die passende Klasse für einen Skill-Namen - oder null.
	 */
	public static Class<? extends Skill> getFor(String skillName) {
		if (SKILL_KEY == null) {
			SKILL_KEY = new TreeMap<String, Class<? extends Skill>>();
			for (Paket p : Paket.getPaket("Skills")) {
				Skill skill = (Skill)p.Klasse;
				SKILL_KEY.put(p.ClassName.intern(), skill.getClass());
				SKILL_KEY.put(p.ClassName.toLowerCase().intern(), skill.getClass());
				SKILL_KEY.put(skill.getName().toLowerCase().intern(), skill.getClass());

				for (String complexNameAlias : EVABase.getNames(p)) {
					SKILL_KEY.put(complexNameAlias.intern(), skill.getClass());
				}
			}
		}

		return SKILL_KEY.get(skillName);
	}

	/**
	 * @param u fragliche Einheit
	 * @return Eine Map Feldname =&gt; Wert, die den Feldern der entsprechenden Datenbank-Tabelle entspricht
	 */
	public Map<String, Object> getDBValues(Unit u) {
		Map<String, Object> fields = new HashMap<String, Object>();

		fields.put("nummer", u.getNummer());
		fields.put("talent", this.getClass().getSimpleName());
		fields.put("lerntage", this.getLerntage());

		return fields;
	}

    /**
     * @param tw Talentwert
     * @return Anzahl Lerntage (pro Person) für das erreichen von Talentwert tw (vorausgesetzt, es gibt keine TW-Modifikationen)
     */
    public static int LerntageFuerTW(int tw) {
        return ((tw * (tw + 1)) / 2) * 30;
    }

	/**
	 * Berechnung des Talentwertes für die Statistik. Hier
	 * wird auch ein Nachkommateil berechnet.
	 * @param lerntage
	 * @return
	 */
	public static float Talentwert(float lerntage) {
		// Talentwert berechnen
		float sn = (lerntage / 30) * 2;	//	(l / 30) * 2
		float tw = (float)(-0.5f + Math.sqrt(0.25 + sn));

		// und zurück liefern
		return tw;
	}

    @Override
    public String toString() {
        return getComplexName().getName(getLerntage());
    }
}
