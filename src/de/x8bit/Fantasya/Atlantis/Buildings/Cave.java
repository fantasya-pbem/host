package de.x8bit.Fantasya.Atlantis.Buildings;

import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.Debug;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.util.ComplexName;
import de.x8bit.Fantasya.util.Random;
import de.x8bit.Fantasya.util.StringUtils;

/**
 * pauschale Oberklasse um in die Unterwelt (bzw. wieder zurück) zu gelangen
 * @author mogel
 *
 */
public class Cave extends Building {

	public static final String PROPERTY_AUSGANG = "OtherPlaneBuilding";
	public static final String PROPERTY_OFFEN = "Open";
	public static final int MUTATIONSDAUER = 10;

	public Cave() {
		super();
		setName("Höhle");
	}

	/**
	 * liefert alle möglichen Höhlen
	 * @param ebene - aber nur für diese Ebene
	 * @return eine Liste aller Höhlen die geschlossen sind !!
	 */
	public static List<Cave> GetCaves(int ebene) {
		List<Cave> alb = new ArrayList<Cave>();

		for (Building b : Building.PROXY) {
			if (b.getCoords().getWelt() != ebene) {
				continue;
			}
			if (!(b instanceof Cave)) {
				continue;
			}

			// gotcha:
			alb.add((Cave) b);
		}

		return alb;
	}

	/**
	 * wird von Environment() aufgerufen
	 */
	public static void NeueRunde() {
		VorhandeneEntwickeln();
	}

	/**
	 * sollte aufgerufen werden, damit alle Höhlen "altern", sich entwickeln, 
	 * d.h. auf eine neue Runde vorbereitet werden.
	 */
	protected static void VorhandeneEntwickeln() {
		List<Building> alle = new ArrayList<Building>();
		alle.addAll(GetCaves(1));
		alle.addAll(GetCaves(-1));

		for (Building cave : alle) {
			// Wert holen, wie lange die Höhle schon unbenutzt (unbetreten) ist
			int open = cave.getIntegerProperty(Cave.PROPERTY_OFFEN, 0);

			if (open > 0) {
				open--;
				if (open == 0) {
					cave.removeProperty(Cave.PROPERTY_AUSGANG);
				}
				new Debug(cave + " [" + cave.getNummerBase36() + "]: setze Property Offen auf " + open + ".");
				cave.setProperty(Cave.PROPERTY_OFFEN, open);
			}

		}
	}

