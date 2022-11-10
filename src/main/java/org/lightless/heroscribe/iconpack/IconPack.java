package org.lightless.heroscribe.iconpack;

import org.apache.commons.compress.archivers.*;
import org.apache.commons.io.*;
import org.lightless.heroscribe.gui.*;
import org.lightless.heroscribe.xml.*;
import org.slf4j.*;

import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.stream.*;

public class IconPack {

	private static final Logger log = LoggerFactory.getLogger(IconPack.class);

	private final ImageLoader imageLoader;
	private final ObjectList systemObjectList;
	private final ObjectsParser objectsParser;

	public IconPack(ImageLoader imageLoader,
					ObjectList systemObjectList,
					ObjectsParser objectsParser) {
		this.imageLoader = imageLoader;
		this.systemObjectList = systemObjectList;
		this.objectsParser = objectsParser;
	}

	public void importBundle(final File bundle) throws IOException {
		log.info("Importing icon pack {}...", bundle.getName());
		final Path tempBundleDirectory = Files.createTempDirectory("hse");
		extract(bundle, tempBundleDirectory);

		final ObjectList bundleObjectList = objectsParser.parse(new File(tempBundleDirectory.toString(), "Objects.xml"));

		final List<ObjectList.Kind> bundleKinds = bundleObjectList.getKind().stream()
				.filter(kind -> !systemObjectList.getKindIds().contains(kind.getId())
				).collect(Collectors.toList());

		bundleKinds.forEach(kind -> log.info("Importing kind {}...", kind.getId()));

		bundleObjectList.getObject()
				.stream()
				.filter(object1 -> !systemObjectList.getKindIds().contains(object1.getKind()))
				.forEach(object -> {
					log.info("Importing object {}...", object.getId());

					// add icons
					loadObjectIcons(object, "Europe", tempBundleDirectory);
					loadObjectIcons(object, "USA", tempBundleDirectory);

					// update system objects
					systemObjectList.getObject().add(object);
				});

		systemObjectList.getKind().addAll(bundleKinds);

		imageLoader.flush();


	}

	private void loadObjectIcons(ObjectList.Object object, String region, Path bundleDirectory) {
		final String iconPath = object.getIconPath(region);
		final Path path = Paths.get(bundleDirectory.toString(),
				systemObjectList.getRasterPrefix(),
				iconPath + systemObjectList.getRasterSuffix());

		final Image image = imageLoader.addImage(path.toString(), 20);
		object.getIcon(region).setImage(image);
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