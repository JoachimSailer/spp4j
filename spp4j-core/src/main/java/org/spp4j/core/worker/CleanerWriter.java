package org.spp4j.core.worker;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.spp4j.core.db.DataSource;

public class CleanerWriter extends AbstractWriter {
	
	private int history = 3;

	@Override
	protected void run(Connection connection) {
		Statement statement = null;
		try {
			statement = connection.createStatement();
			statement.executeUpdate(String.format("DELETE FROM STAT WHERE timeslot < DATEADD( 'DAY', -%s, CURRENT_DATE)", history));
		} catch (SQLException e) {
			LOG.error(e.getMessage(), e);
		} finally {
			DataSource.closeQuitely(statement);
		}
	}

}
