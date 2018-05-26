package de.x8bit.Fantasya.Host.ManualTests.Mantis.bug100;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Spells.HainDerTausendEichen;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;

/**
0000191: Komplexes LEHREN funktioniert nicht
Meine Einheit [456] (13 Mann Bogenschießen T9) hatte den Befehl "LEHRE 4kg 4xb 5kv".
Meine Einheit [1z4] (6 Mann Bogenschießen T9) hatte den Befehl "LEHRE 4kg 4xb 5kv".
Die Einheiten [4kg] (4 Mann), [4xb] (164 Mann) und [5kv] (14 Mann), alle schlechter im TW, haben Bogenschießen gelernt.
Somit haben 19 Mann gelehrt und 182 gelernt. Allerdings hat das nicht geklappt, beispielsweise [4kg] hat nur 30 Lerntage gewonnen.
 * @author hapebe
 */
public class Mantis191 extends TestBase {

	protected void mySetupTest() {
		Partei p = this.getTestWorld().getSpieler1();
		Region r = getRegions().get(0);

		Unit mage = Unit.CreateUnit(p.getRasse(), p.getNummer(), r.getCoords());
		mage.setName("Magier");
		mage.setBeschreibung("Bäume vorher: " + r.getResource(Holz.class).getAnzahl());
		mage.setPersonen(1);
		mage.setSkill(de.x8bit.Fantasya.Atlantis.Skills.Magie.class, 630);
		mage.setItem(Silber.class, 10000);
		mage.setAura(20);

		mage.setSpell(new HainDerTausendEichen());
		mage.setLongOrder("ZAUBERE \"Hain der 1000 Eichen\" 1");

		new Info("Mantis #191 Setup in " + r + " " + r.getCoords() + ".", p);
	}

    @Override
    protected boolean verifyTest() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
