package org.spp4j.core.worker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spp4j.core.entity.MethodDescriptor;

public class MethodWriter extends AbstractWriter {

	private static final Logger LOG = LoggerFactory.getLogger(MethodWriter.class);
	private final BlockingQueue<MethodDescriptor> queue;

	public MethodWriter(BlockingQueue<MethodDescriptor> queue) {
		this.queue = queue;
	}

	public void run(Connection connection) {
		PreparedStatement searchStatement = null;
		PreparedStatement insertStatement = null;
		try {
			searchStatement = connection
					.prepareStatement("SELECT ID FROM METHOD WHERE package = ? AND class = ? and method = ?");
			insertStatement = connection
					.prepareStatement("INSERT INTO METHOD (package, class, method) VALUES (?, ?, ?)");
			MethodDescriptor entry = queue.take();
			while (entry != null) {
				if (!methodEntryExistsInDb(searchStatement, entry)) {
					if (createMethodEntryInDb(insertStatement, entry)) {
						methodEntryExistsInDb(searchStatement, entry);
					}
				}
				entry = queue.take();
			}
		} catch (InterruptedException e) {
			LOG.error(e.getMessage(), e);
		} catch (SQLException e) {
			LOG.error(e.getMessage(), e);
		} finally {
			closeQuitely(searchStatement);
			closeQuitely(insertStatement);
		}
	}

	private boolean methodEntryExistsInDb(PreparedStatement searchStatement, MethodDescriptor entry)
			throws SQLException {
		ResultSet resultSet = null;
		boolean ret = false;
		try {
			searchStatement.clearParameters();
			searchStatement.setString(1, entry.getPackageName());
			searchStatement.setString(2, entry.getClassName());
			searchStatement.setString(3, entry.getMethodSignature());
			resultSet = searchStatement.executeQuery();
			if (resultSet.next()) {
				Long id = resultSet.getLong(1);
				entry.setDbId(id);
				ret = true;
			}
		} finally {
			closeQuitely(resultSet);
		}
		return ret;
	}

	private boolean createMethodEntryInDb(PreparedStatement insertStatement, MethodDescriptor entry)
			throws SQLException {
		insertStatement.clearParameters();
		insertStatement.setString(1, entry.getPackageName());
		insertStatement.setString(2, entry.getClassName());
		insertStatement.setString(3, entry.getMethodSignature());
		int updated = insertStatement.executeUpdate();
		return updated == 1;
	}
}
