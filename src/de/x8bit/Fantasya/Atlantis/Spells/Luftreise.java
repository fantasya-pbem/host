package de.x8bit.Fantasya.Atlantis.Spells;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Spell;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Helper.Elementar;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Regions.Chaos;
import de.x8bit.Fantasya.Host.GameRules;



public class Luftreise extends Spell {

	public Elementar getElementar() { return Elementar.Luft; }
	public String getSpruch() { return "ZAUBERE \"Luftreise\" X Y"; } // Stufe wird über Entfernung berechnet
	public int getStufe() { return 4; }
	public String[] getTemplates() {
		return new String [] { 
				"\"?(luftreise)\"? [-+]?[0-9]+ [-+]?[0-9]+",
		}; 
	}
    public String getCRSyntax() { return "r"; }

    public String getName() { return "Luftreise"; }
	public String getBeschreibung() 
	{
		return "Wenn der Magier diesen Zauber spricht, so sollte er in einer Schüssel sitzen. " +
				"Unter dem Magier entsteht ein kleiner Wirbelsturm, der ihn in eine entfernte " +
				"Region trägt. Pro Stufe fliegt der Magier, auf der Schüssel-Wirbelsturm-Mischung, " +
				"3 Regionen weit. Es könnte sich als ungünstig erweisen, wenn die Zielregion ein Ozean, Chaos oder " +
				"eine andere gefährliche Region ist. Der Magier gibt nicht die Koordinaten seiner Zielregion an, " +
				"sondern die realitve Entfernung.";
	}
	
	public int ExecuteSpell(Unit mage, String[] param) {
		if (param.length < 4) {
			new Fehler(mage + " muss eine Richtung für die Reise aussuchen. ("+this.getSpruch()+")", mage);
			return 0;
		}
		
		if (!mage.canWalkAnimals())	{
			new Fehler("Der Magier ist zu schwer um sich zu bewegen.", mage);
			return 0;
		}
		
		// Ziel überprüfen
        param[2] = param[2].replace("+", "");
        param[3] = param[3].replace("+", "");
		int destinationX = 0;	// Ziel X
		int destinationY = 0;	// Ziel Y
		try { destinationX = Integer.parseInt(param[2]); } catch(Exception ex) { new Fehler("Die Zielregion konnte nicht gefunden werden.", mage); return 0; }
		try { destinationY = Integer.parseInt(param[3]); } catch(Exception ex) { new Fehler("Die Zielregion konnte nicht gefunden werden.", mage); return 0; }

        Partei p = Partei.getPartei(mage.getOwner());
		Coords here = mage.getCoords();
		Region r = Region.Load(here.getX() + destinationX, here.getY() + destinationY, here.getWelt());
		if (r instanceof Chaos) {
			new Fehler(mage + " vermutet, dass dort " + GameRules.TERRAIN_UNSICHTBARER_REGIONEN + " ist und will nicht zaubern.", mage);
			return 0;
		}
		if (!p.canAccess(r)) {
			new Fehler(mage + " vermutet, dass dort " + GameRules.TERRAIN_UNSICHTBARER_REGIONEN + " ist und will nicht zaubern.", mage);
			return 0;
		}
		
		// Entfernung und damit die Stufe berechnen
		int moves = here.getDistance(r.getCoords());

		// mögliche Aura überprüfen
		int stufe = moves / 3 + 1;	// Wunschstufe zum Zaubern
		if (param.length > 4) stufe = getSpellLevel(mage, param[4]);
		if (stufe == 0) return 0;	// Fehler kam schon in getSpellLevel
		if (stufe < (moves/3+1)) {
			new Fehler("Das ist zu weit zum Zaubern.", mage);
			return 0;
		}

		// Einheit "verschieben"
		Region current = Region.Load(here);

		Unit.CACHE.remove(mage);
		mage.setCoords(r.getCoords());
		Unit.CACHE.add(mage);

		// ggf. Gebäude & Schiff verlassen
		if (mage.getSchiff() != 0) mage.Leave();
		if (mage.getGebaeude() != 0) {
			Building b = Building.getBuilding(mage.getGebaeude());
			Building.PROXY.remove(b);
			mage.Leave();
			Building.PROXY.add(b);
		}

		new Info(
				mage  + " braust von " + current + " aus durch die Lüfte nach " + r + ", "
				+ moves + " Regionen entfernt. " + mage + " hat auf Stufe " + stufe + " gezaubert.",
				mage
		);
		
		
		return stufe;
	}
	
}
