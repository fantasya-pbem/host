package de.x8bit.Fantasya.Atlantis.Spells;

import de.x8bit.Fantasya.Atlantis.Spell;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Helper.Elementar;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;

public class Voodoo extends Spell {

	public String getBeschreibung() {
		return "Mit einem Voodoo-Zauber kann ein Magier einem Opfer seinen Willen aufzwingen." +
				" Damit der Voodoo-Zauber eine Wirkung zeigt, muss der schwarze Magier" +
				" sein Opfer sehen. Das Opfer wird dann diese Runde nur den Willen des" +
				" schwarzen Magiers erfüllen.";
	}
	
	public boolean isFirstSpell() { return true; }
	public String getName() { return "Voodoo"; }
	public String getSpruch() { return "ZAUBERE \"Voodoo\" <Einheit> \"<Befehl>\""; }
	public int getStufe() { return 600; }
	public Elementar getElementar() { return Elementar.Elementarlos; }
	public String[] getTemplates() {
		return new String [] { 
				"(\"voodoo\") [a-z0-9]{1,4} (\".*\")"
		}; 
	}
	public String getCRSyntax() { return "uc"; }

	@Override
	public int ExecuteSpell(Unit mage, String[] param) {
		new Fehler("Die Welt ist noch nicht reif für " + getName() + ".", mage, mage.getCoords());
		return 0;

		// Stufe festlegen
		//int stufe = 1; // Wunschstufe zum Zaubern

		//return stufe;
	}

}
