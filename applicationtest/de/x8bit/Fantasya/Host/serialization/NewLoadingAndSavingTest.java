package de.x8bit.Fantasya.Host.serialization;

import de.x8bit.Fantasya.Host.serialization.db.Database;
import de.x8bit.Fantasya.Host.serialization.db.DatabaseAdapter;
import de.x8bit.Fantasya.Host.util.TestingDatabase;
import org.junit.Test;

/** Uses the new serialization code to load everything and write it out again.
 *
 * The goal is to get some handle on how fast it is compared to the old code.
 */

public class NewLoadingAndSavingTest {

	@Test
	public void loadAndSaveEverything() {
		de.x8bit.Fantasya.Host.Paket.Init();

		Database db = TestingDatabase.setUpDatabase();
		Serializer serializer = SerializerFactory.buildSerializer(new DatabaseAdapter(db));

		// load everything
		System.out.println("Loading everything");
		serializer.loadAll();

		// save everything again
//		System.out.println("Saving everything again.");
//		serializer.saveAll();
	}
}
