package de.x8bit.Fantasya.Host.EVA;

import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;

public class BauernArbeiten extends EVABase implements NotACommand
{
	
	public BauernArbeiten() {
		super("Bauern arbeiten (oder faulenzen).");
	}

	public void PostAction() {
		Arbeiten();
	}
	
	public boolean DoAction(Unit u, String[] befehl) { return false; }
	public void DoAction(Region r, String befehl) { }
    public void DoAction(Einzelbefehl eb) { }
	public void PreAction() { }

	/** Bauern arbeiten */
	private void Arbeiten()
	{
		for (Region region : Region.CACHE.values()) {
			int arbeiter = region.getBauern();

			// wenn alles voll, dann den Überschuß abziehen
			if (region.freieArbeitsplaetze() < 0) arbeiter -= (0 - region.freieArbeitsplaetze());
			if (arbeiter < 0) arbeiter = 0;

			int silber = arbeiter * region.getLohn();
//				new Debug(region + " - " + region.getSilber() + " Silber - neu: " + silber, region.getCoords());
			region.setSilber(region.getSilber() + silber);
		}
	}

}
