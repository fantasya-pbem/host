package de.x8bit.Fantasya.Host.ZAT.Battle;

/**
 * Interface für alle Zaubereffekte im Kampf 
 * @author mogel
 */
public interface BattleEffectsAttack extends BattleEffects {
	
	/** 
	 * Berechnung nach der eigentlichen Angriffsberechnung ... hier werden die verschiedenen
	 * Werte entsprechend modifiziert um ggf. einen Angriff abzubrechen - verlieren - verbessern - .......
	 * @param bed - alle Infos des Kampfes
	 */
    @Override
	void Calculate(BattleEffectData bed);
	
}
