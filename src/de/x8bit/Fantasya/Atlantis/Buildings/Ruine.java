package de.x8bit.Fantasya.Atlantis.Buildings;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.util.ComplexName;

public class Ruine extends Building
{
	public void Zerstoere(Unit u)
	{
		new Fehler(u + " ist der Meinung das dies eine schlechte Idee ist", u, u.getCoords());
	}
	
	public String getTyp() { return "Ruine"; }
	
	/**
	 * erstellt ein Gebäude bzw. baut daran weiter
	 * @param u - diese Einheit will daran bauen
	 */
	public void Mache(Unit u)
	{
		new Fehler(u + " würde lieber gerne an einem qualitativ hochwertigem Gebäude bauen", u, u.getCoords());
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Ruine", "Ruinen", null);
	}
}
