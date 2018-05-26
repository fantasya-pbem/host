package de.x8bit.Fantasya.Atlantis;

import de.x8bit.Fantasya.Atlantis.Allianz.AllianzOption;
import de.x8bit.Fantasya.Atlantis.Effect.EFXProduction;
import java.text.NumberFormat;

import de.x8bit.Fantasya.Atlantis.Effect.EFXResourcenCreate;
import de.x8bit.Fantasya.Atlantis.Effect.EFXResourcenUse;
import de.x8bit.Fantasya.Atlantis.Helper.ConstructionCheats;
import de.x8bit.Fantasya.Atlantis.Helper.ConstructionContainer;
import de.x8bit.Fantasya.Atlantis.Items.Resource;
import de.x8bit.Fantasya.Atlantis.Items.AnimalResource;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.util.StringUtils;
import java.util.SortedSet;


/** Temporaere Muellkippe, um die Basisklasse etwas zu entruempeln.
 *
 * Atlantis (die Basisklasse von Einheiten, Gegenstaenden, Regionen, Parteien,
 * ...........) beinhaltet einen Haufen Funktionalitaet, die von der Haelfte
 * der Klassen gar nicht nachgefragt wird. Unter anderem Code, der mit dem
 * Produzieren von Zeugs zusammenhaengt. Um ueberhaupt erst einmal zu Potte zu
 * kommen und die Basisklasse wenigstens ein wenig zu entschlacken, habe ich in
 * einem ersten Schritt diesen Code ungesehen in dieser Zwischenklasse
 * abgeladen. Das ist nur eine temporaere Loesung, der muss trotzdem heftig
 * ueberarbeitet werden.
 *
 * @author Ulf Lorenz
 */

public class Dingens extends Atlantis {

	/** alle benötigten Items und deren Anzahl */
	private ConstructionContainer [] neededConstructionItems = null;
	public void setConstructionItems(ConstructionContainer [] neededConstructionItems) { this.neededConstructionItems = neededConstructionItems; }
	public ConstructionContainer [] getConstructionItems() { return neededConstructionItems; }

	/** alle benötigten Talente */
	private ConstructionContainer [] neededConstructionSkills = null;
	public void setConstructionSkills(ConstructionContainer [] neededConstructionSkills) { this.neededConstructionSkills = neededConstructionSkills; }
	public ConstructionContainer [] getConstructionSkills() { return neededConstructionSkills; }

	/** alle benötigten Gebäude */
	private ConstructionContainer [] neededConstructionBuildings = null;
	public void setConstructionBuildings(ConstructionContainer [] neededConstructionBuildings) { this.neededConstructionBuildings = neededConstructionBuildings; }
	public ConstructionContainer [] getConstructionBuildings() { return neededConstructionBuildings; }

	/** alle Gebäude die Resourcen sparen */
	private ConstructionCheats [] neededConstructionCheats = null;
	public void setConstructionCheats(ConstructionCheats [] neededConstructionCheats) { this.neededConstructionCheats = neededConstructionCheats; }
	public ConstructionCheats [] getConstructionCheats() { return neededConstructionCheats; }

	/** maximale Größe eine AtlantisObjects ... Items haben hier definitiv eine 1 ... wärend Gebäude etc. hier die maximale Größe beinhalten */
	int neededConstructionSize = 0;
	public int getConstructionSize() { return neededConstructionSize; }
	public void setConstructionSize(int size) { neededConstructionSize = size; }
	/** ob das Objekt überhaupt hergestellt werden kann */


	/**
	 * erstellt das Atlantis-Object ... also Item, Gebäude und Schiff ... was
	 * nicht mittels <b>MACHE</b> hergestellt werden kann, liefert eine Fehlermeldung
	 * (Pegasus z.B.)
	 * @param u - diese Einheit stellt das Object her
	 * @param anzahl - wieviel "gemacht" werden soll
	 */
	public void Mache(Unit u, int anzahl)
	{
		new Fehler(u + " - " + this.getClass().getSimpleName() + " kann nicht hergestellt werden.", u);
	}

	/**
	 * erstellt das Atlantis-Object ... also Item, Gebäude und Schiff ... was
	 * nicht mittels <b>MACHE</b> hergestellt werden kann, liefert eine Fehlermeldung
	 * (Pegasus z.B.)
	 * @param u - diese Einheit stellt das Object her
	 */
	public void Mache(Unit u)
	{
		new Fehler(this.getClass().getSimpleName() + " kann nicht hergestellt werden.", u);
	}

