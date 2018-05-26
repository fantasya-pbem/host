package de.x8bit.Fantasya.Host.ManualTests.EVA;

import de.x8bit.Fantasya.Host.ManualTests.*;

import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.EVA.util.NeuerSpieler;
import de.x8bit.Fantasya.Host.GameRules;

/**
 *
 * @author hb
 */
public class NeueSpielerTest extends TestBase {

    @Override
    protected void mySetupTest() {
		GameRules.SetOption(GameRules.NEUE_INSEL_METHODE, "April2011");
		GameRules.Save();

		Partei zero = Partei.Create();
		Partei.PROXY.remove(zero);
		zero.setNummer(0);
		zero.setMonster(1);
		Partei.PROXY.add(zero);
		
        {
            for (int i=1; i<=200; i++) {
				NeuerSpieler n = new NeuerSpieler();
				n.setEmail("fantasya-" + i + "@nowhere.no");
				n.setRasse(Mensch.class);
				n.setTarnung(Mensch.class);
				n.setHolz(15);
				n.setEisen(15);
				n.setSteine(15);
				n.setInsel(0);

				NeuerSpieler.PROXY.add(n);
            }

            new TestMsg(this.getName() + " Setup");
        }
    }

    @Override
    protected boolean verifyTest() {
        new TestMsg(this.getName() + " bitte einzeln und von Hand verifizieren.");
        return false;
    }

}
