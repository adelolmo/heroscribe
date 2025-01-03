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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import org.lightless.heroscribe.gui.Gui;
import org.lightless.heroscribe.gui.ImageLoader;
import org.lightless.heroscribe.gui.ObjectsMediaLoader;
import org.lightless.heroscribe.gui.SplashScreenImageLoader;
import org.lightless.heroscribe.iconpack.IconPackService;
import org.lightless.heroscribe.iconpack.ZipExtractor;
import org.lightless.heroscribe.utils.OS;
import org.lightless.heroscribe.xml.ObjectList;
import org.lightless.heroscribe.xml.ObjectsParser;
import org.lightless.heroscribe.xml.Quest;
import org.lightless.heroscribe.xml.QuestParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static java.lang.String.format;

public class HeroScribe {

	private static final Logger log = LoggerFactory.getLogger(HeroScribe.class);

	public static void main(String[] args) throws Exception {
		log.info("Starting up {} {}", Constants.APPLICATION_NAME, Constants.VERSION);
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
		final Path objectXmlPath = getFilePath(basePath, "Objects.xml");
		final Path objectHtmlPath = getFilePath(basePath, "Objects.html");

		final ImageLoader imageLoader = new ImageLoader();
		final ObjectMapper xmlMapper = new XmlMapper()
				.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true)
				.enable(SerializationFeature.INDENT_OUTPUT);
		final ObjectsParser objectsParser = new ObjectsParser(basePath, preferences);
		final QuestParser questParser = new QuestParser(xmlMapper);

		final ObjectList objectList = objectsParser.parse(objectXmlPath.toFile());
		final Quest quest = new Quest(objectList.getBoard());
		final IconPackService iconPackService = new IconPackService(imageLoader,
				objectList,
				objectsParser,
				new ZipExtractor(),
				objectXmlPath);
		final ObjectsMediaLoader mediaLoader = new ObjectsMediaLoader(imageLoader);

		log.info("Objects read.");

		final SplashScreenImageLoader loader = new SplashScreenImageLoader(imageLoader);
		loader.run(() -> {
			try {
				mediaLoader.loadIcons(objectList);
				iconPackService.loadImportedIconPacks();
			} catch (IOException e) {
				log.error("Unable to start application.", e);
				JOptionPane.showMessageDialog(null,
						format("Unable to start application.\nError: %s\nRoot cause: %s",
								e.getMessage(), e.getCause().getMessage()),
						"Error",
						JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}
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
