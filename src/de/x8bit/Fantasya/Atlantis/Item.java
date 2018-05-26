package de.x8bit.Fantasya.Atlantis;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import de.x8bit.Fantasya.Atlantis.Allianz.AllianzOption;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Host.EVA.EVABase;
import de.x8bit.Fantasya.Host.Paket;
import de.x8bit.Fantasya.util.Random;
import java.util.Set;

/**
 * alle Items im Spiel<br><br> um die Items herzustellen wird einfach 
 * <b>item.produce(u, r)</b> aufgerufen ... sprich ... es wird von der 
 * Einheit das Item geholt und auf das Item die Produktion ausgeführt
 * ... da das Item am besten weis was es zur Herstellung benötigt<br><br>
 * Fehlermeldungen & so werden ebenfalls hier ausgeführt
 * <br><br>&nbsp;<br><br>
 * <b>actionXXX - Methoden</b><br>
 * werden vom Host wärend des ZAT <u>immer</u> aufgerufen ... es gibt eine
 * "Default"-Methode die von den Anderen Überladungen aufgerufen wird ...
 * diese "Default"-Methode muss in den Kind-Klassen implementiert werden, wenn
 * das Verhalten anders sein soll ... dabei können aber Parameter mit NULL
 * übergeben werden !!
 * <br><br>
 * <b>commandXXX - Methoden</b><br>
 * werden vom Spieler über die Befehle aufgerufen
 * @author  mogel
 */
public abstract class Item extends Dingens implements NamedItem {
	
	protected static Map<String, Class<? extends Item>> ITEM_KEY;

	public static Item Create(String item, int anzahl) {
		Item it = null;
		try	{
			it = (Item) Class.forName("de.x8bit.Fantasya.Atlantis.Items." + item).newInstance();
		} catch(Exception ex) { new BigError(ex); }
		it.anzahl = anzahl;
		return it;
	}

	/**
	 * @param itemName Name eines Items, muss nicht dem Namen der Klasse entsprechen. Der Performance zuliebe in Kleinschreibung angeben!
	 * @return die passende Klasse für einen Item-Namen - oder null.
	 */
	public static Class<? extends Item> getFor(String itemName) {
		if (ITEM_KEY == null) {
			ITEM_KEY = new TreeMap<String, Class<? extends Item>>();
			for (Paket p : Paket.getPaket("Items")) {
				Item item = (Item)p.Klasse;
				
				Set<String> itemNames = EVABase.getNames(p);
				Class<? extends Item> clazz = item.getClass();

				for (String name : itemNames) {
					ITEM_KEY.put(name.toLowerCase(), clazz);
					if (!ITEM_KEY.containsKey(name.toLowerCase())) {
						System.err.println(name + " nicht in der Liste");
					}
				}
			}
		}

		Class<? extends Item> retval = ITEM_KEY.get(itemName);
		if (retval != null) return retval;

		return ITEM_KEY.get(itemName.toLowerCase());
	}
	
	/** 
	 * einen Konstruktor-Aufruf im Kind erzwingen
	 * @param gewicht - Gewicht
	 * @param kapazitaet - Kapazität, muss das Gewicht beinhalten (das Item trägt sich selber!!)
	 */
	protected Item(int gewicht, int kapazitaet)
	{
		this.gewicht = gewicht;
		this.kapazitaet = kapazitaet;
		setConstructionSize(1);
	}

	/** Anzahl der Items */
	protected int anzahl = 0;
	public int getAnzahl() { return anzahl; }
	public void setAnzahl(int value) { anzahl = value; }
	public void addAnzahl(int value) { anzahl += value; }
	
	/** das spezifische Gewicht des Items, also eines einzelnen Exemplars */
	protected int gewicht = 0;
	public int getGewicht() { return gewicht; }
	
	/** Kapazität */
	protected int kapazitaet = 0;
	public int getKapazitaet() { return kapazitaet; }
	
	/** zusätzlicher Unterhalt wenn man dieses Item besitzt */
	public int Unterhalt() { return 0; }
	
	@Override
	public String toString() { return getAnzahl() + " " + getName(); }

	/** der Grundpreis für dieses Item (HANDEL) */
	public int getPrice() { return 0; }
	
