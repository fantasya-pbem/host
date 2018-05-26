package de.x8bit.Fantasya.Host.ManualTests.Suiten;

import de.x8bit.Fantasya.Host.ManualTests.TestSuite;
import de.x8bit.Fantasya.Host.ManualTests.EVA.*;
import de.x8bit.Fantasya.Host.ManualTests.Misc.BewacheWirkung;
import de.x8bit.Fantasya.Host.ManualTests.Misc.MagierRekrutieren;
import de.x8bit.Fantasya.Host.ManualTests.Misc.TestMyrrheName;

/**
 *
 * @author hb
 */
public class SuiteEVA extends TestSuite {
    
    public SuiteEVA() {
        super(true); // avoid Exception

        this.addTest(new SuiteBasisTests());
        this.addTest(new TestSpielerLoeschen());
		this.addTest(new MagierRekrutieren());
		this.addTest(new SuiteAlleZauber());
        this.addTest(new BewacheWirkung());
        this.addTest(new SuiteProduktion());
		this.addTest(new TestGebaeudeUnterhalt());
		this.addTest(new TestMyrrheName());
    }



}
