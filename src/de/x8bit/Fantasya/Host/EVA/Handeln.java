package de.x8bit.Fantasya.Host.EVA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.NumberFormat;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Allianz.AllianzOption;
import de.x8bit.Fantasya.Atlantis.Buildings.Burg;
import de.x8bit.Fantasya.Atlantis.Helper.Nachfrage;
import de.x8bit.Fantasya.Atlantis.Items.LuxusGood;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Handelsmeldungen;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.Atlantis.Messages.Debug;
import de.x8bit.Fantasya.Atlantis.Skills.Handel;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.EVA.util.AnzahlHint;
import de.x8bit.Fantasya.Host.Paket;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;
import de.x8bit.Fantasya.Host.EVA.util.DoppelteAusfuehrungException;
import de.x8bit.Fantasya.Host.EVA.util.ItemHint;
import java.util.Locale;

/**
 * @author  mogel
 */
public class Handeln extends EVABase
{
    public final static Map<Integer, Integer> parteiAusgaben = new HashMap<Integer, Integer>();
    public final static Map<Integer, Integer> parteiEinnahmen = new HashMap<Integer, Integer>();;
    
	public Handeln() {
		super("handel", "Handel mit der Bevölkerung");
		
		addTemplate("");
		for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
		BefehlsMuster bm = null;
        List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();

		// Luxus-Items - alle Namen auflisten:
        List<String> itemNames = new ArrayList<String>();
        for (Paket p : Paket.getPaket("Items")) {
			if (!(p.Klasse instanceof LuxusGood)) continue;
            for (String name : getNames(p)) {
                itemNames.add(name);
            }
        }
        // ... und als RegEx formulieren:
        StringBuilder regEx = new StringBuilder();
        regEx.append("(\")?(");
        for (String name : itemNames) {
            if (regEx.length() > 1) regEx.append("|");
            regEx.append("(").append(name).append(")");
        }
        regEx.append(")(\")?");

        // 1 - kaufen ohne anzahl
		bm = new BefehlsMuster(Handeln.class, 1, "^(handel)[n]? ((kaufe)[n]?) " + regEx + "([ ]+(\\/\\/).*)?", "h", Art.MULTILANG);
		bm.addHint(new ItemHint(2));
		retval.add(bm);
		
        // 2 - kaufen mit anzahl
		bm = new BefehlsMuster(Handeln.class, 2, "^(handel)[n]? ((kaufe)[n]?)( [0-9]+) " + regEx + "([ ]+(\\/\\/).*)?", "h", Art.MULTILANG);
		bm.addHint(new AnzahlHint(2));
		bm.addHint(new ItemHint(3));
		retval.add(bm);

        // 11 - verkaufen ohne anzahl
		bm = new BefehlsMuster(Handeln.class, 11, "^(handel)[n]? ((verkaufe)[n]?) " + regEx + "([ ]+(\\/\\/).*)?", "h", Art.MULTILANG);
		bm.addHint(new ItemHint(2));
		retval.add(bm);

		// 12 - verkaufen mit anzahl
		bm = new BefehlsMuster(Handeln.class, 12, "^(handel)[n]? ((verkaufe)[n]?)( [0-9]+) " + regEx + "([ ]+(\\/\\/).*)?", "h", Art.MULTILANG);
		bm.addHint(new AnzahlHint(2));
		bm.addHint(new ItemHint(3));
		retval.add(bm);

		return retval;
    }
	
	/**
	 * @uml.property  name="region"
	 * @uml.associationEnd
	 */
	private Region region = null;

    @Override
	public void DoAction(Einzelbefehl eb) { }
	public boolean DoAction(Unit u, String[] befehl) { return false; }
	public void PreAction() { }
	public void PostAction()
	{
		for (Region r : Region.CACHE.values()) {
			boolean burg = false;
			for (Building b : r.getBuildings()) {
				if ((b.getClass() == Burg.class) && (b.getSize() > 1)) {
					burg = true;
					break;
				}
			}
			if (!burg) continue;

			// Nachfrageänderung
			for(Nachfrage nachfrage : r.getLuxus()) nachfrage.Handeln();
		}
	}

