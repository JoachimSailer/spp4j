package org.spp4j.core.config;

import org.spp4j.core.Performance.Level;

public class Configuration {

	private static Level profileLevel = Level.GENERAL;

	public static void setProfileLevel(Level level) {
		Configuration.profileLevel = level;
	}

	public static Level getProfileLevel() {
		return profileLevel;
	}
}
