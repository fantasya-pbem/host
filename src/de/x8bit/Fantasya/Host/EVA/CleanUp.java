package de.x8bit.Fantasya.Host.EVA;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Helper.Nachfrage;
import de.x8bit.Fantasya.Atlantis.Items.LuxusGood;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.SysErr;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.Atlantis.Regions.Lavastrom;
import de.x8bit.Fantasya.Atlantis.Regions.Ozean;
import de.x8bit.Fantasya.Atlantis.Regions.Sandstrom;
import de.x8bit.Fantasya.Host.Datenbank;
import de.x8bit.Fantasya.Host.Paket;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.util.MapSelection;
import de.x8bit.Fantasya.util.Random;
import de.x8bit.Fantasya.util.StringUtils;

public class CleanUp extends EVABase implements NotACommand
{
	public boolean DoAction(Unit u, String[] befehl) { return false; }
	public void DoAction(Region r, String befehl) { }
	public void PostAction() { }
	public void PreAction() { }
    public void DoAction(Einzelbefehl eb) { }
	
	public CleanUp()
	{
		super("Aufräumen für den ZAT");
		
		Durchreisen();
        Einheiten();
        EinheitenChecks();
	}
	
	private void Durchreisen() {
		Datenbank db = new Datenbank("Durchreisen löschen");
		db.myQuery = "DELETE FROM durchreise"; db.Delete();	db.Close();
    }

    private void Einheiten() {
		for(Unit unit : Unit.CACHE) {
			unit.setEinkommen(0);
		}
	}

    private void EinheitenChecks() {
        // Plausibilitätsprüfungen
        for(Unit u : Unit.CACHE) {
            if (u.getPersonen() > 0) {
                if (u.getLebenspunkte() > u.maxLebenspunkte()) new BigError(u + " ist schon lange tot!");

                if ( u.getBewacht() && (u.getWaffen() < u.getPersonen()) ) {
                    u.setBewacht(false);
                    new Fehler(u + " bewacht nicht mehr die Region, es fehlen Waffen.", u);
                }
            }
        }
    }

	/**
	 * prüft die Spiel-Objekt-Caches auf "Unversehrtheit"
	 */
	public static void SanityChecks() {
		new SysMsg("Sanity Check beginnt...");

		new SysMsg(" --- " + Building.PROXY.size() + " Gebäude...");

		new SysMsg(" --- " + Unit.CACHE.size() + " Einheiten...");


		// TODO Region.getUnits(), .getSchiffe(), .getBuildings(), .anwesendeParteien()
		// <-> die Listen jeweils aller Units, Ships, Buildings, Parteien(/Einheiten)
		new SysMsg(" ... fertig.");
	}

	public static void RegionsChecks() {
		Set<Class <? extends Item>> waren = new HashSet<Class<? extends Item>>();
		for (Paket p : Paket.getPaket("Items")) {
			Item it = (Item)p.Klasse;
			if (it instanceof LuxusGood) {
				waren.add(it.getClass());
			}
		}

		// haben alle Regionen alle Luxusgüter? - sind alle vorhandenen Luxusgüter "legal"?
		for (Region r : Region.CACHE.values()) {
			List<Nachfrage> nachfragen = r.getLuxus();
			Set<Class <? extends Item>> regionsWaren = new HashSet<Class <? extends Item>>();
			for (Nachfrage n : nachfragen) {
				regionsWaren.add(n.getItem());
			}

			for (Class <? extends Item> ware : waren) {
				if (!regionsWaren.contains(ware)) {
					Nachfrage neu = new Nachfrage(ware, 1);
					r.getLuxus().add(neu);
					new SysMsg("Die Nachfrage-Liste in " + r + " enthält keinen Eintrag für " + ware.getSimpleName() + ". Der Eintrag wurde angelegt.");
				}
			}

			for (Class <? extends Item> ware : regionsWaren) {
				if (!waren.contains(ware)) {
					new SysErr("Die Nachfrage-Liste in " + r + " enthält einen Eintrag für " + ware.getSimpleName() + ", was nicht als Luxus-Gut bekannt ist.");
				}
			}
		}

		// hat jede Region genau ein produziertes Gut?
		for (Region r : Region.CACHE.values()) {
			Class<? extends Item> angebot = r.getProduce();
			if (angebot == null) {
				if (!(r instanceof Ozean) || (r instanceof Lavastrom) || (r instanceof Sandstrom)) {
					new SysErr(r + " hat kein produziertes Luxusgut. "
							+ "Empfehlung: " + CleanUp.luxusProduktRaten(r).getSimpleName() + "."
					);
				} else {
					// Ozeane und Lavastrom benötigen kein Luxusgut
//					angebot = CleanUp.luxusProduktRaten(r);
//					for (Nachfrage n : r.getLuxus()) {
//						if (n.getItem() == angebot) {
//							n.setNachfrage(Math.abs(n.getNachfrage()) * -1f);
//							break;
//						}
//					}
//					new SysMsg(r + " hatte kein produziertes Luxusgut - es wurde "
//							+ angebot.getSimpleName() + " festgelegt."
//					);
				}
			} else {
				// gibt es evtl. mehr als eines?
				Set<Class <? extends Item>> angebote = new HashSet<Class <? extends Item>>();
				for (Nachfrage n : r.getLuxus()) {
					if (n.getNachfrage() < 0) angebote.add(n.getItem());
				}
				if (angebote.size() != 1) {
					new SysErr(r + " hat nicht genau ein produziertes Luxusgut: "
							+ StringUtils.aufzaehlung(angebote)
							+ ". Empfehlung: " + CleanUp.luxusProduktRaten(r).getSimpleName() + "."
					);
				}
			}
		}
		
	}

