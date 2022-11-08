package org.lightless.heroscribe.xml;

import com.fasterxml.jackson.dataformat.xml.*;
import org.slf4j.*;

import java.io.*;

/**
 * @author Andoni del Olmo
 * @since 07/11/2022
 */
public class ObjectsParser {

	private static final Logger LOGGER = LoggerFactory.getLogger(ObjectsParser.class);

	public ObjectList parse(File file) throws IOException {
		XmlMapper xmlMapper = new XmlMapper();
		return xmlMapper.readValue(file, ObjectList.class);
	}
}