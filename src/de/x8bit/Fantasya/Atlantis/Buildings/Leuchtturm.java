package de.x8bit.Fantasya.Atlantis.Buildings;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Items.Eisen;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Items.Stein;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Regions.Ozean;
import de.x8bit.Fantasya.Atlantis.Skills.Burgenbau;
import de.x8bit.Fantasya.Atlantis.Skills.Wahrnehmung;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.util.ComplexName;
import de.x8bit.Fantasya.util.MapSelection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p>Aussichts- und Orientierungsturm, der in einem bestimmten Radius
 * Ozean-Regionen samt der dort befindlichen Schiffe anzeigt; und andererseits
 * eigenen Schiffen innerhalb dieses Radius bei der Navigation hilft (bspw.
 * gegen Abdriften im Sturm)</p>
 * <p>Siehe <a href="http://www.fantasya-pbem.de/mantis/view.php?id=157">
 * Mantis-Entrag #157</a></p>
 * @author hapebe
 */
public class Leuchtturm extends Building {

	public final static Item[] MATERIAL_PRO_PUNKT = new Item[] {
			new Stein(2),
			new Eisen(2),
			new Holz(2),
			new Silber(100)
	};


	@Override
	public void Zerstoere(Unit u) {
		super.Zerstoere(u, new Item [] { new Stein(1), new Holz(1), new Eisen(1) });
	}

	@Override
	public String getTyp() { return this.getClass().getSimpleName(); }

	@Override
	public int GebaeudeUnterhalt() { return 100; }

	@Override
	public void Mache(Unit u, int anzahl) {
		super.Mache(u, anzahl);
	}

	@Override
	public void Mache(Unit u) {
		int tw = u.Talentwert(Burgenbau.class);
		if (tw < 5)	{
			new Fehler(u + " hat nicht genügend Talent um an " + this + " zu bauen.", u);
			return;
		}

		// zusätzlichen Gebäude testen
//		Region region = Region.Load(u.getCoords());
//		if (!region.hatGebaeude(Burg.class, 10, u))	{
//			new Fehler(u + " - in " + region + " fehlt ein Turm um " + getTyp() + " bauen zu können", u, u.getCoords());
//			return;
//		}

		GenericMake(u, 0, Burgenbau.class, 5, MATERIAL_PRO_PUNKT );
	}

	/**
	 * @param p Die Ausschau haltende Partei
	 * @return
	 */
	public int getSichtRadius(Partei p) {
		if (!hatFunktion()) return 0;

		int r = (int)Math.floor(Math.log10((double)getSize())) + 1;

		Unit topWahrnehmer = null;
		for (Unit u : this.getBewohner()) {
			if (u.getOwner() != p.getNummer()) continue;
			
			if (topWahrnehmer == null) {
				topWahrnehmer = u;
			} else if (u.Talentwert(Wahrnehmung.class) > topWahrnehmer.Talentwert(Wahrnehmung.class)) {
				topWahrnehmer = u;
			}
		}

		if (topWahrnehmer == null) return 0; // niemand von p drinnen.

		if (r > (topWahrnehmer.Talentwert(Wahrnehmung.class) / 2)) {
			if (topWahrnehmer.Talentwert(Wahrnehmung.class) == 0) {
				new Info(topWahrnehmer + " sieht irgendwie nicht viel.", topWahrnehmer);
			} else {
				new Info(topWahrnehmer + " reibt sich die Augen und hat das Gefühl, mit mehr Übung wohl noch weiter schauen zu können.", topWahrnehmer);
			}
			r = (topWahrnehmer.Talentwert(Wahrnehmung.class) / 2);
		}

		return r;
	}

