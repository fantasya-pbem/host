package de.x8bit.Fantasya.Host.ZAT.Battle.Effects;

import de.x8bit.Fantasya.Host.ZAT.Battle.BattleEffectData;
import de.x8bit.Fantasya.Host.ZAT.Battle.BattleEffectsAttack;
import de.x8bit.Fantasya.Host.ZAT.Battle.Gefecht;
import de.x8bit.Fantasya.Host.ZAT.Battle.Gefecht.BattleTime;
import de.x8bit.Fantasya.util.Random;

/**
 * implementiert die Effekte des Zaubers "Erdbeben"
 * @author mogel
 */
public class BFXErdbeben implements BattleEffectsAttack {
	
	/** Anzahl der noch möglichen Angriffe - in Prozent */
	private int prozent;
    private final int erholung;

    /**
     * @param prozent anfängliche Chance, dass der eigene Angriff ausfällt
     * @param erholung diese Chance nimmt jede Kampfrunde um 0..erholung Punkte ab
     */
    public BFXErdbeben(int prozent, int erholung) {
        this.prozent = prozent;
        this.erholung = erholung;
    }
	
	@Override
	public void Calculate(BattleEffectData bed) {
		if (prozent < 1) return; // Effekt "abgelaufen"
		
		// jetzt testen ob der Angriff definitiv erfolgreich ist
		if (Random.rnd(0, 100) < prozent) {
            bed.addMessage(bed.getAttacker() + " ist verunsichert...");
            bed.setAttackvalue(0);
        }

		// Prozente die nach einem Angriff (Runde) abgezogen werden 
		// prozent -= Random.rnd(0, erholung + 1);
	}

    @Override
    public String toString() {
        return "Erdbeben " + prozent + "%";
    }

	@Override
	public void setEffectDownFor(BattleTime battleTime) {
		if (battleTime == Gefecht.BattleTime.ROUND)
		{
			// Prozente die nach einem Angriff (Runde) abgezogen werden 
			prozent -= Random.rnd(0, erholung + 1);
		}
	}



}
