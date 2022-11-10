package org.lightless.heroscribe.xml;

import com.fasterxml.jackson.annotation.*;

public enum Rotation {
	@JsonProperty("downward")
	DOWNWARD("downward", 0),
	@JsonProperty("rightward")
	RIGHTWARD("rightward", 1),
	@JsonProperty("upward")
	UPWARD("upward", 2),
	@JsonProperty("leftward")
	LEFTWARD("leftward", 3);
	private final String name;
	private final int number;

	Rotation(String name, int number) {
		this.name = name;
		this.number = number;
	}

	public static Rotation fromNumber(int number) {
		for (Rotation rotation : Rotation.values()) {
			if (number == rotation.getNumber()){
				return rotation;
			}
		}
		throw new IllegalStateException("Rotation number not supported: " + number);
	}

	public String getName() {
		return name;
	}

	public int getNumber() {
		return number;
	}
}