	/**
	 * die neue Variante zum Erstellen von Atlantisobjekten
	 * @param unit - diese Einheit produziert dieses Objekt
	 * @param anzahl - soviel soll maximal hergestellt werden 
	 * @return Anzahl der produzierbaren Einheiten bzw. Größenpunkte
	 */
	@SuppressWarnings("unchecked")
	protected int GenericMake2(Unit unit, int anzahl)
	{
		// überprüfen ob das Objekt überhaupt hergestellt werden kann
		if (getConstructionSize() == 0) { new Fehler(unit + " kann " + this.getTyp() + " nicht herstellen.", unit, unit.getCoords()); return 0; }	// nö - also zurück
		
		// Anzahl überprüfen
		anzahl = checkConstructionSkill(unit, anzahl, getConstructionSkills());			// über das Talent
		if (anzahl > 0) anzahl = checkConstructionItem(unit, anzahl, getConstructionItems());			// über die Items
		if (anzahl > 0) anzahl = checkConstructionBuilding(unit, anzahl, getConstructionBuildings());	// über die Gebäude
		if (anzahl == 0) return 0;	// Fehlermeldung kam schon

		// EFXProduction: Magieeffekte, die die Produktions-Fähigkeiten steigern
		for(Effect efx : unit.getEffects()) {
			if (efx instanceof EFXProduction) {
				if (this instanceof Item) {
					anzahl += ((EFXProduction) efx).EFXCalculate(unit, (Item)this, anzahl);
				} else {
					// z.B. für Schiffe und Gebäude
					anzahl += ((EFXProduction) efx).EFXCalculate(unit, null, anzahl);
				}
			}
		}
		
		// jetzt die Items "wegnehmen" :)
		int extra = 0;
		if (getConstructionItems() != null)
		{
			for(int i = 0; i < getConstructionItems().length; i++)
			{
				ConstructionContainer cc = getConstructionItems()[i];
				Item item = unit.getItem((Class<? extends Item>) cc.getClazz());

				double save = checkConstructionCheats(item.getClass(), unit, anzahl * cc.getValue());
				if (save > 0.000000001d) {
					new Info(unit + " spart " + ((int)save) + " von " + anzahl * cc.getValue() + " sonst benötigten " + cc.getClazz().getSimpleName() + ".", unit, unit.getCoords());
				}
				item.setAnzahl(item.getAnzahl() - (anzahl * cc.getValue() - (int)save));
				
				// jetzt die Magieeffekte pro Item
				for(Effect efx : unit.getEffects()) {
					if (efx instanceof EFXResourcenUse) extra += ((EFXResourcenUse) efx).EFXCalculate(unit, item, anzahl);
				}
			}
		} else
		{
			// eine Resource -> oder Fehler
			if (this instanceof Resource) {
				// gibt es Bewacher, die das nicht zulassen wollen?
                if (!unit.canRessourcenAbbauen()) {
                    SortedSet<Unit> verhinderer = unit.getVerhinderer(AllianzOption.Resourcen);
                    new Fehler(
                            unit + " kann keine Ressourcen abbauen: " + 
                            StringUtils.aufzaehlung(verhinderer) +
                            ((verhinderer.size()==1)?" verhindert das.":" verhindern das."),
                            unit,
                            unit.getCoords()
                    );

                    for (Unit u : verhinderer) {
                        new Info(u + " hindert " + unit + " daran, " + this.getName() + " abzubauen.", u, u.getCoords());
                    }
                    
                    return 0;
                }

                Region r = Region.Load(unit.getCoords());
				Item resource = r.getResource(((Item)this).getClass());
				int max = getAvailableResources(unit, ((Item)this).getClass());
				if (max < anzahl) {
					// Mantis #205
					if (max > 0 ) {
						new Info(unit + " findet nicht ausreichend " + ((Item)this).getName() + " - eigentlich wären " + anzahl + " machbar gewesen.", unit, unit.getCoords());
					}
					anzahl = max;
				} // Resourcen sind zu Ende

				// Meldung an die Einheit, wenn das gewünschte nicht (mehr) vorhanden ist.
				if (anzahl == 0) {
					if (unit.getPersonen() == 1) {
						new Info(unit + " würde ja gern, aber findet kein " + ((Item)this).getName() + ". Vielleicht war jemand schneller?", unit, unit.getCoords());
					} else {
						new Info(unit + " würden ja gern, aber finden kein " + ((Item)this).getName() + ". Vielleicht war jemand schneller?", unit, unit.getCoords());
					}
					return 0;
				}

				double save = checkConstructionCheats(resource.getClass(), unit, anzahl);
				if (save > 0.000000001d) {
					new Info(unit + " spart " + ((int)save) + " " + getClass().getSimpleName() + " in der Region.", unit, unit.getCoords());
				}
				// Mantis #181 - persistente Resourcen werden jetzt auch (erstmal) verbraucht,
				// werden aber in Produktion.PreAction() / .PostAction() gesichert / wiederhergestellt.
				int newAnzahl = resource.getAnzahl() - (anzahl - (int)save);
				resource.setAnzahl(newAnzahl);
				
				// jetzt die Magieeffekte pro Item
				for(Effect efx : unit.getEffects()) if (efx instanceof EFXResourcenCreate) extra += ((EFXResourcenCreate) efx).EFXCalculate(unit, resource, anzahl);
			}
		}
		
		// Produktionsmeldung muss in den entsprechenden Basis-Klassen erstellt werden
		return anzahl + extra;
	}

