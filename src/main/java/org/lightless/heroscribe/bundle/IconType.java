package org.lightless.heroscribe.bundle;

public enum IconType {
	VECTOR("Vector"),
	RASTER("Raster"),
	SAMPLE("Name");

	private final String name;

	IconType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
