package de.x8bit.Fantasya.Host.ZAT.Battle.Effects;

import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Host.ZAT.Battle.BattleEffectData;
import de.x8bit.Fantasya.Host.ZAT.Battle.BattleEffectsAttack;
import de.x8bit.Fantasya.Host.ZAT.Battle.Gefecht.BattleTime;

/**
 *
 * @author hapebe
 */
public class BFXFliegendOffensiv implements BattleEffectsAttack {
	
	float bonus = 0;
	
	/**
	 * @param bonus um diesen Wert wird AV und DV der Einheit erhöht
	 */
	public BFXFliegendOffensiv(float bonus) {
		this.bonus = bonus;
	}

	@Override
	public void Calculate(BattleEffectData bed) {
        Class<? extends Skill> talent = bed.getAttacker().usedWeapon().neededSkill();
        float waffenTW = (float)bed.getAttacker().getUnit().Talentwert(talent);

		bed.setAttackvalue(bed.getAttackvalue() + (bonus * waffenTW));
	}
	
	@Override
	public String toString() {
		return "Flug AV+" + bonus;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj.getClass() != BFXFliegendOffensiv.class) return false;
		
		if (this.bonus == ((BFXFliegendOffensiv)obj).bonus) return true;
		return false;
	}

	@Override
	public int hashCode() {
		return (int)this.bonus + 13729626;
	}

	@Override
	public void setEffectDownFor(BattleTime battleTime) {}
	
	

}
