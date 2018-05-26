package de.x8bit.Fantasya.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** Erlaubt Namen, die mehrere Schreibweisen besitzen.
 *
 * Ein uebliches Problem ist, dass bestimmte Objektnamen mehrere Schreibweisen haben.
 * Prominentes Beispiel sind Umlaute, die man mit ae, oe, ue transkribieren kann.
 * Derartige Namen sind in dieser Datenstruktur zusammengefasst, und koennen bequem
 * gematched werden.
 *
 * @author hb
 */
public class ComplexName {
	String singular;
	String plural;

	/** List of all valid strings for the name, including the singular and plural forms. */
	Set<String> aliases = new HashSet<String>();


	/** Convenience constructor. */
    public ComplexName(String singular, String plural) {
		this(singular, plural, null);
    }

	/** Creates a new complex name.
	 *
	 * @param singular  the singular form of the most common name
	 * @param plural    the plural form of the most common name
	 * @param aliases   alternative forms of the name (both singular and plural)
	 * @throws IllegalArgumentException if no singular or plural form is supplied.
	 */
	public ComplexName(String singular, String plural, String[] aliases) {
		if (singular == null || plural == null) {
			throw new IllegalArgumentException("complex name needs a singular and a plural string.");
		}

		this.singular = singular;
		this.plural = plural;

		if (aliases != null) {
			for (String s : aliases) {
				if (s == null) {
					throw new IllegalArgumentException("No null strings allowed");
				}
				
				this.aliases.add(s);
			}
		}
		
		this.aliases.add(singular);
		this.aliases.add(plural);
	}

	/** Returns true if the supplied name matches any of the forms. */
	public boolean matches(String name) {
		return aliases.contains(name);
	}

	/** Returns the common name of the object.
	 *
	 * @param count   the number of items to be described. Used to determine
	 *                whether we use the singular or plural form.
	 * @throws IllegalArgumentException if the count is smaller than zero.
	 */
	public String getName(int count) {
		if (count < 0) {
			throw new IllegalArgumentException("Negative counts should not be possible.");
		}

		if (count <= 1) {
			return singular;
		}
		return plural;
	}

	/** Returns all ways of spelling this object. */
	public Set<String> getAliases() {
		return Collections.unmodifiableSet(aliases);
	}
}
