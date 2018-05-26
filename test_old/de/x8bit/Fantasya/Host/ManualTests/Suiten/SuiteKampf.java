package de.x8bit.Fantasya.Host.ManualTests.Suiten;

import de.x8bit.Fantasya.Host.ManualTests.TestSuite;
import de.x8bit.Fantasya.Host.ManualTests.Kampf.*;
import de.x8bit.Fantasya.Host.ManualTests.Misc.KrakenkampfTest;

/**
 *
 * @author hb
 */
public class SuiteKampf extends TestSuite {
    
    public SuiteKampf() {
        super(true); // avoid Exception

        // Regionsabhängikeit: Berge
        this.addTest(new SteinschlagKampf());
        
        // Regionsabhängigkeit: Ozean
        this.addTest(new KrakenkampfTest());

        // Region egal
        this.addTest(new FeuerballKampf());
        this.addTest(new FeuerwalzenKampf());
        this.addTest(new KatapultKampf());
        this.addTest(new Ruestungstest());
        this.addTest(new KleinesErdbebenKampf());
        this.addTest(new ErdbebenKampf());
        this.addTest(new SturmZauberKampf());
        this.addTest(new BogenschiessKampf());
        this.addTest(new TestKriegselefanten());
        this.addTest(new TaktikerKampf());
        this.addTest(new VerschonungstestA()); // Nicht-angegriffene KÄMPFE-NICHT-Einheiten verschonen
        this.addTest(new VerschonungstestB()); // getarnte Einheiten bei ATTACKIERE PARTEI ... verschonen
        this.addTest(new PegasusKampf()); // funktionieren die Pegasus-Boni wie gewollt?
    }



}
