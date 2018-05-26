package de.x8bit.Fantasya.Atlantis.Spells;

import de.x8bit.Fantasya.Atlantis.Spell;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Helper.Elementar;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.Magie;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Regions.*;

@SuppressWarnings("unchecked")
public class SegenDerGoettin extends Spell {

	@Override
	public String getName() { return "Segen der Göttin"; }

	@Override
	public String getBeschreibung()	{
		return "Mit diesem Segen der Fruchtbarkeitsgoettin werden die Samen der"
		+ " Fruchtbarkeit gesaet, die Winde der Genesung gerufen und die"
		+ " Quellen der Huegel gesegnet. Falls sich der Priester, der dieses"
		+ " Ritual durchfuehrt, in einer Wuestenregion befindet, verwandelt"
		+ " sich die Wueste in eine fruchtbare Ebene."
		+ " Das Heilige Wasser kostet $50.";
	}
	public String getSpruch() { return "ZAUBERE \"" + getName() + "\""; }
	public Elementar getElementar() { return Elementar.Erde; }
	public int getStufe() { return 12; }
	
	public String[] getTemplates() {
		return new String [] { 
				"(\")?(segen der göttin)(\")?",
				"(\")?(segen der goettin)(\")?",
		}; 
	}

	public int ExecuteSpell(Unit mage, String[] param) {
		// int stufe = 12; // Wunschstufe zum Zaubern
		// if (param.length > 2) stufe = getSpellLevel(mage, param[2]);
		int stufe = getSpellLevel(mage, "" + getStufe());
		if (stufe == 0) return 0; // Fehler kam schon in getSpellLevel

		// TODO speziell hier: muss mindestens auf Stufe 11 gezaubert werden, sonst wird's nix:
		if (stufe < getStufe()) {
			new Fehler("Der Segen der Göttin erfordert die " + getStufe() + ". magische Stufe.", mage);
			return 0;
		}

		// es werden 50 Silberstücke benötigt:
		if (mage.getItem(Silber.class).getAnzahl() < 50) {
			// TODO noch bösere Konsequenzen?
			new Fehler("Um den Segen der Göttin einzuwerben, benötigt " + mage + " noch ein wenig Silber.", mage);
			return 0;
		}
		mage.getItem(Silber.class).setAnzahl(mage.getItem(Silber.class).getAnzahl() - 50);


		Region r = Region.Load(mage.getCoords());
		Class<? extends Region> terrain = r.getClass();

		if (terrain != Wueste.class) {
			new Magie("Leider befindet sich " + mage + " nicht in einer Wueste.", mage);
		} else {
			Region newRegion = r.cloneAs(Ebene.class);

			new Info("In " + r + " beschwor " + mage + " den Segen der Göttinen der Fruchtbarkeit.", r);

			Region.CACHE.remove(r.getCoords());
			Region.CACHE.put(newRegion.getCoords(), newRegion);
		}

		return stufe;
	}

	public String getCRSyntax() { return ""; }

}