	@SuppressWarnings("unchecked")
	private int checkConstructionSkill(Unit unit, int anzahl, ConstructionContainer cc []) {
		if (cc == null) return anzahl;
		for(int i = 0; i < cc.length; i++)
		{
			Class<? extends Skill> skill = (Class<? extends Skill>) cc[i].getClazz();
			
			// minimaler Talentwert erreicht?
			if (unit.Talentwert(skill) < cc[i].getValue()) {
                if (this instanceof Item) {
                    Item it = (Item)this;
                    int alteZahl = it.getAnzahl(); // Ursprungsmenge merken
                    it.setAnzahl(anzahl);
                    if (it instanceof AnimalResource) {
                        new Fehler(unit + " ist nicht talentiert genug, um " + this.getName() + " fangen zu können. Nötig wäre wenigstens Talentwert " + cc[i].getValue() + " in " +  skill.getSimpleName() + ".", unit);
                    } else {
                        new Fehler(unit + " ist nicht talentiert genug, um an " + this.getClass().getSimpleName() + " bauen zu können. Nötig wäre " + skill.getSimpleName() + " "+cc[i].getValue()+".", unit);
                    }
                    it.setAnzahl(alteZahl); // Ursprungsmenge wieder herstellen
                    return 0;
                }
                
                // hä, kein Item?
                new Fehler(unit + " ist nicht talentiert genug, um an " + this.getClass().getSimpleName() + " " + anzahl + " bauen zu können. Nötig wäre " + skill.getSimpleName() + " "+cc[i].getValue()+".", unit);
				return 0;
			}

			// Talentwert / (Talentwert / Größe) => Größe bleibt
			int min = (unit.Talentwert(skill) * unit.getPersonen()) / cc[i].getValue();
			if (min < anzahl)
			{
				// die Meldung kommt immer - ist also (mehr oder weniger) ein falsches Falsch - deswegen einfach ausblenden
				// new Fehler("Hat nicht genügend " + cc[i].getClazz().getSimpleName() + " um " + anzahl + " Punkte bauen zu können, reduziert auf " + min, unit, unit.getCoords());
				anzahl = min;
			}
		}
		return anzahl;
	}



	@SuppressWarnings("unchecked")
	private int checkConstructionItem(Unit unit, int anzahl, ConstructionContainer cc []) {
		if (cc == null) return anzahl;
		for(int i = 0; i < cc.length; i++)
		{
			int min = getAvailableItems(unit, (Class<? extends Item>) cc[i].getClazz()) / cc[i].getValue();
			if (min < anzahl)
			{
				new Fehler(unit + " hat nicht genügend " + cc[i].getClazz().getSimpleName() + " um " + anzahl + " Punkte bauen zu können, reduziert auf " + min, unit, unit.getCoords());
				anzahl = min;
			}
		}
		return anzahl;
	}
	
	/**
	 * liefert die vorhanden "theoretischen" Items zurück ... hier werden die ConstruktionCheats mit einberechnet
	 * @param unit - diese Einheit produziert
	 * @param item - dieses Item wird verwendet
	 * @return verfügbare "theoretische" Items
	 */
	private int getAvailableItems(Unit unit, Class<? extends Item> item)
	{
		// reale Anzahl holen
		int anzahl = unit.getItem(item).getAnzahl();
		
		double save = checkConstructionCheats(item, unit, anzahl);

		// Mantis #187
		// oh, oh - hier interessiert letztlich nicht die Einsparung VON DER REALEN Anzahl aus,
		// sondern müssen rausfinden, welcher VON WELCHEM FIKTIVEN Bestand aus bei bekannter 
		// Einsparung alles reale verbraucht würde.
		if (save > 0.0000001d) {
			// bei 1/2 Einsparung: 1/2 , möglich sind 2/1 des realen
			// bei 1/3 Einsparung: 1/3 , möglich sind 3/2 des realen
			// bei 1/4 Einsparung: 1/4 , möglich sind 4/3 des realen
			double saveQuota = save / (double)anzahl; // 1/2 ; 1/3 ; 1/4

			double needQuota = 1d - saveQuota; // 1/2 ; 2/3 ; 3/4

			double extensionQuota = 1d / needQuota; // 2/1 ; 3/2 ; 4/3

			return (int) Math.floor((double)anzahl * extensionQuota);
		}
		
		return anzahl;
	}

