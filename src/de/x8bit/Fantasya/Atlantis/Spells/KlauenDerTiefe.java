package de.x8bit.Fantasya.Atlantis.Spells;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Spell;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Helper.Elementar;
import de.x8bit.Fantasya.Atlantis.Items.Eisen;
import de.x8bit.Fantasya.Atlantis.Messages.Magie;
import de.x8bit.Fantasya.util.Random;

public class KlauenDerTiefe extends Spell
{

	public String getName() { return "Klauen der Tiefe"; }
	public String getBeschreibung()
	{
		return "Für diesen Zauber benötigt man 1o große, eiserne Nägel. An einem heißen Tag stimmt man an" +
				" einen wilden, schrillen Gesang an und beginnt einen chaotischen Tanz. Nach einer Weile beginnt" +
				" man, auf den Boden zu spucken, wilde Flüche an die Geister der Erde zu richten und den Boden" +
				" nieder zu treten. Nach einiger Zeit fängt man zudem an, die eisernen Nägel in den Erdboden zu" +
				" treiben. Hat man alles richtig gemacht, werden die Erdgeister nach einer Stunde zornig genug sein," +
				" um in die Falle zu tapsen: Die Nägel fangen an zu wachsen, werden zu grauenhaften Krallen, welche" +
				" versuchen den Magier in die Tiefe zu reißen. Sobald diese dem Magier aber gefährlich werden, beendet" +
				" er seinen Gesang, und die Krallen erstarren. Diese kann man problemlos zu 1o bis 2o Eisen einschmelzen," +
				" welche der Magier erhält.";
	}
	public String getSpruch() { return "ZAUBERE \"Klauen der Tiefe\" [Stufe]"; }
	public Elementar getElementar() { return Elementar.Erde; }
	public int getStufe() { return 1; }
	
	public String[] getTemplates() {
		return new String [] { 
				"(\"klauen der tiefe\") [0-9]+",
		}; 
	}
	
	public int ExecuteSpell(Unit mage, String[] param) {
		int stufe = 1; // Wunschstufe zum Zaubern
		if (param.length > 2) stufe = getSpellLevel(mage, param[2]);
		if (stufe == 0) return 0; // Fehler kam schon in getSpellLevel

		// Anzahl der neuen Eisen berechnen
		int anzahl = Random.rnd(10, 20) * stufe;
		Item item = mage.getItem(Eisen.class);
		item.setAnzahl(item.getAnzahl() + anzahl);
		
		new Magie(mage + " besitzt nun " + item, mage);

		return stufe;
	}

	public String getCRSyntax() { return "i?"; }
}
