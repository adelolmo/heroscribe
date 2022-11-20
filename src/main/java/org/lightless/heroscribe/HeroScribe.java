/*
  HeroScribe
  Copyright (C) 2002-2004 Flavio Chierichetti and Valerio Chierichetti

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

package org.lightless.heroscribe;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.xml.*;
import com.fasterxml.jackson.dataformat.xml.ser.*;
import org.lightless.heroscribe.gui.*;
import org.lightless.heroscribe.helper.*;
import org.lightless.heroscribe.iconpack.*;
import org.lightless.heroscribe.xml.*;
import org.slf4j.*;

import javax.swing.*;
import java.nio.file.*;
import java.util.*;

public class HeroScribe {

	private static final Logger log = LoggerFactory.getLogger(HeroScribe.class);

	public static void main(String[] args) throws Exception {
		log.info("Starting up HeroScribe Enhanced {}", Constants.VERSION);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

			/* Doing some MacOS X tweaks */
			if (OS.isMacOsX()) {
				System.setProperty("apple.laf.useScreenMenuBar", "true");
			}
		} catch (Exception e) {
			log.error("Error when defining look and feel", e);
		}

		final Preferences preferences = new Preferences(Constants.PREFERENCES_FILE);

		final Path basePath = getBasePath(args);
		final Path objectPath = getFilePath(basePath, "Objects.xml");
		final Path objectHtmlPath = Paths.get(basePath.toString(), "Objects.html");

		final ImageLoader imageLoader = new ImageLoader();
		final ObjectMapper xmlMapper = new XmlMapper()
				.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true)
				.enable(SerializationFeature.INDENT_OUTPUT);
		final ObjectsParser objectsParser = new ObjectsParser(xmlMapper, basePath);
		final QuestParser questParser = new QuestParser(xmlMapper);

		final ObjectList objectList = objectsParser.parse(objectPath.toFile());
		final Quest quest = new Quest(objectList.getBoard());
		final IconPackService iconPackService = new IconPackService(imageLoader,
				objectList,
				objectsParser,
				new ZipExtractor(),
				objectPath);
		final ObjectsMediaLoader mediaLoader = new ObjectsMediaLoader(imageLoader);

		log.info("Objects read.");

		final SplashScreenImageLoader loader = new SplashScreenImageLoader(imageLoader);
		loader.run(() -> {
			mediaLoader.loadIcons(objectList);
			iconPackService.loadImportedIconPacks();
			return null;
		});

		new Gui(imageLoader,
				iconPackService,
				preferences,
				objectList,
				questParser,
				quest,
				objectHtmlPath);

		log.info("GUI done.");
	}

	private static Path getFilePath(Path basePath, String filename) {
		if (basePath != null) {
			return Paths.get(basePath.toString(), filename);
		}
		return Path.of(filename);
	}

	private static Path getBasePath(String[] args) {
		if (args.length == 0) {
			return Paths.get("");
		}
		return Paths.get(Objects.toString(args[0], ""));
	}

}
