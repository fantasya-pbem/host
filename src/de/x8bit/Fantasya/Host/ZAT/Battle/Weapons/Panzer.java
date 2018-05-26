package de.x8bit.Fantasya.Host.ZAT.Battle.Weapons;

import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapon;

/**
 *
 * @author hb
 */
public abstract class Panzer extends Weapon {
    protected Panzer(Unit u) { super(u); }

    /**
     * Mit einem Panzer kann man nicht angreifen
     * @return Immer 0!
     */
    @Override
    public int numberOfAttacks() {
        return 0;
    }

    /** Mit einem Panzer kann man nicht angreifen */
    @Override
    public boolean istFernkampfTauglich() { return false; }

    /**
     * Mit einem Panzer kann man nicht angreifen
     * @param reihe_attacker
     * @param reihe_defender
     * @return Immer 0!
     */
    @Override
    public final int AttackValue(int reihe_attacker, int reihe_defender) { return 0; }

    /**
     * Mit einem Panzer kann man sich nicht verteidigen (im Sinne von parieren -
     * alles andere erledigen die Modifikation....-Methoden und BlockValue()
     * @param reihe_attacker
     * @param reihe_defender
     * @return Immer 0!
     */
    @Override
    public final int DefenceValue(int reihe_attacker, int reihe_defender) { return 0; }


    /**
     * Ein Schlag mit einem Panzer tut nicht weh!
     * @return Immer 0!
     */
    @Override
    public final int DamageValue() { return 0; }

    /**
     * Schläge mit einem Panzer tun nie weh!
     * @return Immer 0!
     */
    @Override
    public final float AverageDamageValue() { return 0f; }

    @Override
    public final WeaponType getWeaponType() { return WeaponType.Panzer;  }

    @Override
    public final Class<? extends Skill> neededSkill() { return Skill.class;  }


}
