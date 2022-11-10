package org.lightless.heroscribe.iconpack;

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
