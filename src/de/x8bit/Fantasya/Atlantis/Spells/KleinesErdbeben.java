package de.x8bit.Fantasya.Atlantis.Spells;

import java.util.ArrayList;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Spell;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Helper.Elementar;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.Magie;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Spell.ConfusionSpell;
import de.x8bit.Fantasya.Host.ZAT.Battle.Krieger;
import de.x8bit.Fantasya.Host.ZAT.Battle.Side;
import de.x8bit.Fantasya.Host.ZAT.Battle.Effects.BFXErdbeben;
import de.x8bit.Fantasya.Host.ZAT.Battle.KriegerCounter;
import de.x8bit.Fantasya.util.Random;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KleinesErdbeben extends Spell implements ConfusionSpell {

	public String getName() { return "Kleines Erdbeben"; }
	public String getBeschreibung() 
	{
		return "Ein kleines Erdbeben läßt die Erde schwanken - Gebäude stürzen ein." +
				" Alle Gebäude in der Region werden mit einer maximalen Wahrscheinlichkeit von 'Stufe x 5' Prozent einstürzen. " +
				" Wird der Zauber im Kampf eingesetzt, so werden die gegnerischen Einheiten vor dem Kampf verunsichert und " +
				" werden nicht angreifen. Dieser Effekt wird aber mit jeder Kampfrunde geringer. Vor dem Kampf werden " + 
				" 'Stufe x 100' Krieger verwirrt.";
	}
	public String getSpruch() { return "ZAUBERE \"Kleines Erdbeben\" [Stufe]"; }
	public String getKampfzauberSpruch() { return "KAMPFZAUBER VERWIRRUNG \"" + getName() + "\" [Stufe]"; }
	public Elementar getElementar() { return Elementar.Erde; }
	public int getStufe() { return 2; }

	public String[] getTemplates() {
		return new String [] { 
				"(\")?(kleines erdbeben)(\")? [1-9][0-9]?",
		}; 
	}	
	public String getCRSyntax() { return "i"; }

	public int ExecuteSpell(Unit mage, String[] param) 
	{
		int stufe = 1; // Wunschstufe zum Zaubern
		if (param.length > 2) stufe = getSpellLevel(mage, param[2]);
		if (stufe == 0) return 0; // Fehler kam schon in getSpellLevel

		Region region = Region.Load(mage.getCoords());
		makeEarthquake(stufe, mage.getCoords());
		new Magie(mage + " lässt die Erde in " + region + " beben", mage);
		
		return stufe;
	}
	
	/**
	 * erzeugt ein einfaches Erdbeben in der aktuellen Region
	 * @param stufe - auf dieser Stufe wurde gezaubert
	 * @param coords - Koordinaten der Region die das Erdbeben erhält
	 */
	private void makeEarthquake(int stufe, Coords coords)
	{
		Region region = Region.Load(coords);
		for(Building b : region.getBuildings())
		{
			int punkte = 1;	// mind. 1 Größenpunkt 
			int max = stufe * 5;
			if (max > 1)
			{
				int prozent = Random.rnd(1, max);
				punkte = (int) ((double) b.getSize() * ((double) prozent / 100.0));
			}
			if (punkte > b.getSize()) punkte = b.getSize();
			b.setSize(b.getSize() - punkte); // wenn 0 dann werden die Gebäude in "ZAT -> Produktion" wieder gelöscht => sind aber für eine Runde sichtbar !!
			Unit unit = Unit.Load(b.getOwner());
			if (unit != null) new Info(b.getTyp() + " " + b + " wurde durch das Erdbeben um " + punkte + " Punkte beschädigt.", unit);
		}
		
		// alle Völker in der Region informieren
		Set<Unit> alu = new HashSet<Unit>();
        for(Unit unit : region.getUnits()) {
			boolean found = false;
			for(Unit u : alu) if (u.getOwner() == unit.getOwner()) found = true;
			if (!found) alu.add(unit);
		}
		for(Unit unit : alu) {
			new Info("Es gab ein Erdbeben in " + region + ".", Partei.getPartei(unit.getOwner()));
		}
	}
	
	@Override // von ConfusionSpell geerbt
	public int ExecuteSpell(Unit mage, Side my, Side other, String[] param) {
		int stufe = 1; // Wunschstufe zum Zaubern
		if (param.length > 2) stufe = getSpellLevel(mage, param[2]);
		if (stufe == 0) return 0; // Fehler kam schon in getSpellLevel
		
		// Effekte an die Krieger verteilen
		PrepareSpell(other, stufe * 100);
		
		// gezauberte Stufe zurück liefern
		return stufe;
	}
	
	// Effekt auf die Krieger verteilen
	private void PrepareSpell(Side opfer, int maxKrieger)
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
			k.addEffect(new BFXErdbeben(75, 25));
            kc.count(k);

			maxKrieger--;
		}

        opfer.getGefecht().meldung("Ein kleines Erdbeben kurz vor dem Angriff verunsichert " + kc.getReportPhrase() + ".", true);
	}


}
