package de.x8bit.Fantasya.Host.ZAT.Battle.Effects;

import de.x8bit.Fantasya.Host.ZAT.Battle.BattleEffectData;
import de.x8bit.Fantasya.Host.ZAT.Battle.BattleEffectsAttack;
import de.x8bit.Fantasya.Host.ZAT.Battle.Gefecht.BattleTime;

/**
 *
 * @author hapebe
 */
public class BFXBurginsasseAttack implements BattleEffectsAttack {
	
	int bonus = 0;
	
	/**
	 * @param bonus um diesen Wert wird AV und DV der Einheit erhöht
	 */
	public BFXBurginsasseAttack(int bonus) {
		this.bonus = bonus;
	}

	@Override
	public void Calculate(BattleEffectData bed) {
		bed.setAttackvalue(bed.getAttackvalue() + (float)bonus);
		// bed.setDefencevalue(bed.getDefencevalue() + (float)bonus);
	}
	
	@Override
	public String toString() {
		return "Burg: AV+" + bonus;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj.getClass() != BFXBurginsasseAttack.class) return false;
		
		if (this.bonus == ((BFXBurginsasseAttack)obj).bonus) return true;
		return false;
	}

	@Override
	public int hashCode() {
		return this.bonus + 964198;
	}

	@Override
	public void setEffectDownFor(BattleTime battleTime) {}
	
	

}
