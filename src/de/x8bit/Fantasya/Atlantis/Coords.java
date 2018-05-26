package de.x8bit.Fantasya.Atlantis;

import java.util.HashSet;
import java.util.Set;

/**
 * Verwaltung von Koordinaten ... das umfasst auch das Verschieben bzw. die Bewegung zu Nachbarregionen
 * @author  mogel
 */
@SuppressWarnings("rawtypes")
public final class Coords implements Comparable {

	/**
	 * @uml.property  name="x"
	 */
	private final int x;
	/**
	 * @uml.property  name="y"
	 */
	private final int y;
	/**
	 * @uml.property  name="welt"
	 */
	private final int welt;
	private final int hashCode;

	/**
	 * @return
	 * @uml.property  name="x"
	 */
	public int getX() {
		return x;
	}

	/**
	 * @return
	 * @uml.property  name="y"
	 */
	public int getY() {
		return y;
	}

	/**
	 * @return
	 * @uml.property  name="welt"
	 */
	public int getWelt() {
		return welt;
	}

	/**
	 * liefert die WHERE-Klausel für diese Koordinaten<br/>
	 * <i>koordx = $X AND koordy = $Y AND welt = $W</i>
	 * @param lang - ob die kurzen Koordinaten (also nur Koordinaten) oder die langen (in Verbindung mit anderen)
	 * @return SQL-String für die DB-Abfrage
	 */
	public String Where(boolean lang) {
		return "koordx=" + x + " AND koordy=" + y + " AND welt=" + welt;
	}

	public Coords(int x, int y, int welt) {
		if ((x < -8192) || (x > 8191)) {
			throw new IllegalArgumentException("Der Koordinatenbereich für x ist -8192 bis 8191 - versucht " + x);
		}
		if ((y < -8192) || (y > 8192)) {
			throw new IllegalArgumentException("Der Koordinatenbereich für y ist -8192 bis 8191 - versucht " + y);
		}
		if ((welt < -4) || (welt > 3)) {
			throw new IllegalArgumentException("Der Koordinatenbereich für Welt ist -4 bis +3 - versucht " + welt);
		}

		this.x = x;
		this.y = y;
		this.welt = welt;

		this.hashCode = asRegionID(true); // newMode - taugt auch als Standard-Sortierkriterium
	}

	public Coords(Coords coords) {
		this(coords.x, coords.y, coords.welt);
	}

	@Override
	public String toString() {
		return "(" + x + "," + y + "," + welt + ")";
	}

	public String xy() {
		return "(" + x + ", " + y + ")";
	}

	/**
	 * liefert die Koordinaten um die angegebene Richtung verschoben ... es wird
	 * ein neues Objekt erstellt !
	 * @param richtung - die Richtung in die verschoben werden soll
	 * @return neuen Koordinaten
	 */
	public Coords shift(Richtung richtung) {
		return new Coords(x + richtung.getDx(), y + richtung.getDy(), welt);
	}

	/**
	 * @return alle direkten Nachbarkoordinaten - also alle, für die
	 * getDistance() 1 liefert.
	 */
	public Set<Coords> getNachbarn() {
		Set<Coords> nachbarn = new HashSet<Coords>();

		for (Richtung ri : Richtung.values()) {
			nachbarn.add(shift(ri));
		}

		return nachbarn;
	}

	public Richtung getRichtungNach(Coords ziel) {
		if (ziel.getWelt() != this.getWelt()) {
			throw new UnsupportedOperationException("Die Koordinaten liegen nicht in der gleichen Welt - getRichtungNach() ist damit derzeit nicht möglich.");
		}
		if (ziel.getX() == getX() && ziel.getY() == getY()) {
			throw new IllegalArgumentException("Die Koordinaten müssen verschieden sein.");
		}

		int deltaX = ziel.getX() - this.getX();
		int deltaY = ziel.getY() - this.getY();

		if (deltaX == 0) {
			if (deltaY < 0) {
				return Richtung.Suedwesten;
			} 
			return Richtung.Nordosten;
		}
		if (deltaY == 0) {
			if (deltaX < 0) {
				return Richtung.Westen;
			}
			return Richtung.Osten;
		}

		// weder X noch Y sind 0:
		if ((deltaX > 0) && (deltaY > 0)) {
			return Richtung.Osten; // das ist dann immer mehr als einen Schritt entfernt, aber fangen wir's an.
		}
		if ((deltaX > 0) && (deltaY < 0)) {
			return Richtung.Suedosten;
		}
		if ((deltaX < 0) && (deltaY > 0)) {
			return Richtung.Nordwesten;
		}
		if ((deltaX < 0) && (deltaY < 0)) {
			return Richtung.Westen;
		}

		throw new RuntimeException("Das ist unmöglich!");
	}

