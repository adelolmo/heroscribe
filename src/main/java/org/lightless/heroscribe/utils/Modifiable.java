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

package org.lightless.heroscribe.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Modifiable<T extends Enum<?>> {

	private static final Logger logger = LoggerFactory.getLogger(Modifiable.class);
	private final List<ModificationListener<T>> listeners = new ArrayList<>();

	public void addModificationListener(ModificationListener<T> listener) {
		listeners.add(listener);
	}

	public void notifyMutation(T type) {
		for (ModificationListener<T> listener : listeners) {
			try {
				listener.onChange(type);
			} catch (Exception e) {
				logger.error("Unable to notify change", e);
			}
		}
	}

}