	protected static void HoehlenEntstehen() {
//        // erste Höhlen anlegen:
//        List<Region> oberweltOrte = new ArrayList<Region>();
//        List<Region> unterweltOrte = new ArrayList<Region>();
//        for (Region r : Region.PROXY) {
//            if (!r.istBetretbar(null)) continue;
//            if (r.getClass() == Sandstrom.class) continue; // keine Höhlen auf/in Sandströmen
//            
//            if (r.getCoords().getWelt() == 1) {
//                oberweltOrte.add(r);
//            } else if (r.getCoords().getWelt() == -1) {
//                unterweltOrte.add(r);
//            }
//        }
//        if (oberweltOrte.isEmpty() || unterweltOrte.isEmpty()) {
//            new ZATMsg("Es gibt entweder keine Ober- oder Unterweltregionen, die sich für Höhlen eignen würden...");
//            return;
//        }
//
//        List<String> hoehlenArten = new ArrayList<String>();
//        hoehlenArten.add("Erdspalte");
//        hoehlenArten.add("Erdloch");
//        hoehlenArten.add("Grotte");
//        hoehlenArten.add("Sandloch");
//        hoehlenArten.add("Erdriss");
//        hoehlenArten.add("Felsspalte");
//        Collections.shuffle(oberweltOrte);
//        Collections.shuffle(unterweltOrte);
//        Set<Integer> inselnMitHoehle = new HashSet<Integer>();
//        for (int i=0; i<5; i++) {
//
//            // Oberwelt:
//            int loop = 0;
//            for (boolean okay = false; !okay; ) {
//                Region r = oberweltOrte.get(0);
//                if (!inselnMitHoehle.contains(r.getInselKennung())) {
//                    okay = true;
//
//                    String hoehlenArt = hoehlenArten.get(Atlantis.rnd(0, hoehlenArten.size()));
//                    if (r.getClass() == Wueste.class) hoehlenArt = "Sandloch";
//                    if (r.getClass() == Sumpf.class) hoehlenArt = "Erdloch";
//                    if (r.getClass() == Moor.class) hoehlenArt = "Erdloch";
//                    if (r.getClass() == Gletscher.class) hoehlenArt = "Felsspalte";
//                    if (r.getClass() == Vulkan.class) hoehlenArt = "Felsspalte";
//                    if (r.getClass() == aktiverVulkan.class) hoehlenArt = "Felsspalte";
//
//                    Building cave = Building.Create(hoehlenArt, r.getCoords());
//                    cave.setSize(Atlantis.W(20) + Atlantis.W(10));
//                    inselnMitHoehle.add(r.getInselKennung());
//                } else {
//                    Collections.shuffle(oberweltOrte);
//                }
//                if (loop++ > 100000) okay = true; // dann gibt es wohl nicht genügend Inseln
//            }
//            // Unterwelt:
//            loop = 0;
//            for (boolean okay = false; !okay; ) {
//                Region r = unterweltOrte.get(0);
//                if (!inselnMitHoehle.contains(r.getInselKennung())) {
//                    okay = true;
//
//                    String hoehlenArt = hoehlenArten.get(Atlantis.rnd(0, hoehlenArten.size()));
//                    if (r.getClass() == Wueste.class) hoehlenArt = "Sandloch";
//                    if (r.getClass() == Sumpf.class) hoehlenArt = "Erdloch";
//                    if (r.getClass() == Moor.class) hoehlenArt = "Erdloch";
//                    if (r.getClass() == Gletscher.class) hoehlenArt = "Felsspalte";
//                    if (r.getClass() == Vulkan.class) hoehlenArt = "Felsspalte";
//                    if (r.getClass() == aktiverVulkan.class) hoehlenArt = "Felsspalte";
//
//                    Building cave = Building.Create(hoehlenArt, r.getCoords());
//                    cave.setSize(Atlantis.W(20) + Atlantis.W(10));
//                    inselnMitHoehle.add(r.getInselKennung());
//                } else {
//                    Collections.shuffle(unterweltOrte);
//                }
//                if (loop++ > 100000) okay = true; // dann gibt es wohl nicht genügend Inseln
//            }
//        }
	}

	/** muss auch von den anderen Höhlen überschrieben werden */
	@Override
	public String toString() {
		return "Höhle";
	}

	/** nop ... Zerstörung nur durch göttliche Intervention */
	@Override
	public void Zerstoere(Unit u) {
		new Fehler(u + " kann keine Höhle zerstören.", u);
	}

	/** keine Produktion von Höhlen */
	@Override
	protected void GenericMake(Unit u, int anzahl, Class<? extends Skill> skill, int talentwert, Item[] needed) {
		new Fehler(u + " kann keine Höhle anlegen.", u);
	}

	/** Einheiten können immer rein bzw. raus */
	@Override
	public boolean canEnter(Unit u) {
		return true;
	}

	/** Einheiten können immer rein bzw. raus */
	@Override
	public Unit istBelagert() {
		return null;
	}

	/** Funktioniert immer */
	@Override
	public boolean hatFunktion() {
		return true;
	}

