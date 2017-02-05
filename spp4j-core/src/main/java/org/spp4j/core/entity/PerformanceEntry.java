package org.spp4j.core.entity;

public class PerformanceEntry {

	private long start;
	private int duration;
	private boolean success;
	private MethodDescriptor method;

	public PerformanceEntry(MethodDescriptor method, long start, int duration, boolean success) {
		if (method == null) {
			throw new IllegalArgumentException("method is null!");
		}
		this.method = method;
		this.start = start;
		this.duration = duration;
		this.success = success;
	}

	public long getStart() {
		return start;
	}

	public int getDuration() {
		return duration;
	}

	public MethodDescriptor getMethod() {
		return method;
	}

	public boolean isSuccess() {
		return success;
	}

	@Override
	public String toString() {
		return String.format("%s: %s]", method.toString(), duration);
	}

}
