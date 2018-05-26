package de.x8bit.Fantasya.Host.ManualTests.Suiten;

import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Welt;
import de.x8bit.Fantasya.Host.EVA.util.EVAFastLoader;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.Main;
import de.x8bit.Fantasya.Host.ManualTests.TestSuite;
import java.sql.SQLException;

/**
 * <p>Hier sollen möglichst alle TestSuite eingebunden werden, so dass der
 * &quot;ganz große Test&quot; möglicht ist.</p>
 * @author hb
 */
public class AlleSuiten extends TestSuite {

    public AlleSuiten() {
        super(true); // avoid Exception

		// Es wird viel Platz gebraucht:
		if (GameRules.getRunde() <= 1) {
			for (int i=0; i<12; i++) {
				Welt.NeueRegionen(1);
			}
		}
		try {
			if (Main.getBFlag("EVA")) EVAFastLoader.loadAll();
		} catch (SQLException ex) {
			new BigError(ex);
		}

        this.addTest(new SuiteBasisTests()); // enthält wiederum SuiteBewegungen
		this.addTest(new SuiteProduktion());
		this.addTest(new SuiteAllianzen());
        this.addTest(new SuiteAlleZauber());
        this.addTest(new SuiteKampf());
    }
}
