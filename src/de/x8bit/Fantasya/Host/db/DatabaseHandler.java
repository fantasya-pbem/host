package de.x8bit.Fantasya.Host.db;

import de.x8bit.Fantasya.Host.serialization.complex.ComplexHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class DatabaseHandler {

	private ComplexHandler handler;
	private String table;
	private ResultSetConverter converter;

	public DatabaseHandler(ComplexHandler handler, String table, ResultSetConverter converter) {
		if (handler == null) {
			throw new IllegalArgumentException("Object setup handler must not be null.");
		}
		if (table == null || table.isEmpty()) {
			throw new IllegalArgumentException("Need a table to operate on.");
		}
		if (converter == null) {
			throw new IllegalArgumentException("Need a converter for result sets.");
		}

		this.handler = handler;
		this.table = table;
		this.converter = converter;
	}

	public void loadFromDB(DatabaseReader reader) throws SQLException {
		ResultSet rs = reader.selectAll(table);
		handler.loadAll(converter.convert(rs));
		reader.cleanup();
	}

	public void saveToDB(DatabaseWriter writer) throws SQLException {
		writer.truncate(table);
		for (Map<String,String> dataItem : handler.saveAll()) {
			writer.insert(table, dataItem);
		}
		writer.update();
	}
}
