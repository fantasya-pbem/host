package de.x8bit.Fantasya.Atlantis.Units;

import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Helper.Monster;
import de.x8bit.Fantasya.Atlantis.Skills.Alchemie;

/**
 * 
 * @author mogel
 *
 * yea ... good old zombies from atlantis
 * 
 */

public class Zombie extends Monster
{
	public Zombie() { }
	
	public int Talentwert(Skill skill)
	{
		int tw = super.Talentwert(skill);
		if (skill.getClass() == Alchemie.class) tw += 2;
		return (tw < 0 ? 0 : tw);
	}

	public int Trefferpunkte() { return 30; }
	
	public boolean Lernen(Class<? extends Skill> skill) { return allowLernen(skill); }

	@Override
	public void planMonster() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean istSpielerRasse() { return false; }

	@Override
	public String getRassenName() {
		if (this.getPersonen() == 1) return getRasse();
		return "Zombies";
	}

    @Override
    public void meldungenAuswerten() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
