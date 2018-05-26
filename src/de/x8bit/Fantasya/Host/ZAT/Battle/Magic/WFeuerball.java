package de.x8bit.Fantasya.Host.ZAT.Battle.Magic;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Messages.Battle;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Spell;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Skills.Magie;
import de.x8bit.Fantasya.Atlantis.Spells.Feuerball;
import de.x8bit.Fantasya.Atlantis.Spells.Feuerwalze;
import de.x8bit.Fantasya.Host.ZAT.Battle.Krieger;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapon;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapons.ITeureWaffe;
import de.x8bit.Fantasya.util.Random;

/**
 * Waffe für Zauberspruch "Feuerball"  und "Feuerwalze"
 * @author mogel
 */
public class WFeuerball extends Weapon implements ITeureWaffe {

	private int stufe = 0;
	private Spell spell = null;
	
	public WFeuerball(Unit unit, int stufe, Spell spell) {
		super(unit); 
		this.stufe = stufe; 
		this.spell = spell; 
	}

    @Override
    public boolean istFernkampfTauglich() { return true; }

    @Override
    public int numberOfAttacksNurZurInfo() {
        int kosten = stufe * spell.getStufe();
		if (getUnit().getAura() < kosten) {
            // zu wenig Aura ... neue Stufe berechnen
            stufe = getUnit().getAura() / spell.getStufe();
        }

        if (stufe < 1) return 0;

        int retval = 0;
        if (spell.getClass().equals(Feuerball.class)) {
            for (int i=0; i<stufe; i++) retval += Random.rnd(10, 20) * stufe;
            // 1 ~ 14.5 * 1
            // 2 ~ 14.5 * 4 => 58
            // 3 ~ 14.5 * 9 => 130.5
            // 4 ~ 14.5 * 16 => 232
            // 5 ~ 14.5 * 25 => 362.5
            // 6 ~ 14.5 * 36 => 522
            // TODO ganz schön heftig; das jeweils mit bis zu 10 Runden und für (Stufe) Aura pro Runde...
            // 3625 Feinde niedermähen für 55 Aura (Stufe 5); machbar für Magier T8
            // 5220 Feinde niedermähen für 66 Aura (Stufe 6); machbar für Magier T9
        }
        if (spell.getClass().equals(Feuerwalze.class)) {
            for (int i=0; i<stufe; i++) retval += Random.rnd(100, 200) * stufe;
            // jeweils etwas mehr als das 10fache, allerdings deutlich teurer wg. Zauber-Stufe 6
        }

        return retval;
    }

    @Override
    public int numberOfAttacks() {
        int kosten = stufe * spell.getStufe();
		if (getUnit().getAura() < kosten) {
            // zu wenig Aura ... neue Stufe berechnen
            stufe = getUnit().getAura() / spell.getStufe();
            kosten = stufe * spell.getStufe();
        }

        if (stufe < 1) {
            new Battle(getUnit() + " hat zu wenig Aura für den Angriff.", getUnit());
            return 0;
        }

        int retval = 0;
        if (spell.getClass().equals(Feuerball.class)) {
            for (int i=0; i<stufe; i++) retval += Random.rnd(10, 20); // * stufe;
        }
        if (spell.getClass().equals(Feuerwalze.class)) {
            for (int i=0; i<stufe; i++) retval += Random.rnd(100, 200); // * stufe;
        }

        getUnit().setAura(getUnit().getAura() - kosten);
        new Battle(
                    getUnit() + ": Angriff mit " + spell.getClass().getSimpleName() +
                    " auf Stufe " + stufe + " für " + kosten +
                    " Punkte Aura, hat dann noch " + getUnit().getAura() + ". Es werden " +
                    retval + " Gegner angegriffen.",
                    getUnit()
        );

		return retval;
    }

    // --- Angriff
	public int AttackValue(int reihe_attacker, int reihe_defender) {
		// das wird hier nur benutzt um zu testen ob der Zauber erfolgreich
		// ist oder ob er fehl schlägt ... der Zauberspruch erzeugt einen
		// CollSchaden und genau da wird die Mana verbraucht ... ABER
		// es wird getestet ob genug Aura vorhanden ist
        
        if (stufe < 1) return 0;

        int retval = Random.rnd(10, 20) * stufe;
        if (spell.getClass().equals(Feuerwalze.class)) retval = Random.rnd(20, 40) * stufe;
        
		return retval;
	}

	public int DefenceValue(int reihe_attacker, int reihe_defender) 					{ return 0; }	
	public float AttackModifikation_Attacker(Krieger attacker, Krieger defender)		{ return 0; }
	public float AttackModifikation_Defender(Krieger attacker, Krieger defender) 		{ return 0; }
	public float DefenceModifikation_Attacker(Krieger attacker, Krieger defender) 		{ return 0; }
	public float DefenceModifikation_Defender(Krieger attacker, Krieger defender) 		{ return 0; }
	// --- Schaden
	public int BlockValue() 															{ return 0; }
    public float AverageBlockValue()                                                    { return 0f;  }
    
	public int DamageValue() { return 100; }
    public float AverageDamageValue(){ return 100f; }

	public float BlockModifikation_Attacker(Krieger attacker, Krieger defender) 		{ return 0; }
	public float BlockModifikation_Defender(Krieger attacker, Krieger defender) 		{ return 0; }
	public float DamageModifikation_Attacker(Krieger attacker, Krieger defender) 		{ return 0; }
	public float DamageModifikation_Defender(Krieger attacker, Krieger defender) 		{ return 0; }

	public Class<? extends Skill> neededSkill() { return Magie.class; }
	public WeaponType getWeaponType() { return WeaponType.Kampfzauber; }

    @Override
    public Class<? extends Item> getUrsprungsItem() { return null; }

    @Override
    public String kurzCode() { return "!FB";  }



}
