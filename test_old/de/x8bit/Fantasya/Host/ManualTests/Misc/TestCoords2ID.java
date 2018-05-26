package de.x8bit.Fantasya.Host.ManualTests.Misc;


import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import de.x8bit.Fantasya.Host.ManualTests.TestWorld;

/**
 *
 * @author hb
 */
public class TestCoords2ID extends TestBase {

    @Override
    protected void mySetupTest() {
        TestWorld tw = this.getTestWorld();

        Region r = tw.nurBetretbar(getRegions()).get(0);

		int id = r.getCoords().asRegionID(true); // new mode
		new SysMsg("Coord-ID zu " + r.getCoords() + " = " + id + " (0x" + Integer.toHexString(id) + ")");

		Coords c = Coords.fromRegionID(id);
		new SysMsg("Zurückgewandelte Coords: " + c);

		tw.setContinueWithZAT(false);
    }

    @Override
    protected boolean verifyTest() {
		throw new UnsupportedOperationException("Dieser Test ist nicht automatisiert.");
    }

}
