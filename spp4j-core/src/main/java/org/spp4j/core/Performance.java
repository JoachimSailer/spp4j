package org.spp4j.core;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Performance {
	
	Level value() default Level.GENERAL;

	public enum Level {
		OFF(0), GENERAL(1), DEBUG(2);
		
		private int value;
		
		private Level(int value) {
			this.value = value;
		}
		
		public int getValue() {
			return value;
		}
	}
}
