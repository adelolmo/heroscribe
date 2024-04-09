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

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

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

	public void saveToDisk(ObjectList objectList, Quest quest, File outputFile) throws IOException {
		final List<Kind> originalKinds = objectList.getKinds().stream()
				.filter(Kind::isOriginal)
				.collect(Collectors.toList());
		quest.setKinds(new HashSet<>(originalKinds));

		if (questContainsKindsFromIconPacks(quest.getBoards(), objectList.getKinds())) {
			for (Quest.Board board : quest.getBoards()) {
				for (Quest.Board.Object object : board.getObjects()) {
					if (object.getKind() != null && !object.getKind().isOriginal()) {
						quest.getKinds().add(object.getKind());
					}
				}
			}
		}

		quest.setFile(outputFile);
		xmlMapper.writeValue(outputFile, quest);
		quest.setModified(false);
	}

	private boolean questContainsKindsFromIconPacks(List<Quest.Board> questBoards, List<Kind> kinds) {
		for (Quest.Board questBoard : questBoards) {
			for (Quest.Board.Object object : questBoard.getObjects()) {
				if (object.getKind() != null) {
					if (!object.getKind().isOriginal()) {
						return true;
					}
				}
			}
		}
		return false;
	}
}