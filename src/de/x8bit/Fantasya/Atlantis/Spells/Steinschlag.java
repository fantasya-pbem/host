package de.x8bit.Fantasya.Atlantis.Spells;

import java.util.ArrayList;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Spell;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Helper.Elementar;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.Magie;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Regions.Berge;
import de.x8bit.Fantasya.Atlantis.Regions.Geroellebene;
import de.x8bit.Fantasya.Atlantis.Regions.Gletscher;
import de.x8bit.Fantasya.Atlantis.Regions.Hochland;
import de.x8bit.Fantasya.Atlantis.Regions.Vulkan;
import de.x8bit.Fantasya.Atlantis.Regions.aktiverVulkan;
import de.x8bit.Fantasya.Atlantis.Spell.ConfusionSpell;
import de.x8bit.Fantasya.Host.ZAT.Battle.Krieger;
import de.x8bit.Fantasya.Host.ZAT.Battle.KriegerCounter;
import de.x8bit.Fantasya.Host.ZAT.Battle.Side;
import de.x8bit.Fantasya.util.Random;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Steinschlag extends Spell implements ConfusionSpell {

	public String getName() { return "Steinschlag"; }
	public String getBeschreibung() {
		return "Wie bei einem Erdbeben wird der Magie den Berg zum Wanken bringen. Dabei werden sich kleinere"+
				" und größere Steine lösen und auf die Gegner rollen. Dabei werden Stufe x Stufe x 100 gegnerische" +
				" Krieger am Anfang des Kampfes verletzt.";
	}
	public String getSpruch() { return "ZAUBERE \"Steinschlag\" [Stufe]"; }
	public String getKampfzauberSpruch() { return "KAMPFZAUBER VERWIRRUNG \"" + getName() + "\" [Stufe]"; }
	public Elementar getElementar() { return Elementar.Erde; }
	public int getStufe() { return 4; }

    @Override
	public String[] getTemplates() {
		return new String [] { 
				"(\")?(steinschlag)(\")? [1-9][0-9]?",
		}; 
	}	
	public String getCRSyntax() { return "i"; }

	public int ExecuteSpell(Unit mage, String[] param) 
	{
		int stufe = 1; // Wunschstufe zum Zaubern
		if (param.length > 2) stufe = getSpellLevel(mage, param[2]);
		if (stufe == 0) return 0; // Fehler kam schon in getSpellLevel

		Region region = Region.Load(mage.getCoords());
		if (checkValidRegions(region))
		{
			makeRockfall(stufe, mage.getCoords());
			new Magie(mage + " lässt die Erde in " + region + " beben.", mage);
		} else
		{
			new Fehler(mage + " versuchte einen " + getName() + " in " + region + " zu zaubern, aber das Land ist einfach zu flach.", mage);
		}
		
		return stufe;
	}
	
	/** 
	 * der Spruch kann nicht überall gezaubert werden
	 * @param region - diese Region
	 * @return
	 */
	private boolean checkValidRegions(Region region)
	{
		if (region.getClass().equals(Berge.class)) return true;
		if (region.getClass().equals(Gletscher.class)) return true;
		if (region.getClass().equals(Hochland.class)) return true;
		if (region.getClass().equals(Vulkan.class)) return true;
		if (region.getClass().equals(aktiverVulkan.class)) return true;
		if (region.getClass().equals(Geroellebene.class)) return true;
        
		return false;
	}
	
	/**
	 * erzeugt einen einfachen Steinschlag in der aktuellen Region
	 * @param stufe - auf dieser Stufe wurde gezaubert
	 * @param coords - Koordinaten der Region die das Erdbeben erhält
	 */
	private void makeRockfall(int stufe, Coords coords)
	{
		Region region = Region.Load(coords);
		for(Building b : region.getBuildings()) {
			int punkte = 1;	// mind. 1 Größenpunkt 
			int max = stufe * 2;
			if (max > 1) {
				int prozent = Random.rnd(1, max);
				punkte = (int) ((double) b.getSize() * ((double) prozent / 100.0));
			}
			if (punkte > b.getSize()) punkte = b.getSize();
			b.setSize(b.getSize() - punkte); // wenn 0 dann werden die Gebäude in "ZAT -> Produktion" wieder gelöscht => sind aber für eine Runde sichtbar !!
			Unit unit = Unit.Load(b.getOwner());
			if (unit != null) new Info("Das Gebäude wurde durch den Steinschlag um " + punkte + " beschädigt.", unit);
		}
		
		// alle Völker in der Region informieren
		Set<Unit> alu = new HashSet<Unit>();
		for(Unit unit : region.getUnits())
		{
			boolean found = false;
			for(Unit u : alu) if (u.getOwner() == unit.getOwner()) found = true;
			if (!found) alu.add(unit);
		}
		for(Unit unit : alu) {
			new Info("Es gab einen Steinschlag in " + region + ".", Partei.getPartei(unit.getOwner()));
		}
	}
	
	@Override // von ConfusionSpell geerbt
	public int ExecuteSpell(Unit mage, Side my, Side other, String[] param) {
		int stufe = 1; // Wunschstufe zum Zaubern
		if (param.length > 2) stufe = getSpellLevel(mage, param[2]);
		if (stufe == 0) return 0; // Fehler kam schon in getSpellLevel
		
		Region region = Region.Load(mage.getCoords());
		if (checkValidRegions(region)) {
			makeRockfall(stufe, mage.getCoords());
			new Magie(mage + " lässt die Erde in " + region + " beben.", mage);
		} else {
			new Fehler(mage + " versuchte einen " + getName() + " in " + region + " zu zaubern, aber das Land ist einfach zu flach.", mage);
			return 0;
		}

		// Effekte an die Krieger verteilen
		PrepareSpell(other, stufe * stufe * 100, stufe);
		
		// gezauberte Stufe zurück liefern
		return stufe;
	}
	
	// Effekt auf die Krieger verteilen
	private void PrepareSpell(Side opfer, int maxKrieger, int stufe)
	{
		// alle Krieger in einer Liste sammeln
		List<Krieger> krieger = new ArrayList<Krieger>();
		for(Krieger k : opfer.vorne) krieger.add(k);
		for(Krieger k : opfer.hinten) krieger.add(k);
		
		// Effekt verteilen
		KriegerCounter kc = new KriegerCounter();
		while(maxKrieger > 0 && krieger.size() > 0)
		{
			int nummer = Random.rnd(0, krieger.size() - 1);
			Krieger k = krieger.get(nummer);
			krieger.remove(k);
			k.setLebenspunkte(Random.rnd(stufe, k.getTrefferpunkte() - 1)); // sollen ja nicht gleich sterben
			kc.count(k);

			maxKrieger--;
		}

        opfer.getGefecht().meldung("Ein Steinschlag kurz vor dem Angriff verletzt " + kc.getReportPhrase() + ".", true);

	}


}
