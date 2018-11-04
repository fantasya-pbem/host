/**
 * 
 */
package de.x8bit.Fantasya.Atlantis.Effects;

import de.x8bit.Fantasya.Atlantis.Effect;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Unit;

/**
 * @author Michael Jahn
 *
 */
public class EFXMultipleProductionEffect extends Effect {
	private static int HUNDRED_PERCENT = 10000;
	
	private int percent = HUNDRED_PERCENT; // 100,00 % der Fertigkeiten stehen am Anfang zur Verfügung.
	
	/** Konstruktor für Reflection */
	public EFXMultipleProductionEffect() { super(); destroyIt(); }
	
	public EFXMultipleProductionEffect(Unit unit)
	{
		super(unit);	// Nummer holen
		destroyIt();
	}

	/**
	 * Reduziert den Prozentwert um die benutzen Fertigkeitspunkte.
	 * @param unit Einheit, die die Fertigkeitspunkte verbraucht hat.
	 * @param skill Fertigkeit, die benutzt wurde.
	 * @param usedSkillPoints Fertigkeitspunkte, die verwendet werden. (Produktionsmindestlevel * Anzahl der produzierten Gegenstände/Größenpunkte)
	 */
	public void reducePercent(Unit unit, Class<? extends Skill> skill, int usedSkillPoints) {
		int newRemainSkillPoints = this.calculateRemainSkillPoints(unit, skill) - usedSkillPoints; // Anzahl der übriggebliebenen Fertigkeitspunkte errechnen.
		int unitSkillPoints = unit.Talentwert(skill) * unit.getPersonen();
		this.percent = newRemainSkillPoints * HUNDRED_PERCENT / unitSkillPoints;
		if (this.percent < 0) { this.percent = 0; }
	}
	
	private int calculateRemainSkillPoints(Unit unit, Class<? extends Skill> skill)
	{
		return (unit.Talentwert(skill) * unit.getPersonen()) * this.percent / HUNDRED_PERCENT;
	}

	/**
	 * Gibt die Anzahl der Fertigkeitspunkte wieder, die noch zur Verfügung stehen.
	 * 
	 * @return Anzahl der verfügbaren Produktionspunkte.
	 */
	public int EFXCalculate(Unit unit, Class<? extends Skill> skill) {
		return this.calculateRemainSkillPoints(unit, skill);
	}
	
}
