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
class ExportQuestTest {

	private static final String LOREM_IPSUM = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.\n" +
			"Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.\n" +
			"Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
	private static final File GHOSTSCRIPT_BIN = new File("/usr/bin/gs");
	private static final String[] ABC = new String[]{"A", "B", "C"};
	private static final String[] ABC_LONG =
			new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I"};
	private static final Quest.Board.Object TREASURE_CHEST = new Quest.Board.Object() {{
		setId("TreasureChest");
		setLeft(2.0f);
		setTop(4.0f);
		setRotation(Rotation.DOWNWARD);
		setZorder(-5.0f);
	}};
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
	void shouldExportEpsEmptyQuest() throws Exception {
		ExportEPS.writeMultiPage(new File("/tmp/empty.eps"),
				createEmptyQuest(),
				objectList);
	}

	@Test
	void shouldExportPdfEmptyQuest() throws Exception {
		writePDF(createEmptyQuest(), new File("/tmp/empty.pdf"));
	}

	@Test
	void shouldExportEpsQuestWithSpeech() throws Exception {
		final Quest quest = createEmptyQuest();
		quest.setSpeech(LOREM_IPSUM);
		ExportEPS.writeMultiPage(new File("/tmp/empty.eps"),
				createEmptyQuest(),
				objectList);
	}

	@Test
	void shouldExportPDFQuestWithSpeech() throws Exception {
		final Quest quest = createEmptyQuest();
		quest.setSpeech(LOREM_IPSUM);
		quest.getBoards().get(0).getObjects().add(TREASURE_CHEST);
		writePDF(quest, new File("/tmp/empty.pdf"));
	}

	@Test
	void shouldExportEpsQuestWithSpeechAndFewNotes() throws Exception {
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
	void shouldExportPdfQuestWithSpeechAndFewNotes() throws Exception {
		final Quest quest = createEmptyQuest();
		quest.setSpeech(LOREM_IPSUM);
		quest.getBoards().get(0).getObjects().add(TREASURE_CHEST);
		for (String character : ABC) {
			quest.getNotes().add(character + " " + LOREM_IPSUM);
		}
		writePDF(quest, new File("/tmp/empty.pdf"));
	}

	@Test
	void shouldExportEpsQuestWithSpeechAndManyNotes() throws Exception {
		final Quest quest = createEmptyQuest();
		quest.setSpeech(LOREM_IPSUM);
		quest.getBoards().get(0).getObjects().add(TREASURE_CHEST);
		for (String character : ABC_LONG) {
			quest.getNotes().add(character + " " + LOREM_IPSUM);
		}
		ExportEPS.writeMultiPage(new File("/tmp/empty.eps"),
				createEmptyQuest(),
				objectList);
	}

	@Test
	void shouldExportPdfQuestWithSpeechAndManyNotes() throws Exception {
		final Quest quest = createEmptyQuest();
		quest.setSpeech(LOREM_IPSUM);
		quest.getBoards().get(0).getObjects().add(TREASURE_CHEST);
		for (String character : ABC_LONG) {
			quest.getNotes().add(character + " " + LOREM_IPSUM);
		}
		writePDF(quest, new File("/tmp/empty.pdf"));
	}

	private Quest createEmptyQuest() {
		final Quest quest = new Quest();
		quest.setName("Lorem ipsum dolor sit amet");
		quest.updateDimensions(objectList.getBoard().getWidth(), objectList.getBoard().getHeight());
		return quest;
	}

	private void writePDF(Quest quest, File output) throws Exception {
		ExportPDF.write(GHOSTSCRIPT_BIN,
				output,
				quest,
				objectList,
				PaperType.A4,
				true);
	}
}