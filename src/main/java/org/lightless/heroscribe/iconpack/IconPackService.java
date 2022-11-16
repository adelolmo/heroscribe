/*
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

package org.lightless.heroscribe.iconpack;

import org.apache.commons.io.*;
import org.lightless.heroscribe.*;
import org.lightless.heroscribe.gui.*;
import org.lightless.heroscribe.xml.*;
import org.slf4j.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class IconPackService {

	private static final Logger log = LoggerFactory.getLogger(IconPackService.class);
	private static final Path TEMP_DIR = Paths.get(System.getProperty("java.io.tmpdir"), "heroscribe");

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

	public void importIconPack(final File iconPackFile) throws IOException {
		log.info("Importing icon pack {}...", iconPackFile.getName());
//		final Path tempBundleDirectory = Files.createTempDirectory("hse");
		final Path tempIconPackDirectory = getTempIconPackDirectory(iconPackFile);
		tempIconPackDirectory.toFile().mkdirs();
		zipExtractor.extract(iconPackFile, tempIconPackDirectory);

		final ObjectList iconPackObjectList =
				objectsParser.parse(new File(tempIconPackDirectory.toString(), "Objects.xml"));

		final List<ObjectList.Kind> iconPackKinds = getNewKindsFromIconPack(systemObjectList, iconPackObjectList);
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
					systemObjectList.getObjects().add(object);
				});

		systemObjectList.getKinds().addAll(iconPackKinds);

		imageLoader.flush();
	}

	public void removePack(File iconPackFile) throws IOException {
		final ObjectList iconPackObjectList =
				objectsParser.parse(
						new File(getTempIconPackDirectory(iconPackFile).toString(), "Objects.xml"));

		final List<String> iconPackKindIds =
				getNewKindsFromIconPack(
						objectsParser.parse(objectXmlPath.toFile()), iconPackObjectList)
						.stream()
						.map(ObjectList.Kind::getId)
						.collect(Collectors.toList());
		Arrays.stream(systemObjectList.getObjects().toArray(new ObjectList.Object[]{}))
//				.filter(Objects::nonNull)
				.filter(object -> iconPackKindIds.contains(object.getKind()))
				.forEach(object -> {
					log.info("<{}> <{}> Removing object '{}'...",
							iconPackFile.getName(), object.getKind(), object.getId());

					// remove icons
					imageLoader.removeImage(object.getIcon("Europe").getImage());
					imageLoader.removeImage(object.getIcon("USA").getImage());

					// update system objects
					systemObjectList.getObjects().remove(object);
				});

		FileUtils.deleteDirectory(getTempIconPackDirectory(iconPackFile).toFile());
		Files.delete(iconPackFile.toPath());

	}

	private Path getTempIconPackDirectory(File iconPackFile) {
		return Paths.get(TEMP_DIR.toString(),
				FilenameUtils.getBaseName(iconPackFile.getName()));
	}

	private List<ObjectList.Kind> getNewKindsFromIconPack(final ObjectList referenceObjectList,
														  final ObjectList iconPackObjectList) {
		return iconPackObjectList.getKinds().stream()
				.filter(kind -> !referenceObjectList.getKindIds().contains(kind.getId()))
				.collect(Collectors.toList());
	}

	private void loadObjectIcons(ObjectList.Object object, String region, Path bundleDirectory) {
		final String iconPath = object.getIconPath(region);
		final Path path = Paths.get(bundleDirectory.toString(),
				systemObjectList.getRasterPrefix(),
				iconPath + systemObjectList.getRasterSuffix());

		object.getIcon(region)
				.setImage(imageLoader.addImage(path.toString(), 20));
	}

}