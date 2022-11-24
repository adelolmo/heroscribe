/*
  HeroScribe Enhanced Skull
  Copyright (C) 2022 Andoni del Olmo

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License version 2 (not
  later versions) as published by the Free Software Foundation.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

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

	public boolean isPair() {
		return number % 2 == 0;
	}

	public boolean isOdd() {
		return number % 2 == 1;
	}
}
