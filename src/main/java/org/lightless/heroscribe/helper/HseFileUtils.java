package org.lightless.heroscribe.helper;

import org.apache.commons.io.*;
import org.slf4j.*;

import java.io.*;
import java.net.*;

public class HseFileUtils {

	private static final Logger log = LoggerFactory.getLogger(HseFileUtils.class);

	public static void downloadToFile(String sourceUrl, File targetFile) throws IOException {
		log.info("Download {} to {}", sourceUrl, targetFile.getAbsolutePath());
//		try {
			final OutputStream os = new FileOutputStream(targetFile);
			final InputStream is = new URL(sourceUrl).openStream();

			IOUtils.copy(is, os);
//		} catch (IOException e) {
//			log.warn("Unable to download icon pack: {}  from: {}", targetFile.getName(), sourceUrl);
//		}
	}
}