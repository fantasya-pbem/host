package de.x8bit.Fantasya.Host.ManualTests;

import java.util.ArrayList;
import java.util.List;

/**
 * Fasst mehrere Tests zu einer gemeinsam abzuarbeitenden Suite zusammen.
 * @author hb
 */
public abstract class TestSuite extends TestBase {
    protected List<TestBase> tests  = new ArrayList<TestBase>();

    public TestSuite() {
        throw new IllegalStateException("Der TestSuite-Konstruktor muss von abgeleiteten Klassen überschrieben werden!");
    }

    public TestSuite(boolean dummy) {}

    @Override
	public void setTestWorld(TestWorld testWorld) {
        super.setTestWorld(testWorld);
        for (TestBase test : this.getTests()) test.setTestWorld(testWorld);
	}

    @Override
    public void setupTest() {
        for (TestBase test : this.getTests()) test.setupTest();
    }

    @Override
    protected void mySetupTest() {
        // noop
    }

    @Override
    protected boolean verifyTest() {
        boolean retval = true;
        for (TestBase test : this.getTests()) {
            try {
                if (!test.verifyTest()) retval = false;
            } catch(RuntimeException ex) {
                retval = this.fail("Exception während des Prüfens: " + ex.getMessage());
				ex.printStackTrace();
            }
        }
        // if (!retval) this.fail(this.getClass().getSimpleName() + " - Test-Suite ist fehlgeschlagen.");
        return retval;
    }

    public List<TestBase> getTests() {
        return tests;
    }

    public void addTest(TestBase test) {
        this.tests.add(test);
    }

}