	/**
	 * erweitert die 'knownRegions' jeder Insassen-Partei entsprechend Leuchtturm-Größe
	 * und Wahrnehmungstalent
	 */
	public void leuchten() {
		Region r = Region.Load(getCoords());
		Set<Partei> insassen = new HashSet<Partei>();
		for (Unit u : r.getUnits()) {
			if (u.getGebaeude() == getNummer()) insassen.add(Partei.getPartei(u.getOwner()));
		}

		for (Partei p : insassen) {
			int radius = getSichtRadius(p);

			if (radius <= 0) continue;

			MapSelection sichtBereich = new MapSelection();
			sichtBereich.add(r.getCoords());
			
			// die sichtbare Karte ausdehnen - dabei die (schiffbare) Entfernung merken
			// erstmal nur bis vor den Horizont (radius - 1), der Rand kommt dann später dazu
			Map<Coords, Integer> segelEntfernung = new HashMap<Coords, Integer>();
			segelEntfernung.put(r.getCoords(), 0);
			for (int i=0; i < radius - 1; i++) {
				for (Coords c : sichtBereich.getAussenKontur()) {
                    int entfernung = i+1;
					if (!(Region.Load(c) instanceof Ozean)) continue;
					if (!sichtBereich.contains(c)) {
						sichtBereich.add(c);
						segelEntfernung.put(c, entfernung);

//						if (ZATMode.CurrentMode().isDebug()) {
//							Region reg = Region.Load(c);
//							reg.setBeschreibung(r.getBeschreibung() + " [" + p.getNummerBase36() + "]G" + entfernung);
//						}
					}
				}
			}
            // für radius = 1 enthält sichtBereich und segelEntfernung nur r selbst (jeweils 0)

			// der Randbereich - hier kommt auch Land in Frage:
			for (Coords c : sichtBereich.getAussenKontur()) {
				int cEntfernung = Integer.MAX_VALUE - 100;
                
				// den (segelbaren) Nachbarn mit der geringesten Segel-Entfernung zum Zentrum suchen:
				for (Coords n : c.getNachbarn()) {
					if (segelEntfernung.containsKey(n)) {
						if (!(Region.Load(n) instanceof Ozean)) continue;
						if (segelEntfernung.get(n) < cEntfernung) cEntfernung = segelEntfernung.get(n);
					}
				}
                // für radius == 1 Sonderbehandlung, weil es keine Segelentfernungen für den Rand gibt:
                if ((radius == 1) && (c.getDistance(r.getCoords()) == 1)) cEntfernung = 0;
                
				segelEntfernung.put(c, cEntfernung + 1); // +1, weil es ja die Entfernung des "nahesten" Nachbarn ist.
				sichtBereich.add(c);
//				if (ZATMode.CurrentMode().isDebug()) {
//					Region reg = Region.Load(c);
//					reg.setBeschreibung(r.getBeschreibung() + " [" + p.getNummerBase36() + "]R" + (cEntfernung + 1));
//				}
			}

			// jetzt vergleichen - Regionen mit Segel-Entfernung > Luftlinie --> raus aus dem Sichtbereich
			MapSelection luftlinie = new MapSelection();
			luftlinie.add(r.getCoords());
			Set<Coords> loeschliste = new HashSet<Coords>();
			for (int i=0; i<radius; i++) {
				for (Coords c : luftlinie.getAussenKontur()) {
					if (!sichtBereich.contains(c)) continue;
					if (segelEntfernung.get(c) > i + 1) {
						loeschliste.add(c);
//						if (ZATMode.CurrentMode().isDebug()) {
//							Region reg = Region.Load(c);
//							reg.setBeschreibung(reg.getBeschreibung() + " INVISIBLE: L.rad. " + segelEntfernung.get(c) + " > " + (i + 1));
//							p.addKnownRegion(c, true, Leuchtturm.class);
//						}
					}
				}
				luftlinie.addAll(luftlinie.getAussenKontur());
			}
			sichtBereich.removeAll(loeschliste);
			

			for (Coords c : sichtBereich) {
				// der echte Sichtbereich:
				if (c.equals(r.getCoords())) {
					// der Standort selbst,
					p.addKnownRegion(c, true, Leuchtturm.class);
				} else if (Region.Load(c) instanceof Ozean) {
					// ...und Wasser
					p.addKnownRegion(c, true, Leuchtturm.class);
				} else {
					// nicht der Ausgangspunkt, nicht segelbar:
					p.addKnownRegion(c, false, Leuchtturm.class);
//					if (ZATMode.CurrentMode().isDebug()) {
//						Region reg = Region.Load(c);
//						reg.setBeschreibung(reg.getBeschreibung() + " [" + p.getNummerBase36() + "]HAZY(land)dist" + segelEntfernung.get(c));
//						p.addKnownRegion(c, true, Leuchtturm.class);
//					}
				}

//				if (ZATMode.CurrentMode().isDebug()) {
//					Region reg = Region.Load(c);
//					reg.setBeschreibung(reg.getBeschreibung() + " [" + p.getNummerBase36() + "]L.rad. " + segelEntfernung.get(c));
//				}
			}

		}
	}

	@Override
	public ComplexName getComplexName() {
		return new ComplexName("Leuchtturm", "Leuchttürme", null);
	}

}
