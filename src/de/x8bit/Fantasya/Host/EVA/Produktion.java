package de.x8bit.Fantasya.Host.EVA;

import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.PersistentResource;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.Paket;
import de.x8bit.Fantasya.Host.EVA.util.AnzahlHint;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.ItemHint;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;
import de.x8bit.Fantasya.Host.EVA.util.DoppelteAusfuehrungException;

public class Produktion extends EVABase {
	/** 
	 * <p>speichert (sehr heimlich - ist von außen nicht zu sehen / zugreifbar und soll das auch nicht sein!)
	 * persistente Ressourcen zwecks Wiederherstellung der Regionen nach der Produktion.</p>
	 * <p>Evtl. mit eleganterer Implementierung ersetzen.</p>
	 */
	private static List<ResourceRecord> persistentResources;

	public Produktion()	{
		super("mache", "Produktion von allem Möglichen");	// Regionen weise!
		
		addTemplate("");
		for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
		BefehlsMuster bm = null;
        List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();

		// Items - alle Namen auflisten:
        List<String> itemNames = new ArrayList<String>();
        for (Paket p : Paket.getPaket("Items")) {
            for (String name : getNames(p)) {
                itemNames.add(name);
            }
        }
        // ... und als RegEx formulieren:
        StringBuilder regEx = new StringBuilder();
        regEx.append("(");
        for (String name : itemNames) {
            if (regEx.length() > 1) regEx.append("|");
            regEx.append("(" + name + ")");
        }
        regEx.append(")");

        // Variante 1 - Items ohne Anzahl
        bm = new BefehlsMuster(Produktion.class, 1,
				"^(mache)[n]? " + regEx + "([ ]+(\\/\\/).*)?",
				"m", Art.MULTILANG);
		bm.addHint(new ItemHint(1));
        bm.setKeywords("mache", "machen");
		retval.add(bm);
        // Variante 2 - Items mit Anzahl
        bm = new BefehlsMuster(Produktion.class, 2,
				"^(mache)[n]? ([0-9]+) " + regEx + "([ ]+(\\/\\/).*)?",
				"m", Art.MULTILANG);
		bm.addHint(new AnzahlHint(1));
		bm.addHint(new ItemHint(2));
        bm.setKeywords("mache", "machen");
		retval.add(bm);


        return retval;
    }

	@Override
	public boolean DoAction(Unit u, String[] befehl) { return false; }

	@Override
	public void DoAction(Region r, String befehl) {
		List<Einzelbefehl> befehle = BefehlsSpeicher.getInstance().get(this.getClass(), r.getCoords());

		for (Einzelbefehl eb : befehle) {
			if (eb.isPerformed()) throw new DoppelteAusfuehrungException(eb.toString());

			// Angaben über die "Akteure" selbst:
			Unit u = eb.getUnit();
			int variante = eb.getVariante();

			
			// MACHE <item>				-> MACHE <anzahl> <item> var. 1
			// MACHE <anzahl> <item>	=> neue Items var. 2
			if ((variante == 1) || (variante == 2)) {
				// Item herstellen
				int anzahl = 0;
				if (variante == 2) anzahl = eb.getAnzahl();

				Class<? extends Item> clazz = eb.getItem();

				Item item = u.getItem(clazz);
				if (anzahl == 0) {
					item.Mache(u);
				} else {
					item.Mache(u, anzahl);
				}
				eb.setPerformed();
			}

			// MACHE <whatever>			=> welcome to the future
		}
	}
	
    @Override
	public void PostAction() {
		// Mantis #181 - böser Hack, siehe PreAction()
		// persistente Resourcen in den Regionen wiederherstellen
		for (ResourceRecord memory : Produktion.persistentResources) { // static list
			Region r = Region.Load(memory.getCoords());
			r.setResource(memory.getResource(), memory.getAnzahl());
		}
		// Speicher freigeben:
		Produktion.persistentResources.clear();
	}

	@Override
	public void PreAction() {
		// Mantis #181 - eher ein böser Hack, vielleicht ein zusätzliches Feld "permanentAnzahl" 
		// in der Tabelle resourcen ODER eine Nutzung von property_regionen
		// siehe auch PostAction()
		//
		// persistente Resourcen der Regionen vor dem Abbau merken:
		List<Paket> allItems = Paket.getPaket("Items");
		List<Item> persistentResourceTypes = new ArrayList<Item>();
		for (Paket p:allItems) {
			Item i = (Item)p.Klasse;
			if (i instanceof PersistentResource) {
				persistentResourceTypes.add(i);
			}
		}

		Produktion.persistentResources = new ArrayList<ResourceRecord>(); // static list
		for (Region r : Region.CACHE.values()) {
			for (Item i:persistentResourceTypes) {
				int anzahl = r.getResource(i.getClass()).getAnzahl();
				if (anzahl > 0) {
					ResourceRecord memory = new ResourceRecord(r.getCoords(), anzahl, i.getClass());
					Produktion.persistentResources.add(memory); // static list
				}
			}
		}
	}

    @Override
    public void DoAction(Einzelbefehl eb) { }


	private class ResourceRecord {
		Coords coords;
		int anzahl;
		Class<? extends Item> resource;

		public int getAnzahl() {
			return anzahl;
		}

		public Coords getCoords() {
			return coords;
		}

		public Class<? extends Item> getResource() {
			return resource;
		}
		
		public ResourceRecord(Coords coords, int anzahl, Class<? extends Item> resource) {
			this.coords = coords;
			this.anzahl = anzahl;
			this.resource = resource;
		}

	}
}
