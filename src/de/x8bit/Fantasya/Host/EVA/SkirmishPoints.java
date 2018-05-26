package de.x8bit.Fantasya.Host.EVA;

import java.util.Set;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Buildings.Bergwerk;
import de.x8bit.Fantasya.Atlantis.Buildings.Burg;
import de.x8bit.Fantasya.Atlantis.Buildings.Hafen;
import de.x8bit.Fantasya.Atlantis.Buildings.Holzfaellerhuette;
import de.x8bit.Fantasya.Atlantis.Buildings.Kueche;
import de.x8bit.Fantasya.Atlantis.Buildings.Leuchtturm;
import de.x8bit.Fantasya.Atlantis.Buildings.Mine;
import de.x8bit.Fantasya.Atlantis.Buildings.Monument;
import de.x8bit.Fantasya.Atlantis.Buildings.Saegewerk;
import de.x8bit.Fantasya.Atlantis.Buildings.Sattlerei;
import de.x8bit.Fantasya.Atlantis.Buildings.Schiffswerft;
import de.x8bit.Fantasya.Atlantis.Buildings.Schmiede;
import de.x8bit.Fantasya.Atlantis.Buildings.Seehafen;
import de.x8bit.Fantasya.Atlantis.Buildings.Steg;
import de.x8bit.Fantasya.Atlantis.Buildings.Steinbruch;
import de.x8bit.Fantasya.Atlantis.Buildings.Steingrube;
import de.x8bit.Fantasya.Atlantis.Buildings.Steuerturm;
import de.x8bit.Fantasya.Atlantis.Buildings.Werkstatt;
import de.x8bit.Fantasya.Atlantis.Helper.RegionsSicht;
import de.x8bit.Fantasya.Atlantis.Messages.SysErr;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.Host.Datenbank;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;

public class SkirmishPoints extends EVABase implements NotACommand {
	
	public SkirmishPoints() {
		super("Punkteberechnung für Skirmishspiel");
		
		if (GameRules.isSkirmish()) {
			calcPointsMain();
			saveHistory();
		} else {
			new SysMsg("kein Skirmish-Spiel");
		}
	}

	@Override public boolean DoAction(Unit u, String[] befehl) { return false; }
	@Override public void DoAction(Region r, String befehl) { }
	@Override public void DoAction(Einzelbefehl eb) { }
	@Override public void PreAction() { }
	@Override public void PostAction() { }

	/**
	 * allg. Punkteberechnung - erstmal hart codiert
	 */
	private void calcPointsMain() {
		for(Partei partei : Partei.PROXY) {
			if (partei.isMonster()) continue;							// Monster ohne Punkte
			int punkte = 0;
			Set<RegionsSicht> liste = partei.getKnownRegions(false);	// alle bekannten Regionen des Volkes
			for(RegionsSicht rs : liste) {
				Region region = Region.Load(rs.getCoords());
				Partei owner = Partei.getPartei(region.getOwner());
				if (owner == null) continue;							// der steht zwar da, aber hat keine Macht und Punkte gibt es nur bei Macht
				if (owner.getNummer() == 0) continue;					// Partei 0
				
				punkte += 100;
				punkte += region.getBauern();
				
				for(Building building : region.getBuildings()) {
					if (building == null) { new SysErr("Region::getBuilding() hat einmal null"); continue; }
					if (building.getOwner() == 0) continue;				// kein Befehlshaber im Gebäude
					Unit u = Unit.Get(building.getOwner());
					if (u == null) continue;							// kein Befehlshaber im Gebäude
					if (u.getOwner() != owner.getNummer()) continue;	// kein eigenes Gebäude
					if (building instanceof Burg)					punkte += ((Burg)building).getBurgSize();
					if (building instanceof Holzfaellerhuette)		punkte += 1;
					if (building instanceof Saegewerk)				punkte += 2;
					if (building instanceof Mine)					punkte += 1;
					if (building instanceof Bergwerk)				punkte += 2;
					if (building instanceof Steingrube)				punkte += 1;
					if (building instanceof Steinbruch)				punkte += 2;
					if (building instanceof Schmiede)				punkte += 2;
					if (building instanceof Werkstatt)				punkte += 2;
					if (building instanceof Sattlerei)				punkte += 2;
					if (building instanceof Kueche)					punkte += 5;
					if (building instanceof Steuerturm)				punkte += 2;
					if (building instanceof Steg)					punkte += 1;
					if (building instanceof Hafen)					punkte += 2;
					if (building instanceof Seehafen)				punkte += 3;
					if (building instanceof Schiffswerft)			punkte += 2;
					if (building instanceof Leuchtturm)				punkte += 5;
					if (building instanceof Monument)				punkte += building.getSize() * 100;
				}
			}
			
			// Punkte für einzelne Einheiten
			for(Unit u : partei.getEinheiten()) punkte += u.getPersonen();
			
			partei.setProperty("punkte.ende",  punkte);		// Punkte vergeben
		}
	}

	/**
	 * speichert die Punkte in einer tollen Tabelle
	 */
	private void saveHistory() {
		Datenbank.Enable();
		Datenbank db = new Datenbank("Skirmishpunkte sichern");
		for(Partei partei : Partei.PROXY) {
			if (partei.isMonster()) continue;							// Monster ohne Punkte
			String query = "INSERT INTO history (runde, base10, krieg, ende, total, name) VALUES (";
			query += GameRules.getRunde() + ", ";
			query += partei.getNummer() + ", ";
			query += partei.getIntegerProperty("punkte.krieg", 0) + ", ";
			query += partei.getIntegerProperty("punkte.ende", 0) + ", ";
			query += (partei.getIntegerProperty("punkte.krieg", 0) + partei.getIntegerProperty("punkte.ende", 0)) + ", ";
			query += "'" + Datenbank.CheckValue(partei.getName(), true) + "')";
			db.myQuery = query;
			db.Insert();
		}
		Datenbank.Disable();
	}

}
