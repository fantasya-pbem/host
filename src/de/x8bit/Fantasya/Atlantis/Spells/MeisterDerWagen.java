package de.x8bit.Fantasya.Atlantis.Spells;

import de.x8bit.Fantasya.Atlantis.Spell;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Effects.EFXWagnerMeister;
import de.x8bit.Fantasya.Atlantis.Helper.Elementar;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Magie;
import de.x8bit.Fantasya.util.Codierung;

public class MeisterDerWagen extends Spell {

	public Elementar getElementar() { return Elementar.Erde; }
	public String getSpruch() { return "ZAUBERE \"Meister Der Wagen\" [Einheit] [Stufe]"; }
	public int getStufe() { return 3; }
	public String[] getTemplates() { return new String [] { "\"?(meister der wagen)\"? [a-z0-9]{1,4}( [0-9]+)?" }; }
	public String getBeschreibung() {
		return "Der Zauberer ist mit diesem Spruch in der Lage die Fähigkeiten eines Wagners um ca. 20%%" +
				" zu verbessern. Hierbei wird der Wagner 20%% effektiver seine Resourcen in einer Werkstatt verwenden. Pro" +
				" Stufe kann die Effektivität für 10 Gegenstände verbessert werden.";
	}
	public String getName() { return "Meister der Wagen"; }
    public String getCRSyntax() { return "ui?"; }

	@Override
	public int ExecuteSpell(Unit mage, String[] param) {
		if (param.length < 3) {
			new Fehler(mage + " muss für den Zauber eine Zieleinheit aussuchen. ("+this.getSpruch()+")", mage, mage.getCoords());
			return 0;
		}
		
		// Opfer holen
		Unit victim = Unit.Load(Codierung.fromBase36(param[2]));
		if (victim == null) {
			new Fehler(mage + " kann nicht \"" + getName() + "\" zaubern, denn Einheit [" + param[2] + "] ist beim besten Willen nicht gefunden worden.", mage, mage.getCoords());
			return 0;
		}
		
		// Stufe festlegen
		int stufe = 1; // Wunschstufe zum Zaubern
		if (param.length > 3) stufe = getSpellLevel(mage, param[3]);
		if (stufe == 0) return 0; // Fehler kam schon in getSpellLevel
		
		// neuen Effekt hinzufügen
		EFXWagnerMeister sm = new EFXWagnerMeister(victim);
		sm.setProperty("anzahl", 10 * stufe);
		victim.addEffect(sm);
		new Magie(mage + " zaubert \"" + getName() + "\" mit Stufe " + stufe + " an " + victim, mage);
		
		return stufe;
	}

}
