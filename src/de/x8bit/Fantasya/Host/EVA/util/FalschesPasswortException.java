package de.x8bit.Fantasya.Host.EVA.util;

import de.x8bit.Fantasya.Atlantis.Partei;

/**
 *
 * @author hapebe
 */
public class FalschesPasswortException extends RuntimeException {
	private static final long serialVersionUID = 8572273680049034709L;
	
	final Partei partei;

	public FalschesPasswortException(Partei p) {
		this.partei = p;
	}

	public Partei getPartei() {
		return partei;
	}
}
