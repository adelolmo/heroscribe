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
package org.lightless.heroscribe.export;

import com.fasterxml.jackson.dataformat.xml.*;
import org.junit.jupiter.api.*;
import org.lightless.heroscribe.helper.*;
import org.lightless.heroscribe.xml.*;

import java.io.*;
import java.nio.file.*;

class ExportQuestTest {

	private static final String LOREM_IPSUM =
			"Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.\n" +
			"Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.\n" +
			"Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
	private static final File GHOSTSCRIPT_BIN = new File("/usr/bin/gs");
	private static final String[] ABC = new String[]{"A", "B", "C"};
	private static final String[] ABCDEFGHI =
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
		ExportEPS.writeMultiPage(PaperType.A4, new File("/tmp/empty.eps"),
				createEmptyQuest(),
				objectList);
	}

	@Test
	void shouldExportPdfEmptyQuest() throws Exception {
		writePDF(PaperType.A4, createEmptyQuest(), new File("/tmp/empty.pdf"));
	}

	@Test
	void shouldExportEpsQuestWithSpeech() throws Exception {
		final Quest quest = createEmptyQuest();
		quest.setSpeech(LOREM_IPSUM);
		ExportEPS.writeMultiPage(PaperType.A4, new File("/tmp/empty.eps"),
				createEmptyQuest(),
				objectList);
	}

	@Test
	void shouldExportPDFQuestWithSpeech() throws Exception {
		final Quest quest = createEmptyQuest();
		quest.setSpeech(LOREM_IPSUM);
		quest.getBoards().get(0).getObjects().add(TREASURE_CHEST);
		writePDF(PaperType.A4, quest, new File("/tmp/empty.pdf"));
	}

	@Test
	void shouldExportEpsQuestWithSpeechAndFewNotes() throws Exception {
		final Quest quest = createEmptyQuest();
		quest.setSpeech(LOREM_IPSUM);
		for (String character : ABC) {
			quest.getNotes().add(character + " " + LOREM_IPSUM);
		}
		ExportEPS.writeMultiPage(PaperType.A4, new File("/tmp/empty.eps"),
				createEmptyQuest(),
				objectList);
	}

	@Test
	void shouldExportA4PdfQuestWithSpeechAndFewNotes() throws Exception {
		final Quest quest = createEmptyQuest();
		quest.setSpeech(LOREM_IPSUM);
		quest.getBoards().get(0).getObjects().add(TREASURE_CHEST);
		for (String character : ABC) {
			quest.getNotes().add(character + " " + LOREM_IPSUM);
		}
		writePDF(PaperType.A4, quest, new File("/tmp/empty.pdf"));
	}

	@Test
	void shouldExportLetterPdfQuestWithSpeechAndFewNotes() throws Exception {
		final Quest quest = createEmptyQuest();
		quest.setSpeech(LOREM_IPSUM);
		quest.getBoards().get(0).getObjects().add(TREASURE_CHEST);
		for (String character : ABC) {
			quest.getNotes().add(character + " " + LOREM_IPSUM);
		}
		writePDF(PaperType.LETTER, quest, new File("/tmp/empty.pdf"));
	}

	@Test
	void shouldExportEpsQuestWithSpeechAndManyNotes() throws Exception {
		final Quest quest = createEmptyQuest();
		quest.setSpeech(LOREM_IPSUM);
		quest.getBoards().get(0).getObjects().add(TREASURE_CHEST);
		for (String character : ABCDEFGHI) {
			quest.getNotes().add(character + " " + LOREM_IPSUM);
		}
		ExportEPS.writeMultiPage(PaperType.A4, new File("/tmp/empty.eps"),
				createEmptyQuest(),
				objectList);
	}

	@Test
	void shouldExportPdfQuestWithSpeechAndManyNotes() throws Exception {
		final Quest quest = createEmptyQuest();
		quest.setSpeech(LOREM_IPSUM);
		quest.getBoards().get(0).getObjects().add(TREASURE_CHEST);
		for (String character : ABCDEFGHI) {
			quest.getNotes().add(character + " " + LOREM_IPSUM);
		}
		writePDF(PaperType.A4, quest, new File("/tmp/empty.pdf"));
	}

	private Quest createEmptyQuest() {
		final Quest quest = new Quest();
		quest.setName("Lorem ipsum dolor sit amet");
		quest.updateDimensions(objectList.getBoard().getWidth(), objectList.getBoard().getHeight());
		return quest;
	}

	private void writePDF(PaperType paperType, Quest quest, File output) throws Exception {
		ExportPDF.write(GHOSTSCRIPT_BIN,
				output,
				quest,
				objectList,
				paperType,
				true);
	}
}