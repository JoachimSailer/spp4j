package org.spp4j.core.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spp4j.core.Performance.Level;
import org.spp4j.core.config.Configuration;

public class DataSource {

	private static final Logger LOG = LoggerFactory.getLogger(DataSource.class);
	private static DataSource INSTANCE = null;
	private BasicDataSource dbDataSource;

	private String driverClassName;
	private String username = "SA";
	private String password = "";
	private String url = "jdbc:hsqldb:mem:mymemdb";
	private int minIdle = 5;
	private int maxIdle = 20;
	private int maxOpenPreparedStatements = 180;
	private boolean init;

	private void init() {
		boolean hsqldb = driverClassName == null;
		dbDataSource = new BasicDataSource();
		if (!hsqldb) {
			dbDataSource.setDriverClassName(driverClassName);
		}
		dbDataSource.setUsername(username);
		dbDataSource.setPassword(password);
		dbDataSource.setUrl(url);
		dbDataSource.setMinIdle(minIdle);
		dbDataSource.setMaxIdle(maxIdle);
		dbDataSource.setMaxOpenPreparedStatements(maxOpenPreparedStatements);
		if (hsqldb) {
			initDB();
		}
		init = true;
	}

	private void initDB() {
		Connection connection = null;
		Statement createTable = null;
		try {
			connection = dbDataSource.getConnection();
			createTable = connection.createStatement();
			createTable.executeUpdate(
					"CREATE TABLE IF NOT EXISTS METHOD (ID INTEGER IDENTITY PRIMARY KEY, package VARCHAR(512), class VARCHAR(128), method VARCHAR(128));");
			createTable.executeUpdate(
					"CREATE TABLE IF NOT EXISTS SAMPLE (ID DECIMAL IDENTITY PRIMARY KEY, method INTEGER, start DECIMAL, duration INTEGER, success BIT, FOREIGN KEY (method) REFERENCES METHOD(ID));");
			createTable.executeUpdate(
					"CREATE TABLE IF NOT EXISTS STAT (ID DECIMAL IDENTITY PRIMARY KEY, method INTEGER, timeslot DATETIME DEFAULT NOW(), samplecount INTEGER, minduration INTEGER, maxduration INTEGER, meanduration DECIMAL, success BIT, FOREIGN KEY (method) REFERENCES METHOD(ID));");
		} catch (SQLException e) {
			LOG.error(String.format("Problems on connecting to HSQLDB caused deactivation of spp4j! Cause: %s",
					e.getMessage()));
			Configuration.setProfileLevel(Level.OFF);
		} finally {
			closeQuitely(createTable);
			closeQuitely(connection);
		}
	}

	public Connection getConnection() throws SQLException {
		if (!init) {
			init();
		}
		return dbDataSource.getConnection();
	}

	public static DataSource getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new DataSource();
		}
		return INSTANCE;
	}

	public static void closeQuitely(AutoCloseable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}
}
