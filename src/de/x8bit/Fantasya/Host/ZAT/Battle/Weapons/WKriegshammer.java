package de.x8bit.Fantasya.Host.ZAT.Battle.Weapons;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Items.Kriegshammer;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Skills.Hiebwaffen;
import de.x8bit.Fantasya.Host.ZAT.Battle.Krieger;
import de.x8bit.Fantasya.util.Random;

public class WKriegshammer extends NahkampfWaffe {
	public WKriegshammer(Unit unit) {
        super(unit);
        this.ursprungsItem = Kriegshammer.class;
    }

	// --- Angriff
	public float AttackModifikation_Attacker(Krieger attacker, Krieger defender) 		{ return 0; }
	public float AttackModifikation_Defender(Krieger attacker, Krieger defender) 		{ return 0; }
	public float DefenceModifikation_Attacker(Krieger attacker, Krieger defender) 	{ return 0; }
	public float DefenceModifikation_Defender(Krieger attacker, Krieger defender) 	{ return 0; }
    
	// --- Schaden
	public int DamageValue(){ return Random.W(7) + 10; }
    public float AverageDamageValue() { return 14f; }
    
	public float BlockModifikation_Attacker(Krieger attacker, Krieger defender) 		{ return 0; }
	public float BlockModifikation_Defender(Krieger attacker, Krieger defender) 		{ return 0; }
	public float DamageModifikation_Attacker(Krieger attacker, Krieger defender) 		{ return 0; }
	public float DamageModifikation_Defender(Krieger attacker, Krieger defender) 		{ return 0; }

	public Class<? extends Skill> neededSkill() { return Hiebwaffen.class; }

    public Class<? extends Item> getUrsprungsItem() { return ursprungsItem; }

    @Override
    public String kurzCode() { return "KHa"; }
}
