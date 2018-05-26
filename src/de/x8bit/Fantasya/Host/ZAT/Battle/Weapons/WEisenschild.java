package de.x8bit.Fantasya.Host.ZAT.Battle.Weapons;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Items.Eisenschild;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.ZAT.Battle.Krieger;
import de.x8bit.Fantasya.util.Random;

public class WEisenschild extends Schild {
	public WEisenschild(Unit unit) {
        super(unit);
        this.ursprungsItem = Eisenschild.class;
    }

	// --- Angriff
	public float AttackModifikation_Attacker(Krieger attacker, Krieger defender) 		{ return 0 - Random.rnd(10, 15); }
	public float AttackModifikation_Defender(Krieger attacker, Krieger defender) 		{ return 0; }
	public float DefenceModifikation_Attacker(Krieger attacker, Krieger defender) 	{ return Random.rnd(10, 15); }
	public float DefenceModifikation_Defender(Krieger attacker, Krieger defender) 	{ return 0 - Random.rnd(10, 15); }
	// --- Schaden
	public int BlockValue() { return Random.rnd(3, 7); } // 3..6 = W4+2
    public float AverageBlockValue() { return 4.5f; }

	public float BlockModifikation_Attacker(Krieger attacker, Krieger defender) 		{ return 0; }
	public float BlockModifikation_Defender(Krieger attacker, Krieger defender) 		{ return 0; }
	public float DamageModifikation_Attacker(Krieger attacker, Krieger defender) 		{ return 0; }
	public float DamageModifikation_Defender(Krieger attacker, Krieger defender) 		{ return 0; }

    @Override
    public Class<? extends Item> getUrsprungsItem() { return ursprungsItem; }

    @Override
    public String kurzCode() { return "-\u25cf-"; } // black circle
}
