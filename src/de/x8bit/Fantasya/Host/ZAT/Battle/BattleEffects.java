package de.x8bit.Fantasya.Host.ZAT.Battle;

/**
 *
 * @author hb
 */
public interface BattleEffects {

	/**
     * Berechnet die Wirkungen des Effekts. Wird in den abgeleiteten Klassen / Interfaces weiter spezifiziert.
     * @param bed
     */
    void Calculate(BattleEffectData bed);
    
    /**
     * Setzt den Magischen Effekt um eine Stufe runter.
     */
    void setEffectDownFor(Gefecht.BattleTime battleTime);

}
