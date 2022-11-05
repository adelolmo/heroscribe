package org.lightless.heroscribe.helper;

import org.slf4j.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;

public class ResourceHelper {

	private static final Logger log = LoggerFactory.getLogger(ResourceHelper.class);

	private ResourceHelper() {
	}

	public static InputStream getResourceAsStream(Path path) {
		return getResourceAsStream(path.toString());
	}

	public static InputStream getResourceAsStream(String name) {
		return Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
	}

	public static URL getResourceUrl(String name) {
		return ClassLoader.getSystemResource(name);
	}

	public static File getResourceAsFile(String name) {
		URL resourceUrl = getResourceUrl(name);
		log.info(resourceUrl.toString());
		String file = resourceUrl.getFile();
		log.info(file);
		return new File(file);
	}
}
