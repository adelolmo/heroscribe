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

package org.lightless.heroscribe;

import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Constants {
	public static final String APPLICATION_NAME = "HeroScribe Enhanced Skull";
	public static final Path TEMP_DIR = Paths.get(System.getProperty("java.io.tmpdir"), "heroscribe");


	public static final String OBJECT_VERSION = "1.0";
	public static final String QUEST_VERSION = "1.0";
	public static final String VERSION;

	public static final String applicationVersionSuffix = "";

	public static final Color EUROPE_CORRIDOR_COLOR = new Color(255, 255, 255, 255);
	public static final Color USA_CORRIDOR_COLOR = new Color(246, 246, 246, 255);

	public static final Color EUROPE_DARK_COLOR = new Color(204, 204, 204, 255);
	public static final Color USA_DARK_COLOR = new Color(178, 178, 178, 255);

	public static final Color EUROPE_TRAP_COLOR = new Color(0, 0, 0, 0);
	public static final Color USA_TRAP_COLOR = new Color(250, 125, 51, 255);

	public static final File PREFERENCES_FILE;

	static {
		final String appVersion = ResourceBundle
				.getBundle("version", Locale.ENGLISH)
				.getString("app.version");
		VERSION = "v" + ("${project.version}".equals(appVersion) ? "" : appVersion);

		final File prefDir = getConfigurationDirectory();
		PREFERENCES_FILE = new File(prefDir, "Preferences.xml");
	}

	public static File getConfigurationDirectory() {
		final String home = System.getProperty("user.home");
		final File prefDir = new File(home, ".heroscribe");
		if (!prefDir.exists()) {
			prefDir.mkdir();
		}
		return prefDir;
	}

	public static File getIconPackDirectory() {
		final File bundleDir = new File(getConfigurationDirectory(), "iconPacks");
		if (!bundleDir.exists()) {
			bundleDir.mkdir();
		}
		return bundleDir;
	}
}
