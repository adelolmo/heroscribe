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

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lightless.heroscribe.xml.ObjectList;
import org.lightless.heroscribe.xml.ObjectsParser;
import org.lightless.heroscribe.xml.Quest;
import org.lightless.heroscribe.xml.Rotation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.lightless.heroscribe.ResourceUtils.getResourceAsFile;

class ExportQuestTest {

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
		final String currentPath = getResourceAsFile(".").getAbsolutePath();
		objectList = parser.parse(
				new File(currentPath.substring(0, currentPath.indexOf("/target")),
						"Objects.xml"));
	}

	@Test
	void shouldExportThumbnail() throws Exception {
		final Quest quest = createEmptyQuest();
		quest.setWidth(1);
		quest.setHeight(2);
		final Quest.Board.Object barbarian = createObject("Barbarian", 4.0f);
		final Quest.Board.Object wizard = createObject("Wizard", 2.0f);
		quest.setBoards(List.of(createBoard(barbarian, wizard), createBoard()));
		ExportPDF.writeThumbNail(GHOSTSCRIPT_BIN,
				new File("/tmp/multiboard.pdf"),
				quest,
				objectList,
				PaperType.A4);
	}

	@Test
	void shouldExportEpsEmptyQuest() throws Exception {
		ExportEPS.writeMultiPage(PaperType.A4,
				new File("/tmp/empty.eps"),
				createEmptyQuest(),
				objectList);
	}

	@Test
	void shouldExportPdfEmptyQuest() throws Exception {
		Quest quest = createEmptyQuest();
		ExportPDF.write(GHOSTSCRIPT_BIN,
				new File("/tmp/empty.pdf"),
				quest,
				objectList,
				PaperType.A4);
	}

	@Test
	void shouldExportEpsQuestWithSpeech() throws Exception {
		final Quest quest = createEmptyQuest();
		quest.setSpeech(LOREM_IPSUM);
		ExportEPS.writeMultiPage(PaperType.A4,
				new File("/tmp/speech-only-dina4.eps"),
				createEmptyQuest(),
				objectList);
	}

	@Test
	void shouldExportPDFQuestWithSpeech() throws Exception {
		final Quest quest = createEmptyQuest();
		quest.setSpeech(LOREM_IPSUM);
		quest.getBoards().get(0).addObject(TREASURE_CHEST);
		ExportPDF.write(GHOSTSCRIPT_BIN,
				new File("/tmp/speech-only-dina4.pdf"),
				quest,
				objectList,
				PaperType.A4);
	}

	@Test
	void shouldExportEpsQuestWithSpeechAndFewNotes() throws Exception {
		final Quest quest = createEmptyQuest();
		quest.setSpeech(LOREM_IPSUM);
		for (String character : ABC) {
			quest.getNotes().add(character + " " + LOREM_IPSUM);
		}
		ExportEPS.writeMultiPage(PaperType.A4,
				new File("/tmp/one-page-notes-dina4.eps"),
				createEmptyQuest(),
				objectList);
	}

	@Test
	void shouldExportA4PdfQuestWithSpeechAndFewNotes() throws Exception {
		final Quest quest = createEmptyQuest();
		quest.setSpeech(LOREM_IPSUM);
		quest.getBoards().get(0).addObject(TREASURE_CHEST);
		for (String character : ABC) {
			quest.getNotes().add(character + " " + LOREM_IPSUM);
		}
		ExportPDF.write(GHOSTSCRIPT_BIN,
				new File("/tmp/one-page-notes-dina4.pdf"),
				quest,
				objectList,
				PaperType.A4);
	}

	@Test
	void shouldExportA4PdfQuestWithSpeechAndFewNotes_TwoBoards() throws Exception {
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
				new File("/tmp/multiboard-one-page-notes-dina4.pdf"),
				quest,
				objectList,
				PaperType.A4);
	}

	@Test
	void shouldExportDinA4PdfQuestWithSpeechAndManyNotes_TwoPages() throws Exception {
		final Quest quest = createEmptyQuest();
		quest.setSpeech(LOREM_IPSUM);
		quest.getBoards().get(0).addObject(TREASURE_CHEST);
		for (String character : ABCDEFGHI) {
			quest.getNotes().add(character + " " + LOREM_IPSUM);
		}
		ExportPDF.write(GHOSTSCRIPT_BIN,
				new File("/tmp/two-pages-notes-dina4.pdf"),
				quest,
				objectList,
				PaperType.A4);
	}

	@Test
	void shouldExportDinA4PdfQuestWithSpeechAndManyNotes_ThreePages() throws Exception {
		final Quest quest = createEmptyQuest();
		quest.setSpeech(LOREM_IPSUM);
		quest.getBoards().get(0).addObject(TREASURE_CHEST);
		for (String character : ABCDEFGHIJKLMNOPQRSTUVWXYZ) {
			quest.getNotes().add(character + " " + LOREM_IPSUM);
		}
		ExportPDF.write(GHOSTSCRIPT_BIN,
				new File("/tmp/three-pages-notes-dina4.pdf"),
				quest,
				objectList,
				PaperType.A4);
	}

	@Test
	void shouldExportLetterPdfQuestWithSpeechAndFewNotes() throws Exception {
		final Quest quest = createEmptyQuest();
		quest.setSpeech(LOREM_IPSUM);
		quest.getBoards().get(0).addObject(TREASURE_CHEST);
		for (String character : ABC) {
			quest.getNotes().add(character + " " + LOREM_IPSUM);
		}
		ExportPDF.write(GHOSTSCRIPT_BIN,
				new File("/tmp/one-page-notes-usletter.pdf"),
				quest,
				objectList,
				PaperType.LETTER);
	}

	@Test
	void shouldExportLetterPdfQuestWithSpeechAndManyNotes_TwoPages() throws Exception {
		final Quest quest = createEmptyQuest();
		quest.setSpeech(LOREM_IPSUM);
		quest.getBoards().get(0).addObject(TREASURE_CHEST);
		for (String character : ABCDEFGHI) {
			quest.getNotes().add(character + " " + LOREM_IPSUM);
		}
		ExportPDF.write(GHOSTSCRIPT_BIN,
				new File("/tmp/two-pages-notes-usletter.pdf"),
				quest,
				objectList,
				PaperType.LETTER);
	}

	@Test
	void shouldExportLetterPdfQuestWithSpeechAndManyNotes_ThreePages() throws Exception {
		final Quest quest = createEmptyQuest();
		quest.setSpeech(LOREM_IPSUM);
		quest.getBoards().get(0).addObject(TREASURE_CHEST);
		for (String character : ABCDEFGHIJKLMNOPQRSTUVWXYZ) {
			quest.getNotes().add(character + " " + LOREM_IPSUM);
		}
		ExportPDF.write(GHOSTSCRIPT_BIN,
				new File("/tmp/three-pages-notes-usletter.pdf"),
				quest,
				objectList,
				PaperType.LETTER);
	}

	@Test
	void shouldExportEpsQuestWithSpeechAndManyNotes() throws Exception {
		final Quest quest = createEmptyQuest();
		quest.setSpeech(LOREM_IPSUM);
		quest.getBoards().get(0).addObject(TREASURE_CHEST);
		for (String character : ABCDEFGHI) {
			quest.getNotes().add(character + " " + LOREM_IPSUM);
		}
		ExportEPS.writeMultiPage(PaperType.A4, new File("/tmp/empty.eps"),
				createEmptyQuest(),
				objectList);
	}

	private Quest createEmptyQuest() {
		return new Quest() {{
			setName("Lorem ipsum dolor sit amet");
			updateDimensions(objectList.getBoard().getWidth(), objectList.getBoard().getHeight());
		}};
	}


	private Quest.Board createBoard(Quest.Board.Object... objects) {


		return new Quest.Board() {{
//			final List<Object> object = new ArrayList<>() {{
//				add(createObject("Barbarian", 4.0f));
//				add(createObject("Wizard", 2.0f));
//			}};

			final List<Object> collect = Arrays.stream(objects)
					.collect(Collectors.toList());

			setObjects(collect);
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

}