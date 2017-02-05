package org.spp4j.core.worker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spp4j.core.entity.PerformanceEntry;

public class SampleWriter extends AbstractWriter {

	private static final Logger LOG = LoggerFactory.getLogger(SampleWriter.class);
	private final ConcurrentLinkedQueue<PerformanceEntry> queue;
	private boolean shutdown;

	public SampleWriter(ConcurrentLinkedQueue<PerformanceEntry> queue) {
		this.queue = queue;
	}

	public void run(Connection connection) {
		PreparedStatement insertStatement = null;
		try {
			connection.setAutoCommit(false);
			insertStatement = connection
					.prepareStatement("INSERT INTO SAMPLE (method, start, duration, success) VALUES (?, ?, ?, ?)");
			PerformanceEntry entry = queue.poll();
			boolean update = false;
			int counter = 0;
			while (entry != null && !shutdown) {
				if (entry.getMethod().getDbId() > -1) {
					createSampleEntry(insertStatement, entry);
					insertStatement.addBatch();
					update = true;
					counter++;
					if (counter >= 1000) {
						insertStatement.executeBatch();
						connection.commit();
						update = false;
						counter = 0;
					}
				} else {
					queue.add(entry);
				}
				entry = queue.poll();
			}
			if (update) {
				insertStatement.executeBatch();
				connection.commit();
			}
		} catch (SQLException e) {
			LOG.error(e.getMessage(), e);
			try {
				connection.rollback();
			} catch (SQLException e1) {
				LOG.error(e1.getMessage(), e1);
			}
		} finally {
			closeQuitely(insertStatement);
		}
	}

	private void createSampleEntry(PreparedStatement insertStatement, PerformanceEntry entry) throws SQLException {
		insertStatement.setLong(1, entry.getMethod().getDbId());
		insertStatement.setLong(2, entry.getStart());
		insertStatement.setInt(3, entry.getDuration());
		insertStatement.setInt(4, entry.isSuccess() ? 1 : 0);
	}

	public void setShutdown(boolean shutdown) {
		this.shutdown = shutdown;
	}

}
