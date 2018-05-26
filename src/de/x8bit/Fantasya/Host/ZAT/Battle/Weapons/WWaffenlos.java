package de.x8bit.Fantasya.Host.ZAT.Battle.Weapons;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Skills.Monsterkampf;
import de.x8bit.Fantasya.Host.ZAT.Battle.Krieger;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapon;
import de.x8bit.Fantasya.util.Random;

// TODO: Minimalchance - Regeländerung vorschlagen / bestätigen lassen
public class WWaffenlos extends Weapon {
	public WWaffenlos(Unit unit) { super(unit); }

    @Override
    public int numberOfAttacks() { return 1; }
    
    @Override
    public boolean istFernkampfTauglich() { return false; }
	

    // --- Angriff
	public int AttackValue(int reihe_attacker, int reihe_defender) {
        if (Krieger.Distanz(reihe_attacker, reihe_defender) == 0) return 1;
        return 0;
    }
	public int DefenceValue(int reihe_attacker, int reihe_defender) 					{ return 0; }
	public float AttackModifikation_Attacker(Krieger attacker, Krieger defender)		{ return 0; }
	public float AttackModifikation_Defender(Krieger attacker, Krieger defender) 		{ return 0; }
	public float DefenceModifikation_Attacker(Krieger attacker, Krieger defender) 		{ return 0; }
	public float DefenceModifikation_Defender(Krieger attacker, Krieger defender) 		{ return 0; }

	// --- Schaden
	public int BlockValue() 															{ return 0; }
    public float AverageBlockValue() { return 0f; }

    @Override
    public int DamageValue() {
        // p=0.5  :  = 0
        // p~ 6%  :  > 3
        return (int)Math.round(Random.NextChiSquare(1, 1));
    }
    public float AverageDamageValue() { return 1f; }

	public float BlockModifikation_Attacker(Krieger attacker, Krieger defender) 		{ return 0; }
	public float BlockModifikation_Defender(Krieger attacker, Krieger defender) 		{ return 0; }
	public float DamageModifikation_Attacker(Krieger attacker, Krieger defender) 		{ return 0; }
	public float DamageModifikation_Defender(Krieger attacker, Krieger defender) 		{ return 0; }

	public Class<? extends Skill> neededSkill() 										{ return Monsterkampf.class; }
	public WeaponType getWeaponType() { return WeaponType.keineWaffe; }

    @Override
    public Class<? extends Item> getUrsprungsItem() { return null; }

    @Override
    public String kurzCode() { return "nix"; }

}
