package de.x8bit.Fantasya.Host.EVA.util;

import de.x8bit.Fantasya.Atlantis.Atlantis;
import de.x8bit.Fantasya.Atlantis.Item;

/**
 *
 * @author hapebe
 */
public class ItemHint implements ParamHint {
	protected final int position;

	public static final ItemHint LAST = new ItemHint(Integer.MAX_VALUE);

	public ItemHint(int position) {
		this.position = position;
	}


	public Class<? extends Atlantis> getType() {
		return Item.class;
	}

	public int getPosition() {
		return position;
	}

}
