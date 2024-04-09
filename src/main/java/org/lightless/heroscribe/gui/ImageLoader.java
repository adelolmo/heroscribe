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

package org.lightless.heroscribe.gui;

import org.lightless.heroscribe.*;

import javax.swing.*;
import java.awt.*;

public class ImageLoader extends JWindow {

	private final MediaTracker mt;
	private final Toolkit tk;

	public ImageLoader() {
		super();
		mt = new MediaTracker(this);
		tk = Toolkit.getDefaultToolkit();
	}

	public Dimension getScreenSize() {
		return tk.getScreenSize();
	}

	public Image addImageAndFlush(String path, int id) {
		final Image image = tk.createImage(ClassLoader.getSystemResource(path));
		mt.addImage(image, id);

		try {
			mt.waitForID(id);
		} catch (InterruptedException e) {
			throw new HeroScribeException(e);
		}

		if (mt.isErrorID(id)) {
			throw new HeroScribeException("Can't load images.");
		}

		return image;
	}

	public Image addImage(String path, int id) {
		final Image image = tk.createImage(path);
		mt.addImage(image, id);
		return image;
	}

	public void removeImage(Image image) {
		mt.removeImage(image);
	}

	public void flush() {
		try {
			mt.waitForAll();
		} catch (InterruptedException e) {
			throw new HeroScribeException(e);
		}

		if (mt.isErrorAny()) {
			throw new HeroScribeException("Can't load all PNG icons.");
		}
	}

}