package de.x8bit.Fantasya.Host.EVA;

import de.x8bit.Fantasya.Atlantis.Allianz;
import de.x8bit.Fantasya.Atlantis.Building;

import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Items.AnimalResource;
import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Messages.Battle;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Regions.Lavastrom;
import de.x8bit.Fantasya.Atlantis.Regions.Ozean;
import de.x8bit.Fantasya.Atlantis.Ship;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.util.StringUtils;
import de.x8bit.Fantasya.util.UnitList;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Test ist der Gleiche wie NMR
 * @author mogel
 *
 */
public class SpielerLoeschen extends EVABase implements NotACommand
{
	public SpielerLoeschen(boolean withoutTemp)	{
		super("löschen von leeren Einheiten");

		Ertrinken();
		LeereEinheiten(withoutTemp);
		LeereVoelker();
		KommandoAnSichReissen();
	}
	
	@Override
	public void PostAction() { }
	@Override
	public void PreAction() { }

	private void Ertrinken() {
		for (Region r : Region.CACHE.values()) {
			if (r.istBetretbar(null)) continue;

			SortedSet<Unit> anwesende = new TreeSet<Unit>(r.getUnits());
			if (anwesende.isEmpty()) continue;

			if (r instanceof Ozean) {
				// "normales" Ertrinken:
				for(Unit u : anwesende) {
					if (u.canSwim()) continue; // Einheit kann schwimmen
					if (u.getSchiff() == 0) {
						Partei p = Partei.getPartei(u.getOwner());
						Coords c = p.getPrivateCoords(r.getCoords());

						StringBuilder meldung = new StringBuilder();
						meldung.append(u).append(" ertrinkt in " + r + " " + c.xy() + "!");

						List<Item> items = u.getItems();
						List<Item> gegenstaende = new ArrayList<Item>();
						List<Item> tiere = new ArrayList<Item>();
						for (Item it : items) {
							if (it.getAnzahl() <= 0) continue;
							if (it instanceof AnimalResource) {
								tiere.add(it);
							} else {
								gegenstaende.add(it);
							}
						}

						if (!tiere.isEmpty()) meldung.append(" " + StringUtils.aufzaehlung(tiere) + " werden mit in die Tiefe gezogen.");
						if (!gegenstaende.isEmpty()) meldung.append(" Auf Nimmerwiedersehen versinken " + StringUtils.aufzaehlung(gegenstaende) + ".");

						for (Item it : items) { it.setAnzahl(0); }

						u.setPersonen(0);

						// TODO Was ist besser?
						// new Info(meldung.toString(), u);
						new Info(meldung.toString(), p);

						u.purge(false); // keine weitere Meldung, wir haben ja schon den Todeskampf dokumentiert.
					}
				}
			} else {
				if (r instanceof Lavastrom) {
					for(Unit u : anwesende) {
						Partei p = Partei.getPartei(u.getOwner());
						Coords c = p.getPrivateCoords(r.getCoords());

						StringBuilder meldung = new StringBuilder();
						meldung.append(u).append(" verbrennt in ").append(r).append(" ").append(c.xy()).append("!");

						List<Item> items = u.getItems();
						List<Item> gegenstaende = new ArrayList<Item>();
						List<Item> tiere = new ArrayList<Item>();
						for (Item it : items) {
							if (it.getAnzahl() <= 0) continue;
							if (it instanceof AnimalResource) {
								tiere.add(it);
							} else {
								gegenstaende.add(it);
							}
						}

						if (!tiere.isEmpty()) meldung.append(" " + StringUtils.aufzaehlung(tiere) + " gehen ebenfalls zugrunde.");
						if (!gegenstaende.isEmpty()) meldung.append(" " + StringUtils.aufzaehlung(gegenstaende) + " gehen in Rauch auf.");

						for (Item it : items) { it.setAnzahl(0); }

						u.setPersonen(0);

						new Info(meldung.toString(), p);

						u.purge(false); // keine weitere Meldung, wir haben ja schon den Todeskampf dokumentiert.
					}
					continue; // nächste Region
				} // endif Lavastrom
				
				// nicht segelbar und nicht betretbar? Hier ist was oberfaul!
				throw new RuntimeException("Es befinden sich Einheiten (" + StringUtils.aufzaehlung(anwesende) + ") in " + r + "?");
			}
		}
	}
	
	private void LeereVoelker()	{
		new SysMsg(1, " - löschen von Völkern");
		List<Partei> loeschliste = new ArrayList<Partei>();
		for(Partei partei : Partei.PROXY) {
			if (partei.isMonster()) continue;

			int anzahl = 0;
			for(@SuppressWarnings("unused") Unit unit : Unit.CACHE.getAll(partei.getNummer())) {
				anzahl++;
				break;
			}
			if (anzahl > 0) continue; // hat noch Einheiten

            boolean keep = false;
            for (Message m : Message.Retrieve(partei, (Coords)null, null)) {
                if (m instanceof Battle) {
                    keep = true;
                    break;
                }
            }
            // die Partei hat Kampfmeldungen - wahrscheinlich über ihre Vernichtung. Die wollen wir ihnen nicht vorenthalten.
            if (keep) continue;

			partei.Delete();
			loeschliste.add(partei);
		}
		for (Partei partei : loeschliste) Partei.PROXY.remove(partei);

		// Es kann jetzt sein, dass Allianzen ohne aktive Allianz-Option existieren:
		for (Partei p : Partei.PROXY) {
			List<Integer> partnerLoeschliste = new ArrayList<Integer>();
			for (int partnerNr : p.getAllianzen().keySet()) {
				Allianz a = p.getAllianz(partnerNr);
				if (a.getOptionen() == 0) {
					partnerLoeschliste.add(partnerNr);
				}
			}
			for (int partner : partnerLoeschliste) {
				p.getAllianzen().remove(partner);
			}
		}
	}
	
