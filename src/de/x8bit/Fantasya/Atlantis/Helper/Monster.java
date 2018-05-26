package de.x8bit.Fantasya.Atlantis.Helper;

import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Dingens;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Ship;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Host.ZAT.Battle.Krieger;

/**
 * eigentlich nur eine Zwischenklasse um zu verhindern das Monster irgendwas
 * herstellen, lernen etc. ... damit muss nicht bei jedem Monster die passende
 * Methode überschrieben werden ... darf das Monster doch lernen so muss es nur
 * in der Monster-Klasse erlaubt werden<br><br>
 * da Java etwas unflexibel ist, erlauben die <i>allowSOMTHING</i> Methoden wiederrum
 * zur Lernen etc. ... <b>super</b>.<i>super</i>.SOMTHING() ist ja nicht möglich
 * mit Java
 * @author mogel
 */
public abstract class Monster extends Unit
{
    @Override
	public boolean Lernen(Class<? extends Skill> skill) { new Fehler(this + " kann nicht lernen.", this); return false; }
	protected boolean allowLernen(Class<? extends Skill> skill) { return super.Lernen(skill); }

	/**
	 * setzt die entsprechenden Monster-Waffen
	 * @param krieger - dieser Krieger wird gesetzt
	 */
	public void Factory(Krieger krieger)
	{
		// per Default nutzen die nur die üblichen/öffentlichen Waffen
	}

	/**
	 * alle Monster können per Default nicht hungern
	 */
    @Override
	public boolean actionUnterhalt()
	{
		return false;
	}
	
	/**
	 * alle Monster können per Default nicht hungern -> manche sollen aber doch:
	 */
	public boolean actionUnterhaltNormal() {
		return super.actionUnterhalt();
	}
	

    public abstract void planMonster();
    
    /**
     * wird vor der Monsterplanung aufgerufen - bevor die Meldungen der 
     * vergangenen Runde gelöscht werden. Hier kann man also auf das Geschehene
     * reagieren.
     */
    public abstract void meldungenAuswerten();
    
    /**
     * 
     */
	public static List<Unit> getAttackableUnitList(Unit monster, Dingens victimAccessible)
	{
		// List<Unit> units = new ArrayList<Unit>();
		// for(Unit u : region.getUnits()) if (u.getSchiff() == ship.getNummer()) units.add(u);
		List<Unit> attackableUnitList = new ArrayList<Unit>(); // Liste an Einheiten geholt.
		Region region = Region.Load(monster.getCoords()); // Region des Monsters geholt.
		
		if (victimAccessible == null)
		{
			// alle Einheiten, die nicht der Monsterpartei gehören
			for(Unit unit : region.getUnits()) if (unit.getOwner() != monster.getOwner()) attackableUnitList.add(unit);
		}
		else if (victimAccessible instanceof Ship)
		{
			Ship ship = (Ship) victimAccessible;
			// Alle Einheiten auf dem Schiff
			for(Unit u : region.getUnits()) if (u.getSchiff() == ship.getNummer()) attackableUnitList.add(u);
		}
		else if (victimAccessible instanceof Building)
		{
			Building building = (Building) victimAccessible;
			// Alle Einheiten in dem Gebäude
			for(Unit u : region.getUnits()) if (u.getGebaeude() == building.getNummer()) attackableUnitList.add(u);
		}
		
		return attackableUnitList;
	}
}
