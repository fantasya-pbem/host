package de.x8bit.Fantasya.Host.ZAT.Battle.Effects;

import de.x8bit.Fantasya.Host.ZAT.Battle.BattleEffectData;
import de.x8bit.Fantasya.Host.ZAT.Battle.BattleEffectsDefence;
import de.x8bit.Fantasya.Host.ZAT.Battle.Gefecht.BattleTime;

/**
 *
 * @author hapebe
 */
public class BFXBurginsasseDefence implements BattleEffectsDefence {
	
	int bonus = 0;
	
	/**
	 * @param bonus um diesen Wert wird AV und DV der Einheit erhöht
	 */
	public BFXBurginsasseDefence(int bonus) {
		this.bonus = bonus;
	}

	@Override
	public void Calculate(BattleEffectData bed) {
		// bed.setAttackvalue(bed.getAttackvalue() + (float)bonus);
		bed.setDefencevalue(bed.getDefencevalue() + (float)bonus);
	}
	
	@Override
	public String toString() {
		return "Burg: DV+" + bonus;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj.getClass() != BFXBurginsasseDefence.class) return false;
		
		if (this.bonus == ((BFXBurginsasseDefence)obj).bonus) return true;
		return false;
	}

	@Override
	public int hashCode() {
		return this.bonus + 4165480;
	}

	@Override
	public void setEffectDownFor(BattleTime battleTime) {}
	
	

}
