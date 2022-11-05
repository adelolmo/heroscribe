package org.lightless.heroscribe;
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

import org.lightless.heroscribe.gui.*;
import org.lightless.heroscribe.helper.*;
import org.lightless.heroscribe.list.List;
import org.lightless.heroscribe.list.Read;
import org.lightless.heroscribe.quest.*;
import org.slf4j.*;

import javax.swing.*;
import java.nio.file.*;
import java.util.*;

public class HeroScribe {

	private static final Logger log = LoggerFactory.getLogger(HeroScribe.class);

	public static void main(String[] args) {
		log.info("Starting up HeroScribe {}", Constants.VERSION);

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

			/* Doing some MacOS X tweaks */
			if (OS.isMacOsX()) {
				System.setProperty("apple.laf.useScreenMenuBar", "true");
			}
		} catch (Exception e) {
			log.error("Error when defining look and feel", e);
		}

		final Preferences preferences = new Preferences(Constants.preferencesFile);

		final Path basePath = Path.of(Objects.toString(args[0],""));
		final Path objectPath = getPath(basePath, "Objects.xml");
		final List objects = new Read(basePath, objectPath.toFile()).getObjects();

		log.info("Objects read.");

		new SplashScreenImageLoader(objects);

		final Quest quest = new Quest(1, 1, objects.getBoard(), null);

		new Gui(preferences, objects, quest);

		log.info("GUI done.");
	}

	private static Path getPath(Path path, String filename) {
		if (path != null) {
			return Paths.get(path.toString(), filename);
		}
		return Path.of(filename);
	}

}