	/**
	 * Vergleich der Koordinaten miteinander
	 * @param obj - anderen Koordinaten
	 * @return true wenn die Koordinaten stimmen
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj instanceof Coords) {
			final Coords that = (Coords) obj;
			if (this.hashCode == that.hashCode) {
				return true;
			}
		}
		return false;
	}

	public int getDistance(Coords ziel) {
		int dx = ziel.getX() - this.getX();
		int dy = ziel.getY() - this.getY();

		int absDX = Math.abs(dx);
		int absDY = Math.abs(dy);

		if ((dx < 0) && (dy > 0)) {
			return Math.max(absDX, absDY);
		}
		if ((dx > 0) && (dy < 0)) {
			return Math.max(absDX, absDY);
		}
		return absDX + absDY;
	}

	public int asRegionID(boolean newmode) {
		// alte Methode -- wechsel zu neuer Methode für Regionen-Propertys
		if (!newmode) {
			return (((getX() << 14) + getY()) << 2) + getWelt();
		}

		// neue Methode für die Properties
		int x1 = x + 8192;
		int y1 = y + 8192;
		int w1 = welt + 4;
		return (w1 << 28) + (y1 << 14) + x1;
	}

	@Override
	public int hashCode() {
		return this.hashCode;
	}

	public int compareTo(Coords other) {
		if (this.hashCode() < other.hashCode()) {
			return -1;
		}
		if (this.hashCode() > other.hashCode()) {
			return +1;
		}

		return 0;
	}

	@Override
	public int compareTo(Object o) {
		if (o == null) {
			throw new UnsupportedOperationException("Kann Coords nicht mit null vergleichen.");
		}
		if (getClass() != o.getClass()) {
			throw new UnsupportedOperationException("Kann Coords nicht mit " + o.getClass().getSimpleName() + " vergleichen.");
		}

		return compareTo((Coords) o);
	}

	/**
	 * @param map
	 * @return den Mittelpunkt des Koordinaten-Sets - kann eine Koordinate sein, die nicht in map enthalten ist.
	 */
	public static Coords Mittelpunkt(Set<Coords> map) {
		if (map.isEmpty()) {
			throw new IllegalArgumentException("Die Koordinaten-Menge darf nicht leer sein.");
		}

		double sumX = 0;
		double sumY = 0;
		double sumZ = 0;

		for (Coords c : map) {
			sumX += c.getX();
			sumY += c.getY();
			sumZ += c.getWelt();
		}
		double n = map.size();
		return new Coords(
				(int) Math.round(sumX / n),
				(int) Math.round(sumY / n),
				(int) Math.round(sumZ / n));
	}

	public static Coords fromRegionID(int id) {
		int w1 = ((id & 0x70000000) >> 28) - 4;
		int y1 = ((id & 0x0fffc000) >> 14) - 8192;
		int x1 = (id & 0x3fff) - 8192;

		return new Coords(x1, y1, w1);
	}

	public static Coords fromString(String s) {
		String src = s.replaceAll("\\(", "").replaceAll("\\)", "");
		Coords c = null;
		try {
			String[] coords = src.split("\\s");
			if (coords.length < 2) {
				return null;
			}

			int x = Integer.parseInt(coords[0]);
			int y = Integer.parseInt(coords[1]);

			int z = 1;
			if (coords.length >= 3) {
				z = Integer.parseInt(coords[2]);
			}

			c = new Coords(x, y, z);

		} catch (NumberFormatException ex) {
			throw new IllegalArgumentException("Coords aus '" + s + "' ist nicht möglich - " + ex.getMessage());
		}
		return c;
	}
}
