package de.x8bit.Fantasya.Host.ZAT.Battle.Weapons;

import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.ZAT.Battle.Krieger;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapon;

/**
 *
 * @author hb
 */
public abstract class NahkampfWaffe extends Weapon {

    protected NahkampfWaffe(Unit u) { super(u); }

    /**
     * TODO!
     * @return
     */
    @Override
    public int numberOfAttacks() {
        return 1;
    }

    @Override
    public boolean istFernkampfTauglich() { return false; }

	// --- Angriff
	public final int AttackValue(int reihe_attacker, int reihe_defender) {
		return Krieger.Distanz(reihe_attacker, reihe_defender) == 0 ? getUnit().Talentwert(neededSkill()) : 0;
	}
	public final int DefenceValue(int reihe_attacker, int reihe_defender)	{
		return Krieger.Distanz(reihe_attacker, reihe_defender) == 0 ? getUnit().Talentwert(neededSkill()) : 0;
	}

    /**
     * Nahkampfwaffen schlucken normalerweise keinen Schaden.
     * @return
     */
    public final int BlockValue() { return 0; }

    @Override
    public final float AverageBlockValue() { return 0f; }


	public final WeaponType getWeaponType() { return WeaponType.Nahkampf; }
}
