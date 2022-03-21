package org.lightless.heroscribe.helper;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceHelper {
	
	private static final Logger log = LoggerFactory.getLogger(ResourceHelper.class);

	private ResourceHelper() {
	}

	/**
	 * @param name
	 * @return InputStream
	 */
	public static InputStream getResourceAsStream(String name) {
		return Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
	}

	/**
	 * @param name
	 * @return URL
	 */
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
