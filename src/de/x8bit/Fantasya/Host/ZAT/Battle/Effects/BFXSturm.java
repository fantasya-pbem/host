package de.x8bit.Fantasya.Host.ZAT.Battle.Effects;

import de.x8bit.Fantasya.Host.ZAT.Battle.BattleEffectData;
import de.x8bit.Fantasya.Host.ZAT.Battle.BattleEffectsAttack;
import de.x8bit.Fantasya.Host.ZAT.Battle.Gefecht;
import de.x8bit.Fantasya.Host.ZAT.Battle.Gefecht.BattleTime;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapon;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapons.SchussWaffe;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapons.WArmbrust;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapons.WKatapult;

/**
 * implementiert die Effekte des Prä-Kampf-Zaubers "Sturm"
 * @author hapebe
 */
public class BFXSturm implements BattleEffectsAttack {

    /**
     * @param dauer die maximale Dauer dieses Sturms in Kampfrunden
     */
    public BFXSturm(int dauer) {
        this.dauer = dauer;
    }
    
    private final int dauer;
    
    /**
     * wie viele Kampfrunden wirkt der Effekt schon?
     */
    private int alter = 0;
	
	@Override
	public void Calculate(BattleEffectData bed) {
        if (alter >= dauer) return; // Effekt "abgelaufen"
        
		// if (prozent < 1) return; // Effekt "abgelaufen"
		
		// Prozente die nach einem erfolgreichen Angriff abgezogen werden 
		// int weniger = Atlantis.rnd(0, prozent);
		
		// jetzt testen ob der Angriff definitiv erfolgreich ist
		// if (Atlantis.rnd(0, 100) < prozent) {  }

		// die Prozente reduzieren
		// prozent -= weniger;
        
        Weapon w = bed.getAttacker().usedWeapon();
        if (w instanceof SchussWaffe ) {
            String wasIsses = "den Pfeil";
            if (w instanceof WArmbrust) wasIsses = "den Bolzen";
            if (w instanceof WKatapult) wasIsses = "das Bombardement";
            
            bed.addMessage(bed.getAttacker() + ": Eine Böe fegt " + wasIsses + " beiseite.");
            bed.setAttackvalue(0);
        }
        
        // alter ++;
	}

    @Override
    public String toString() {
        if (alter == 0) return "Sturm";
        return "Sturm seit " + alter + " Kampfrunden";
    }

	@Override
	public void setEffectDownFor(BattleTime battleTime) {
		if (battleTime == Gefecht.BattleTime.ATTACK_ATTACKER)
		{
			alter ++;
		}
	}
    
}
