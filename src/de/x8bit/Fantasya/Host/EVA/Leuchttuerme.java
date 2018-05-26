package de.x8bit.Fantasya.Host.EVA;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Buildings.Leuchtturm;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;

/**
 *
 * @author hapebe
 */
public class Leuchttuerme extends EVABase implements NotACommand {

	public Leuchttuerme() {
		super("Leuchttürme weisen den Weg.");
	}


	@Override
	public void PreAction() { }

	@Override
	public void PostAction() {
		for (Building b : Building.PROXY) {
			if (b.getClass() == Leuchtturm.class) {
				((Leuchtturm)b).leuchten();
			}
		}
	}






	@Override
	public boolean DoAction(Unit u, String[] befehl) { throw new UnsupportedOperationException("Not supported yet."); }

	@Override
	public void DoAction(Region r, String befehl) { throw new UnsupportedOperationException("Not supported yet."); }

	@Override
	public void DoAction(Einzelbefehl eb) {	throw new UnsupportedOperationException("Not supported yet."); }


}
