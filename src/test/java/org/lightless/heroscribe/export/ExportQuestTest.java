package org.lightless.heroscribe.export;


import com.fasterxml.jackson.dataformat.xml.*;
import org.junit.jupiter.api.*;
import org.lightless.heroscribe.helper.*;
import org.lightless.heroscribe.xml.*;

import java.io.*;
import java.nio.file.*;

/**
 * @author Andoni del Olmo
 * @since 14/11/2022
 */
class ExportEPSTest {

	private static final String LOREM_IPSUM = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
	private static final String[] ABC = new String[]{"A", "B", "C"};
	private static final String[] ABC_LONG =
			new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I"};
	private ObjectList objectList;


	@BeforeEach
	void setUp() throws IOException {
		final ObjectsParser parser = new ObjectsParser(new XmlMapper(), Path.of("."));
		final String currentPath = ResourceHelper.getResourceAsFile(".").getAbsolutePath();
		objectList = parser.parse(
				new File(currentPath.substring(0, currentPath.indexOf("/target")),
						"Objects.xml"));
	}

	@Test
	void shouldExportEmptyQuest() throws Exception {
		ExportEPS.writeMultiPage(new File("/tmp/empty.eps"), createEmptyQuest(), objectList);
	}

	@Test
	void shouldExportQuestWithSpeech() throws Exception {
		final Quest quest = createEmptyQuest();
		quest.setSpeech(LOREM_IPSUM);
		ExportEPS.writeMultiPage(new File("/tmp/empty.eps"),
				createEmptyQuest(),
				objectList);
	}

	@Test
	void shouldExportQuestWithSpeechAndFewNotes() throws Exception {
		final Quest quest = createEmptyQuest();
		quest.setSpeech(LOREM_IPSUM);
		for (String character : ABC) {
			quest.getNotes().add(character + " " + LOREM_IPSUM);
		}
		ExportEPS.writeMultiPage(new File("/tmp/empty.eps"),
				createEmptyQuest(),
				objectList);
	}

	@Test
	void shouldExportQuestWithSpeechAndManyNotes() throws Exception {
		final Quest quest = createEmptyQuest();
		quest.setSpeech(LOREM_IPSUM);
		for (String character : ABC_LONG) {
			quest.getNotes().add(character + " " + LOREM_IPSUM);
		}
		ExportEPS.writeMultiPage(new File("/tmp/empty.eps"),
				createEmptyQuest(),
				objectList);
	}

	private Quest createEmptyQuest() {
		final Quest quest = new Quest();
		quest.updateDimensions(objectList.getBoard().getWidth(), objectList.getBoard().getHeight());
		return quest;
	}
}