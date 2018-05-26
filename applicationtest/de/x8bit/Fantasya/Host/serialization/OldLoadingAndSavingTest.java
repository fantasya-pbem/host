package de.x8bit.Fantasya.Host.serialization;

import de.x8bit.Fantasya.Host.EVA.util.EVAFastLoader;
import de.x8bit.Fantasya.Host.util.TestingDatabase;
import org.junit.Test;

/** This test uses the old serialization code to load all data and write it out
 *  again.
 *
 * Quantities of interest are things like memory consumption and run time.
 */

public class OldLoadingAndSavingTest {

	@Test
	public void loadAndSaveEverything() throws Exception {
		TestingDatabase.setUpOldDatabase();
		de.x8bit.Fantasya.Host.Paket.Init();

		System.out.println("Loading all data.");
		EVAFastLoader.loadAll();

//		System.out.println("Saving all data again.");
//		EVAFastSaver.saveAll(true);
	}
}
