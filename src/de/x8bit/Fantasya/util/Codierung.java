package de.x8bit.Fantasya.util;

/** Hilfsfunktionen. */

// TODO: Kann mit FreieNummern vielleicht in eine Nummernutils-Klasse
// zusammengefasst werden

public class Codierung {
	
	/** Ausgabe einer Zahl in Base36-Codierung */
	public static String toBase36(int nummer) {
		return Integer.toString(nummer, 36).replace('l', 'L');
	}
	
	/** Umrechnung von Base36 nach Base10 */
	public static int fromBase36(String nummer) {
		return Integer.parseInt(nummer, 36);
	}
}	
