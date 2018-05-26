package de.x8bit.Fantasya.Host.EVA.util;

import de.x8bit.Fantasya.Atlantis.Atlantis;
import de.x8bit.Fantasya.Atlantis.Spell;

/**
 *
 * @author hapebe
 */
public class SpellHint implements ParamHint {
	protected final int position;

	public static final SpellHint LAST = new SpellHint(Integer.MAX_VALUE);

	public SpellHint(int position) {
		this.position = position;
	}


	public Class<? extends Atlantis> getType() {
		return Spell.class;
	}

	public int getPosition() {
		return position;
	}

}
