package de.x8bit.Fantasya.Host.ManualTests.Suiten;

import de.x8bit.Fantasya.Host.ManualTests.TestSuite;
import de.x8bit.Fantasya.Host.ManualTests.Mantis.bug200.Mantis235;

/**
 * @author hb
 */
public class SuiteAllianzen extends TestSuite {

    public SuiteAllianzen() {
        super(true); // avoid Exception

		this.addTest(new Mantis235());
    }

}