	/**
	 * eine Einheit will in die Unterwelt oder zurück
	 */
	@Override
	public void Enter(Unit unit) {
		// prüfen, ob Bewegung möglich wäre:
		if (!unit.canWalkAnimals()) {
			if (unit.getPersonen() == 1) {
				new Fehler(unit + " hat seine Tiere nicht unter Kontrolle.", unit);
			} else {
				new Fehler(unit + " haben ihre Tiere nicht unter Kontrolle.", unit);
			}
			return;
		}
		// Gewicht & Kapazität überprüfen - false = zu Fuß
		if (unit.gesamteFreieKapazitaet(false) < 0) {
			new Fehler(unit + " ist zu schwer um sich zu bewegen.", unit);
			return;
		}

		// prüfen, ob die Einheit große Gegenstände / Tiere dabei hat:
		List<String> sperrige = new ArrayList<String>();
		for (Item it : unit.getItems()) {
			if (it.getAnzahl() == 0) {
				continue;
			}
			if (it.getGewicht() > 5000) { // Pferde gehen gerade noch so
				sperrige.add(it.toString());
			}
		}
		if (!sperrige.isEmpty()) {
			new Fehler(unit + " hat " + StringUtils.aufzaehlung(sperrige) + " dabei und kommt damit nicht in die Höhle.", unit);
			return;
		}


		// Wert holen, wie lange die Höhle schon unbenutzt (unbetreten) ist
		int open = getIntegerProperty(Cave.PROPERTY_OFFEN, 0);

		// die Höhle ist geschlossen ... also öffnen
		if (open == 0) {
			List<Cave> hoehlen = GetCaves(0 - getCoords().getWelt());

			// TODO andere Fehlerbehandlung
			if (hoehlen.isEmpty()) {
				return;
			}

			Cave other = hoehlen.get(Random.rnd(0, hoehlen.size()));
			setProperty(Cave.PROPERTY_AUSGANG, other.getNummer());
		}

		// jetzt die Ebene ändern, dabei wird auch das "offen"-Property auf den Ausgangswert gesetzt.
		ChangePlane(unit);
	}

	private void ChangePlane(Unit unit) {
		Building ausgang = Building.getBuilding(getIntegerProperty(Cave.PROPERTY_AUSGANG, 0));
		if (ausgang == null) {
			new BigError(new RuntimeException("Korrespondierende Höhle auf der anderen Ebene wurde nie ausgewählt oder existiert nicht mehr."));
		}
		Region ziel = Region.Load(ausgang.getCoords());
		if (ziel == null) {
			new BigError(new RuntimeException("Ziel-Region @" + ausgang.getCoords() + " von Höhle " + ausgang + " existiert nicht (mehr)."));
		}

		Partei partei = Partei.getPartei(unit.getOwner());

		if (!partei.canAccess(ziel)) {
			new Fehler(unit + " irrt in den Gängen von " + this + " herum und kommt schließlich wieder am Ausgangsort an.", unit);
			return;
		}

		if (!ziel.istBetretbar(unit)) {
			new Fehler(unit + " hat Angst, " + ziel + " zu betreten und kehrt um.", unit);
		}


		Unit.CACHE.remove(unit);
		unit.setCoords(ausgang.getCoords());
		Unit.CACHE.add(unit);

		// auf jeden Fall Infos in die Reporte:
		Partei.getPartei(unit.getOwner()).addKnownRegion(ausgang.getCoords(), true, Cave.class);

		String verb = (unit.getPersonen() == 1 ? "steigt" : "steigen");
		String richtung = (ausgang.getCoords().getWelt() == -1 ? "hinab" : "hinauf");
		new Info(unit + " " + verb + " durch finstere Gänge " + richtung + " nach " + Region.Load(unit.getCoords()) + ".", unit);

		UpdateStates(this, ausgang);
		UpdateStates(ausgang, this);
	}

	private void UpdateStates(Building currentplain, Building otherplain) {
		// wieder für 10 Wochen offen
		currentplain.setProperty(Cave.PROPERTY_OFFEN, MUTATIONSDAUER);
		new Debug(this + "[" + this.getNummerBase36() + "]: setze Property Offen auf " + MUTATIONSDAUER + ".");

		// die Höhle der anderen Ebene festlegen
		currentplain.setProperty(Cave.PROPERTY_AUSGANG, otherplain.getNummer());
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Höhle", "Höhlen", null);
	}
}
