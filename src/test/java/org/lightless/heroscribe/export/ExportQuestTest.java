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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lightless.heroscribe.Preferences;
import org.lightless.heroscribe.xml.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.lightless.heroscribe.ResourceUtils.getResourceAsFile;

class ExportQuestTest {

	private static final Path TMP_DIR =
			Paths.get(System.getProperty("java.io.tmpdir"), "heroscribe-test");

	private static final String SHORT_LOREM_IPSUM =
			"Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.";
	private static final String LOREM_IPSUM =
			"Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.\n" +
					"Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.\n" +
					"Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
	private static final File GHOSTSCRIPT_BIN = new File("/usr/bin/gs");
	private static final String[] ABC = new String[]{"A", "B", "C"};
	private static final String[] ABCDEFGHI =
			new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I"};
	private static final String[] ABCDEFGHIJKLMNOPQRSTUVWXYZ =
			new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
	private static final List<String> ABCDEFGHIJKLMNOPQRSTUVWXYZ_x2 = Stream.concat(
					Arrays.stream(ABCDEFGHIJKLMNOPQRSTUVWXYZ),
					Arrays.stream(ABCDEFGHIJKLMNOPQRSTUVWXYZ))
			.collect(Collectors.toList());
	private static final Quest.Board.Object TREASURE_CHEST = new Quest.Board.Object() {{
		setId("TreasureChest");
		setLeft(2.0f);
		setTop(4.0f);
		setRotation(Rotation.DOWNWARD);
		setZorder(-5.0f);
	}};
	private ObjectList objectList;

	@BeforeEach
	void setUp() throws HeroScribeParseException {
		TMP_DIR.toFile().mkdirs();
		final ObjectsParser parser = new ObjectsParser(Path.of("."), new Preferences());
		final String currentPath = getResourceAsFile(".").getAbsolutePath();
		objectList = parser.parse(
				new File(currentPath.substring(0, currentPath.indexOf("/target")),
						"Objects.xml"));
	}

	@Test
	void shouldExportThumbnail() throws Exception {
		for (PaperType paperType : PaperType.values()) {
			ExportPDF.writeThumbNail(GHOSTSCRIPT_BIN,
					createFile("thumbnail-board.pdf", paperType),
					createEmptyQuest(),
					objectList,
					paperType);
		}
	}

	@Test
	void shouldExportMultiboardThumbnail() throws Exception {
		for (PaperType paperType : PaperType.values()) {
			final Quest quest = createEmptyQuest();
			quest.setWidth(1);
			quest.setHeight(2);
			quest.setBoards(List.of(
					createBoard(
							createObject("Barbarian", 4.0f),
							createObject("Wizard", 2.0f)),
					createBoard(
							createObject("Barbarian", 18.0f),
							createObject("Wizard", 14.0f))));
			ExportPDF.writeThumbNail(GHOSTSCRIPT_BIN,
					createFile("thumbnail-multiboard.pdf", paperType),
					quest,
					objectList,
					paperType);
		}
	}

	@Test
	void shouldExportEpsEmptyQuest() throws Exception {
		for (PaperType paperType : PaperType.values()) {
			ExportEPS.writeMultiPage(paperType,
					createFile("empty.eps", paperType),
					createEmptyQuest(),
					objectList);
		}
	}

	@Test
	void shouldExportPdfEmptyQuest() throws Exception {
		for (PaperType paperType : PaperType.values()) {
			ExportPDF.write(GHOSTSCRIPT_BIN,
					createFile("empty.pdf", paperType),
					createEmptyQuest(),
					objectList,
					paperType);
		}
	}

	@Test
	void shouldExportEpsQuestWithSpeech() throws Exception {
		for (PaperType paperType : PaperType.values()) {
			final Quest quest = createEmptyQuest();
			quest.setSpeech(LOREM_IPSUM);
			ExportEPS.writeMultiPage(paperType,
					createFile("speech-only.eps", paperType),
					createEmptyQuest(),
					objectList);
		}
	}

	@Test
	void shouldExportPDFQuestWithSpeech() throws Exception {
		for (PaperType paperType : PaperType.values()) {
			final Quest quest = createEmptyQuest();
			quest.setSpeech(LOREM_IPSUM);
			quest.getBoards().get(0).addObject(TREASURE_CHEST);
			ExportPDF.write(GHOSTSCRIPT_BIN,
					createFile("speech-only.pdf", paperType),
					quest,
					objectList,
					paperType);
		}
	}

	@Test
	void shouldExportPDFQuestWithSpeechAndManyShortNotes() throws Exception {
		for (PaperType paperType : PaperType.values()) {
			final Quest quest = createEmptyQuest();
			quest.setSpeech(LOREM_IPSUM);
			quest.getBoards().get(0).addObject(TREASURE_CHEST);
			quest.setNotes(Stream.concat(
							ABCDEFGHIJKLMNOPQRSTUVWXYZ_x2.stream(),
							ABCDEFGHIJKLMNOPQRSTUVWXYZ_x2.stream())
					.map(s -> s + " " + SHORT_LOREM_IPSUM)
					.collect(Collectors.toList()));
			ExportPDF.write(GHOSTSCRIPT_BIN,
					createFile("many-short-notes.pdf", paperType),
					quest,
					objectList,
					paperType);
		}
	}

