package de.x8bit.Fantasya.Atlantis.Units;

import java.util.List;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Richtung;
import de.x8bit.Fantasya.Atlantis.Ship;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Helper.Monster;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.Atlantis.Messages.ZATMsg;
import de.x8bit.Fantasya.Atlantis.Regions.Ozean;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapons.MKrakenarm;
import de.x8bit.Fantasya.util.Codierung;
import de.x8bit.Fantasya.util.Random;

public class Krake extends Monster {
	
	public Krake() { }
	
    @Override
	public int Talentwert(Skill skill)
	{
		int tw = super.Talentwert(skill);
		return (tw < 0 ? 0 : tw);
	}

    @Override
	public int Trefferpunkte() { return 100; }
	
    @Override
	public boolean canSwim()
	{
		return true;
	}

	/**
	 * setzt die entsprechenden Monster-Waffen
	 * @param krieger - dieser Krieger wird gesetzt
	 */
    @Override
	public void Factory(de.x8bit.Fantasya.Host.ZAT.Battle.Krieger krieger)
	{
		krieger.weapons.add(new MKrakenarm(this));
	}
	
	/*
	 *	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	 */
	
    @Override
	public void planMonster() {
		Befehle.clear();	// alten Befehle löschen
		
		Coords coords = this.getCoords();
		
//		new SysMsg(" - Bewegung für Krake " + this);
		// Kraken schwimmen immer gerade aus - bis sie auf Festland treffen ... dann
		// wählen sie ein zufällige neue Richtung
		String direction = getStringProperty("richtung", Richtung.random().getShortcut() );
		Region destination = Region.Load(coords.shift(Richtung.getRichtung(direction)));
		int versuche = 0;
		while(!(destination instanceof Ozean)) {
			direction = Richtung.random().getShortcut();
			destination = Region.Load(coords.shift(Richtung.getRichtung(direction)));
			if (versuche++ > 20) {
				new SysMsg(this + " hat keine Richtung auswählen können -> abgebrochen");
				return;
			}
		}
		Befehle.add("NACH " + direction);
		this.setProperty("richtung", direction);
		
//		new SysMsg(" - Richtung gesetzt");
		
		Region region = Region.Load(this.getCoords());
		if (!region.getShips().isEmpty()) {
			int value = Random.rnd(0, GameRules.Monster.DARK.Krake.AttackMax());
			value = 0;
			if (value < GameRules.Monster.DARK.Krake.AttackMin()) {
				Ship ship = region.getShips().get(Random.rnd(0, region.getShips().size()));
				// List<Unit> units = new ArrayList<Unit>();
				// for(Unit u : region.getUnits()) if (u.getSchiff() == ship.getNummer()) units.add(u);
				List<Unit> units = Monster.getAttackableUnitList(this, ship);
				if (!units.isEmpty()) { // kann ja auch ein leeres Schiff gewesen sein
					Unit victim = units.get(Random.rnd(0, units.size()));
					Befehle.add("ATTACKIERE " + victim.getNummerBase36());
				}
			}
		}
//		new SysMsg(" - " + this + " ist fertig");
	}
	
	/*
	 *	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	 */
	
	public static void spawnMonster()
	{
		new ZATMsg("Monsterplanung: Kraken");
		for(int insel = 0; insel < GameRules.getInselkennungErzeugung(); insel++)
		{
			int count = 0;
			List<Region> regionen = Region.getRegions4Insel(insel, Ozean.class);
			if (regionen.isEmpty()) {
				new SysMsg("Keinen Ozean für Inselkennung " + insel + " gefunden...");
				continue;
			}
			
			for(Region r : regionen) for(Unit u : r.getUnits()) if (u instanceof Krake) count++;
			if (count >= GameRules.Monster.DARK.Krake.SpawnMax()) continue;
			
			// neuen Kraken aussetzen
			spawn(regionen.get(Random.rnd(0, regionen.size())));
		}
	}
	
	public static Krake spawn(Region region)
	{
		Krake krake = (Krake) Unit.CreateUnit("Krake", Codierung.fromBase36("dark"), region.getCoords());
		krake.setPersonen(Random.rnd(GameRules.Monster.DARK.Krake.PersonMin(), GameRules.Monster.DARK.Krake.PersonsMax()));
		krake.setTarnPartei(0);
		krake.setName("Kraken");
		return krake;
	}


	@Override
	public boolean istSpielerRasse() { return false;	}

	@Override
	public String getRassenName() {
		if (this.getPersonen() == 1) return getRasse();
		return "Kraken";
	}

    @Override
    public void meldungenAuswerten() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
