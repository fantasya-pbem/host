package de.x8bit.Fantasya.Host.EVA;

import de.x8bit.Fantasya.Atlantis.Helper.RegionsSicht;
import de.x8bit.Fantasya.Atlantis.Messages.Debug;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.InselVerwaltung;

/**
 *
 * @author hapebe
 */
public class AtlantenLesen extends EVABase implements NotACommand {
    
    public AtlantenLesen() {
        super("Die Atlanten der Partei werden studiert...");
    }

    @Override
    public void PreAction() {
        for (Partei p : Partei.PROXY) {
            // nachdem Einheiten gereist sind, muss ggf. die detaillierte Regionssicht wieder entfernt werden!
            for (Unit u : Unit.CACHE) {
                if (u.getOwner() == p.getNummer()) {
                    p.addKnownRegion(u.getCoords(), true, Unit.class);
                }
            }

			if (!p.hasProperty(Partei.PROPERTY_ATLAS)) {
				continue;
			}
            String bibliothek = p.getStringProperty(Partei.PROPERTY_ATLAS);
            
            // die erste und die letzte geschweifte Klammer abtrennen:
            bibliothek = bibliothek.substring(1, bibliothek.length() - 1);
            
            String[] eintraege = bibliothek.split("\\}\\s+\\{");
            for (String eintrag : eintraege) {
                RegionsSicht rs = RegionsSicht.FromCode("{" + eintrag + "}");
                p.atlas.put(rs.getCoords(), rs);
            }
            
            // new Debug("Partei " + p + ":\n" + p.atlas.toString());
            new Debug("Partei " + p + " hat einen Atlas mit " + p.atlas.size() + " Regionen im Regal.");
            
        }
        
        InselVerwaltung.getInstance().karteVerarbeiten();
        InselVerwaltung.getInstance().parteiInseln();
    }

    @Override
    public void PostAction() {  }
    
    @Override
    public boolean DoAction(Unit u, String[] befehl) { throw new UnsupportedOperationException("Not supported yet."); }

    @Override
    public void DoAction(Region r, String befehl) { throw new UnsupportedOperationException("Not supported yet."); }

    @Override
    public void DoAction(Einzelbefehl eb) { throw new UnsupportedOperationException("Not supported yet."); }

}
