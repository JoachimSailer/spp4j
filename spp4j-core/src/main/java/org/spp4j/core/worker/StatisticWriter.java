package org.spp4j.core.worker;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.spp4j.core.db.DataSource;

public class StatisticWriter extends AbstractWriter {

	public void run(Connection connection) {
		Statement statement = null;
		try {
			connection.setAutoCommit(false);
			statement = connection.createStatement();
			statement
					.addBatch("INSERT INTO STAT (method, samplecount, minduration, maxduration, meanduration, success) "
							+ "SELECT method, COUNT(*), MIN(duration), MAX(duration), AVG(duration), success FROM SAMPLE GROUP BY method, success");
			statement.addBatch("DELETE FROM SAMPLE");
			statement.executeBatch();
			connection.commit();
		} catch (SQLException e) {
			LOG.error(e.getMessage(), e);
			try {
				connection.rollback();
			} catch (SQLException e1) {
				LOG.error(e1.getMessage(), e1);
			}
		} finally {
			DataSource.closeQuitely(statement);
		}
	}

}
