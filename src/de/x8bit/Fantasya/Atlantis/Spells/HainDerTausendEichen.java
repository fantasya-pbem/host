package de.x8bit.Fantasya.Atlantis.Spells;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Spell;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Helper.Elementar;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Messages.Magie;
import de.x8bit.Fantasya.util.Random;

public class HainDerTausendEichen extends Spell
{
	public HainDerTausendEichen() { }
	
	public String getName() { return "Hain der 1000 Eichen"; }
	public String getBeschreibung() 
	{
		return "Einen heiligen Hain der 1ooo jährigen Eichen lässt man wachsen, indem man geweihte" + 
				" Eicheln pflanzt und einen Regen der Fruchtbarkeit beschwört. Wird dieser Zauber" +
				" gemäß der Anleitung ausgeführt, werden zwischen 10 und 20 erhabene Eichen, pro Stufe, während" +
				" des nächtlichen Sturmes aus dem Boden schießen und in wenigen Stunden ihre volle Höhe" +
				" von fast 40 Metern erreicht haben.";
	}
	public String getSpruch() { return "ZAUBERE \"Hain der 1000 Eichen\" [Stufe]"; }
	public Elementar getElementar() { return Elementar.Erde; }
	public int getStufe() { return 1; }

	public String[] getTemplates() {
		return new String [] { 
				"\"?(hain der 1000 eichen)\"?( [0-9]+)",
		}; 
	}	

	public int ExecuteSpell(Unit mage, String[] param) 
	{
		int stufe = 1; // Wunschstufe zum Zaubern
		if (param.length > 2) stufe = getSpellLevel(mage, param[2]);
		if (stufe == 0) return 0; // Fehler kam schon in getSpellLevel

		Region region = Region.Load(mage.getCoords());
		
		// Anzahl der neuen Bäume berechnen
		Item resource = region.getResource(Holz.class);
		int anzahl = Random.rnd(10, 20) * stufe;
		// Überprüfen ob überhaupt Platz
		int frei = region.freieArbeitsplaetze();
		if (frei < anzahl) anzahl = frei;
		if (anzahl <= 0) {
			new Magie(mage + " möchte " + Random.rnd(10, 20) * stufe + " Eichen entstehen lassen, aber es ist kein freier Boden vorhanden!", mage);
		} else {
			resource.setAnzahl(resource.getAnzahl() + anzahl);
			new Magie(mage + " hat einen Hain von " + anzahl + " Eichen entstehen lassen", mage);
		}
		
		return stufe;
	}

	public String getCRSyntax() { return "i?"; }

}
