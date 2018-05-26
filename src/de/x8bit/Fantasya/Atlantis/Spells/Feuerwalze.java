package de.x8bit.Fantasya.Atlantis.Spells;

import de.x8bit.Fantasya.Atlantis.Spell;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Helper.Elementar;
import de.x8bit.Fantasya.Atlantis.Messages.Magie;
import de.x8bit.Fantasya.Atlantis.Spell.AttackSpell;
import de.x8bit.Fantasya.Host.ZAT.Battle.Krieger;
import de.x8bit.Fantasya.Host.ZAT.Battle.Magic.WFeuerball;

public class Feuerwalze extends Spell implements AttackSpell {

    @Override
	public String getName() { return "Feuerwalze"; }
    @Override
	public String getBeschreibung() 
	{
		return "Der Zauber Feuerwalze ist ein reiner Angriffszauber. Setzt der Magier diesen Zauber ein, so ist" + 
				" es möglich, dass der Kampfzauber in einer Kampfrunde fehlschlägt. Gelingt der Zauber," +
				" so werden pro Stufe 100 bis 200 Gegner verbrannt.";
	}
    @Override
	public String getSpruch() { return "ZAUBERE \"Feuerwalze\" [Stufe]"; }
    @Override
	public String getCRSyntax() { return ""; }
    @Override
	public String getKampfzauberSpruch() { return "KAMPFZAUBER ANGRIFF \"" + getName() + "\" [Stufe]"; }
    @Override
	public Elementar getElementar() { return Elementar.Feuer; }
    @Override
	public int getStufe() { return 6; }

    @Override
	public String[] getTemplates() {
		return new String [] { 
				"(\")?(feuerwalze)(\")? [1-9][0-9]?",
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

}
