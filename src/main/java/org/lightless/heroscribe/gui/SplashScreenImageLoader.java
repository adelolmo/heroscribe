/*
  HeroScribe
  Copyright (C) 2002-2004 Flavio Chierichetti and Valerio Chierichetti

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

import javax.swing.*;
import java.awt.*;
import java.time.*;

public class SplashScreenImageLoader extends JWindow {

	private final MediaTracker mt;
	private final Image splash;
	private final int splashID = 1;

	public SplashScreenImageLoader(ImageLoader imageLoader) {
		super();

		splash = imageLoader.addImageAndFlush("Splash.jpg", splashID);
		mt = new MediaTracker(this);

		setSize(splash.getWidth(null), splash.getHeight(null));

		setLocation((imageLoader.getScreenSize().width - this.getWidth()) / 2,
				(imageLoader.getScreenSize().height - this.getHeight()) / 2);
	}

	public void visible() {
		setVisible(true);
	}

	public void invisible() {
		try {
			Thread.sleep(Duration.ofSeconds(2));
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		setVisible(false);
	}

	public void paint(Graphics g) {
		if (mt.checkID(splashID)) {
			g.drawImage(splash, 0, 0, this);
		}
	}
}