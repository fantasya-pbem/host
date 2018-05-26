package de.x8bit.Fantasya.Host.ZAT.Battle.Weapons;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Items.Katapult;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Skills.Katapultbedienung;
import de.x8bit.Fantasya.Host.ZAT.Battle.Krieger;
import de.x8bit.Fantasya.util.Random;

public class WKatapult extends SchussWaffe {
	public WKatapult(Unit unit) {
        super(unit);
        this.ursprungsItem = Katapult.class;
    }

	private int shootcounter = Random.rnd(1, 5);

    /**
     * TODO
     * @return
     */
    @Override
    public int numberOfAttacks() {
        if (shootcounter != 0) {
			return 0;
		}
        return Random.rnd(10, 21);
    }

    @Override
    public void naechsteRunde() {
        shootcounter = (shootcounter + 1) % 5;
    }

	// --- Angriff
	public int AttackValue(int reihe_attacker, int reihe_defender) {
		return Krieger.Distanz(reihe_attacker, reihe_defender) >= 1 ? getUnit().Talentwert(neededSkill()) : 0;
	}
	public int DefenceValue(int reihe_attacker, int reihe_defender) 				{ return 0; }	
	public float AttackModifikation_Attacker(Krieger attacker, Krieger defender) 		{ return 0; }
	public float AttackModifikation_Defender(Krieger attacker, Krieger defender) 		{ return 0; }
	public float DefenceModifikation_Attacker(Krieger attacker, Krieger defender) 	{ return 0; }
	public float DefenceModifikation_Defender(Krieger attacker, Krieger defender) 	{ return 0; }
	// --- Schaden
	public int DamageValue(){ return Random.rnd(1, 21) + Random.rnd(1, 21) + Random.rnd(1, 21); }	 // 3W20
    public float AverageDamageValue() { return 31.5f; }

	public float BlockModifikation_Attacker(Krieger attacker, Krieger defender) 		{ return 0; }
	public float BlockModifikation_Defender(Krieger attacker, Krieger defender) 		{ return 0; }
	public float DamageModifikation_Attacker(Krieger attacker, Krieger defender) 		{ return 0; }
	public float DamageModifikation_Defender(Krieger attacker, Krieger defender) 		{ return 0; }

	public Class<? extends Skill> neededSkill() { return Katapultbedienung.class; }

    @Override
    public Class<? extends Item> getUrsprungsItem() { return ursprungsItem; }

    @Override
    public String kurzCode() { return "Kat"; }
}
