package de.x8bit.Fantasya.Atlantis.Effects;

import de.x8bit.Fantasya.Atlantis.Effect;
import de.x8bit.Fantasya.Atlantis.Unit;

public class EFXBewegungSail extends Effect implements de.x8bit.Fantasya.Atlantis.Effect.EFXBewegung {

	/** Konstruktor für Reflection */
	public EFXBewegungSail() { super(); }
	
	public EFXBewegungSail(Unit unit)
	{
		super(unit);	// Nummer holen
	}
	
	/**
	 * wenn das abgefragt wurde, dann wird der Effekt gelöscht!
	 */
	@Override
	public int EFXCalculate() {
		int regionen = getIntegerProperty("regionen", 0);
		setProperty("regionen", 0);
		destroyIt();
		return regionen;
	}

}
