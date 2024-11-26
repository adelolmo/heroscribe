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
package org.lightless.heroscribe.iconpack;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.lightless.heroscribe.Constants;
import org.lightless.heroscribe.gui.ImageLoader;
import org.lightless.heroscribe.xml.Kind;
import org.lightless.heroscribe.xml.ObjectList;
import org.lightless.heroscribe.xml.ObjectsParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.lightless.heroscribe.Constants.TEMP_DIR;

public class IconPackService {

	private static final Logger log = LoggerFactory.getLogger(IconPackService.class);

	private final ImageLoader imageLoader;
	private final ObjectList systemObjectList;

	private final Path objectXmlPath;
	private final ObjectsParser objectsParser;
	private final ZipExtractor zipExtractor;

	public IconPackService(ImageLoader imageLoader,
						   ObjectList systemObjectList,
						   ObjectsParser objectsParser,
						   ZipExtractor zipExtractor,
						   Path objectXmlPath) {
		this.imageLoader = imageLoader;
		this.systemObjectList = systemObjectList;
		this.objectsParser = objectsParser;
		this.zipExtractor = zipExtractor;
		this.objectXmlPath = objectXmlPath;
	}

	public void loadImportedIconPacks() throws IOException {
		for (File iconPackFile : getInstalledIconPacks()) {
			importIconPack(iconPackFile);
		}
	}

	public List<File> getInstalledIconPacks() {
		final String[] iconPackFilenames =
				Constants.getIconPackDirectory()
						.list((dir, name) -> name.endsWith(".zip"));
		if (iconPackFilenames == null) {
			return Collections.emptyList();
		}
		return Arrays.stream(iconPackFilenames)
				.map(filename -> new File(Constants.getIconPackDirectory(), filename))
				.collect(Collectors.toList());
	}

	public List<IconPack> getInstalledIconPackDetails() {
		try {
			final List<File> installedIconPacks = getInstalledIconPacks();
			final ObjectList originalObjectList = objectsParser.parse(objectXmlPath.toFile());
			return installedIconPacks.stream()
					.map(file -> {
						try {
							final Path iconPackDirectory = getTempIconPackDirectory(file);
							final File objectsXmlFile = new File(iconPackDirectory.toFile(), "Objects.xml");
							final ObjectList objectList = objectsParser.parse(objectsXmlFile);
							final List<Kind> iconPackKinds = getNewKindsFromIconPack(originalObjectList, objectList);

							return new IconPack(file,
									iconPackKinds);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}).collect(Collectors.toList());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void importIconPack(final File iconPackFile) throws IOException {
		log.info("Importing icon pack {}...", iconPackFile.getName());

		final Path tempIconPackDirectory = getTempIconPackDirectory(iconPackFile);
		tempIconPackDirectory.toFile().mkdirs();
		zipExtractor.extract(iconPackFile, tempIconPackDirectory);

		final ObjectList iconPackObjectList =
				objectsParser.parse(new File(tempIconPackDirectory.toString(), "Objects.xml"));

		final List<Kind> iconPackKinds = getNewKindsFromIconPack(systemObjectList, iconPackObjectList);
		iconPackKinds.forEach(kind -> log.info("<{}> Importing kind {}...", iconPackFile.getName(), kind.getId()));

		iconPackObjectList.getObjects()
				.stream()
				.filter(object -> !systemObjectList.getKindIds().contains(object.getKind()))
				.forEach(object -> {
					log.info("<{}> <{}> Importing object '{}'...",
							iconPackFile.getName(), object.getKind(), object.getId());

					// add icons
					loadObjectIcons(object, "Europe", tempIconPackDirectory);
					loadObjectIcons(object, "USA", tempIconPackDirectory);

					// update system objects
					systemObjectList.addObject(object);
				});

		iconPackKinds.forEach(systemObjectList::addKind);
		imageLoader.flush();
	}

	public void removePack(File iconPackFile) throws IOException {
		final ObjectList iconPackObjectList =
				objectsParser.parse(
						new File(getTempIconPackDirectory(iconPackFile).toString(), "Objects.xml"));

		final List<Kind> kinds = getNewKindsFromIconPack(
				objectsParser.parse(objectXmlPath.toFile()), iconPackObjectList);
		final List<String> iconPackKindIds = kinds
				.stream()
				.map(Kind::getId)
				.collect(Collectors.toList());
		Arrays.stream(systemObjectList.getObjects().toArray(new ObjectList.Object[]{}))
				.filter(object -> iconPackKindIds.contains(object.getKind()))
				.forEach(object -> {
					log.info("<{}> <{}> Removing object '{}'...",
							iconPackFile.getName(), object.getKind(), object.getId());

					// remove icons
					imageLoader.removeImage(object.getIcon("Europe").getImage());
					imageLoader.removeImage(object.getIcon("USA").getImage());

					// update system objects
					systemObjectList.removeObject(object);
				});

		kinds.forEach(systemObjectList::removeKind);
		FileUtils.deleteDirectory(getTempIconPackDirectory(iconPackFile).toFile());
		Files.delete(iconPackFile.toPath());
	}

	private Path getTempIconPackDirectory(File iconPackFile) {
		return Paths.get(TEMP_DIR.toString(),
				FilenameUtils.getBaseName(iconPackFile.getName()));
	}

	private List<Kind> getNewKindsFromIconPack(final ObjectList referenceObjectList,
											   final ObjectList iconPackObjectList) {
		return iconPackObjectList.getKinds().stream()
				.filter(kind -> !referenceObjectList.getKindIds().contains(kind.getId()))
				.peek(kind -> kind.setOriginal(false))
				.collect(Collectors.toList());
	}

	private void loadObjectIcons(ObjectList.Object object, String region, Path bundleDirectory) {
		final String iconPath = object.getIconPath(region);
		final Path path = Paths.get(bundleDirectory.toString(),
				systemObjectList.getRasterPrefix(),
				iconPath + systemObjectList.getRasterSuffix());

		object.getIcon(region)
				.setImage(imageLoader.addImage(path.toString()));
	}

	public static class IconPack {
		private final File zipFile;
		private final List<Kind> kinds;

		public IconPack(File zipFile, List<Kind> kinds) {
			this.zipFile = zipFile;
			this.kinds = kinds;
		}

		public File getZipFile() {
			return zipFile;
		}

		public List<Kind> getKinds() {
			return kinds;
		}

		public String getKindNames() {
			return kinds.stream()
					.map(Kind::getName)
					.collect(Collectors.joining(" & "));
		}
	}
}