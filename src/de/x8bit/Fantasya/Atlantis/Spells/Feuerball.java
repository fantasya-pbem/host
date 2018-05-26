package de.x8bit.Fantasya.Atlantis.Spells;

import de.x8bit.Fantasya.Atlantis.Spell;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Helper.Elementar;
import de.x8bit.Fantasya.Atlantis.Messages.Magie;
import de.x8bit.Fantasya.Atlantis.Spell.AttackSpell;
import de.x8bit.Fantasya.Host.ZAT.Battle.Krieger;
import de.x8bit.Fantasya.Host.ZAT.Battle.Magic.WFeuerball;

public class Feuerball extends Spell implements AttackSpell {

	public String getName() { return "Feuerball"; }
	public String getBeschreibung() 
	{
		return "Der Zauber Feuerball ist ein reiner Angriffszauber. Setzt der Magier diesen Zauber ein, so ist " +
				"es möglich das der Kampfzauber in einer Kampfrunde fehlschlägt. Schlägt dieser Zauber nicht " +
				"fehl, so werden pro Stufe 10 bis 20 Gegner verbrannt.";
	}
	public String getSpruch() { return "ZAUBERE \"Feuerball\" [Stufe]"; }
	public String getKampfzauberSpruch() { return "KAMPFZAUBER ANGRIFF \"" + getName() + "\" [Stufe]"; }
	public Elementar getElementar() { return Elementar.Feuer; }
	public int getStufe() { return 1; }

	public String[] getTemplates() {
		return new String [] { 
				"(\")?(feuerball)(\")? [1-9][0-9]?",
		}; 
	}	

	@Override // normaler Zauber - via ZAUBERE
	public int ExecuteSpell(Unit mage, String[] param) {
		int stufe = 1; // Wunschstufe zum Zaubern
		if (param.length > 2) stufe = getSpellLevel(mage, param[2]);
		if (stufe == 0) return 0; // Fehler kam schon in getSpellLevel

		// nur eine Meldung - das wars ... aber Aura ist weg :)
		new Magie(mage + " spielt mit dem Feuer.", mage);
		
		return stufe;
	}
	
	@Override // Zauber im Kriegseinsatz
	public int ExecuteSpell(Krieger krieger, String [] param) {
		Unit mage = krieger.getUnit();
		
		int stufe = 1; // Wunschstufe zum Zaubern
		if (param.length > 2) stufe = getSpellLevel(mage, param[2]);
		if (stufe == 0) return 0; // Fehler kam schon in getSpellLevel

		krieger.weapons.add(new WFeuerball(mage, stufe, this));
		
		return stufe;
	}

    @Override
	public String getCRSyntax() { return "i"; }

}
