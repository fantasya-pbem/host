package de.x8bit.Fantasya.Host.ZAT.Battle.Weapons;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Items.Plattenpanzer;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.ZAT.Battle.Krieger;
import de.x8bit.Fantasya.util.Random;

public class WPlattenpanzer extends Panzer {
	public WPlattenpanzer(Unit unit) {
        super(unit);
        ursprungsItem = Plattenpanzer.class;
    }

	// --- Angriff
    // wenn der Angreifer einen Plattenpanzer hat:
	public float AttackModifikation_Attacker(Krieger attacker, Krieger defender) { return 0 - Random.rnd(10, 15); }
	public float DefenceModifikation_Attacker(Krieger attacker, Krieger defender) { return Random.rnd(10, 15); }
    // wenn der Verteidiger einen Plattenpanzer hat:
	public float AttackModifikation_Defender(Krieger attacker, Krieger defender) { return 0; }
	public float DefenceModifikation_Defender(Krieger attacker, Krieger defender) { return 0 - Random.rnd(10, 15); }
    
	// --- Schaden
	public int BlockValue() { return Random.W(4) + 3; } // 4 .. 7
    public float AverageBlockValue() { return 5.5f; }

    // wenn der Angreifer einen Plattenpanzer hat:
    public float BlockModifikation_Attacker(Krieger attacker, Krieger defender)	{ return 0; }
	public float DamageModifikation_Attacker(Krieger attacker, Krieger defender) { return 0; }
    
    // wenn der Verteidiger einen Plattenpanzer hat:
	public float BlockModifikation_Defender(Krieger attacker, Krieger defender)	{ 
		float value = 0;
		// gegen Bogen / Pfeile hilft der Plattenpanzer besonders gut:
        if (attacker.usedWeapon().getClass().equals(WBogen.class)) value = Random.rnd(5, 10);
		return value; 
	}
	public float DamageModifikation_Defender(Krieger attacker, Krieger defender) { return 0; }

    public Class<? extends Item> getUrsprungsItem() { return ursprungsItem;  }

    @Override
    public String kurzCode() { return "Pp#"; }

}
