package org.spp4j.core.test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.spp4j.core.db.DataSource;

public class SimpleTest {

	private static final DataSource datasource = DataSource.getInstance();

	@After
	public void afterTest() throws SQLException {
		Runtime runtime = Runtime.getRuntime();
		runtime.gc();
		long usedMem = (int) ((runtime.totalMemory() - runtime.freeMemory()) / 1024);
		System.out.println(String.format("Used memory: %s kb", usedMem));
		Connection connection = datasource.getConnection();
		Statement statement = connection.createStatement();
		int size = statement.executeUpdate("DELETE FROM SAMPLE");
		System.out.println(String.format("Deleted %s entries", size));
		DataSource.closeQuitely(statement);
		DataSource.closeQuitely(connection);
	}

	private int searchEntries() throws SQLException {
		int ret = 0;
		ResultSet resultSet = null;
		Connection connection = datasource.getConnection();
		try {
			Statement statement = connection.createStatement();
			resultSet = statement.executeQuery("SELECT * FROM SAMPLE");
			while (resultSet.next()) {
				ret++;
			}
		} finally {
			DataSource.closeQuitely(resultSet);
			DataSource.closeQuitely(connection);
		}
		return ret;
	}

	@Test
	public void simpleTest() throws SQLException, InterruptedException {
		MeasuredClass1 test = new MeasuredClass1();
		boolean result = test.doSomething("test", 1);
		Assert.assertEquals(true, result);
		Thread.sleep(1000);
		int size = searchEntries();
		Assert.assertEquals(1, size);
	}

	@Test
	public void simpleTest2() throws SQLException, InterruptedException {
		MeasuredClass2 test = new MeasuredClass2();
		boolean result = test.doSomething(new String[] { "test1", "test2" }, new double[] { 2.0 });
		Assert.assertEquals(true, result);
		Thread.sleep(1000);
		int size = searchEntries();
		Assert.assertEquals(1, size);
	}

	@Test
	public void multiTest() throws SQLException, InterruptedException {
		MeasuredClass1 test = new MeasuredClass1();
		for (int i = 0; i < 1000; i++) {
			boolean result = test.doSomething(String.format("test-%s", i), i);
			Assert.assertEquals(true, result);
		}
		Thread.sleep(1000);
		int size = searchEntries();
		Assert.assertEquals(1000, size);
	}

	@Test
	public void multiTest2() throws SQLException, InterruptedException {
		MeasuredClass1 test = new MeasuredClass1();
		MeasuredClass2 test2 = new MeasuredClass2();
		for (int i = 0; i < 1000; i++) {
			boolean result = test.doSomething(String.format("test-%s", i), i);
			Assert.assertEquals(true, result);
			result = test2.doSomething(new String[] { "test1", "test2" }, new double[] { 2.0 });
			Assert.assertEquals(true, result);
		}
		Thread.sleep(1000);
		int size = searchEntries();
		Assert.assertEquals(2000, size);
	}

	@Test
	public void multithreadingTest() throws SQLException, InterruptedException {
		int threadCount = 100;
		ExecutorService threadPool = Executors.newFixedThreadPool(threadCount);
		for (int i = 0; i < threadCount; i++) {
			threadPool.submit(new Requester(i));
		}
		threadPool.awaitTermination(1, TimeUnit.MINUTES);
		Thread.sleep(1000);
		int size = searchEntries();
		Assert.assertEquals(100000, size);
	}

	private class Requester implements Runnable {

		private int threadNumber;

		public Requester(int threadNumber) {
			this.threadNumber = threadNumber;
		}

		public void run() {
			try {
				MeasuredClass1 test = new MeasuredClass1();
				for (int i = 0; i < 1000; i++) {
					boolean result = test.doSomething(String.format("test-%s-%s", threadNumber, i), i);
					Assert.assertEquals(true, result);
				}
			} catch (Exception e) {
				e.printStackTrace();
				Assert.fail();
			}
		}

	}
}
