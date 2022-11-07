package org.lightless.heroscribe.bundle;

import org.apache.commons.compress.archivers.*;
import org.apache.commons.io.*;
import org.lightless.heroscribe.gui.*;
import org.lightless.heroscribe.list.*;
import org.lightless.heroscribe.xml.*;
import org.slf4j.*;

import javax.xml.bind.*;
import java.io.*;
import java.nio.file.*;

public class Bundle {

	private static final Logger LOGGER = LoggerFactory.getLogger(Bundle.class);

	private final ImageLoader imageLoader;

	public Bundle(ImageLoader imageLoader) {
		this.imageLoader = imageLoader;
	}

	public void importBundle(File bundle, List objects) throws IOException {
		final Path tempBundleFile = Files.createTempDirectory("hse");
		extract(bundle, tempBundleFile);

		final ObjectsParser parser = new ObjectsParser();

		try {
			final ObjectList objectList = parser.parse(new File(tempBundleFile.toString(), "Objects.xml"));
			System.out.println(objectList);


		} catch (JAXBException e) {
			throw new IOException(e);
		}
	}

	private void extract(File zipFile, Path targetDir) throws IOException {
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