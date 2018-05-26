package de.x8bit.Fantasya.Atlantis.Items;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Buildings.Saegewerk;
import de.x8bit.Fantasya.Atlantis.Helper.ConstructionCheats;
import de.x8bit.Fantasya.Atlantis.Helper.ConstructionContainer;
import de.x8bit.Fantasya.Atlantis.Regions.Gletscher;
import de.x8bit.Fantasya.Atlantis.Regions.Trockenwald;
import de.x8bit.Fantasya.Atlantis.Regions.Wald;
import de.x8bit.Fantasya.Atlantis.Skills.Holzfaellen;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.util.ComplexName;

public class Holz extends Item implements Resource {

	public Holz()
	{
		super(500, 0);
		setConstructionSkills(new ConstructionContainer [] { new ConstructionContainer(Holzfaellen.class, 1) } );
		setConstructionCheats(new ConstructionCheats [] { 
				new ConstructionCheats(Saegewerk.class, new ConstructionContainer [] { new ConstructionContainer(Holz.class, 2)})
				});
	}
	
	public Holz(int anzahl)
	{
		super(500, 0);
		setAnzahl(anzahl);
		setConstructionSkills(new ConstructionContainer [] { new ConstructionContainer(Holzfaellen.class, 1) } );
		setConstructionCheats(new ConstructionCheats [] { 
				new ConstructionCheats(Saegewerk.class, new ConstructionContainer [] { new ConstructionContainer(Holz.class, 2)})
				});
	}
		
	@Override
	public String getName()
	{
		if (anzahl != 1) return "Holz";
		return "Holz";
	}
	
	@Override
	public void actionWachstum(Region r) {
		int jz[] = new int[] { 0, 0, 2, 3, 4, 6, 9, 8, 6, 4, 2, 1 }; // Prozente pro Monat
		int wachstum = jz[GameRules.getJahreszeit()];
		
		// Bäume wachsen im Wald/Trockenwald besser
		if (r.getClass().equals(Wald.class) || r.getClass().equals(Trockenwald.class)) wachstum += 2;	// im Wald/Trockenwald 2% extra
		// im Winter schrupfen die Bäume im Gletscher
		if (r.getClass().equals(Gletscher.class)) wachstum -= 2;
		
		// das Wachstum
		if (r.freieArbeitsplaetze() < 1) {
			// new Info("Kein Wachstum von Holz in " + r + " " + r.getCoords() + ": kein Platz.", 0);
			return;
		}
		int neu = anzahl > 4 ? (int) ((float) anzahl * wachstum / 100.0f) + 1 : 0;
		// new Info("Wachstum von Holz in " + r + " " + r.getCoords() + " (Mon. " + GameRules.getJahreszeit() + "/" + wachstum + "pct.) , " + anzahl + " -> " + (anzahl+neu) + ".", 0);
		
		if (r.freieArbeitsplaetze() < neu) return;	// nue wachsen wenn noch freie Abreitsplätze da sind
		
		anzahl += neu;
		if (anzahl < 0) anzahl = 0;
	}
	
	@Override
	public boolean surviveBattle() { return true; }

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Holz", "Holz", null);
	}
}
