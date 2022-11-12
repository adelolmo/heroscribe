package org.lightless.heroscribe.iconpack;

import org.apache.commons.compress.archivers.*;
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

	private final ImageLoader imageLoader;
	private final ObjectList systemObjectList;
	private final ObjectsParser objectsParser;

	public IconPackService(ImageLoader imageLoader,
						   ObjectList systemObjectList,
						   ObjectsParser objectsParser) {
		this.imageLoader = imageLoader;
		this.systemObjectList = systemObjectList;
		this.objectsParser = objectsParser;
	}

	public void loadImportedIconPacks() throws IOException {
		final String[] iconPackFilenames = Constants.getIconPackDirectory()
				.list((dir, name) -> name.endsWith(".zip"));
		if (iconPackFilenames == null) {
			return;
		}
		for (String iconPackFilename : iconPackFilenames) {
			importIconPack(new File(Constants.getIconPackDirectory(), iconPackFilename));
		}
	}

	public void importIconPack(final File iconPackFile) throws IOException {
		log.info("Importing icon pack {}...", iconPackFile.getName());
		final Path tempBundleDirectory = Files.createTempDirectory("hse");
		extract(iconPackFile, tempBundleDirectory);

		final ObjectList bundleObjectList =
				objectsParser.parse(new File(tempBundleDirectory.toString(), "Objects.xml"));

		final List<ObjectList.Kind> iconPackKinds = getNewKindsFromIconPack(bundleObjectList);
		iconPackKinds.forEach(kind -> log.info("<{}> Importing kind {}...", iconPackFile.getName(), kind.getId()));

		bundleObjectList.getObjects()
				.stream()
				.filter(object -> !systemObjectList.getKindIds().contains(object.getKind()))
				.forEach(object -> {
					log.info("<{}> <{}> Importing object '{}'...",
							iconPackFile.getName(), object.getKind(), object.getId());

					// add icons
					loadObjectIcons(object, "Europe", tempBundleDirectory);
					loadObjectIcons(object, "USA", tempBundleDirectory);

					// update system objects
					systemObjectList.getObjects().add(object);
				});

		systemObjectList.getKinds().addAll(iconPackKinds);

		imageLoader.flush();
	}

	private List<ObjectList.Kind> getNewKindsFromIconPack(ObjectList bundleObjectList) {
		return bundleObjectList.getKinds().stream()
				.filter(kind -> !systemObjectList.getKindIds().contains(kind.getId()))
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

	private void extract(final File zipFile, final Path targetDir) throws IOException {
		final Path zipFilePath = zipFile.toPath();
		final ArchiveStreamFactory archiveStreamFactory = new ArchiveStreamFactory();

		try (InputStream inputStream = Files.newInputStream(zipFilePath);
			 ArchiveInputStream archiveInputStream = archiveStreamFactory
					 .createArchiveInputStream(ArchiveStreamFactory.ZIP, inputStream)) {

			ArchiveEntry archiveEntry = null;
			while ((archiveEntry = archiveInputStream.getNextEntry()) != null) {
				Path path = Paths.get(targetDir.toString(), archiveEntry.getName());
				File file = path.toFile();
				if (archiveEntry.isDirectory()) {
					if (!file.isDirectory()) {
						file.mkdirs();
					}
				} else {
					File parent = file.getParentFile();
					if (!parent.isDirectory()) {
						parent.mkdirs();
					}
					try (OutputStream outputStream = Files.newOutputStream(path)) {
						IOUtils.copy(archiveInputStream, outputStream);
					}
				}
			}
		} catch (ArchiveException e) {
			throw new IOException(e);
		}
	}
}