package de.x8bit.Fantasya.Host.ZAT.Battle;

public interface BattleEffectsDefence extends BattleEffects {

	/** 
	 * Berechnung nach der eigentlichen Verteidigungsberechnung ... hier werden die verschiedenen
	 * Werte entsprechend modifiziert um ggf. einen Angriff abzubrechen - verlieren - verbessern - .......
	 * @param bed - alle Infos des Kampfes
	 */
    @Override
	void Calculate(BattleEffectData bed);
	
}
