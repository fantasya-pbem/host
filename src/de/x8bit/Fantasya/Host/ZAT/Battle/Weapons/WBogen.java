package de.x8bit.Fantasya.Host.ZAT.Battle.Weapons;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Items.Bogen;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Skills.Bogenschiessen;
import de.x8bit.Fantasya.Host.ZAT.Battle.Krieger;
import de.x8bit.Fantasya.util.Random;

public class WBogen extends SchussWaffe {
	public WBogen(Unit unit) {
        super(unit);
        this.ursprungsItem = Bogen.class;
    }

    @Override
    public int numberOfAttacks() { return 1; }

	// --- Angriff
	public int AttackValue(int reihe_attacker, int reihe_defender)
	{
		return Krieger.Distanz(reihe_attacker, reihe_defender) >= 1 ? getUnit().Talentwert(neededSkill()) : 0;
	}
	public int DefenceValue(int reihe_attacker, int reihe_defender)
	{
		return Krieger.Distanz(reihe_attacker, reihe_defender) >= 1 ? getUnit().Talentwert(neededSkill()) / 2 : 0;
	}	
	public float AttackModifikation_Attacker(Krieger attacker, Krieger defender) 		{ return 0; }
	public float AttackModifikation_Defender(Krieger attacker, Krieger defender) 		{ return 0; }
	public float DefenceModifikation_Attacker(Krieger attacker, Krieger defender) 	{ return 0; }
	public float DefenceModifikation_Defender(Krieger attacker, Krieger defender) 	{ return 0; }

	// --- Schaden
	public int DamageValue(){
        int tw = this.getUnit().Talentwert(neededSkill());
        return Random.W(tw / 2 + 4) + 4; }	// T0: 5..8; T4: 5..10; T6 5..11 T12: 5..15
    public float AverageDamageValue() {
        int tw = this.getUnit().Talentwert(neededSkill());
        return ((float)tw / 2f + 4) / 2f + 4.5f;
    }
    
	public float BlockModifikation_Attacker(Krieger attacker, Krieger defender) 		{ return 0; }
	public float BlockModifikation_Defender(Krieger attacker, Krieger defender) 		{ return 0; }
	public float DamageModifikation_Attacker(Krieger attacker, Krieger defender) 		{ return 0; }
	public float DamageModifikation_Defender(Krieger attacker, Krieger defender) 		{ return 0; }

	public Class<? extends Skill> neededSkill() { return Bogenschiessen.class; }

    @Override
    public Class<? extends Item> getUrsprungsItem() { return ursprungsItem; }

    @Override
    public String kurzCode() { return "Bog"; }
}
