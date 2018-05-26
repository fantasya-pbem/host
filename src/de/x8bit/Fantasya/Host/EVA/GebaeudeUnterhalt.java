package de.x8bit.Fantasya.Host.EVA;

import java.util.ArrayList;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Buildings.Burg;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import java.util.HashSet;
import java.util.List;

public class GebaeudeUnterhalt extends EVABase implements NotACommand
{
	public GebaeudeUnterhalt() {
		super("Gebäude pflegen und erhalten.");
	}

	public void PostAction() {
		// wg. concurrent modification exception
		for (Building building : new HashSet<Building>(Building.PROXY)) {
			Region region = Region.Load(building.getCoords());

			if (building.getOwner() == 0) {
				Einsturz(building, region);
			} else {
				Unterhalt(building, region);
			}
		}

		// Gebäude mit Größe 0 verschwinden:
		List<Building> remove = new ArrayList<Building>();
		for (Building building : Building.PROXY) {
			if (building.getSize() == 0) {
				// Einheiten an die Luft setzen:
				for (Unit u : Unit.CACHE.getAll(building.getCoords())) {
					if (u.getGebaeude() == building.getNummer()) u.Leave();
				}

				// und das Gebäude "verschwinden" lassen:
				remove.add(building);
			}
		}
		// -- jetzt aber wirklich weg
		for(Building building : remove)	{
			Building.PROXY.remove(building);
		}
	}

	public void PreAction() { }



	
	public boolean DoAction(Unit u, String[] befehl) { return false; }
	public void DoAction(Region r, String befehl) { }
	
	/**
	 * verdammt ... das Gebäude stürzt ein
	 * @param building - dieses Gebäude
	 * @param region - diese Region
	 */
	private void Einsturz(Building building, Region region)	{
		if (building.hatFunktion())	{
			// erstmal nur keine Funktion ... aber nächste Runde !!
			building.setFunktion(false);
			return;
		}
		
		// Halbieren
		int groesse = building.getSize();
		int verfall = groesse / 2;
		if (verfall < 1) verfall = 1;	// 1 / 2 = 0.5   =>   in Java 0 !!
		if (verfall > 10) verfall = 10;	// Monument
		building.setSize(groesse - verfall);
		
		// alle Einheiten informieren
		for(Unit unit : region.getUnits()) {
			if (unit.getGebaeude() == building.getNummer()) {
				// falls das Gebäude nun keine Größe mehr hat => löschen ... dazu
				// müssen alle Einheiten raus
				if (building.getSize() == 0) {
					unit.Leave();
					new Info("Das Gebäude " + building + " stürzt über dem Kopf von " + unit + " restlos ein.", unit);
				} else {
					new Info("Teile von " + building + " (von " + groesse + " auf " + building.getSize()  + " Größenpunkte) stürzen über dem Kopf von " + unit + " ein.", unit);
				}
			}
		}
	}
	
	/**
	 * Gebäude kann unterhalten werden ... wird zumindest versucht
	 * @param building - dieses Gebäude
	 * @param region - diese Region
	 */
	private void Unterhalt(Building building, Region region)
	{
		if (building.getClass().equals(Burg.class)) return;
			
		int unterhalt = building.GebaeudeUnterhalt();
		if (unterhalt == 0) return; // kein Unterhalt nötig
		
		Unit owner = Unit.Load(building.getOwner());
		if (owner == null) { // Gebäude steht ohne Besitzer da
			building.setOwner(0);
			Einsturz(building, region);
			return;
		}
		
		Item geld = owner.getItem(Silber.class);
		if (unterhalt < geld.getAnzahl()) {
			// super ... der Boss kann Unterhalt zahlen
			geld.setAnzahl(geld.getAnzahl() - unterhalt);
			owner.setEinkommen(owner.getEinkommen() - unterhalt);
			new Info(owner + " zahlt " + unterhalt + " Silber Unterhalt für " + building + ".", owner, owner.getCoords());
			building.setFunktion(true);
			return;
		}
		
		int collected = region.CollectMoney(owner, unterhalt - geld.getAnzahl(), "für den Unterhalt von " + building);
		if (geld.getAnzahl() + collected < unterhalt) {
			geld.setAnzahl(geld.getAnzahl() + collected); // das gesammelte Geld behalten
			new Fehler(
					owner + " kann nur "
					+ geld.getAnzahl() + " von " + unterhalt + " Silber"
					+ " für den Unterhalt von " + building + " aufbringen.",
					owner);
			Einsturz(building, region);
			return;
		} else {
			new Info(
					owner + " sammelt " + (geld.getAnzahl() > 0?"zusätzlich ":" ") + collected
					+ " für die " + unterhalt + " Silber Unterhalt von " + building + ".",
					owner
				);
			geld.setAnzahl(geld.getAnzahl() + collected);	// erstmal das gesammelte geld hinzufügen
			geld.setAnzahl(geld.getAnzahl() - unterhalt);	// dann den Unterhalt für das gebäude abziehen
			owner.setEinkommen(owner.getEinkommen() - unterhalt);
			building.setFunktion(true);
		}
	}

    @Override
	public void DoAction(Einzelbefehl eb) { }

}
