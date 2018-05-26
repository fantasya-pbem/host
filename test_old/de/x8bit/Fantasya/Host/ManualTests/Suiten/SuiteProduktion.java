package de.x8bit.Fantasya.Host.ManualTests.Suiten;

import de.x8bit.Fantasya.Host.ManualTests.Befehle.MachePferd;
import de.x8bit.Fantasya.Host.ManualTests.TestSuite;
import de.x8bit.Fantasya.Host.ManualTests.Mantis.bug100.Mantis148;
import de.x8bit.Fantasya.Host.ManualTests.Mantis.bug100.Mantis181;
import de.x8bit.Fantasya.Host.ManualTests.Mantis.bug100.Mantis187;
import de.x8bit.Fantasya.Host.ManualTests.Mantis.bug200.Mantis202;
import de.x8bit.Fantasya.Host.ManualTests.Mantis.bug200.Mantis224;
import de.x8bit.Fantasya.Host.ManualTests.Mantis.bug200.Mantis225;
import de.x8bit.Fantasya.Host.ManualTests.Misc.TestPegasusFangen;

/**
 *
 * @author hapebe
 */
public class SuiteProduktion extends TestSuite {

	public SuiteProduktion() {
        super(true); // avoid Exception
        this.addTest(new Mantis148());
        this.addTest(new Mantis181());
        this.addTest(new Mantis187());
		this.addTest(new Mantis202());
        this.addTest(new Mantis224());
        this.addTest(new Mantis225());
        this.addTest(new MachePferd());
        this.addTest(new TestPegasusFangen());
	}


}
