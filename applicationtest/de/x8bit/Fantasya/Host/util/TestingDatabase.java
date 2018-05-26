package de.x8bit.Fantasya.Host.util;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import de.x8bit.Fantasya.Host.Datenbank;
import de.x8bit.Fantasya.Host.serialization.db.Database;

/** A helper class to setup a database object from some global properties set in the build file. */

public class TestingDatabase {

	public static Database setUpDatabase() {
		String url = System.getProperty("fantasya.db.url");
		String user = System.getProperty("fantasya.db.user");
		String password = System.getProperty("fantasya.db.password");

		if (url == null || user == null || password == null) {
			throw new RuntimeException("Database properties were not set.");
		}

		MysqlDataSource source = new MysqlDataSource();
		source.setURL(url);

		return new Database(source, user, password);
	}

	public static void setUpOldDatabase() {
		Datenbank.SetServer(System.getProperty("fantasya.db.server"));
		Datenbank.SetDatenbank(System.getProperty("fantasya.db.datenbank"));
		Datenbank.SetBenutzer(System.getProperty("fantasya.db.user"));
		Datenbank.SetPasswort(System.getProperty("fantasya.db.password"));
	}
}
