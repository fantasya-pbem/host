package de.x8bit.Fantasya.Host.EVA.util;

import de.x8bit.Fantasya.Atlantis.Coords;

/**
 *
 * @author hapebe
 */
public class MultiCoordsHint implements ParamHint {

	public MultiCoordsHint() {
		// nop - das ist einfach ein Marker-Objekt, dass durch seine nackte Existenz eine Information transportiert.
	}

	@SuppressWarnings("rawtypes")
	public Class getType() {
		return Coords.class;
	}

	public int getPosition() {
		throw new RuntimeException("Ein MultiCoordsHint möchte nicht nach seiner Position im Befehl gefragt werden.");
	}

}