	public void DoAction(Region r, String befehl) {
		kaufen = new HashMap<Integer, List<Item>>();
		verkaufen = new HashMap<Integer, List<Item>>();

		// wird bei Expand$$$ benötigt
		region = r;

		List<Einzelbefehl> befehle = BefehlsSpeicher.getInstance().get(this.getClass(), region.getCoords());

		for (Einzelbefehl eb : befehle) {
			if (eb.isPerformed()) throw new DoppelteAusfuehrungException(eb.toString());

			Unit unit = eb.getUnit();
//			Class<? extends Item> luxury = eb.getItem();
			
			unit.wants = 0;	// alte Verkäufe/Käufe löschen (sollte aber bereits 0 sein)
			
			if (!r.canNachfrage())
			{
				eb.setError();
				new Fehler(unit + " - im Terrain " + r.getClass().getSimpleName() + " kann nicht gehandelt werden.", unit);
				continue;
			}
			
			if (!region.hasKaufNachfrage())
			{
				eb.setError();
				new Fehler(unit + " möchte Luxusgüter einkaufen. Es gibt aber keine Luxusgüter zum einkaufen.", unit);
				new SysMsg("Region " + region + " hat kein Luxusgut zum einkaufen!");
				continue;
			}

			if (!r.hatGebaeude(Burg.class, 2, null)) {
				eb.setError();
				new Fehler(unit + " - hier ist keine Burg zum Handeln.", unit);
				continue; // um aus der While-Schleife rauszukommen
			}

			if (r.getBauern() == 0)	{
				eb.setError();
				new Fehler(unit + " findet keine Bauern denen er sein Zeug schmackhaft kann.", unit);
				continue; // um aus der While-Schleife rauszukommen
			}

			if (unit.Talentwert(Handel.class) == 0) {
				eb.setError();
				new Fehler(unit + " hat keine Ahnung vom Luxushandel.", unit);
				continue; // um aus der While-Schleife rauszukommen
			}

			if (!r.istBewacht(unit, AllianzOption.Handel)) {
				if ((eb.getVariante() == 1) || (eb.getVariante() == 2)) InitHandel(eb, kaufen);
				if ((eb.getVariante() == 11) || (eb.getVariante() == 12)) InitHandel(eb, verkaufen);
			} else {
				eb.setError();
				new Fehler(unit + " darf nicht handeln - die Region wird von nicht-alliierten Einheiten bewacht.", unit);
				continue;
			}

			eb.setPerformed();
		}

		ExpandVerkaufen();
		ExpandKaufen();

		// Es kann passieren, dass nur Fehler auftreten - dann müssen wir zumindest ein leeres Log erstellen:
		if (handelsLog == null) handelsLog = new HandelsLog();

		NumberFormat f = NumberFormat.getIntegerInstance(Locale.GERMANY);
		f.setGroupingUsed(true);
		
		// Meldungen erstellen:
		// Alle aktiven Einheiten,
		for (int unitNr : handelsLog.getAlleDeals().keySet() ) {
			Unit u = Unit.Load(unitNr);
            if (!parteiAusgaben.containsKey(u.getOwner())) parteiAusgaben.put(u.getOwner(), 0);
            if (!parteiEinnahmen.containsKey(u.getOwner())) parteiEinnahmen.put(u.getOwner(), 0);
            
			Map<Class<? extends Item>, HandelsLogEintrag> log = handelsLog.getAlleDeals().get(unitNr);

			// bei jeder Einheit über alle gehandelten Waren...
			for (Class<? extends Item> ware : log.keySet()) {
				try {
					HandelsLogEintrag hle = log.get(ware);
					Item it = ware.newInstance();
					if (hle.getWarenBilanz() > 0) {
                        // kaufen:
						it.setAnzahl(hle.getWarenBilanz());
						String silberTxt = f.format(-1 * hle.getSilberBilanz());
						new Handelsmeldungen(u + " kauft " + it + " für $" + silberTxt + hle.getPreisPhrase() + ".", u);

						u.setEinkommen(u.getEinkommen() + hle.getSilberBilanz());
                        
                        // das addiert die Ausgaben (weil die Silber-Bilanz negativ ist)
                        parteiAusgaben.put(u.getOwner(), parteiAusgaben.get(u.getOwner()) - hle.getSilberBilanz());
					}
					if (hle.getWarenBilanz() < 0) {
                        // verkaufen:
						it.setAnzahl(-1 * hle.getWarenBilanz());
						String silberTxt = f.format(hle.getSilberBilanz());
						new Handelsmeldungen(u + " verkauft " + it + " für $" + silberTxt + hle.getPreisPhrase() + ".", u);

						u.setEinkommen(u.getEinkommen() + hle.getSilberBilanz());
                        
                        parteiEinnahmen.put(u.getOwner(), parteiEinnahmen.get(u.getOwner()) + hle.getSilberBilanz());
					}
				} catch (Exception ex) {
					new BigError(ex);
				}
			} // nächstes Gut,
		} // nächste Einheit

        // erledigt, HandelsLog leeren:
        handelsLog.clear();
	}

