package de.x8bit.Fantasya.Host.serialization.db;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** A class to simplify the mocking of databases.
 *
 * It gets a list of column names on creation, and provides the minimum
 * functionality to browse through these column names.
 */

public class FakeResultSetMetaData implements ResultSetMetaData {

	private List<String> columns;

	public FakeResultSetMetaData(List<String> columns) {
		if (columns == null) {
			throw new IllegalArgumentException("No columns given for faking.");
		}

		this.columns = new ArrayList<String>(columns);
	}

	@Override
	public int getColumnCount() throws SQLException {
		return columns.size();
	}

	@Override
	public String getColumnName(int column) throws SQLException {
		return columns.get(column-1);
	}

	@Override
	public boolean isAutoIncrement(int column) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isCaseSensitive(int column) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isSearchable(int column) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isCurrency(int column) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int isNullable(int column) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isSigned(int column) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int getColumnDisplaySize(int column) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String getColumnLabel(int column) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String getSchemaName(int column) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int getPrecision(int column) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int getScale(int column) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String getTableName(int column) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String getCatalogName(int column) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int getColumnType(int column) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String getColumnTypeName(int column) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isReadOnly(int column) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isWritable(int column) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isDefinitelyWritable(int column) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String getColumnClassName(int column) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}