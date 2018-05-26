package de.x8bit.Fantasya.Host.ZAT;


import de.x8bit.Fantasya.Atlantis.Spell;
import de.x8bit.Fantasya.Host.Paket;



public class Zaubern {

	/** sucht den richtigen Spruch */
	public static Spell FindSpell(String spruch)
	{
		for(Paket p : Paket.getPaket("Spells"))
		{
			Spell spell = (Spell) p.Klasse;
			if (spell.getName().toLowerCase().equalsIgnoreCase(spruch)) return spell;
		}
		
		return null;
	}
}
