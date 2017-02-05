package org.spp4j.core.worker;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spp4j.core.db.DataSource;

public abstract class AbstractWriter implements Runnable {

	protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

	public final void run() {
		Connection connection = null;
		try {
			connection = DataSource.getInstance().getConnection();
			run(connection);
		} catch (SQLException e) {
			LOG.error(e.getMessage(), e);
		} finally {
			closeQuitely(connection);
		}
	}

	protected abstract void run(Connection connection);

	protected void closeQuitely(AutoCloseable closeable) {
		DataSource.closeQuitely(closeable);
	}
}
