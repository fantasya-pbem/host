package de.x8bit.Fantasya.Host.EVA;

import de.x8bit.Fantasya.Atlantis.Atlantis;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Helper.RegionsSicht;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.InselVerwaltung;
import de.x8bit.Fantasya.Host.GameRules;

/**
 *
 * @author hapebe
 */
public class Kartografieren extends EVABase implements NotACommand {

    public Kartografieren() {
        super("Die Geographen werden auf die Welt losgelassen...");
    }

    @Override
    public void PreAction() {
        // Inseln werden bestimmt, jede Partei erhält ihren Satz an eigenen Insel-Bezeichnungen:
        for (Partei partei : Partei.PROXY) {
            if (partei.getNummer() == 0) {
                for (Region r : Region.CACHE.values()) {
                    partei.addKnownRegion(r, true, Atlantis.class);
                }
                continue;
            }

            for (Unit u : Unit.CACHE) {
                if (u.getOwner() == partei.getNummer()) {
                    partei.addKnownRegion(u.getCoords(), true, Unit.class);
                }
            }
        }
        InselVerwaltung iv = InselVerwaltung.getInstance();
        iv.karteVerarbeiten();
        iv.parteiInseln();
    }

    @Override
    public void PostAction() {
        final int runde = GameRules.getRunde();
        
        for (Partei partei : Partei.PROXY) {
			// capture potential errors with parties that have no units
			if (partei.getKnownRegions(false).isEmpty()) {
				continue;
			}
			
            for (RegionsSicht rs : partei.getKnownRegions(false)) {
                Region r = Region.Load(rs.getCoords());
                rs.copyStrassen(r);
                rs.setTerrain(r.getTyp());
                partei.atlas.put(rs.getCoords(), rs);
                
                if (rs.hasDetails()) {
                    if (!Unit.CACHE.getAll(r.getCoords(),partei.getNummer()).isEmpty()) {
                        // dann auch die Nachbarn mit aufnehmen:
                        for (Coords n : rs.getCoords().getNachbarn()) {
                            Region nr = Region.Load(n);
                            if (partei.canAccess(nr)) {
                                RegionsSicht nrs = new RegionsSicht(runde, n, false, Region.class);
                                nrs.copyStrassen(nr);
                                nrs.setTerrain(nr.getTyp());
                                partei.atlas.put(nrs.getCoords(), nrs);
                            }
                        }
                    }
                }
            }

            partei.setProperty(Partei.PROPERTY_ATLAS, partei.atlas.asCodes());
        }
    }
    
    @Override
    public boolean DoAction(Unit u, String[] befehl) { throw new UnsupportedOperationException("Not supported yet."); }

    @Override
    public void DoAction(Region r, String befehl) { throw new UnsupportedOperationException("Not supported yet."); }

    @Override
    public void DoAction(Einzelbefehl eb) { throw new UnsupportedOperationException("Not supported yet."); }
}
