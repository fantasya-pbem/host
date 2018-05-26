package de.x8bit.Fantasya.Host.ZAT.Battle.Weapons;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Items.Holzschild;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.ZAT.Battle.Krieger;
import de.x8bit.Fantasya.util.Random;

public class WHolzschild extends Schild {
	public WHolzschild(Unit unit) {
        super(unit);
        this.ursprungsItem = Holzschild.class;
    }

	// --- Angriff
	public float AttackModifikation_Attacker(Krieger attacker, Krieger defender) 	{ return 0 - Random.rnd(5, 10);}
	public float AttackModifikation_Defender(Krieger attacker, Krieger defender) 	{ return 0; }
	public float DefenceModifikation_Attacker(Krieger attacker, Krieger defender) 	{ return 0; }
	public float DefenceModifikation_Defender(Krieger attacker, Krieger defender) 	{ return 0 - Random.rnd(5, 10); }
	// --- Schaden
	public int BlockValue() { return Random.rnd(2, 5); } // 2..4 = W3+1
    public float AverageBlockValue() { return 3f; }


	public float BlockModifikation_Attacker(Krieger attacker, Krieger defender) 		{ return 0; }
	public float BlockModifikation_Defender(Krieger attacker, Krieger defender) 		{ return 0; }
	public float DamageModifikation_Attacker(Krieger attacker, Krieger defender) 		{ return 0; }
	public float DamageModifikation_Defender(Krieger attacker, Krieger defender) 		{ return 0; }

    @Override
    public Class<? extends Item> getUrsprungsItem() { return ursprungsItem;  }

    public String kurzCode() { return "-\u2742-"; } // circled open centre eight pointed star
}
