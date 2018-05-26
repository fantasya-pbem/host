package de.x8bit.Fantasya.Host.ZAT.Battle.Weapons;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Items.Schwert;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Skills.Hiebwaffen;
import de.x8bit.Fantasya.Host.ZAT.Battle.Krieger;
import de.x8bit.Fantasya.util.Random;

/**
 * das Schwert ist eine Waffe ... dies ermöglicht erst überhaupt einen sinnvollen Angriffswert 
 * @author mogel
 */
public class WSchwert extends NahkampfWaffe
{
	public WSchwert(Unit unit) {
        super(unit);
        this.ursprungsItem = Schwert.class;
    }
    
	public float AttackModifikation_Attacker(Krieger attacker, Krieger defender) 		{ return 0; }
	public float AttackModifikation_Defender(Krieger attacker, Krieger defender) 		{ return 0; }
	public float DefenceModifikation_Attacker(Krieger attacker, Krieger defender) 		{ return 0; }
	public float DefenceModifikation_Defender(Krieger attacker, Krieger defender) 		{ return 0; }
	// --- Schaden
	public int DamageValue(){ return Random.W(7) + 6; }
    public float AverageDamageValue() { return 10f; }

	public float BlockModifikation_Attacker(Krieger attacker, Krieger defender) 		{ return 0; }
	public float BlockModifikation_Defender(Krieger attacker, Krieger defender) 		{ return 0; }
	public float DamageModifikation_Attacker(Krieger attacker, Krieger defender) 		{ return 0; }
	public float DamageModifikation_Defender(Krieger attacker, Krieger defender) 		{ return 0; }

	public Class<? extends Skill> neededSkill() { return Hiebwaffen.class; }

    public Class<? extends Item> getUrsprungsItem() { return this.ursprungsItem; }

    @Override
    public String kurzCode() { return "Swe"; }
}
