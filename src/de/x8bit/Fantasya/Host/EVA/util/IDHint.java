package de.x8bit.Fantasya.Host.EVA.util;

import de.x8bit.Fantasya.Atlantis.Atlantis;

/**
 *
 * @author hapebe
 */
public class IDHint implements ParamHint {
	protected final int position;

	public static final IDHint LAST = new IDHint(Integer.MAX_VALUE);

	public IDHint(int position) {
		this.position = position;
	}


	public Class<? extends Atlantis> getType() {
		return Atlantis.class;
	}

	public int getPosition() {
		return position;
	}

}
