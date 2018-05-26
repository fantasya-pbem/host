package de.x8bit.Fantasya.Atlantis.Spells;

import de.x8bit.Fantasya.Atlantis.Spell;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Effects.EFXPlattnerMeister;
import de.x8bit.Fantasya.Atlantis.Helper.Elementar;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Magie;
import de.x8bit.Fantasya.util.Codierung;

public class MeisterDerPlatten extends Spell {

	@Override
	public Elementar getElementar() { return Elementar.Feuer; }
	@Override
	public String getSpruch() { return "ZAUBERE \"Meister Der Platten\" [Einheit] [Stufe]"; }
	@Override
	public int getStufe() { return 3; }
	@Override
	public String[] getTemplates() { return new String [] { "\"?(meister der platten)\"? [a-z0-9]{1,4}( [0-9]+)?" }; }
	@Override
	public String getBeschreibung() {
		return "Der Zauberer ist mit diesem Spruch in der Lage die Fähigkeiten eines Plattner um ca. 20%%" +
				" zu verbessern. Hierbei wird der Plattner 20%% effektiver seine Resourcen in einer Sattlerei verwenden. Pro" +
				" Stufe kann die Effektivität für 10 Gegenstände verbessert werden.";
	}
	@Override
	public String getName() { return "Meister der Platten"; }
	@Override
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
		EFXPlattnerMeister sm = new EFXPlattnerMeister(victim);
		sm.setProperty("anzahl", 10 * stufe);
		victim.addEffect(sm);
		new Magie(mage + " zaubert \"" + getName() + "\" mit Stufe " + stufe + " an " + victim, mage);
		
		return stufe;
	}

}
