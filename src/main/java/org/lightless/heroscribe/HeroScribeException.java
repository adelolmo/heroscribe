package org.lightless.heroscribe;

public class HeroScribeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public HeroScribeException(Exception cause) {
		super(cause);
	}

	public HeroScribeException(String msg) {
		super(msg);
	}

}
