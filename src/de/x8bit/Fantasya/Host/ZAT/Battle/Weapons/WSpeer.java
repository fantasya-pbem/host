package de.x8bit.Fantasya.Host.ZAT.Battle.Weapons;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Items.Speer;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Skills.Speerkampf;
import de.x8bit.Fantasya.Host.ZAT.Battle.Krieger;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapon;
import de.x8bit.Fantasya.util.Random;

public class WSpeer extends Weapon {
	public WSpeer(Unit unit) {
        super(unit);
        this.ursprungsItem = Speer.class;
    }

    /**
     * TODO
     * @return
     */
    @Override
    public int numberOfAttacks() { return 1; }

    @Override
    public boolean istFernkampfTauglich() { return false; }



    // --- Angriff
	public int AttackValue(int reihe_attacker, int reihe_defender)
	{
		return Krieger.Distanz(reihe_attacker, reihe_defender) == 0 ? getUnit().Talentwert(neededSkill()) : 0;
	}
	public int DefenceValue(int reihe_attacker, int reihe_defender)
	{
		return Krieger.Distanz(reihe_attacker, reihe_defender) == 0 ? getUnit().Talentwert(neededSkill()) : 0;
	}	
	public float AttackModifikation_Attacker(Krieger attacker, Krieger defender) 		{ return 0; }
	public float AttackModifikation_Defender(Krieger attacker, Krieger defender) 		{ return 0; }
	public float DefenceModifikation_Attacker(Krieger attacker, Krieger defender) 	{ return 0; }
	public float DefenceModifikation_Defender(Krieger attacker, Krieger defender) 	{ return 0; }

	// --- Schaden
	public int BlockValue() { return 0; }
    public float AverageBlockValue() { return 0f; }
	public int DamageValue(){ return Random.W(7) + 6; }
    public float AverageDamageValue() { return 10f; }

	public float BlockModifikation_Attacker(Krieger attacker, Krieger defender) 		{ return 0; }
	public float BlockModifikation_Defender(Krieger attacker, Krieger defender) 		{ return 0; }
	public float DamageModifikation_Attacker(Krieger attacker, Krieger defender) 		{ return 0; }
	public float DamageModifikation_Defender(Krieger attacker, Krieger defender) 		{ return 0; }

	public Class<? extends Skill> neededSkill() { return Speerkampf.class; };
	public WeaponType getWeaponType() { return WeaponType.Distanzkampf; }

    @Override
    public Class<? extends Item> getUrsprungsItem() { return ursprungsItem; }

    @Override
    public String kurzCode() { return "Spe"; }


}
