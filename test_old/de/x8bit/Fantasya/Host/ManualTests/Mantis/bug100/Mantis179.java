package de.x8bit.Fantasya.Host.ManualTests.Mantis.bug100;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Skills.Magie;
import de.x8bit.Fantasya.Atlantis.Spells.Feuerball;
import de.x8bit.Fantasya.Atlantis.Spells.KleinesErdbeben;
import de.x8bit.Fantasya.Atlantis.Spells.MeisterDerPlatten;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;

public class Mantis179 extends TestBase {

	@Override
	protected void mySetupTest() {
		Partei partei = Partei.getPartei(1);
		Region region = (this.getTestWorld().nurBetretbar(this.getTestWorld().getAlleRegionen())).get(0);
		
		Unit mage = this.createUnit(partei, region);
		mage.setSkill(Magie.class, 30);
		mage.setAura(100);
		mage.setSpell(new Feuerball());
		mage.setSpell(new KleinesErdbeben());
		mage.setSpell(new MeisterDerPlatten());
		mage.Befehle.add("LERNE Magie");
		mage.Befehle.add("KAMPFZAUBER Angriff \"Feuerball\"");
		mage.setItem(Silber.class, 2000);
	}

    @Override
    protected boolean verifyTest() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
