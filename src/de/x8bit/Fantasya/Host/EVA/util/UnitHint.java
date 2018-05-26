package de.x8bit.Fantasya.Host.EVA.util;

import de.x8bit.Fantasya.Atlantis.Atlantis;
import de.x8bit.Fantasya.Atlantis.Unit;

/**
 *
 * @author hapebe
 */
public class UnitHint implements ParamHint {
	protected final int position;

	public static final UnitHint LAST = new UnitHint(Integer.MAX_VALUE);

	public UnitHint(int position) {
		this.position = position;
	}


	public Class<? extends Atlantis> getType() {
		return Unit.class;
	}

	public int getPosition() {
		return position;
	}

}