	/** die Liste der zu kaufenden Artikel pro Einheit */
	private HashMap<Integer, List<Item>> kaufen;

	/** die Liste der zu verkaufenden Artikel pro Einheit */
	private HashMap<Integer, List<Item>> verkaufen;

	/** Log über die tatsächliche Handelstätigkeit, zwecks Meldung an die Einheiten */
	private HandelsLog handelsLog;

	/** Zugriff auf die einzelnen Artikel beim Handel - entweder die Felder &quot;kaufen&quot; oder &quot;verkaufen&quot;, siehe dort. */
	private Item getWants(Unit unit, Class<? extends Item> item, HashMap<Integer, List<Item>> liste)
	{
		if (item == null) return null;
		List<Item> a = liste.get(unit.getNummer());
		if (a == null) {
			a = new ArrayList<Item>();
			liste.put(unit.getNummer(), a);
		}
		return getWants2(item, a);
	}

	/**
	 * @param item Klasse des Luxusguts
	 * @param liste Liste, in der bereits "aktive" Luxusgüter verwaltet werden.
	 * @return Wenn ein Item der Klasse "item" schon in "liste" enthalten ist, dann dieses. Ansonsten wird eines angelegt und in die Liste eingtragen.
	 */
	private Item getWants2(Class<? extends Item> item, List<Item> liste)
	{
		Item it = null;

		for(int i = 0; i < liste.size(); i++) if (liste.get(i).getClass().equals(item)) return liste.get(i);

		// noch nicht da ... erzeugen und hinzufügen
		it = Item.Create(item.getSimpleName(), 0);
		liste.add(it);

		return it;
	}

	/** Handel starten (bzw. Befehl "abarbeiten") */
	private void InitHandel(Einzelbefehl eb, HashMap<Integer, List<Item>> liste) {
		if (handelsLog == null) handelsLog = new HandelsLog();

		Unit unit = eb.getUnit();
		Class<? extends Item> luxury = eb.getItem();

		int anzahl = unit.Talentwert(Handel.class) * unit.getPersonen() * 10; // Maximum annehmen
		if ((eb.getVariante() == 2) || (eb.getVariante() == 12)) {
			// mit Angabe der Anzahl:
			anzahl = eb.getAnzahl();
		}
		Item item = getWants(unit, luxury, liste);
		if (item == null) {
			eb.setError();
			new Fehler(unit + " - Handelsgut '" + luxury.getSimpleName() + "' nicht erkannt.", unit, unit.getCoords());
			return;
		}

		// Das gewünschte Handelsvolumen wird für das Item selbst dazugerechnet:
		item.setAnzahl(item.getAnzahl() + anzahl);

		// .. und für die Einheit - der Gesamtkapazität wegen.
		unit.wants += anzahl;
	}

