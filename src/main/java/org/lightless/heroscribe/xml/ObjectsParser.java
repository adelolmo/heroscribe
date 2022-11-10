/*
  HeroScribe
  Copyright (C) 2002-2004 Flavio Chierichetti and Valerio Chierichetti

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

import com.fasterxml.jackson.databind.*;

import java.io.*;
import java.nio.file.*;

public class ObjectsParser {

	private final ObjectMapper objectMapper;
	private final Path basePath;

	public ObjectsParser(ObjectMapper objectMapper, Path basePath) {
		this.objectMapper = objectMapper;
		this.basePath = basePath;
	}

	public ObjectList parse(File file) throws IOException {
		final ObjectList objectList = objectMapper.readValue(file, ObjectList.class);
		objectList.setBasePath(basePath);
		return objectList;
	}
}