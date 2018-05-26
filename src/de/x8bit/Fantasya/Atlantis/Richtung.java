package de.x8bit.Fantasya.Atlantis;

import de.x8bit.Fantasya.util.Random;

/**
 * @author   mogel
 */
public enum Richtung 
{
	// Änderung an der Reihenfolge muss in #Coords angepasst werden !!!
	/**
	 * @uml.property  name="nordwesten"
	 * @uml.associationEnd  
	 */
	Nordwesten("NW", new String[] {"NW", "Nordwesten"}, -1, 1),
	/**
	 * @uml.property  name="nordosten"
	 * @uml.associationEnd  
	 */
	Nordosten("NO", new String[] {"NO", "Nordosten"}, 0, 1),
	/**
	 * @uml.property  name="osten"
	 * @uml.associationEnd  
	 */
	Osten("O", new String[] {"O", "Osten"}, 1, 0),
	/**
	 * @uml.property  name="suedosten"
	 * @uml.associationEnd  
	 */
	Suedosten("SO", new String[] {"SO", "Suedosten", "Südosten"}, 1, -1),
	/**
	 * @uml.property  name="suedwesten"
	 * @uml.associationEnd  
	 */
	Suedwesten("SW", new String[] {"SW", "Suedwesten", "Südwesten"}, 0, -1),
	/**
	 * @uml.property  name="westen"
	 * @uml.associationEnd  
	 */
	Westen("W", new String[] {"W", "Westen"}, -1, 0);


	/** shortcut */
	private String shortcut;
	/** full name */
	private String[] names;
	/** The shift in x direction */
	private int dx;
	/** The shift in y direction. */
	private int dy;

	/** Erzeugt die Richtung.
	 *
	 * @param shortcut das Kuerzel ("O" fuer Osten etc.)
	 * @param names eine Liste von moeglichen Namen fuer diese Richtung.
	 * @param dx um wieviel sich die x-Koordinate aendert
	 * @param dy um wieviel sich die y-Koordinate aendert
	 */
	private Richtung(String shortcut, String[] names, int dx, int dy) {
		this.shortcut = shortcut;
		this.names = names;
		this.dx = dx;
		this.dy = dy;
	}

	public String getShortcut() {
		return shortcut;
	}

	public int getDx() {
		return dx;
	}

	public int getDy() {
		return dy;
	}

	public Richtung invert() {
		switch(this) {
			case Nordosten: 	return Suedwesten;
			case Nordwesten:	return Suedosten;
			case Osten:			return Westen;
			case Suedwesten:	return Nordosten;
			case Suedosten:		return Nordwesten;
			case Westen:		return Osten;
		}

		throw new IllegalStateException("Unbekannte Richtung.");
	}

	/**
	 * macht aus einer Abkürzung wieder eine Richtung
	 * @param richtung - NO / NW / O / SO / SW / W - (geht auch Nordosten / Nordwesten / etc.)
	 * @return die passende Richtung
	 * @throws IllegalArgumentException falls die Zeichenkette auf keine Richtung passt.
	 */
	public static Richtung getRichtung(String text)	{
		for (Richtung dir : values()) {
			for (String name : dir.names) {
				if (name.equalsIgnoreCase(text)) {
					return dir;
				}
			}
		}

		throw new IllegalArgumentException("Zeichenkette beschreibt keine gueltige Richtung.");
	}

	/** Liefert eine zufaellige Richtung zurueck. */
	public static Richtung random() {
		int entry = Random.rnd(0, values().length);
		return values()[entry];
	}

	/**
	 * liefert eine Nachbarrichtung zur aktuellen Richtung zurück (z.B. abdriften beim Sturm)
	 * @return
	 */
	public Richtung randomNachbar() {
		int entry = this.ordinal() + (Random.rnd(0, 100) < 50 ? 1 : -1);	// eine der beiden Nachbarregionen
		if (entry < 0) entry = values().length - 1;
		if (entry >= values().length) entry = 0;
		return values()[entry];
	}
	
	
}
