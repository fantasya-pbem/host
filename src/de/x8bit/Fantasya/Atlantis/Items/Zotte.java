package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Regions.Berge;
import de.x8bit.Fantasya.Atlantis.Regions.Geroellebene;
import de.x8bit.Fantasya.Atlantis.Regions.Moor;
import de.x8bit.Fantasya.Atlantis.Regions.Oedland;
import de.x8bit.Fantasya.Atlantis.Regions.Wueste;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.util.ComplexName;
import de.x8bit.Fantasya.util.Random;

public class Zotte extends Pferd {
	public Zotte() { super(); }

	@Override
	public String getName()	{
		if (anzahl != 1) return "Zotten";
		return "Zotte";
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Zotte", "Zotten", null);
	}
	
	// Rest wie Pferd
	
	// TODO Wachstum anpassen
	@Override
	public void actionWachstum(Region r)
	{
		// kein Wachstum in der Oberwelt:
		if (r.getCoords().getWelt() > 0) return;
		
		int rnd = Random.rnd(0, 1);
		int jz[] = new int[] { rnd, rnd, rnd, rnd, rnd, rnd, rnd, rnd, rnd, rnd, rnd, rnd };
		
		// idealen regionen
		if (r.getClass().equals(Geroellebene.class) || r.getClass().equals(Berge.class) ||
				r.getClass().equals(Oedland.class))
			jz = new int[] { 1, 2, 3, 4, 5, Random.rnd(4, 8), Random.rnd(4, 8), Random.rnd(4, 8), 5, 4, 3, 2 };
		
		if (r.getClass().equals(Moor.class) || r.getClass().equals(Wueste.class))
			jz = new int[] { 0, 0, 1, 2, 3, Random.rnd(2, 5), Random.rnd(2, 5), Random.rnd(2, 5), 3, 2, 1, 0 };

		int wachstum = jz[GameRules.getJahreszeit()];

		// das Wachstum
		int neu = anzahl > 4 ? (int) ((float) anzahl * wachstum / 100) + 1 : 0;
		
		if (r.freieArbeitsplaetze() < neu) return;	// nue wachsen wenn noch freie Abreitsplätze da sind
		
		//new Debug(this + " - " + anzahl + " Pferde - neu: " + neu, getCoords());
		anzahl += neu;
		
		if (anzahl < 0) anzahl = 0;
	}
}
