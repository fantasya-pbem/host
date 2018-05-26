package de.x8bit.Fantasya.Host.ManualTests.Suiten;

import de.x8bit.Fantasya.Host.ManualTests.Mantis.bug100.Mantis170;
import de.x8bit.Fantasya.Host.ManualTests.Mantis.bug200.Mantis292;
import de.x8bit.Fantasya.Host.ManualTests.TestSuite;
import de.x8bit.Fantasya.Host.ManualTests.Misc.*;

/**
 *
 * @author hapebe
 */
public class SuiteAlleZauber extends TestSuite {

    public SuiteAlleZauber() {
        super(true); // avoid Exception

		// regionsabhängig:
        this.addTest(new ZauberFernsicht());

        this.addTest(new ZauberHammerDerGoetter());
		this.addTest(new ZauberProvokationDerTitanen());
		this.addTest(new ZauberSegenDerGoettin());
		this.addTest(new ZauberMeisterDesSchiffs());
        this.addTest(new ZauberMeisterDerSchmiede());
        this.addTest(new ZauberMeisterDerPlatten());
        this.addTest(new ZauberMeisterDerWagen());
        this.addTest(new ZauberMeisterDerResourcen());
		this.addTest(new ZauberKleinesErdbeben());
		this.addTest(new ZauberErdbeben());
		this.addTest(new ZauberFeuerball());
		this.addTest(new ZauberFeuerwalze());
        this.addTest(new ZauberGuterWind());
        this.addTest(new ZauberHain());
        this.addTest(new ZauberKlauen());
        this.addTest(new ZauberLuftreise());
        this.addTest(new ZauberSteinschlag());
        this.addTest(new ZauberSturm());

		this.addTest(new Mantis170());
		this.addTest(new Mantis292()); // ZAUBERBUCH-Meldung?
    }

}
