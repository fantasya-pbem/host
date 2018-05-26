package de.x8bit.Fantasya.util;

import java.security.SecureRandom;


/**
 * Statische Methoden zum Auswuerfeln von Zufallszahlen.
 *
 * Kopiert vom urspruenglichen Atlantis-Objekt, wo diese Methoden nichts zu
 * suchen haben.
 */
public class Random {
	private static final SecureRandom SEC_RND = new SecureRandom();

	/**
	 * berechnet eine Zufallszahl innerhalb eines Bereiches
	 * @param von - Startwert (inklusive)
	 * @param bis - Stopwert (exklusive)
	 * @return (von + sr.nextInt(bis - von))
	 */ 
	public static int rnd(int von, int bis)
	{
		if (von == bis) return von;
		int random = bis - von;
		if (random < 0) return von;
		return von + SEC_RND.nextInt(random);
	}

	/**
	 * @param seiten Seiten des Würfels
	 * @return Ergebnis eines Würfelwurfs
	 */
	public static int W(int seiten) {
        //		return rnd(1, seiten + 1);
        return rnd(1, seiten + 1);
	}
    
    public static double NextGaussian() {
        return SEC_RND.nextGaussian();
    }
    
    public static double NextChiSquare(int df, double erwartungswert) {
        double sum = 0d;
        for (int i=0; i<df; i++) {
            double rnd = NextGaussian();
            sum += rnd * rnd;
        }
        sum /= (double)df;
        
        return sum * erwartungswert;
    }
}
