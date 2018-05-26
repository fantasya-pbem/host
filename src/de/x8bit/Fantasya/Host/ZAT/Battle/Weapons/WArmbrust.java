package de.x8bit.Fantasya.Host.ZAT.Battle.Weapons;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Items.Armbrust;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Skills.Armbrustschiessen;
import de.x8bit.Fantasya.Host.ZAT.Battle.Krieger;
import de.x8bit.Fantasya.util.Random;

public class WArmbrust extends SchussWaffe {
	public WArmbrust(Unit unit) {
        super(unit);
        this.ursprungsItem = Armbrust.class;
    }

	private int shootcounter = Random.rnd(0, 2);

    /**
     * TODO
     * @return 1
     */
    @Override
    public int numberOfAttacks() {
		return shootcounter;
    }

    @Override
    public void naechsteRunde() {
        shootcounter = 1 - shootcounter; // togglen
    }


	
	// --- Angriff
	public int AttackValue(int reihe_attacker, int reihe_defender) {
        return Krieger.Distanz(reihe_attacker, reihe_defender) == 1 ? getUnit().Talentwert(neededSkill()) : 0;
	}
	public int DefenceValue(int reihe_attacker, int reihe_defender)
	{
		return Krieger.Distanz(reihe_attacker, reihe_defender) == 1 ? getUnit().Talentwert(neededSkill()) / 2: 0;
	}	
	public float AttackModifikation_Attacker(Krieger attacker, Krieger defender) 		{ return 0; }
	public float AttackModifikation_Defender(Krieger attacker, Krieger defender) 		{ return 0; }
	public float DefenceModifikation_Attacker(Krieger attacker, Krieger defender) 	{ return 0; }
	public float DefenceModifikation_Defender(Krieger attacker, Krieger defender) 	{ return 0; }
	// --- Schaden
	public int DamageValue(){ return Random.W(3) + 9; }	
    public float AverageDamageValue() { return 11f; }

	public float BlockModifikation_Attacker(Krieger attacker, Krieger defender) 		{ return 0; }
	public float BlockModifikation_Defender(Krieger attacker, Krieger defender) 		{ return 0; }
	public float DamageModifikation_Attacker(Krieger attacker, Krieger defender) 		{ return 0; }
	public float DamageModifikation_Defender(Krieger attacker, Krieger defender) 		{ return 0; }

    @Override
	public Class<? extends Skill> neededSkill() { return Armbrustschiessen.class; }

    @Override
    public Class<? extends Item> getUrsprungsItem() { return ursprungsItem; }

    @Override
    public String kurzCode() { return "ArB"; }

}
