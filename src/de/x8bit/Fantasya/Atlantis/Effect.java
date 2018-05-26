package de.x8bit.Fantasya.Atlantis;

import java.util.HashMap;
import java.util.Map;

import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.util.FreieNummern;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;



/**
 * Basis-Klasse für alle Effekte ... eine Zuaberspruch löst ja meistens einen Effekt aus - dafür
 * ist dann einer der entsprechenden Klassen zuständig
 * @author mogel
 *
 */
public abstract class Effect extends Atlantis {

//	/**
//	 * führt einen Effekt auf die Einheite / Region / Schiff / Gebäude aus
//	 * @param eo - ein EffektObject
//	 */
//	public abstract void Prepare(EffektObject eo);
	
	public static List<Effect> PROXY = new ArrayList<Effect>();
	
	private boolean todestroy = false;
	private int unit;

	public boolean toDestroy() { return todestroy; }
	/** zerstört den Effekt - wird also nicht mehr gespeichert */
	public void destroyIt() { todestroy = true; }

	/** Returns unit affected */
	public int getUnit() {return unit;}
	/** Sets the affected unit. */
	public void setUnit(int unitId) {this.unit = unitId;}

	/**
	 * Die favorisierte Methode, um EVA-Effekte aus der Datenbank zu fischen.
	 * @param rs
	 * @return
	 */
	public static Effect fromResultSet(ResultSet rs) {
		Effect efx = null;

		try {
			efx = (Effect) Class.forName("de.x8bit.Fantasya.Atlantis.Effects." + rs.getString("name")).newInstance();
			efx.setNummer(rs.getInt("id"));
		} catch (Exception e) {
			new BigError(e);
		}

		return efx;
	}


	/**
	 * @return Eine Map Feldname =&gt; Wert, die den Feldern der entsprechenden Datenbank-Tabelle entspricht
	 */
	public Map<String, Object> getDBValues(Unit u) {
		Map<String, Object> fields = new HashMap<String, Object>();

		fields.put("id", getNummer());
		fields.put("name", getTyp());
		fields.put("einheit", u.getNummer());

		return fields;
	}
	
	/**
	 * irgendwie muss das ja initialisiert werden ... hier wird aber kein Create durchgeführt,
	 * da sonst immer ein Effekt erzeugt wird (vgl. Load) ... ein Create muss dann in den
	 * jeweiligen Kindklassen durchgeführt werden
	 * @see Create
	 * @see Load
	 */
	protected Effect() { }
	
	/**
	 * Dieser Konstruktor erstellt einen neuen, persistenten Effekt - er ist
	 * also nicht zum "Laden" eines Effektes gedacht; siehe Effect().
	 * @param unit - der Effekt ist für diese Einheit
	 */
	public Effect(Unit unit)
	{
		setNummer(FreieNummern.freieNummer(Effect.PROXY));
		this.unit = unit.getNummer();

		Effect.PROXY.add(this);
	}

	/** Auswirkung auf die Resourcen Herstellung */
	public interface EFXResourcenCreate 
	{
		/**
		 * Berechnung des Effektes
		 * @param unit - diese Einheit produziert
		 * @param resource - diese Resource wird hergestellt
		 * @param anzahl - soviel will die Einheit herstellen
		 * @return
		 */
		int EFXCalculate(Unit unit, Item resource, int anzahl); 
	}
	
	/** Interface ist für alle Effekte die Auswirkung auf Resourcen Verwendung haben */
	public interface EFXResourcenUse 
	{
		/**
		 * Berechnung des Effektes
		 * @param unit - diese Einheit stellt etwas her
		 * @param item - dieses Item wird dafür verwendet
		 * @param anzahl - soviel soll produziert werden
		 * @return zusätzliche Produktionspunkte
		 */
		int EFXCalculate(Unit unit, Item item, int anzahl); 
	}
	
	/** für alle Effekte die auf die Produktion (Schwert etc.) auswirken */
	public interface EFXProduction
	{
		/**
		 * Berechnung von Zusätzlichen Punkten bei der Produktion von Items
		 * @param unit - diese Einheit
		 * @param item - stellt dieses Item her
		 * @param anzahl - soviel kann sie ohne
		 * @return zusätzlich produzierte Items
		 */
		int EFXCalculate(Unit unit, Item item, int anzahl); 
	}
	
	/** Effekte für die Bewegung */
	public interface EFXBewegung
	{
		/**
		 * liefert die Zusätzlichen Bewegungspunkte für die Einheit
		 * @return
		 */
		int EFXCalculate();
	}	
}
