package de.x8bit.Fantasya.Host.ZAT.Battle.Weapons;

import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapon;

/**
 *
 * @author hb
 */
public abstract class Tier extends Weapon {
    protected Tier(Unit u) { super(u); }

    /**
     * Tiere schlucken normalerweise keinen Schaden.
     * @return
     */
    public final int BlockValue() { return 0; }

    @Override
    public final float AverageBlockValue() { return 0f; }


	public final WeaponType getWeaponType() { return WeaponType.Tier; }
}
