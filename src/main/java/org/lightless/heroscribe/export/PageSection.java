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

package org.lightless.heroscribe.export;

import java.util.concurrent.atomic.AtomicInteger;

public class PageSection {

	final private AtomicInteger sectionCount = new AtomicInteger(1);

	public void increase() {
		sectionCount.incrementAndGet();
	}

	public PagePosition pagePosition() {
		if ((sectionCount.get() % 2) == 0) {
			return PagePosition.BOTTOM;
		}
		return PagePosition.TOP;
	}

	public boolean isTopSection() {
		return PagePosition.TOP.equals(pagePosition());
	}

	public boolean isBottomSection() {
		return PagePosition.BOTTOM.equals(pagePosition());
	}

	public int count() {
		return sectionCount.get();
	}

	public void increase(int increase) {
		sectionCount.addAndGet(increase);
	}

	public enum PagePosition {
		TOP, BOTTOM
	}
}