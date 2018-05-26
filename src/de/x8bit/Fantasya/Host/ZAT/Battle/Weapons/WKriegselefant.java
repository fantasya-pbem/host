package de.x8bit.Fantasya.Host.ZAT.Battle.Weapons;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Items.Kriegselefant;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Skills.Reiten;
import de.x8bit.Fantasya.Host.ZAT.Battle.Krieger;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapon;
import de.x8bit.Fantasya.util.Random;

public class WKriegselefant extends Tier {
	public WKriegselefant(Unit unit) {
        super(unit);
        this.ursprungsItem = Kriegselefant.class;
    }

    @Override
    public int numberOfAttacks() {
        int n = 4;
        int tw = this.getUnit().Talentwert(this.neededSkill());
        if (tw < 3) {
            n -= 1;
        } else if (tw > 5) {
            n += (tw - 5);
        }
        return n;
    }

    @Override
    public boolean istFernkampfTauglich() { return false; }


    // --- Angriff
	@Override
	public int AttackValue(int reihe_attacker, int reihe_defender)
	{
		return Krieger.Distanz(reihe_attacker, reihe_defender) == 0 ? getUnit().Talentwert(neededSkill()) : 0;
	}
	@Override
	public int DefenceValue(int reihe_attacker, int reihe_defender)
	{
		return Krieger.Distanz(reihe_attacker, reihe_defender) == 0 ? getUnit().Talentwert(neededSkill()) : 0;
	}

	/**
     * Modifiziert den _eigenen_ AV von attacker, wird für alle Waffen von attacker aufgerufen.
     * @param attacker
     * @param defender
     * @return
     */
	@Override
    public float AttackModifikation_Attacker(Krieger attacker, Krieger defender) { return 0; }
    
    /**
     * Modifiziert den AV von attacker, wobei diese Methode für alle Waffen des defenders aufgerufen wird.
     * @param attacker
     * @param defender
     * @return
     */
	@Override
    public float AttackModifikation_Defender(Krieger attacker, Krieger defender) {
		/* Kriegselefanten können normalerweise mit Nahkampfwaffen gar nicht 
         * angegriffen werden, außer mit Speeren - bei denen sinkt der 
         * Angriffswert auf 50%.
         */
        float value = 0;
		if (attacker.usedWeapon().getClass().equals(WSpeer.class)) {
			value = -50;
		} else if(attacker.usedWeapon().getWeaponType() == Weapon.WeaponType.Nahkampf) {
            value = -100;
		}
		return value; 
	}
	
    /**
     * Modifiziert den DV von defender, wird für alle Waffen von attacker aufgerufen.
     * @param attacker
     * @param defender
     * @return
     */
	@Override
    public float DefenceModifikation_Attacker(Krieger attacker, Krieger defender) {
		/* gegen Kriegselefanten kann man sich normalerweise mit Nahkampfwaffen
         * gar nicht verteidigen, außer mit Speeren - bei denen sinkt der
         * Verteidigungswert auf 50%.
         */
        float value = 0;
		if (defender.usedWeapon().getClass().equals(WSpeer.class)) {
			value = -50;
		} else if (defender.usedWeapon().getWeaponType() == Weapon.WeaponType.Nahkampf) {
            value = -100;
		}
		return value;
    }
    
	@Override
	public float DefenceModifikation_Defender(Krieger attacker, Krieger defender) 	{ return 0; }
    
	// --- Schaden
	@Override
	public int DamageValue(){ return Random.rnd(5, 26); } // W20 + 4
	@Override
    public float AverageDamageValue() { return 14.5f; }

	@Override
	public float BlockModifikation_Attacker(Krieger attacker, Krieger defender) 		{ return 0; }
	@Override
	public float BlockModifikation_Defender(Krieger attacker, Krieger defender) 		{ return 0; }
	@Override
	public float DamageModifikation_Attacker(Krieger attacker, Krieger defender) 		{ return 0; }
	@Override
	public float DamageModifikation_Defender(Krieger attacker, Krieger defender) 		{ return 0; }

	@Override
	public Class<? extends Skill> neededSkill() { return Reiten.class; }

    @Override
    public Class<? extends Item> getUrsprungsItem() { return this.ursprungsItem; }

    @Override
    public String kurzCode() { return "KgE"; }

}
