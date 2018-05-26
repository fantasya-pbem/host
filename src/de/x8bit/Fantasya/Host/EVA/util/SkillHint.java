package de.x8bit.Fantasya.Host.EVA.util;

import de.x8bit.Fantasya.Atlantis.Atlantis;
import de.x8bit.Fantasya.Atlantis.Skill;

/**
 *
 * @author hapebe
 */
public class SkillHint implements ParamHint {
	protected final int position;

	public static final SkillHint LAST = new SkillHint(Integer.MAX_VALUE);

	public SkillHint(int position) {
		this.position = position;
	}


	public Class<? extends Atlantis> getType() {
		return Skill.class;
	}

	public int getPosition() {
		return position;
	}

}
