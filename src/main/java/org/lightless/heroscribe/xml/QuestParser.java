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