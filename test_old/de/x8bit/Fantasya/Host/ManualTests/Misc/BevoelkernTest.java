package de.x8bit.Fantasya.Host.ManualTests.Misc;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Host.ManualTests.*;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;

/**
 *
 * @author hb
 */
public class BevoelkernTest extends TestBase {

    @Override
    protected void mySetupTest() {
        TestWorld tw = this.getTestWorld();
        
		Partei p = tw.createPartei(Mensch.class);
		p.setName(getName() + "-Partei");
        p.setUrsprung(new Coords(0,0,0));

        int cnt = 1;
        for (Region r : tw.nurBetretbar(getRegions())) {
            Unit u = this.createUnit(p, r);
            u.setName(this.getName() + " " + cnt);

        }
        new TestMsg(this.getName() + " Setup.");
    }

    @Override
    protected boolean verifyTest() {
        throw new UnsupportedOperationException();
    }

}
