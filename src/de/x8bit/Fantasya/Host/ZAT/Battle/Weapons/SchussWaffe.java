package de.x8bit.Fantasya.Host.ZAT.Battle.Weapons;

import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapon;

/**
 *
 * @author hb
 */
public abstract class SchussWaffe extends Weapon {
    public SchussWaffe(Unit u) { super(u); }

    @Override
    public boolean istFernkampfTauglich() { return true; }

    /**
     * Fernkampfwaffen schlucken normalerweise keinen Schaden.
     * @return
     */
    @Override
    public final int BlockValue() { return 0; }

    @Override
    public final float AverageBlockValue() { return 0f; }

    @Override
	public final WeaponType getWeaponType() { return WeaponType.Fernkampf; }
}
