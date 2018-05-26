package de.x8bit.Fantasya.Host.EVA;

import de.x8bit.Fantasya.Atlantis.Helper.Monster;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;

/**
 *
 * @author hapebe
 */
public class MonsterMeldungenAuswerten extends EVABase implements NotACommand {

    public MonsterMeldungenAuswerten() {
        super("Monster werten die Meldungen von letztem Monat aus...");
    }
    
    @Override
    public void PreAction() {
        for (Unit u : Unit.CACHE) {
            if (u instanceof Monster) {
                Partei p = Partei.getPartei(u.getOwner());
                if (p.isMonster()) {
                    
                    try {
                        ((Monster)u).meldungenAuswerten();
                    } catch(UnsupportedOperationException ex) {
                        ; // NOP
                    }
                    
                }
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
    public void DoAction(Einzelbefehl eb) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void PostAction() {  }
    
}