	public static void RegelChecks() {
		for (Partei p : Partei.PROXY) {
			if (p.isMonster()) continue;

			Unit einheimischer = null;
			for (Unit maybe : p.getEinheiten()) {
				if (maybe.getRasse().equalsIgnoreCase(p.getRasse())) {
					einheimischer = maybe;
					break;
				}
			}
			if (einheimischer == null) {
				if (p.getPersonen() > 0) new SysErr("Partei " + p + " hat keinen einzigen Einheimischen?");
				continue;
			}

			if (p.getMagier().getPersonen() > einheimischer.maxMagier()) {
				new SysErr(p + " hat mehr Magier als sie sollten... (" + p.getMagier().getPersonen() + " von " + einheimischer.maxMagier() + ")");
				new Fehler(p + " hat mehr Magier als sie sollten... (" + p.getMagier().getPersonen() + " von " + einheimischer.maxMagier() + ")", p);
			}

			if (p.getMigranten().getPersonen() > p.getMaxMigranten()) {
				new SysErr(p + " hat mehr Migranten als sie sollten... (" + p.getMigranten().getPersonen() + " von " + p.getMaxMigranten() + ")");
				new Fehler(p + " hat mehr Migranten als sie sollten... (" + p.getMigranten().getPersonen() + " von " + p.getMaxMigranten() + ")", p);
			}
		}
	}


	public static Class<? extends Item> luxusProduktRaten(Region r) {
		MapSelection rundum = new MapSelection();
		rundum.add(r.getCoords());
		rundum.wachsen(6);

		Map<Class<? extends Item>, Integer> frequencies = new HashMap<Class<? extends Item>, Integer>();
		for (Paket p : Paket.getPaket("Items")) {
			Item it = (Item)p.Klasse;
			if (it instanceof LuxusGood) {
				frequencies.put(it.getClass(), 0);
			}
		}

		for (Coords c : rundum) {
			Region nachbar = Region.Load(c);
			if (nachbar != null) {
				Class<? extends Item> ware = nachbar.getProduce();
				if (ware != null) {
					frequencies.put(ware, frequencies.get(ware) + 1);
				}
			}
		}

		// häufigstes Gut:
		int max = 0;
		Class<? extends Item> a = null;
		for(Class<? extends Item> ware : frequencies.keySet()) {
			if (frequencies.get(ware) > max) {
				a = ware;
				max = frequencies.get(ware);
			}
		}

		if (a != null) frequencies.remove(a);

		// zweithäufigstes Gut:
		max = 0;
		Class<? extends Item> b = null;
		for(Class<? extends Item> ware : frequencies.keySet()) {
			if (frequencies.get(ware) > max) {
				b = ware;
				max = frequencies.get(ware);
			}
		}

		if ((a != null) && (b != null)) {
			if (Random.rnd(0, 2) == 0) return a;
			return b;
		}
		if (a != null) return a;

		// hmm, irgendwas:
		List<Class<? extends Item>> rnd = new ArrayList<Class<? extends Item>>();
		rnd.addAll(frequencies.keySet());
		Collections.shuffle(rnd);
		return rnd.get(0);
	}
}