	/**
	 *  True wenn das Item den Kampf überstanden hat - Default 50%
	 * @return True - wenn überstanden
	 */
	public boolean surviveBattle() { return Random.rnd(0, 100) > 50; } 
	
	/*
	 * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	 */
	
	/** 
	 * Item wird übergeben 
	 * @param u - diese Einheit besitzt das Item
	 * @param partner - diese Einheit soll dieses Item erhalten
	 * @param anzahl - soviel soll gewechselt werden
	 */
	public void Gib(Unit u, Unit partner, int anzahl) {
		if (anzahl < 0) return; // ohne Worte
		
		// nicht zuviel übergeben
		if (getAnzahl() < anzahl) {
			if (getAnzahl() == 0) {
				new Fehler(u + " hat gar keine " + this.getName() + " zum übergeben.", u, u.getCoords());
				return; // keine Übergabe
			}
			new Fehler(u + " hat keine " + anzahl + " " + this.getName() + " zum übergeben.", u, u.getCoords());
			anzahl = getAnzahl();
		}
		
		// Übergabe wenn sich u die Empfänger-Einheit sehen kann
		if (u.cansee(partner) && u.hatKontakt(partner, AllianzOption.Gib)) {
			Item i2 = partner.getItem(this.getClass());
			i2.setAnzahl(i2.getAnzahl() + anzahl);
			this.setAnzahl(getAnzahl() - anzahl);
			String verbErhalten = (partner.getPersonen() > 1?" erhalten ":" erhält ");
			new Info(partner +verbErhalten + anzahl + " " + getName() + " von " + u + ".", partner, partner.getCoords());
			String verbGeben = (u.getPersonen() > 1?" übergeben ":" übergibt ");
			new Info(u + verbGeben + anzahl + " " + getName() + " an " + partner + ".", u, u.getCoords());
		} else {
			new Fehler(u + " hat keinen Kontakt zu '" + partner + "'.", u, u.getCoords());
		}
	}

	/**
	 * erstellen eines Items
	 * @param unit - diese Einheit will das aktuelle Item herstellen
	 */
	@Override
	public void Mache(Unit unit)
	{
		Mache(unit, Integer.MAX_VALUE);
	}
	
	/**
	 * erstellen eines Items
	 * @param unit - diese Einheit will das aktuelle Item herstellen
	 * @param anzahl - maximal soviel soll hergestellt werden
	 */
    @Override
	public void Mache(Unit unit, int anzahl)
	{
		// produzieren
		int punkte = super.GenericMake2(unit, anzahl);	// Produktionspunkte holen
		setAnzahl(getAnzahl() + punkte);
		
		// Meldung
		new Info(unit + " produziert " + punkte + " " + this.getName() + ".", unit, unit.getCoords());
	}
	
	/** misserabler Hack ... signalisiert aber das hier noch was gemacht werden muss */
	public static boolean ExpandNeeded = false;
	
	/**
	 * @uml.property  name="lastregion"
	 * @uml.associationEnd  
	 */
	public static Region lastregion = null;
	
	/**
	 * Short-Cut für Wachstum ... die Einheit wird einfach
	 * weg gelassen ... dann gehört das Item der Region (Holz) 
	 * @param u - gehört zu dieser Einheit
	 */
	public void actionWachstum(Region r) { }


	/**
	 * @return Eine Map Feldname =&gt; Wert, die den Feldern der entsprechenden Datenbank-Tabelle (Regions-Resourcen) entspricht
	 */
	public Map<String, Object> getResourceDBValues(Coords c) {
		Map<String, Object> fields = new HashMap<String, Object>();

		fields.put("koordx", c.getX());
		fields.put("koordy", c.getY());
		fields.put("welt", c.getWelt());
		fields.put("anzahl", this.getAnzahl());
		fields.put("resource", this.getClass().getSimpleName());

		return fields;
	}

	/**
	 * @return Eine Map Feldname =&gt; Wert, die den Feldern der entsprechenden Datenbank-Tabelle (Gegenstände von Einheiten) entspricht
	 */
	public Map<String, Object> getItemDBValues(Unit u) {
		Map<String, Object> fields = new HashMap<String, Object>();

		fields.put("nummer", u.getNummer());
		fields.put("anzahl", this.getAnzahl());
		fields.put("item", this.getClass().getSimpleName());

		return fields;
	}
}
