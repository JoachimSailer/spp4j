package org.spp4j.core.test;

import org.spp4j.core.Performance;
import org.spp4j.core.Performance.Level;

public class MeasuredClass1 {

	@Performance(Level.GENERAL)
	public boolean doSomething(String test, int a) {
		return true;
	}
}
