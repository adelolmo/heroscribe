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

import com.fasterxml.jackson.databind.*;

import java.io.*;

public class QuestParser {
	private final ObjectMapper xmlMapper;

	public QuestParser(ObjectMapper xmlMapper) {
		this.xmlMapper = xmlMapper;
	}

	public Quest parse(File file, int boardWidth, int boardHeight) throws IOException {
		final Quest quest = xmlMapper.readValue(file, Quest.class);
		quest.setFile(file);
		quest.updateDimensions(boardWidth, boardHeight);
		return quest;
	}

	public void saveToDisk(Quest quest, File outputFile) throws IOException {
		xmlMapper.writeValue(outputFile, quest);
		quest.setModified(false);
	}
}