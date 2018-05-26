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

public class Mastodon extends Elefant {
	public Mastodon() { super(); }
	
	public String getName() {
		if (anzahl != 1) return "Mastodonten";
		return "Mastodon";
	}
	
	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Mastodon", "Mastodonten", new String[]{"Mastodons"});
	}

	// Rest Elefant
	
	// TODO Wachstum
	@Override
	public void actionWachstum(Region r)
	{
		// kein Wachstum in der Oberwelt:
		if (r.getCoords().getWelt() > 0) return;
		
		int rnd = Random.rnd(-2, 2);
		int jz[] = new int[] { rnd, rnd, rnd, rnd, rnd, rnd, rnd, rnd, rnd, rnd, rnd, rnd };
		
		// idealen Regionen
		if (r.getClass().equals(Geroellebene.class) || r.getClass().equals(Moor.class) ||
				r.getClass().equals(Oedland.class) || r.getClass().equals(Wueste.class))
			jz = new int[] { 0, 0, 1, 2, 3, Random.rnd(2, 5), Random.rnd(2, 5), Random.rnd(2, 5), 3, 2, 1, 0 };
		
		// Berge sind immer schlecht
		if (r.getClass().equals(Berge.class))
			jz = new int[] { -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2 };

		int wachstum = jz[GameRules.getJahreszeit()];

		// das Wachstum
		int neu = anzahl > 4 ? (int) ((float) anzahl * wachstum / 100) + 1 : 0;

		if (r.freieArbeitsplaetze() < neu) return;	// nue wachsen wenn noch freie Abreitsplätze da sind
		
		//new Debug(this + " - " + anzahl + " Elefanten - neu: " + neu, getCoords());
		anzahl += neu;
		if (anzahl < 0) anzahl = 0;
	}
}
