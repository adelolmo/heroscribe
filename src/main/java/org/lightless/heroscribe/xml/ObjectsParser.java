/*
  HeroScribe Enhanced Skull
  Copyright (C) 2022 Andoni del Olmo

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

package org.lightless.heroscribe.xml;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import org.lightless.heroscribe.Preferences;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class ObjectsParser {

	private final Path basePath;
	private final Preferences preferences;

	public ObjectsParser(Path basePath, Preferences preferences) {
		this.basePath = basePath;
		this.preferences = preferences;
	}

	public ObjectList parse(File file) throws HeroScribeParseException {
		try {
			final ObjectList objectList = getObjectMapper(preferences.forceIconPackInstall)
					.readValue(file, ObjectList.class);
			objectList.setBasePath(basePath);
			return objectList;
		} catch (IOException e) {
			throw new HeroScribeParseException(
					String.format("Cannot parse objects xml '%s'", file.getAbsoluteFile()),e);
		}
	}

	private ObjectMapper getObjectMapper(boolean failOnUnknownProperties) {
		return new XmlMapper()
				.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true)
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, !failOnUnknownProperties)
				.enable(SerializationFeature.INDENT_OUTPUT);
	}
}