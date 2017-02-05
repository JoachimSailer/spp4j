package org.spp4j.core.test;

import org.spp4j.core.Performance;
import org.spp4j.core.Performance.Level;

public class MeasuredClass2 {

	@Performance(Level.GENERAL)
	public boolean doSomething(String[] test, double[] b) {
		return true;
	}
}
