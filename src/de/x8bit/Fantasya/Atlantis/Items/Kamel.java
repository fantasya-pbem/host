package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Helper.ConstructionContainer;
import de.x8bit.Fantasya.Atlantis.Regions.Berge;
import de.x8bit.Fantasya.Atlantis.Regions.Ebene;
import de.x8bit.Fantasya.Atlantis.Regions.Gletscher;
import de.x8bit.Fantasya.Atlantis.Regions.Hochland;
import de.x8bit.Fantasya.Atlantis.Regions.Sumpf;
import de.x8bit.Fantasya.Atlantis.Regions.Wald;
import de.x8bit.Fantasya.Atlantis.Regions.Wueste;
import de.x8bit.Fantasya.Atlantis.Skills.Pferdedressur;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.util.ComplexName;
import de.x8bit.Fantasya.util.Random;

public class Kamel extends Item implements AnimalResource {
	public Kamel()
	{
		super(4000, 8000);
		setConstructionSkills(new ConstructionContainer [] { new ConstructionContainer(Pferdedressur.class, 1) } );
	}
	
	@Override
	public String getName()
	{
		if (anzahl != 1) return "Kamele";
		return "Kamel";
	}

//	/** das Kamel von der Gegend abziehen */
//	protected void Make(Unit u, int anzahl)
//	{
//		Region region = Region.Load(u.getCoords()); 
//		Item resource = region.getResource(this.getClass());
//		
//		// Sicherheitscheck
//		if (anzahl > resource.getAnzahl()) anzahl = resource.getAnzahl();
//		
//		// von der Gegend abziehen
//		resource.setAnzahl(resource.getAnzahl() - anzahl);
//		
//		// nun noch herstellen
//		super.Make(u, anzahl);
//	}
	
	@Override
	public void actionWachstum(Region r)
	{
		// kein Wachstum in der Unterwelt:
		if (r.getCoords().getWelt() < 0) return;
		
		int rnd = Random.rnd(0, 2);
		int jz[] = new int[] { rnd, rnd, rnd, rnd, rnd, rnd, rnd, rnd, rnd, rnd, rnd, rnd };
		
		// idealen regionen
		if (r.getClass().equals(Hochland.class) || r.getClass().equals(Wueste.class))
			jz = new int[] { 1, 2, 3, 4, 5, Random.rnd(4, 8), Random.rnd(4, 8), Random.rnd(4, 8), 5, 4, 3, 2 };
		
		if (r.getClass().equals(Gletscher.class) || r.getClass().equals(Berge.class))
			jz = new int[] { 0, 0, 1, 2, 3, Random.rnd(2, 5), Random.rnd(2, 5), Random.rnd(2, 5), 3, 2, 1, 0 };

		int wachstum = jz[GameRules.getJahreszeit()];

		if (r.getClass().equals(Sumpf.class)) wachstum -= 2;
		if (r.getClass().equals(Ebene.class) || r.getClass().equals(Wald.class)) wachstum -= 1;

		// das Wachstum
		int neu = anzahl > 4 ? (int) ((float) anzahl * wachstum / 100) + 1 : 0;
		
		if (r.freieArbeitsplaetze() < neu) return;	// nue wachsen wenn noch freie Abreitsplätze da sind
		
		// new Debug(this + " - " + anzahl + " Kamele - neu: " + neu, getCoords());
		anzahl += neu;
		
		if (anzahl < 0) anzahl = 0;
	}

	@Override
	public boolean surviveBattle() { return true; }

	@Override
	public boolean willWandern(Region r) {
		int p = 100; // 1%
		if (r instanceof Wueste) { p = 50; } // 0,5%
		if (r instanceof Sumpf) { p = 1000; } // 10%
		if (r instanceof Gletscher) { p = 9000; } // 90%
		if (Random.W(10000) < p) return true;
		return false;
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Kamel", "Kamele", null);
	}
}
