package de.x8bit.Fantasya.Atlantis.Units;

import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Helper.Monster;
import de.x8bit.Fantasya.Atlantis.Regions.Wald;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.util.Codierung;
import de.x8bit.Fantasya.util.Random;

public class Puschkin extends Monster {
	
	public static void spawnMonster() {
		// erstmal alle Wälder holen wo maximal 5 Personen sind
		List<Region> regionen = getWald();
		// dann pauschal einen Bär spawnen
		for(Region r : regionen) {
			int spawn = Random.rnd(0, 100);
			if (spawn > GameRules.Monster.TIER.Puschkin.SpawnPercent()) continue;
			Unit b = Unit.CreateUnit("Puschkin", Codierung.fromBase36("tier"), r.getCoords());
			b.setPersonen(1);
			b.setTarnPartei(0);
			b.setName("Bär");
			b.setProperty("lifetime", GameRules.Monster.TIER.Puschkin.getLifemax());
		}
	}
	
	private static List<Region> getWald() {
		List<Region> regionen = new ArrayList<Region>();
		for (Region r : Region.CACHE.values()) {
			if (r.getClass() != Wald.class) {
				int anzahl = 0;
				for(Unit u : r.getUnits()) anzahl += u.getPersonen();
				if (anzahl > 0 && anzahl < 6) regionen.add(r);
			}
		}
		return regionen;
	}

	@Override
	public void planMonster() {
		int lifetime = getIntegerProperty("lifetime", -1);
		if (lifetime-- < 0) {
			setPersonen(0);
		} else {
			int attack = Random.rnd(0, 100);
			if (attack < GameRules.Monster.TIER.Puschkin.AttackPercent()) {
				List<Unit> victimList =  Monster.getAttackableUnitList(this, null);
				if (victimList.isEmpty())
				{
					Befehle.add("faulenze");
				}
				else
				{
					int idx = Random.rnd(0, victimList.size() - 1);
					Unit victimUnit = victimList.get(idx);
					Befehle.add("ATTACKIERE " + victimUnit.getNummerBase36());
				}
			} else {
				Befehle.add("faulenze");
			}
		}
	}

	@Override
	public void meldungenAuswerten() {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean istSpielerRasse() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getRassenName() {
		return "Bär";
	}

}
