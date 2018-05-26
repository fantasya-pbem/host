package de.x8bit.Fantasya.Atlantis.Units;

import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Skills.Alchemie;
import de.x8bit.Fantasya.Atlantis.Skills.Armbrustschiessen;
import de.x8bit.Fantasya.Atlantis.Skills.Ausdauer;
import de.x8bit.Fantasya.Atlantis.Skills.Bergbau;
import de.x8bit.Fantasya.Atlantis.Skills.Bogenbau;
import de.x8bit.Fantasya.Atlantis.Skills.Bogenschiessen;
import de.x8bit.Fantasya.Atlantis.Skills.Burgenbau;
import de.x8bit.Fantasya.Atlantis.Skills.Handel;
import de.x8bit.Fantasya.Atlantis.Skills.Hiebwaffen;
import de.x8bit.Fantasya.Atlantis.Skills.Holzfaellen;
import de.x8bit.Fantasya.Atlantis.Skills.Katapultbedienung;
import de.x8bit.Fantasya.Atlantis.Skills.Kraeuterkunde;
import de.x8bit.Fantasya.Atlantis.Skills.Magie;
import de.x8bit.Fantasya.Atlantis.Skills.Pferdedressur;
import de.x8bit.Fantasya.Atlantis.Skills.Reiten;
import de.x8bit.Fantasya.Atlantis.Skills.Religion;
import de.x8bit.Fantasya.Atlantis.Skills.Ruestungsbau;
import de.x8bit.Fantasya.Atlantis.Skills.Schiffbau;
import de.x8bit.Fantasya.Atlantis.Skills.Segeln;
import de.x8bit.Fantasya.Atlantis.Skills.Speerkampf;
import de.x8bit.Fantasya.Atlantis.Skills.Spionage;
import de.x8bit.Fantasya.Atlantis.Skills.Steinbau;
import de.x8bit.Fantasya.Atlantis.Skills.Steuereintreiben;
import de.x8bit.Fantasya.Atlantis.Skills.Strassenbau;
import de.x8bit.Fantasya.Atlantis.Skills.Taktik;
import de.x8bit.Fantasya.Atlantis.Skills.Tarnung;
import de.x8bit.Fantasya.Atlantis.Skills.Unterhaltung;
import de.x8bit.Fantasya.Atlantis.Skills.Waffenbau;
import de.x8bit.Fantasya.Atlantis.Skills.Wagenbau;
import de.x8bit.Fantasya.Atlantis.Skills.Wahrnehmung;
import de.x8bit.Fantasya.util.Random;

public class Aquaner extends Unit	// SKILL
{
	public Aquaner()
	{
		RekrutierungsKosten = 80;

		minHunger = 6;
		maxHunger = 10;
	}
	
	public int Talentwert(Skill skill)
	{
		int tw = super.Talentwert(skill);
		if (tw == 0) return 0;
		
		if (skill.getClass().equals(Armbrustschiessen.class))	tw += -1;	// SKILL
		if (skill.getClass().equals(Bogenschiessen.class))		tw +=  0;	// SKILL
		if (skill.getClass().equals(Katapultbedienung.class))	tw += -2;	// SKILL
		if (skill.getClass().equals(Hiebwaffen.class)) 			tw += -1;	// SKILL
		if (skill.getClass().equals(Speerkampf.class))			tw +=  1;	// SKILL
		if (skill.getClass().equals(Reiten.class))				tw += -1;	// SKILL
		if (skill.getClass().equals(Taktik.class))				tw +=  0;	// SKILL
		if (skill.getClass().equals(Bergbau.class))				tw += -1;	// SKILL
		if (skill.getClass().equals(Burgenbau.class))			tw +=  0;	// SKILL
		if (skill.getClass().equals(Handel.class))				tw +=  2;	// SKILL
		if (skill.getClass().equals(Holzfaellen.class))			tw +=  1;	// SKILL
		if (skill.getClass().equals(Magie.class))				tw +=  0;	// SKILL
		if (skill.getClass().equals(Pferdedressur.class))		tw += -1;	// SKILL
		if (skill.getClass().equals(Ruestungsbau.class))		tw += -2;	// SKILL
		if (skill.getClass().equals(Schiffbau.class))			tw +=  2;	// SKILL
		if (skill.getClass().equals(Segeln.class))				tw +=  2;	// SKILL
		if (skill.getClass().equals(Steinbau.class))			tw +=  0;	// SKILL
		if (skill.getClass().equals(Strassenbau.class))			tw += -2;	// SKILL
		if (skill.getClass().equals(Tarnung.class))				tw +=  0;	// SKILL
		if (skill.getClass().equals(Unterhaltung.class))		tw +=  0;	// SKILL
		if (skill.getClass().equals(Waffenbau.class))			tw +=  0;	// SKILL
		if (skill.getClass().equals(Wagenbau.class))			tw += -1;	// SKILL
		if (skill.getClass().equals(Wahrnehmung.class))			tw +=  0;	// SKILL
		if (skill.getClass().equals(Steuereintreiben.class))	tw +=  0;	// SKILL
		if (skill.getClass().equals(Bogenbau.class))			tw +=  0;	// SKILL
		if (skill.getClass().equals(Alchemie.class))			tw += -1;	// SKILL
		if (skill.getClass().equals(Kraeuterkunde.class))		tw +=  1;	// SKILL
		if (skill.getClass().equals(Spionage.class))			tw +=  0;	// SKILL
		if (skill.getClass().equals(Ausdauer.class))			tw +=  0;	// SKILL
		if (skill.getClass().equals(Religion.class))			tw += -1;	// SKILL

		return (tw < 0 ? 0 : tw);
	}

	public int Trefferpunkte() { return 25; }

	@Override
	public boolean istSpielerRasse() { return true;	}

	@Override
	public String getRassenName() {
		return "Aquaner"; // Einzahl und Mehrzahl sind gleich.
	}
}
