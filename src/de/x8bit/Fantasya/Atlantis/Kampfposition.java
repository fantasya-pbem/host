package de.x8bit.Fantasya.Atlantis;

/**
 * @author   mogel
 */
public enum Kampfposition
{
	/**
	 * @uml.property  name="aggressiv"
	 * @uml.associationEnd  
	 */
	Aggressiv,
	
	/**
	 * @uml.property  name="vorne"
	 * @uml.associationEnd  
	 */
	Vorne,
	
	/**
	 * @uml.property  name="fliehe"
	 * @uml.associationEnd  
	 */
	Fliehe,
	
	/**
	 * @uml.property  name="hinten"
	 * @uml.associationEnd  
	 */
	Hinten,
	
	/**
	 * @uml.property  name="nicht"
	 * @uml.associationEnd  
	 */
	Nicht,

    Immun;
	
	/**
	 * liefert den Enum zum String zurück oder NULL falls nicht verfügbar
	 */
	public static Kampfposition ordinal(String option)
	{
		for(Kampfposition kp : Kampfposition.values()) {
			if (kp.name().toLowerCase().equals(option.toLowerCase())) return kp;
		}
        if ("vorn".equalsIgnoreCase(option)) return Kampfposition.Vorne;
		// new SysMsg("Kampfposition '" + option + "' ist unbekannt");
		return null;
	}
	
	/**
	 * wandelt zwischen Fantasya und Eressea/Magellen
	 * @param kp - Kampfposition
	 * @return int für Magellan
	 */
	public static int PositionCR(Kampfposition kp)
	{
		switch(kp)
		{
			case Aggressiv:	return 0;			// aggrerssiv
			case Vorne:		return 1;			// nicht, Variante A (lt. Fremd-Doku!)
			// case Vorne:		return 3;			// defensiv
			case Nicht:		return 4;			// nicht, Variante B
			case Fliehe:	return 5;			// fliehe
			case Hinten:	return 2;			// hinten
		}
		return 1;								// oder 4 ?!
	}
}