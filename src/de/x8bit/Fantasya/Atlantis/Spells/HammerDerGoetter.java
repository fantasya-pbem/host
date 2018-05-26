package de.x8bit.Fantasya.Atlantis.Spells;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Item;
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
import de.x8bit.Fantasya.util.Random;
import de.x8bit.Fantasya.util.StringUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class HammerDerGoetter extends Spell {

	private final static Map<Class<? extends Region>, Class<? extends Region>> TRANSFORMATIONS;
	static {
		TRANSFORMATIONS = new HashMap();
		TRANSFORMATIONS.put(Wald.class, Sumpf.class);
		TRANSFORMATIONS.put(Ebene.class, Sumpf.class);
		TRANSFORMATIONS.put(Wueste.class, Sumpf.class);
		TRANSFORMATIONS.put(Sumpf.class, Ozean.class);
		TRANSFORMATIONS.put(Gletscher.class, Berge.class);
		TRANSFORMATIONS.put(Berge.class, Hochland.class);
		TRANSFORMATIONS.put(Hochland.class, Wueste.class);
	}

	@Override
	public String getName() { return "Hammer der Götter"; }

	@Override
	public String getBeschreibung()	{
		return	"Der Zauber benoetigt ein paar Naegel im Wert von $50. Diese werden"
				+ " schoen saeuberlich in den Boden gesteckt, und dann wird in einem"
				+ " mehrtaegigen Ritual der Zorn der Goetter beschworen. Am dreizehnten Tage"
				+ " wird ein Gewitter losbrechen, und Blitz wird zur Erde fallen, und der"
				+ " Hammer der Goetter wird niederfahren und die Region zermalmen. Die"
				+ " Region wird damit um einige hundert Meter abgesenkt...";
	}
    @Override
	public String getSpruch() { return "ZAUBERE \"" + getName() + "\""; }
    @Override
	public Elementar getElementar() { return Elementar.Erde; }
    @Override
	public int getStufe() { return 10; }
	
    @Override
	public String[] getTemplates() {
		return new String [] { 
				"(\")?(hammer der götter)(\")?",
				"(\")?(hammer der goetter)(\")?",
		}; 
	}

    @Override
	public int ExecuteSpell(Unit mage, String[] param) {
		// int stufe = 12; // Wunschstufe zum Zaubern
		// if (param.length > 2) stufe = getSpellLevel(mage, param[2]);
		int stufe = getSpellLevel(mage, "" + getStufe());
		if (stufe == 0) return 0; // Fehler kam schon in getSpellLevel

		// TODO speziell hier: muss mindestens auf Stufe 10 gezaubert werden, sonst wird's nix:
		if (stufe < getStufe()) {
			new Fehler("Der Hammer der Götter muss mindestens im " + getStufe() + ". Grade beschworen werden.", mage);
			return 0;
		}

		// es werden 50 Silberstücke benötigt:
		if (mage.getItem(Silber.class).getAnzahl() < 50) {
			// TODO noch bösere Konsequenzen?
			new Fehler("Um den Hammer der Götter zu beschwören, fehlt es " + mage + " an Opfergaben.", mage);
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

		if (terrain == Ozean.class) {
			new Magie("In " + r + " beschwor " + mage + " den Hammer der Goetter und loeste eine Flutwelle aus.", mage);
			handled = true;
		}

		if (newTerrain != null) {
			// so, jetzt wird es ernst:
			if (newTerrain != Ozean.class) {
				// 20% der Bauern sterben:
				int tote = 0;
				for (int i=r.getBauern(); i>0; i--) {
					if (Random.rnd(0, 100) < 20) tote ++;
				}
				if (tote > 0) {
					r.setBauern(r.getBauern() - tote);
				}

				boolean gebaeudeSchaden = false;
				for (Building b : r.getBuildings()) {
					int newSize = Math.max(b.getSize() / 2, 1);
					if (newSize != b.getSize()) gebaeudeSchaden = true;
					b.setSize(newSize);
				}

				List<String> ereignisse = new ArrayList();
				ereignisse.add("In " + r + " beschwor " + mage + " den Hammer der Götter. Der Boden senkte sich permanent");
				if (tote > 0) ereignisse.add(tote + " Bauern starben");
				if (gebaeudeSchaden) ereignisse.add("alle Gebaeude nahmen beträchtlichen Schaden");

				new Info(StringUtils.aufzaehlung(ereignisse) + ".", r);
			} else {
				// das Land versinkt im Ozean - alle und alles ist futsch!
				for (Item res : r.getResourcen()) res.setAnzahl(0);
				int tote = r.getBauern();
				r.setBauern(0);
				// TODO Luxuspreise auf Anfang

				boolean gebaeudeSchaden = false;
				for (Building b : r.getBuildings()) {
					b.setSize(0);
					gebaeudeSchaden = true;
				}
				// das Ertrinken wird in SpielerLoeschen erledigt.

				List<String> ereignisse = new ArrayList();
				ereignisse.add("In " + r + " beschwor " + mage + " den Hammer der Götter. Das Land wird nun vom Meer verschlungen");
				if (tote > 0) ereignisse.add(tote + " Bauern ertrinken");
				if (gebaeudeSchaden) ereignisse.add("alle Gebaeude sind verloren");

				new Info(StringUtils.aufzaehlung(ereignisse) + ".", r);

				// TODO Was soll mit dem Regions-Namen passieren?
			}




			Region newRegion = r.cloneAs(newTerrain);

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
			new Magie(mage + " beschwört den Hammer der Götter, aber nichts passiert.", mage);
			return 0;
		}

		return stufe;
	}

    @Override
	public String getCRSyntax() { return ""; }

}
