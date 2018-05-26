package de.x8bit.Fantasya.Host.ManualTests.Misc;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Eisen;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import de.x8bit.Fantasya.Host.ManualTests.TestWorld;

/**
 * Beim Löschen leerer Einheiten wurde das Übergabe-Procedere an eine weiter bestehende Einheit
 * der selben Partei geändert. Na, mal sehen, ob's geht!
 * 
 * @author hapebe
 */
public class TestEinheitAufloesen extends TestBase {

	@Override
	public void mySetupTest() {
		TestWorld testWorld = this.getTestWorld();
		Partei p = testWorld.getSpieler1();
		{
			Region r = testWorld.nurBetretbar(testWorld.getAlleRegionen()).get(0);

			Unit bleibt = this.createUnit(p, r);
			bleibt.setBeschreibung("Erwartet: Einheit hat hinterher 6 Eisen und ist allein in der Region. Es gibt eine entsprechende Meldung beim Empfaenger.");
			bleibt.setItem(Silber.class, 1000);
			bleibt.setItem(Eisen.class, 1);
			bleibt.setLongOrder("LERNE Wahrnehmung");

			Unit weg = this.createUnit(p, r);
			weg.setItem(Eisen.class, 5);
			weg.setLongOrder("GIB BAUERN 1 PERSONEN");

			new Info(this.getClass().getSimpleName() + " (Empfänger vorhanden) Setup in " + r + ".", bleibt, bleibt.getCoords());
		}
		{
			Region r = testWorld.nurBetretbar(testWorld.getAlleRegionen()).get(1);

			Unit bleibt = this.createUnit(Partei.getPartei(0), r);
			bleibt.setBeschreibung("Erwartet: Andere Einheit ist weg.");
			bleibt.setItem(Silber.class, 1000);
			bleibt.setItem(Eisen.class, 1);
			bleibt.setLongOrder("LERNE Wahrnehmung");

			Unit weg = this.createUnit(p, r);
			weg.setItem(Eisen.class, 5);
			weg.setLongOrder("GIB BAUERN 1 PERSONEN");

			new Info(this.getClass().getSimpleName() + " (KEIN Empfänger) Setup in " + r + ".", p);
		}

	}

    @Override
    protected boolean verifyTest() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