	/**
	 * Handel (Kaufen) für alle Einheiten in der Region .. jeder kommt einmal an die Reihe und kauft
	 * ein Gegenstand - dann ist der Nächste dran ... das geht solange keiner mehr Gegenstände kaufen will
	 */
	private void ExpandKaufen()
	{
		StringBuffer sb = new StringBuffer();
		for (Nachfrage n : region.getLuxus()) sb.append(n.getItem().getSimpleName() + ": " + n.getNachfrage() + ", ");
		new Debug("Kaufen in " + region + " - " + sb);

		boolean ende = false;
		// Kaufgut der Region herausfinden.
		Class<? extends Item> buyItemClass = region.getKaufNachfrage().getItem();
		 
		while(!ende)
		{
			ende = true; // pauschal ist das die letzte Handelsrunde
			
			for(Unit unit : region.getUnits())
			{
				List<Item> liste = kaufen.get(unit.getNummer());
				if (liste == null) continue; // der will nix Kaufen
				for(Item item : liste)
				{
					if (item.getAnzahl() == 0) continue; // wurde schon alleDeals gekauft
					if (!item.getClass().equals(buyItemClass)) // Luxusgut nicht zum Kaufen gedacht.
					{
						new Handelsmeldungen(unit  + " versucht illegal " + item.getClass().getSimpleName() + " zu kaufen und sollte sich besser nicht erwischen lassen. Die Bauern produzieren hier " + buyItemClass.getSimpleName() + ".", unit);
						item.setAnzahl(0);
						continue;
					}
					if (handelsLog.getHandelsVolumen(unit.getNummer()) >= unit.getMaxHandelsVolumen()) {
						int gap = item.getAnzahl();
						new Handelsmeldungen(unit  + " - muss seine Einkaufsliste um " + gap + " " + item.getName() + " kürzen - zu beschäftigt.", unit);
						item.setAnzahl(0);
						continue;
					}

					// "Schulden" bzw. Preis ausrechnen
					float Nachfrage = region.getNachfrage(item.getClass()).getNachfrage();
					int preis = (int) Math.abs(item.getPrice() * Nachfrage);
					// new Debug(unit + " kauft " + item.getClass().getSimpleName() + ", Nachfrage: " + Nachfrage + ", preis: " + preis);

					// Kaufen
					if (unit.getItem(Silber.class).getAnzahl() > preis)
					{
						// kann gekauft werden
						unit.getItem(Silber.class).addAnzahl(0 - preis);	// Silber abziehen
						unit.getItem(item.getClass()).addAnzahl(1);			// Item zum Händler
						item.addAnzahl(-1);									// vom Wunsch abziehen
						// if (item.getAnzahl() == 0) new Handelsmeldungen(unit + " hat jetzt " + unit.getItem(item.getClass()), unit);
						handelsLog.kauft(unit, item.getClass(), preis);

						// Nachfrageänderung - es steigt weil Bauern mehr wollen
						Nachfrage += (float) 25.0 / (float) region.getBauern() * Math.signum(Nachfrage); // beim produzierten Gut ist die Nachfrage negativ (?)
						if (Nachfrage > 100.0) Nachfrage = 100.0f;
						if (Nachfrage < -100.0) Nachfrage = -100.0f;
						region.setNachfrage(item.getClass(), Nachfrage);

						// es konnte Handel getrieben werden - also noch nicht beenden
						ende = false;
					} else
					{
						// nicht mehr genügend Silber vorhanden
						new Fehler(unit + " kann die letzten " + item + " nicht kaufen, der Preis ist zu teuer", unit);
						item.setAnzahl(0);
					}
				} // nächstes Item
			} // nächste Einheit
		} // solange noch irgendwas verkauft wurde in dieser Schleife

	}

	private void ExpandVerkaufen()
	{
		StringBuffer sb = new StringBuffer();
		for (Nachfrage n : region.getLuxus()) sb.append(n.getItem().getSimpleName() + ": " + n.getNachfrage() + ", ");
		new Debug("Verkaufen in " + region + " - " + sb);

		boolean ende = false;
		while(!ende) {
			ende = true; // pauschal ist das die letzte Handelsrunde
			for(Unit unit : region.getUnits()) {
				List<Item> liste = verkaufen.get(unit.getNummer());
				if (liste == null) continue; // der will nix Kaufen
				for(Item item : liste) {
					if (item.getAnzahl() == 0) continue; // wurde schon alleDeals gekauft
					if (unit.getItem(item.getClass()).getAnzahl() <= 0) continue; // die Einheit hat nix (mehr)
					if (handelsLog.getHandelsVolumen(unit.getNummer()) >= unit.getMaxHandelsVolumen()) {
						int gap = item.getAnzahl();
						if (gap > unit.getItem(item.getClass()).getAnzahl() ) gap = unit.getItem(item.getClass()).getAnzahl();
						new Handelsmeldungen(unit  + " - kann die letzten " + gap + " " + item.getName() + " nicht verkaufen - zu beschäftigt.", unit);
						item.setAnzahl(0);
						continue;
					}

					// Gewinn ausrechnen
					float Nachfrage = region.getNachfrage(item.getClass()).getNachfrage();
					int preis = (int) Math.abs(item.getPrice() * Nachfrage);
					// new Debug(unit + " verkauft " + item.getClass().getSimpleName() + ", Nachfrage: " + Nachfrage + ", preis: " + preis);

					// kann verkauft werden
					unit.getItem(Silber.class).addAnzahl(preis);	// Silber abziehen
					unit.getItem(item.getClass()).addAnzahl(-1);	// Item zu den Bauern
					item.addAnzahl(-1);								// vom Wunsch abziehen
					handelsLog.verkauft(unit, item.getClass(), preis);
					// if (item.getAnzahl() == 0) new Handelsmeldungen(unit + " hat jetzt " + unit.getItem(item.getClass()), unit);

					// Nachfrageänderung - es sinkt weil Bauern genug haben
					if (Math.signum(Nachfrage) == +1) {
						Nachfrage -= (float) 25.0 / (float) region.getBauern();
						if (Nachfrage < 1.0) Nachfrage = (float) 1.0;
					} else {
						// Nachfrage ist negativ, es handelt sich also um ein produziertes Gut:
						Nachfrage += (float) 25.0 / (float) region.getBauern();
						if (Nachfrage > -1.0) Nachfrage = (float) -1.0;
					}
					region.setNachfrage(item.getClass(), Nachfrage);

					// es konnte Handel getrieben werden - also noch nicht beenden
					ende = false;
				}
			}
		}
	}

