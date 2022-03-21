package org.lightless.heroscribe.helper;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

public class ResourceHelper {

	private ResourceHelper() {}

	/**
	 * Retorna um {@link InputStream} com o recurso a ser utilizado no teste, relativo ao diretorio 'src/test/resources'
	 * 
	 * @param name
	 *            Nome do recurso
	 * @return InputStream
	 */
	public static InputStream getResourceAsStream(String name) {
		return Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
	}

	/**
	 * Retorna uma {@link URL} com o recurso a ser utilizado no teste, relativo ao diretorio 'src/test/resources'
	 * 
	 * @param name
	 *            Nome do recurso
	 * @return InputStream
	 * @throws ResourceInvalidoException
	 */
	public static URL getResourceUrl(String name) {
		URL url = ClassLoader.getSystemResource(name);
		if (url == null) {
			throw new RuntimeException(name);
		}
		return url;
	}

	public static File getResourceAsFile(String name) {
		return new File(getResourceUrl(name).getFile());
	}
}
