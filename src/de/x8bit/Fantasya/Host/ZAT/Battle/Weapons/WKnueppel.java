package de.x8bit.Fantasya.Host.ZAT.Battle.Weapons;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Skills.Hiebwaffen;
import de.x8bit.Fantasya.Host.ZAT.Battle.Krieger;
import de.x8bit.Fantasya.util.Random;

/**
 * Die Waffe von Goblins und Kobolden
 * @author mogel
 */
public class WKnueppel extends NahkampfWaffe {
	public WKnueppel(Unit unit) { super(unit); }

	public float AttackModifikation_Attacker(Krieger attacker, Krieger defender) 		{ return 0; }
	public float AttackModifikation_Defender(Krieger attacker, Krieger defender) 		{ return 0; }
	public float DefenceModifikation_Attacker(Krieger attacker, Krieger defender) 		{ return 0; }
	public float DefenceModifikation_Defender(Krieger attacker, Krieger defender) 		{ return 0; }

	// --- Schaden
	public int DamageValue(){ return Random.W(3) + 1; } // W3 + 1 ~ 2 .. 4
    public float AverageDamageValue() { return 3f; }

	public float BlockModifikation_Attacker(Krieger attacker, Krieger defender) 		{ return 0; }
	public float BlockModifikation_Defender(Krieger attacker, Krieger defender) 		{ return 0; }
	public float DamageModifikation_Attacker(Krieger attacker, Krieger defender) 		{ return 0; }
	public float DamageModifikation_Defender(Krieger attacker, Krieger defender) 		{ return 0; }

	public Class<? extends Skill> neededSkill() { return Hiebwaffen.class; }

    @Override
    public Class<? extends Item> getUrsprungsItem() { return null; }

    @Override
    public String kurzCode() { return "Knü"; }

}
