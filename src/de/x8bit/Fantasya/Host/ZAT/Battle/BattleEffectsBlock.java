package de.x8bit.Fantasya.Host.ZAT.Battle;

public interface BattleEffectsBlock extends BattleEffects {

	/** 
	 * Berechnung nach der eigentlichen Blockberechnung ... hier werden die verschiedenen
	 * Werte entsprechend modifiziert um ggf. einen Block zu verbessern
	 * @param bed - alle Infos des Kampfes
	 */
    @Override
	void Calculate(BattleEffectData bed);

}
