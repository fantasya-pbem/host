package de.x8bit.Fantasya.Host.EVA.util;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.util.Codierung;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Erzeugt einen schnell abfragbaren Cache der jeweils höchsten Talente
 * von Parteien in einer Region (bzw. einige zusammengefasste Top-TWs).
 * Die Klasse wird beim Erzeugen der Reporte verwendet, da hier viele Abfragen
 * von Top-Talentwerten stattfinden: Tarnung und so...
 * @author hapebe
 */
public final class TopSkillCache extends HashMap<Coords, Map<Class<? extends Skill>, Map<Integer, Unit>>> {
	private static final long serialVersionUID = -1440258234180998415L;
	
	final Map<Integer, Map<Class<? extends Skill>, Unit>> maxFuerPartei = new HashMap<Integer, Map<Class<? extends Skill>, Unit>>();
	final Map<Class<? extends Skill>, Unit> maxFuerSkill = new HashMap<Class<? extends Skill>, Unit>();

	public TopSkillCache(Collection<Unit> units) {
		for (Unit u : units) {
			List<Skill> echteSkills = new ArrayList<Skill>();
			for (Skill sk : u.getSkills()) {
				if (u.Talentwert(sk) > 0) echteSkills.add(sk);
			}
			if (echteSkills.isEmpty()) continue;

			Coords c = u.getCoords();
			if (!this.containsKey(c)) this.put(c, new HashMap<Class<? extends Skill>, Map<Integer, Unit>>());

			for (Skill sk : echteSkills) {
				int myTW = u.Talentwert(sk);

				if (!this.get(c).containsKey(sk.getClass())) {
					this.get(c).put(sk.getClass(), new HashMap<Integer, Unit>());
				}

				// die Partei selbst:
				Unit top = this.get(c).get(sk.getClass()).get(u.getOwner());
				if (top == null) {
					// wir sind die ersten:
					this.get(c).get(sk.getClass()).put(u.getOwner(), u);
					continue; // nächster Skill
				} else {
					if (myTW > top.Talentwert(sk)) {
						this.get(c).get(sk.getClass()).put(u.getOwner(), u);
					}
				}

				// 0 - alle Parteien in der Region:
				top = this.get(c).get(sk.getClass()).get(0);
				if (top == null) {
					// wir sind die ersten:
					this.get(c).get(sk.getClass()).put(0, u);
					continue; // nächster Skill
				} else {
					if (myTW > top.Talentwert(sk)) {
						this.get(c).get(sk.getClass()).put(0, u);
					}
				}

				// die ganze Partei, ungeachtet der Coords:
				if (!maxFuerPartei.containsKey(u.getOwner())) maxFuerPartei.put(u.getOwner(), new HashMap<Class<? extends Skill>, Unit>());
				if (!maxFuerPartei.get(u.getOwner()).containsKey(sk.getClass())) {
					// wird sind die ersten:
					maxFuerPartei.get(u.getOwner()).put(sk.getClass(), u);
				} else {
					top = maxFuerPartei.get(u.getOwner()).get(sk.getClass());
					if (myTW > top.Talentwert(sk)) {
						maxFuerPartei.get(u.getOwner()).put(sk.getClass(), u);
					}
				}

				// das Talent, ungeachtet von Partei oder Coords:
				if (!maxFuerSkill.containsKey(sk.getClass())) {
					// wir sind die ersten:
					maxFuerSkill.put(sk.getClass(), u);
				} else {
					top = maxFuerSkill.get(sk.getClass());
					if (myTW > top.Talentwert(sk)) {
						maxFuerSkill.put(sk.getClass(), u);
					}
				}
			}
		}
	}

	/**
	 * @param skill Talent
	 * @return den höchsten Talentwert einer Einheit überhaupt
	 */
	public int topTW(Class<? extends Skill> skill) {
		if (!maxFuerSkill.containsKey(skill)) return 0;
		return maxFuerSkill.get(skill).Talentwert(skill);
	}

	/**
	 * @param partei Partei-Nummer
	 * @param skill Talent
	 * @return Höchsten Talentwert von Partei(partei) - bzw. 0, falls das Talent in der Partei gar nicht vorkommt.
	 */
	public int topTW(int partei, Class<? extends Skill> skill) {
		if (!maxFuerPartei.containsKey(partei)) return 0;
		if (!maxFuerPartei.get(partei).containsKey(skill)) return 0;
		return maxFuerPartei.get(partei).get(skill).Talentwert(skill);
	}

	/**
	 * @param c Koordinaten einer Region
	 * @param skill Talent
	 * @return Höchster Talentwert aller Parteien in Region(c)
	 */
	public int topTW(Coords c, Class<? extends Skill> skill) {
		return topUnit(c, 0, skill).Talentwert(skill);
	}

	/**
	 * @param c Koordinaten einer Region
	 * @param partei Nummer einer Partei
	 * @param skill Talent
	 * @return Höchster Talentwert von Partei(partei) in Region(c)
	 */
	public int topTW(Coords c, int partei, Class<? extends Skill> skill) {
		return topUnit(c, partei, skill).Talentwert(skill);
	}

	public Unit topUnit(Coords c, int partei, Class<? extends Skill> skill) {
		if (!this.containsKey(c)) return null;
		if (!this.get(c).containsKey(skill)) return null;
		if (!this.get(c).get(skill).containsKey(partei)) return null;

		return this.get(c).get(skill).get(partei);
	}
	
	public String toString(Class<? extends Skill> skill) {
		StringBuilder sb = new StringBuilder();
		sb.append("\n\n").append(skill.getSimpleName().toUpperCase()).append("\n\n");
		
		for (Coords c : this.keySet()) {
			if (!this.get(c).containsKey(skill)) continue;
			
			sb.append(Region.Load(c)).append(" ").append(c).append(":\n");
			for (int partei : this.get(c).get(skill).keySet()) {
				Unit top = this.get(c).get(skill).get(partei);
				int tw = top.Talentwert(skill);
				sb.append("\t").append(Codierung.toBase36(partei)).append(": ").append(top).append(" mit TW " + tw + ".\n");
			}
			sb.append("\n");
		}
		
		return sb.toString();
	}

}
