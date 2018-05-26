package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Region;

/**
 * Marker-Interface für Tiere.
 *
 * Tiere sind logischerweise immer Resourcen.
 */
public interface AnimalResource extends Resource {

	/**
	 * @param r Ort des Geschehens - oder null für ein neutrales Ergebnis (?)
	 * @return Für genau ein Tier: Will es in diesem Monat in eine Nachbarregion wandern?
	 */
	public boolean willWandern(Region r);
}
