package de.x8bit.Fantasya.Host.ZAT.Battle;

import de.x8bit.Fantasya.util.StringUtils;
import java.util.ArrayList;
import java.util.List;

public class BattleEffectData {

	private final Krieger attacker;
	private final Krieger defender;
	private float attackvalue;
	private float defencevalue;

    private final List<String> messages = new ArrayList<String>();

	/**
	 * alle Daten über den Angriff
	 * @param attacker - Angreifer
	 * @param defender - Verteidiger
	 * @param av - AttackValue oder BlockValue
	 * @param dv - DefenceValue oder DamageValue
	 */
	public BattleEffectData(Krieger attacker, Krieger defender, float av, float dv)	{
		this.attacker = attacker;
		this.defender = defender;
		this.attackvalue = av;
		this.defencevalue = dv;
	}
	
	public Krieger getAttacker() 					{ return attacker; }
	
	public Krieger getDefender() 					{ return defender; }
	
	public void setAttackvalue(float attackvalue) 	{ this.attackvalue = attackvalue; }
	public float getAttackvalue() 					{ return attackvalue; }
	
	public void setDefencevalue(float defencevalue)	{ this.defencevalue = defencevalue; }
	public float getDefencevalue() 					{ return defencevalue; }

    public void addMessage(String msg) {
        messages.add(msg);
    }

    public String getMessage() {
        return StringUtils.aufzaehlung(messages);
    }
	
}