	/**
	 * liefert die vorhanden "theoretischen" Resourcen zurück ... hier werden die ConstruktionCheats mit einberechnet
	 * @param unit ... diese Einheit will eine Resource abbauen
	 * @param resource ... diese Resource wird abgebaut
	 * @return
	 */
	private int getAvailableResources(Unit unit, Class<? extends Item> resource)
	{
		// reale Anzahl holen
		Region region = Region.Load(unit.getCoords());
		int anzahl = region.getResource(resource).getAnzahl();		
		double save = checkConstructionCheats(resource, unit, anzahl);

		// Mantis #148
		// das gleiche wie in getAvailableItems
		if (save > 0.0000001d) {
			// bei 1/2 Einsparung: 1/2 , möglich sind 2/1 des realen
			// bei 1/3 Einsparung: 1/3 , möglich sind 3/2 des realen
			// bei 1/4 Einsparung: 1/4 , möglich sind 4/3 des realen
			double saveQuota = save / (double)anzahl; // 1/2 ; 1/3 ; 1/4

			double needQuota = 1d - saveQuota; // 1/2 ; 2/3 ; 3/4

			double extensionQuota = 1d / needQuota; // 2/1 ; 3/2 ; 4/3

			return (int) Math.floor((double)anzahl * extensionQuota);
		}

		return anzahl;
	}

	@SuppressWarnings("unchecked")
	private int checkConstructionBuilding(Unit unit, int anzahl, ConstructionContainer cc []) {
		if (cc == null) return anzahl;
		Region region = Region.Load(unit.getCoords());
		for(int i = 0; i < cc.length; i++)
		{
			boolean building = region.hatGebaeude((Class<? extends Building>) cc[i].getClazz(), cc[i].getValue(), unit);
			if (!building)
			{
				new Fehler("Es steht kein " + cc[i].getClazz().getSimpleName() + " in der Region um " + this.getTyp() + " bauen zu können", unit, unit.getCoords());
				return 0;
			}
		}
		return anzahl;
	}

	/**
	 * Berechnet die Vorteile durch die Gebäude
	 * @param item - dieses Item wird verwendet
	 * @param unit - diese Einheit produziert
	 * @param anzahl - soviel wurde benötigt
	 * @return so viele Items wurde "gesichert"
	 */
	private double checkConstructionCheats(Class<? extends Item> item, Unit unit, int anzahl)
	{
		if (getConstructionCheats() == null) return 0; // keine Verbesserungen
		
		if (unit.getGebaeude() == 0) return 0; // keine Gebäude -> kein Effekt
		
		Building building = Building.getBuilding(unit.getGebaeude());	// Einheit ist in diesem Gebäude
		if (building == null) { unit.setGebaeude(0); return 0; }


        // Belegung des Gebäudes testen und ggf. die Effizienz mindern:
        int insassen = 0;
        for (Unit u : Unit.CACHE.getAll(unit.getCoords())) {
            if (u.getGebaeude() == building.getNummer()) insassen += u.getPersonen();
        }


        double zukleinFaktor = (double)building.getSize() / (double)insassen;
        if (zukleinFaktor > 1) zukleinFaktor = 1;
        if (zukleinFaktor < 0.999) {
            new Fehler(unit.toString() + " stellt fest das das Gebäude " + building.toString() + " überbelegt ist und kann es nur zu "
                    + NumberFormat.getPercentInstance().format(zukleinFaktor) + " Prozent nutzen.",
                    unit, unit.getCoords());
        }

		for(ConstructionCheats cc : getConstructionCheats())
		{
			int matchValue = 0; // >0 wenn das Item weniger verbraucht wird
			if (building.getClass().equals(cc.getConstructionBuilding()))	// das Gebäude passt schon mal
			{
				// Item testen
				for(ConstructionContainer ci : cc.getConstructionItems())
				{
					if (item.equals(ci.getClazz())) matchValue = ci.getValue();
				}
				// Item gefunden -> gespartes zurück geben
				if (matchValue > 0) return (double)anzahl / (double)matchValue * zukleinFaktor;
			}			
		}
			
		return 0;
	}
}
