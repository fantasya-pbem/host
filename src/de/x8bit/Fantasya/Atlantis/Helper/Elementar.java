package de.x8bit.Fantasya.Atlantis.Helper;

import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;

public enum Elementar {

	Elementarlos,
	Feuer,
	Wasser,
	Erde,
	Luft;
	
	public static Elementar ordinal(String option)
	{
		for(Elementar mg : Elementar.values())
		{
			if (mg.name().toLowerCase().equals(option.toLowerCase())) return mg;
		}
		new SysMsg("Magiegebiet '" + option + "' ist unbekannt");
		return null;
	}

}
