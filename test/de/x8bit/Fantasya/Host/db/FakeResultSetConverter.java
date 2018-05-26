package de.x8bit.Fantasya.Host.db;

import de.x8bit.Fantasya.Host.serialization.util.SerializedData;

import java.sql.ResultSet;

/** Stores the supplied result set, and returns a given SerializedData item for
 * testing. */

public class FakeResultSetConverter extends ResultSetConverter {

	public ResultSet resultSet;
	public SerializedData data;

	@Override
	public SerializedData convert(ResultSet resultSet) {
		if (this.resultSet != null) {
			throw new IllegalStateException("conversion was called multiple times.");
		}

		this.resultSet = resultSet;
		return data;
	}
}
