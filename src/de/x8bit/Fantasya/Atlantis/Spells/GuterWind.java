package de.x8bit.Fantasya.Atlantis.Spells;

import de.x8bit.Fantasya.Atlantis.Ship;
import de.x8bit.Fantasya.Atlantis.Spell;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Effects.EFXBewegungSail;
import de.x8bit.Fantasya.Atlantis.Helper.Elementar;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Magie;
import de.x8bit.Fantasya.Host.Main;
import de.x8bit.Fantasya.util.Codierung;

public class GuterWind extends Spell {

	@Override
	public Elementar getElementar() { return Elementar.Luft; }
	@Override
	public String getSpruch() { return "ZAUBERE \"Guter Wind\" [Kapitäns-Einheit] [Stufe]"; }
	@Override
	public int getStufe() { return 2; }
	@Override
	public String[] getTemplates() {
		return new String [] { 
				"\"?(guter wind)\"? [a-z0-9]{1,4}( [0-9]+)?",
		}; 
	}
	@Override
	public String getCRSyntax() { return "ui?"; }
	@Override
	public String getName() { return "Guter Wind"; }
	@Override
	public String getBeschreibung() 
	{
		return "Zum Füllen der Segeln, kann der Magier den Zauberspruch \"Guter Wind\" verwenden. " +
				"Damit kann das Schiff pro Stufe eine Region weiter reisen.";
	}

	@Override
	public int ExecuteSpell(Unit mage, String[] param) {
		if (param.length < 3) {
			new Fehler(mage + " muss für den Zauber eine Zieleinheit aussuchen. ("+this.getSpruch()+")", mage, mage.getCoords());
			return 0;
		}
		
		// Stufe holen
		int stufe = 1;	// Wunschstufe zum Zaubern
		if (param.length > 3) stufe = getSpellLevel(mage, param[3]);
		if (stufe == 0) return 0;	// Fehler kam schon in getSpellLevel
		
		// Opfer holen
		Unit victim = Unit.Load(Codierung.fromBase36(param[2]));
		if (victim == null) {
			new Fehler(mage + " macht mächtig Wind - aber der geht ins Leere, denn Einheit [" + param[2] + "] ist beim besten Willen nicht zu finden.", mage, mage.getCoords());
			return 0;
		}
		if (victim.getSchiff() == 0)
		{
			new Fehler(mage + " kann nicht \"Guter Wind\" zaubern, " + victim + " ist auf keinem Schiff.", mage, mage.getCoords());
			return 0;
		}
		
		Ship ship = Ship.Load(victim.getSchiff());
		if (ship == null) new BigError("Schiff existiert nicht!");
		if (ship.getOwner() != victim.getNummer())
		{
			new Fehler(mage + " kann nicht \"Guter Wind\" zaubern, " + victim + " ist kein Kapitän.", mage, mage.getCoords());
			return 0;
		}

		// neuen Effekt hinzufügen
		EFXBewegungSail sm = new EFXBewegungSail(victim);
		sm.setProperty("regionen", stufe);
		victim.addEffect(sm);

		new Magie(mage + " zaubert \"" + getName() + "\" mit Stufe " + stufe + " für " + victim + ".", mage);

		return stufe;
	}

}
