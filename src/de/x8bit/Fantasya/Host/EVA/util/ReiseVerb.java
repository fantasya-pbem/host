package de.x8bit.Fantasya.Host.EVA.util;

/**
 * Kapselt das Bewegungs-Verb für Meldungen über Reisen.
 * Wird in Bewegungen verwendet.
 * @author hapebe
 */
public class ReiseVerb {
	String verb;

	public ReiseVerb(String verb) {
		this.verb = verb;
	}

	public String getVerb() {
		return verb;
	}

	public void setVerb(String verb) {
		this.verb = verb;
	}

}