	private class HandelsLog {
		final Map<Integer, Map<Class<? extends Item>, HandelsLogEintrag>> alleDeals =
				new HashMap<Integer, Map<Class<? extends Item>, HandelsLogEintrag>>();

		public HandelsLog() { }

		public void verkauft(Unit u, Class<? extends Item> ware, int preis) {
			Map<Class<? extends Item>, HandelsLogEintrag> myRecord = alleDeals.get(u.getNummer());
			if (myRecord == null) {
				myRecord = new HashMap<Class<? extends Item>, HandelsLogEintrag>();
				alleDeals.put(u.getNummer(), myRecord);
			}
			HandelsLogEintrag bilanz = myRecord.get(ware);
			if (bilanz == null) myRecord.put(ware, new HandelsLogEintrag());
			myRecord.get(ware).verkaufen(preis);
		}

		public void kauft(Unit u, Class<? extends Item> ware, int preis) {
			Map<Class<? extends Item>, HandelsLogEintrag> myRecord = alleDeals.get(u.getNummer());
			if (myRecord == null) {
				myRecord = new HashMap<Class<? extends Item>, HandelsLogEintrag>();
				alleDeals.put(u.getNummer(), myRecord);
			}
			HandelsLogEintrag bilanz = myRecord.get(ware);
			if (bilanz == null) myRecord.put(ware, new HandelsLogEintrag());
			myRecord.get(ware).kaufen(preis);
		}

		public Map<Integer, Map<Class<? extends Item>, HandelsLogEintrag>> getAlleDeals() {
			return alleDeals;
		}

		public int getHandelsVolumen(int unitId) {
			int retval = 0;

			Map<Class<? extends Item>, HandelsLogEintrag> unitDeals = alleDeals.get(unitId);
			if (unitDeals == null) return 0;

			for(Class<? extends Item> luxus : unitDeals.keySet()) {
				HandelsLogEintrag hle = unitDeals.get(luxus);
				retval += hle.getHandelsVolumen();
			}
			return retval;
		}

        public void clear() {
            for (int unitNr : alleDeals.keySet()) {
                Map<Class<? extends Item>, HandelsLogEintrag> entries = alleDeals.get(unitNr);
                entries.clear();
            }
            alleDeals.clear();
        }

	}

	private class HandelsLogEintrag {
		int warenVerkauft;
		int warenGekauft;
		int silberAusgegeben;
		int silberBekommen;

		int minPreis = Integer.MAX_VALUE;
		int maxPreis = Integer.MIN_VALUE;

		public HandelsLogEintrag() {
			this.warenVerkauft = 0;
			this.warenGekauft = 0;
			this.silberAusgegeben = 0;
			this.silberBekommen = 0;
		}

		public int getSilberBilanz() {
			return silberBekommen - silberAusgegeben;
		}

		public int getWarenBilanz() {
			return warenGekauft - warenVerkauft;
		}

		public int getHandelsVolumen() {
			return warenGekauft + warenVerkauft;
		}

		public void verkaufen(int preis) {
			warenVerkauft ++;
			silberBekommen += preis;

			if (preis > maxPreis) maxPreis = preis;
			if (preis < minPreis) minPreis = preis;
		}

		public void kaufen(int preis) {
			warenGekauft ++;
			silberAusgegeben += preis;

			if (preis > maxPreis) maxPreis = preis;
			if (preis < minPreis) minPreis = preis;
		}

		public String getPreisPhrase() {
			if (minPreis == maxPreis) return " zum Preis von jeweils $" + minPreis;
			return " bei Preisen von $" + minPreis + " bis $" + maxPreis;
		}


	}


}
