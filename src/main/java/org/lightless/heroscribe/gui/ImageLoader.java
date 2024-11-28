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

import org.lightless.heroscribe.HeroScribeException;

import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ImageLoader extends JWindow {

	private final MediaTracker mt;
	private final Toolkit tk;
	private final ImageCache imageTracker = new ImageCache();

	public ImageLoader() {
		super();
		mt = new MediaTracker(this);
		tk = Toolkit.getDefaultToolkit();
	}

	public Dimension getScreenSize() {
		return tk.getScreenSize();
	}

	public Image addImageAndFlush(String path) {
		final Image image = tk.createImage(ClassLoader.getSystemResource(path));
		final int imageId = imageTracker.addResource(path);
		mt.addImage(image, imageId);

		try {
			mt.waitForID(imageId);
		} catch (InterruptedException e) {
			throw new HeroScribeException(e);
		}

		if (mt.isErrorID(imageId)) {
			throw new HeroScribeException("Can't load images.");
		}

		return image;
	}

	public ImageResource addImage(Path path) {
		try {
			final int imageId = imageTracker.addResource(path);
			final Image image = tk.createImage(path.toUri().toURL());
			mt.addImage(image, imageId);
			return new ImageResource(image, imageId);
		} catch (MalformedURLException e) {
			throw new HeroScribeException("Can't load image icon '%s'");
		}
	}

	public void removeImage(ImageResource image) {
		imageTracker.removeResource(image);
		mt.removeImage(image.data());
	}

	public void flush() {
		try {
			mt.waitForAll();
		} catch (InterruptedException e) {
			throw new HeroScribeException(e);
		}

		for (int i = 0; i < imageTracker.size(); i++) {
			if (mt.isErrorID(i)) {
				final Path resource = imageTracker.getResource(i);
				throw new HeroScribeException(String.format("Can't load image icon '%s'", resource));
			}
		}
	}

	private static class ImageCache {
		private final AtomicInteger imageCounter = new AtomicInteger(0);
		private final Map<Integer, Path> cache = new HashMap<>();

		public int addResource(String path) {
			final int id = imageCounter.getAndIncrement();
			cache.put(id, Path.of(path));
			return id;
		}

		public int addResource(Path path) {
			final int id = imageCounter.getAndIncrement();
			cache.put(id, path);
			return id;
		}

		public Path getResource(int id) {
			return cache.get(id);
		}

		public int size() {
			return cache.size();
		}

		public void removeResource(ImageResource imageResource) {
			cache.remove(imageResource.id());
		}
	}
}