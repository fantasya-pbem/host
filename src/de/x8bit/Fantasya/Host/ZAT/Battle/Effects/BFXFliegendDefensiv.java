package de.x8bit.Fantasya.Host.ZAT.Battle.Effects;

import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Host.ZAT.Battle.BattleEffectData;
import de.x8bit.Fantasya.Host.ZAT.Battle.BattleEffectsDefence;
import de.x8bit.Fantasya.Host.ZAT.Battle.Gefecht.BattleTime;

/**
 *
 * @author hapebe
 */
public class BFXFliegendDefensiv implements BattleEffectsDefence {
	
	float bonus = 0;
	
	/**
	 * @param bonus um diesen Wert wird AV und DV der Einheit erhöht
	 */
	public BFXFliegendDefensiv(float bonus) {
		this.bonus = bonus;
	}

	@Override
	public void Calculate(BattleEffectData bed) {
        Class<? extends Skill> talent = bed.getDefender().usedWeapon().neededSkill();
        float waffenTW = (float)bed.getDefender().getUnit().Talentwert(talent);

        float effekt = bonus * waffenTW;

        // um 75% reduzierter Bonus gegen Fernkampfwaffen:
        if (bed.getAttacker().usedWeapon().istFernkampfTauglich()) effekt /= 4;

        bed.setDefencevalue(bed.getDefencevalue() + effekt);
	}
	
	@Override
	public String toString() {
		return "Flug DV+" + bonus;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj.getClass() != BFXFliegendDefensiv.class) return false;
		
		if (this.bonus == ((BFXFliegendDefensiv)obj).bonus) return true;
		return false;
	}

	@Override
	public int hashCode() {
		return (int)this.bonus + 13725465;
	}

	@Override
	public void setEffectDownFor(BattleTime battleTime) {}
	
	

}
