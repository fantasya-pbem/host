package de.x8bit.Fantasya.Atlantis.Spells;

import de.x8bit.Fantasya.Atlantis.Spell;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Helper.Elementar;
import de.x8bit.Fantasya.Atlantis.Items.Eisen;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Items.Stein;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.Magie;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Regions.*;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class ProvokationDerTitanen extends Spell {

	private final static Map<Class<? extends Region>, Class<? extends Region>> TRANSFORMATIONS;
	static {
		TRANSFORMATIONS = new HashMap();
		TRANSFORMATIONS.put(Ozean.class, Sumpf.class);
		TRANSFORMATIONS.put(Sumpf.class, Wueste.class);
		TRANSFORMATIONS.put(Ebene.class, Hochland.class);
		TRANSFORMATIONS.put(Wald.class, Hochland.class);
		TRANSFORMATIONS.put(Wueste.class, Hochland.class);
		TRANSFORMATIONS.put(Hochland.class, Berge.class);
		TRANSFORMATIONS.put(Berge.class, Gletscher.class);
	}

	@Override
	public String getName() { return "Provokation der Titanen"; }

	@Override
	public String getBeschreibung()	{
		return "Mit diesem Zauber koennen die Titanen der Unterwelt provoziert"
		+ " werden. Hierzu zertruemmert man einen langen Eichenstab im Wert von"
		+ " $50 und singt Spottlieder ueber die Titanen der Urzeit in der Alten"
		+ " Sprache. Alsbald werden sich die Titanen regen, in der Tiefe toben"
		+ " und an ihren ewigen Ketten reissen. Dies wird die Region permanent"
		+ " um einige hundert Meter anheben."
		+ " Dieser Zauberspruch kann sogar auf hoher See gezaubert werden!";
	}
	public String getSpruch() { return "ZAUBERE \"" + getName() + "\""; }
	public Elementar getElementar() { return Elementar.Erde; }
	public int getStufe() { return 11; }
	
	public String[] getTemplates() {
		return new String [] { 
				"(\")?(provokation der titanen)(\")?",
		}; 
	}

	public int ExecuteSpell(Unit mage, String[] param) {
		// int stufe = 11; // Wunschstufe zum Zaubern
		// if (param.length > 2) stufe = getSpellLevel(mage, param[2]);
		int stufe = getSpellLevel(mage, "" + getStufe());
		if (stufe == 0) return 0; // Fehler kam schon in getSpellLevel

		// TODO speziell hier: muss mindestens auf Stufe 11 gezaubert werden, sonst wird's nix:
		if (stufe < getStufe()) {
			new Fehler("Die Titanen müssen mindestens im " + getStufe() + ". Grade verspottet werden.", mage);
			return 0;
		}

		// es werden 50 Silberstücke benötigt:
		if (mage.getItem(Silber.class).getAnzahl() < 50) {
			// TODO noch bösere Konsequenzen?
			new Fehler("Um die Titanen zu provozieren, fehlt " + mage + " das nötige Silber, um einen passenden Eichenstab zu erwerben.", mage);
			return 0;
		}
		mage.getItem(Silber.class).setAnzahl(mage.getItem(Silber.class).getAnzahl() - 50);


		Region r = Region.Load(mage.getCoords());
		Class<? extends Region> terrain = r.getClass();
		Class<? extends Region> newTerrain = null;
		boolean handled = false;
		if (TRANSFORMATIONS.containsKey(terrain)) {
			newTerrain = TRANSFORMATIONS.get(terrain);
			handled = true;
		}

		if (terrain == Gletscher.class) {
			new Magie("Auf dem " + r + " provozierte " + mage + " die Titanen und loeste einige Lawinen aus.", mage);
			handled = true;
		}

		if (newTerrain != null) {


			Region newRegion = r.cloneAs(newTerrain);

			// so, jetzt wird es ernst:
			if (terrain == Ozean.class) {
				// TODO Luxuspreise auf Anfang
				// TODO Was soll mit dem Regions-Namen passieren?
			} else {
			}
			new Info("In " + r + " provozierte " + mage + " die Titanen, und der Boden hob sich permanent.", r);

			// Bodenschätze neu anlegen:
			if (newTerrain == Berge.class) {
				((Berge)newRegion).InitMinerals();
			} else if (newTerrain == Hochland.class) {
				((Hochland)newRegion).InitMinerals();
			} else if (newTerrain == Gletscher.class) {
				((Gletscher)newRegion).InitMinerals();
			} else {
				newRegion.setResource(Eisen.class, 0);
				newRegion.setResource(Stein.class, 0);
			}

			Region.CACHE.remove(r.getCoords());
			Region.CACHE.put(newRegion.getCoords(), newRegion);
		}

		if (!handled) {
			new Magie(mage + " provoziert die Titanen, aber nichts passiert.", mage);
			return 0;
		}

		return stufe;
	}

	public String getCRSyntax() { return ""; }

}