	/**
	 * löscht Einheiten die keine Personen mehr haben (z.B. nach einem Krieg) 
	 * @param withoutTemp - TRUE wenn die Temp-Einheiten nicht gelöscht werden sollen
	 */
	private void LeereEinheiten(boolean withoutTemp)
	{
		new SysMsg(1, " - löschen von Einheiten");
		
		Set<Unit> loeschliste = new HashSet<Unit>();
        for (Unit u : Unit.CACHE) {
            if (withoutTemp && (u.getTempNummer() != 0)) continue;
            if (u.getPersonen() <= 0) {
                for (Einzelbefehl eb : u.BefehleExperimental) {
                    // die Befehle stören und liefern Fehler in EVABase::Action()
                    BefehlsSpeicher.getInstance().remove(eb);
                }
                
                u.clearProperties();

                // gibt es einen Empfänger für Hab und Gut?
                Set<Unit> hinterbliebene = Region.Load(u.getCoords()).getUnits();
                for (Unit erbe : hinterbliebene) {
                    if (erbe.equals(u)) continue;
                    if (erbe.getOwner() != u.getOwner()) continue;
                    if (erbe.getPersonen() == 0) continue;

                    // gotcha!
                    List<String> sachen = new ArrayList<String>();
                    for (Item item : u.getItems()) {
                        if (item.getAnzahl() > 0) {
                            sachen.add(item.getAnzahl() + " " + item.getName());

                            int hatSchon = erbe.getItem(item.getClass()).getAnzahl();
                            erbe.getItem(item.getClass()).setAnzahl(hatSchon + item.getAnzahl());
                            item.setAnzahl(0);
                        }
                    }
                    if (!sachen.isEmpty()) {
                        new Info(erbe + " erbt " + StringUtils.aufzaehlung(sachen) + " von der aufgelösten Einheit " + u + ".", erbe);
                    }

                    break;
                }

				// die Gebäude/Schiffe freigeben ... sonst hat nie wieder einer das Kommando darüber
                List<Building> buildings = new ArrayList<Building>();
				buildings.addAll(Building.PROXY.getAll(u.getCoords()));
				for (Building b : buildings) {
					if (b.getOwner() == u.getNummer()) {
						Building.PROXY.remove(b);
						b.setOwner(0);
						Building.PROXY.add(b);
					}
				}
                for (Ship s : Ship.PROXY) if (s.getOwner() == u.getNummer()) s.setOwner(0);

                // ... and now I face the final curtain ...
                loeschliste.add(u);
            }
        } // nächste Einheit

		// TODO topTW
        for (Unit u : loeschliste) {
            u.purge(true); // ggf. Meldungen geben
        }
	}
	
	/**
	 * hier werden ggf. herrenlos gewordene Schiffe und Gebäude wieder einem
	 * Kommando unterstellt
	 */
	private void KommandoAnSichReissen() {
		List<Building> buildings = new ArrayList<Building>();
		buildings.addAll(Building.PROXY);
		for (Building b : buildings) {
			if (b.getOwner() != 0) continue;
			if (b.getUnits().isEmpty()) continue;

			UnitList l = new UnitList();
			l.addAll(b.getUnits());
			int neueHerren = l.getGroesstePartei();
			if (neueHerren == -1) continue; // niemand! (?)


			for (Unit u : l) {
				if (u.getOwner() != neueHerren) continue;
				
				Building.PROXY.remove(b);
				b.setOwner(u.getNummer());
				Building.PROXY.add(b);
				
				new Info(u + " ergreift das Kommando über " + b + ", da der Vorbesitzer irgendwo verschwunden ist.", u);
				break;
			}
		}
		
		List<Ship> ships = new ArrayList<Ship>();
		ships.addAll(Ship.PROXY);
		for (Ship s : ships) {
			if (s.getOwner() != 0) continue;
			if (s.getUnits().isEmpty()) continue;

			UnitList l = new UnitList();
			l.addAll(s.getUnits());
			int neueHerren = l.getGroesstePartei();
			if (neueHerren == -1) continue; // niemand! (?)


			for (Unit u : l) {
				if (u.getOwner() != neueHerren) continue;
				s.setOwner(u.getNummer());
				new Info(u + " ergreift das Kommando über " + s + ", da der Vorbesitzer wohl über Bord gegangen ist.", u);
				break;
			}
		}
		
		
	}

    @Override
    public boolean DoAction(Unit u, String[] befehl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void DoAction(Region r, String befehl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
	
    @Override
    public void DoAction(Einzelbefehl eb) { }

}
