package org.lightless.heroscribe;

public class HeroScribeException extends RuntimeException {

	public HeroScribeException(String message, Exception exception) {
		super(message, exception);
	}

	public HeroScribeException(Exception exception) {
		super(exception);
	}

	public HeroScribeException(String message) {
		super(message);
	}
}