	@Test
	void shouldExportEpsQuestWithSpeechAndFewNotes() throws Exception {
		for (PaperType paperType : PaperType.values()) {
			ExportEPS.writeMultiPage(paperType,
					createFile("one-page-notes.eps", paperType),
					createQuestWithNotes(ABC),
					objectList);
		}
	}

	@Test
	void shouldExportPdfQuestWithSpeechAndFewNotes() throws Exception {
		for (PaperType paperType : PaperType.values()) {
			ExportPDF.write(GHOSTSCRIPT_BIN,
					createFile("one-page-notes.pdf", paperType),
					createQuestWithNotes(ABC),
					objectList,
					paperType);
		}
	}

	@Test
	void shouldExportPdfQuestWithSpeechAndFewNotes_TwoBoards() throws Exception {
		for (PaperType paperType : PaperType.values()) {
			final Quest quest = createEmptyQuest();
			quest.setSpeech(LOREM_IPSUM);
			quest.setWidth(1);
			quest.setHeight(2);
			quest.setBoards(List.of(
					createBoard(
							createObject("Barbarian", 4.0f),
							createObject("Wizard", 2.0f)),
					createBoard(
							createObject("Barbarian", 18.0f),
							createObject("Wizard", 14.0f))));
			for (String character : ABC) {
				quest.getNotes().add(character + " " + LOREM_IPSUM);
			}
			ExportPDF.write(GHOSTSCRIPT_BIN,
					createFile("multiboard-one-page-notes.pdf", paperType),
					quest,
					objectList,
					paperType);
		}
	}

	@Test
	void shouldExportDinPdfQuestWithSpeechAndManyNotes_TwoPages() throws Exception {
		for (PaperType paperType : PaperType.values()) {
			ExportPDF.write(GHOSTSCRIPT_BIN,
					createFile("two-pages-notes.pdf", paperType),
					createQuestWithNotes(ABCDEFGHI),
					objectList,
					paperType);
		}
	}

	@Test
	void shouldExportPdfQuestWithSpeechAndManyNotes_ThreePages() throws Exception {
		for (PaperType paperType : PaperType.values()) {
			ExportPDF.write(GHOSTSCRIPT_BIN,
					createFile("three-pages-notes.pdf", paperType),
					createQuestWithNotes(ABCDEFGHIJKLMNOPQRSTUVWXYZ),
					objectList,
					paperType);
		}
	}

	@Test
	void shouldExportEpsQuestWithSpeechAndManyNotes_ThreePages() throws Exception {
		for (PaperType paperType : PaperType.values()) {
			ExportEPS.write(
					paperType,
					createFile("three-pages-notes.eps", paperType),
					createQuestWithNotes(ABCDEFGHIJKLMNOPQRSTUVWXYZ),
					objectList);
		}
	}

	@Test
	void shouldExportPdfQuestWithSpeechAndManyNotes_TwoPages() throws Exception {
		for (PaperType paperType : PaperType.values()) {
			ExportPDF.write(GHOSTSCRIPT_BIN,
					createFile("two-pages-notes.pdf", paperType),
					createQuestWithNotes(ABCDEFGHI),
					objectList,
					paperType);
		}
	}

	@Test
	void shouldExportEpsQuestWithSpeechAndManyNotes() throws Exception {
		for (PaperType paperType : PaperType.values()) {
			ExportEPS.writeMultiPage(
					paperType,
					createFile("two-pages-notes.eps", paperType),
					createQuestWithNotes(ABCDEFGHI),
					objectList);
		}
	}

	private Quest createQuestWithNotes(String[] noteLetters) {
		final Quest quest = createEmptyQuest();
		quest.setSpeech(LOREM_IPSUM);
		quest.getBoards().get(0).addObject(TREASURE_CHEST);
		for (String noteLetter : noteLetters) {
			quest.getNotes().add(noteLetter + " " + LOREM_IPSUM);
		}
		return quest;
	}

	private Quest createEmptyQuest() {
		return new Quest() {{
			setName("Lorem ipsum dolor sit amet");
			updateDimensions(objectList.getBoard().getWidth(), objectList.getBoard().getHeight());
		}};
	}


	private Quest.Board createBoard(Quest.Board.Object... objects) {
		return new Quest.Board() {{
			setObjects(Arrays.stream(objects)
					.collect(Collectors.toList()));
		}};
	}

	private static Quest.Board.Object createObject(String id, final float left) {
		return new Quest.Board.Object() {{
			setId(id);
			setLeft(left);
			setTop(2.0f);
			setRotation(Rotation.DOWNWARD);
		}};
	}

	private File createFile(String filename, PaperType paperType) {
		final String name = filename.split("\\.")[0];
		final String extension = filename.split("\\.")[1];
		return new File(TMP_DIR.toFile(), String.format("%s.%s.%s", name, paperType.getId(), extension));
	}

}