package de.x8bit.Fantasya.Atlantis.Effects;

import de.x8bit.Fantasya.Atlantis.Effect;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Effect.EFXResourcenCreate;
import de.x8bit.Fantasya.Atlantis.Effect.EFXResourcenUse;
import de.x8bit.Fantasya.Atlantis.Items.Resource;
import de.x8bit.Fantasya.Atlantis.Messages.Magie;


/**
 * Einheiten mit diesem Effekt produzieren etwas besser als die anderen :) ... es
 * werden alle möglichen Items produziert<br><br>
 * <b>Wirkung</b> - Einheiten produzieren 20% mehr Resourcen ... max. 10 Stück (pro
 * gezauberter Stufe) 
 * @author mogel
 */
public class EFXResourcenMeister extends Effect implements EFXResourcenUse, EFXResourcenCreate {

	/** Konstruktor für Reflection */
	public EFXResourcenMeister() { super(); }
	
	public EFXResourcenMeister(Unit unit)
	{
		super(unit);	// Nummer holen
	}
	
	@Override
	public int EFXCalculate(Unit unit, Item item, int anzahl) {
		if (!(item instanceof Resource)) return 0;
		
		int free = getIntegerProperty("anzahl", 10);	// 10 für Stufe 1 gezaubert
		if (free <= 0) { destroyIt(); /* Effekt zerstören */ return 0; }
		
		// Effekt berechnen
		int extra = (int) ((double) anzahl * 0.2);	// 20% extra
		if (extra > free) extra = free;						// Grenze checken
		int rest = free - extra;
		setProperty("anzahl", rest);
		
		// Meldung
		new Magie(unit + " produziert durch seine Erfahrung mit Resourcen " + extra + " Punkte mehr.", unit);
		
		return extra;
	}

}
