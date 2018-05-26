package de.x8bit.Fantasya.Host.ZAT.Battle.Weapons;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Items.Pegasus;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Skills.Reiten;
import de.x8bit.Fantasya.Atlantis.Skills.Speerkampf;
import de.x8bit.Fantasya.Host.ZAT.Battle.Krieger;

public class WPegasus extends Tier
{
	public WPegasus(Unit unit) {
        super(unit);
        this.ursprungsItem = Pegasus.class;
    }

    @Override
    public int numberOfAttacks() { return 0; }

    @Override
    public boolean istFernkampfTauglich() { return false; }

	// --- Angriff
	public int AttackValue(int reihe_attacker, int reihe_defender) 					{ return 0; }
	public int DefenceValue(int reihe_attacker, int reihe_defender) 				{ return 0; }	
	public float AttackModifikation_Attacker(Krieger attacker, Krieger defender) 		
	{
		float value = 0;
		if (defender.usedWeapon().getClass().equals(WSpeer.class)) {
			value = -47 - 3 * defender.getUnit().Talentwert(Speerkampf.class);
		} else {
			value = 5 * attacker.getUnit().Talentwert(attacker.usedWeapon().neededSkill());
		}
		return value; 
	}
	public float AttackModifikation_Defender(Krieger attacker, Krieger defender) 		{ return 0; }
	public float DefenceModifikation_Attacker(Krieger attacker, Krieger defender) 		{ return 0; }
	public float DefenceModifikation_Defender(Krieger attacker, Krieger defender) 		{ return 0; }
	// --- Schaden
	public int DamageValue(){ return 0; }	
    public float AverageDamageValue() { return 0f; }

	public float BlockModifikation_Attacker(Krieger attacker, Krieger defender) 		{ return 0; }
	public float BlockModifikation_Defender(Krieger attacker, Krieger defender) 		{ return 0; }
	public float DamageModifikation_Attacker(Krieger attacker, Krieger defender) 		{ return 0; }
	public float DamageModifikation_Defender(Krieger attacker, Krieger defender) 		{ return 0; }

	public Class<? extends Skill>  neededSkill() { return Reiten.class; }
    
    @Override
    public Class<? extends Item> getUrsprungsItem() { return ursprungsItem; }

    @Override
    public String kurzCode() { return "Peg"; }
}
