package org.lightless.heroscribe.bundle;

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

public class Bundle {

	private static final Logger log = LoggerFactory.getLogger(Bundle.class);

	private final ImageLoader imageLoader;
	private final ObjectList systemObjectList;

	public Bundle(ImageLoader imageLoader, ObjectList systemObjectList) {
		this.imageLoader = imageLoader;
		this.systemObjectList = systemObjectList;
	}

	public void importBundle(final File bundle) throws IOException {
		log.info("Importing bundle {}...", bundle.getName());
		final Path tempBundleDirectory = Files.createTempDirectory("hse");
		extract(bundle, tempBundleDirectory);

		final ObjectsParser parser = new ObjectsParser();
		final ObjectList bundleObjectList = parser.parse(new File(tempBundleDirectory.toString(), "Objects.xml"));

		final List<ObjectList.Kind> bundleKinds = bundleObjectList.getKind().stream()
				.filter(kind -> !systemObjectList.getKindIds().contains(kind.getId())
				).collect(Collectors.toList());

		bundleKinds.forEach(kind -> log.info("Importing kind {}...", kind.getId()));

		bundleObjectList.getObject()
				.stream()
				.filter(object1 -> !systemObjectList.getKindIds().contains(object1.getKind()))
				.forEach(object -> {
					System.out.println(object.getId());

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