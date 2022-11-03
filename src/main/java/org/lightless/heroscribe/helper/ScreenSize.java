/*
  HeroScribe
  Copyright (C) 2002-2004 Flavio Chierichetti and Valerio Chierichetti

  HeroScribe Enhanced (changes are prefixed with HSE in comments)
  Copyright (C) 2011 Jason Allen

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

package org.lightless.heroscribe.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;


public class ScreenSize {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScreenSize.class);

	public static int getWidth() {
		final GraphicsConfiguration graphicsConfiguration = getGraphicsConfiguration();
		final Insets ins = Toolkit.getDefaultToolkit().getScreenInsets(graphicsConfiguration);
		return graphicsConfiguration.getBounds().width - ins.left - ins.right;
	}

	public static int getHeight() {
		final GraphicsConfiguration graphicsConfiguration = getGraphicsConfiguration();
		final Insets ins = Toolkit.getDefaultToolkit().getScreenInsets(graphicsConfiguration);
		return graphicsConfiguration.getBounds().height - ins.top - ins.bottom;
	}

	private static GraphicsConfiguration getGraphicsConfiguration() {
		final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		final GraphicsDevice gd = ge.getDefaultScreenDevice();
		return gd.getDefaultConfiguration();
	}
}