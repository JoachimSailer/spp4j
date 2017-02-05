package org.spp4j.core.entity;

import org.spp4j.core.Performance.Level;

public class MethodDescriptor {

	private long dbId = -1;
	private Level tracingLevel;
	private String packageName;
	private String className;
	private String methodSignature;

	public MethodDescriptor(String packageName, String className, String methodSignature, Level profileLevel) {
		if (packageName == null) {
			throw new IllegalArgumentException("packageName is null!");
		}
		if (className == null) {
			throw new IllegalArgumentException("className is null!");
		}
		if (methodSignature == null) {
			throw new IllegalArgumentException("methodSignature is null!");
		}
		if (profileLevel == null) {
			throw new IllegalArgumentException("profileLevel is null!");
		}
		this.packageName = packageName;
		this.className = className;
		this.methodSignature = methodSignature;
		this.tracingLevel = profileLevel;
	}

	public Level getTracingLevel() {
		return tracingLevel;
	}

	public String getPackageName() {
		return packageName;
	}

	public String getClassName() {
		return className;
	}

	public String getMethodSignature() {
		return methodSignature;
	}

	public long getDbId() {
		return dbId;
	}

	public void setDbId(long dbId) {
		this.dbId = dbId;
	}

	@Override
	public String toString() {
		String ret = null;
		if (packageName == null || packageName.length() == 0) {
			ret = String.format("%s.%s: level=%s", className, methodSignature, tracingLevel);
		} else {
			ret = String.format("%s.%s.%s: level=%s", packageName, className, methodSignature, tracingLevel);
		}
		return ret;
	}

}
