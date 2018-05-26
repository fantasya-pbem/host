package de.x8bit.Fantasya.Host.EVA.util;

import de.x8bit.Fantasya.Atlantis.Atlantis;

/**
 *
 * @author hapebe
 */
public class AnzahlHint implements ParamHint {
	protected final int position;

	public static final AnzahlHint LAST = new AnzahlHint(Integer.MAX_VALUE);

	public AnzahlHint(int position) {
		this.position = position;
	}


	@Override
	public Class<? extends Atlantis> getType() {
		return null;
	}

	@Override
	public int getPosition() {
		return position;
	}

}
