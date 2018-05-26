package de.x8bit.Fantasya.Host.ZAT.Battle;

public interface BattleEffectsDamage extends BattleEffects {

	/** 
	 * Berechnung nach der eigentlichen Schadensberechnung ... hier werden die verschiedenen
	 * Werte entsprechend modifiziert um ggf. einen Schaden zu verbessern - generell immer Schaden zu machen - .....
	 * @param bed - alle Infos des Kampfes
	 */
    @Override
	public void Calculate(BattleEffectData bed);
	
}
