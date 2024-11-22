/*
  HeroScribe
  Copyright (C) 2002-2004 Flavio Chierichetti and Valerio Chierichetti

  HeroScribe Enhanced (changes are prefixed with HSE in comments)
  Copyright (C) 2011 Jason Allen

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


import org.lightless.heroscribe.xml.ObjectList;
import org.lightless.heroscribe.xml.Quest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;

import static org.lightless.heroscribe.Constants.APPLICATION_NAME;
import static org.lightless.heroscribe.Constants.VERSION;

public class ExportEPS {

	private static final Logger log = LoggerFactory.getLogger(ExportEPS.class);
	private static final int LINES_PER_BLOCK = 500;

	public static void write(PaperType paperType,
							 File file,
							 Quest quest,
							 ObjectList objects) throws Exception {
		final FormatterWriter out = new FormatterWriter(new PrintWriter(new BufferedWriter(new FileWriter(file))));


		out.println("%!PS-Adobe-3.0");
		out.println("%%Creator: %s %s", APPLICATION_NAME, VERSION);
		out.println("%%Title: %s", quest.getName());
		out.println("%%LanguageLevel: 2");
		out.println("%%BoundingBox: 0 0 %s %s",
				Math.round((double) paperType.getWidth()), // 528
				Math.round((double) paperType.getHeight())); // 794
		out.println("%%HiResBoundingBox: 0 0 %s %s",
				paperType.getWidth(),
				paperType.getHeight());  // 528.0 793.6
		out.println("%%Pages: %s",
				quest.getHeight() * quest.getWidth());
		out.println("/adjacentBoardsOffset %s def",
				objects.getBoard().getAdjacentBoardsOffset());

		appendPS(objects.getBoardVectorPath(quest.getRegion()),
				out,
				false);

		out.println("%d %d BoundingBox",
				quest.getWidth(), quest.getHeight());
		out.println("/sysshowpage {systemdict /showpage get exec} def");
		// HSE - output all the postscript definitions to write text
		out.println("/gs /gsave def /gr /grestore def");
		out.println("/np /newpath def /cp /closepath def ");
		out.println("/mt {/moveto} def /rt /rmoveto def ");
		out.println("/li /lineto def /rl /rlineto def ");
		out.println("/ct /curveto def /tr /translate def ");
		out.println("/st /stroke def /set { gs setlinewidth st gr } def ");
		out.println("/gray {gs setgray fill gr} def ");
		out.println("/ro /rotate def /rp /repeat def");
		out.println("/box { np mt rl rl rl cp set }def ");
		out.println("/circle { np arc set }def ");
		out.println("/ph %d def",
				paperType.getHeight());  // 793.6
		out.println("/s /show load def /L { newline } def /n { s L } def");
		out.println("/textbox { /lm 35 def /bm 0 def /rm %d def /tm 35 def lm tm moveto } def",
				paperType.getWidth() - 20);
		out.println("/newline { tm 12 sub /tm exch def lm tm moveto } def");
		out.println("/centre { dup stringwidth pop 2 div rm lm sub 2 div exch sub lm add tm moveto } def");
		out.println("/n { show newline } def /c {centre n } def /s {show } def /L { newline } def");
		// HSE - definitions to handle word wrapping
		out.println("/space ( ) def");
		out.println("/spacecount { 0 exch ( ) { search { pop 3 -1 roll 1 add 3 1 roll } { pop exit } ifelse } loop } def");
		out.println("/toofar? { ( ) search pop dup stringwidth pop currentpoint pop add rm gt } def");
		out.println("/a { tm exch sub TM lm tm moveto } bind def");
		out.println("/LG { /lg exch def } def 12 LG");
		out.println("/S { dup spacecount { toofar? { L s s } { s s } ifelse } repeat pop } bind def");
		out.println("/P { S L } bind def ");

		out.println("2 dict dup dup /showpage {} put /setpagedevice {} put begin");

		final TreeSet<String> set = new TreeSet<>();
		for (int i = 0; i < quest.getWidth(); i++) {
			for (int j = 0; j < quest.getHeight(); j++) {
				for (Quest.Board.Object object : quest.getBoard(i, j).getObjects()) {
					set.add(object.getId());
				}
			}
		}

		for (String id : set) {
			out.println("/Icon%s << /FormType 1 /PaintProc { pop",
					id);

			/* the postscript is divided in "{ } exec" blocks to broaden
			 * compatibility
			 */
			final int[] boundingBox =
					appendPS(
							objects.getObjectVectorPath(id, quest.getRegion()),
							out,
							true);

			out.println(" } bind /Matrix [1 0 0 1 0 0] /BBox [%d %d %d %d] >> def",
					boundingBox[0],
					boundingBox[1],
					boundingBox[2],
					boundingBox[3]);

		}
		// HSE - add wandering monster object
		out.println("/Icon%s << /FormType 1 /PaintProc { pop",
				quest.getWanderingId());

		/* the postscript is divided in "{ } exec" blocks to broaden
		 * compatibility
		 */
		final int[] boundingBox =
				appendPS(
						objects.getObjectVectorPath(quest.getWanderingId(), quest.getRegion()),
						out,
						true);

		out.println(" } bind /Matrix [1 0 0 1 0 0] /BBox [%d %d %d %d] >> def",
				boundingBox[0],
				boundingBox[1],
				boundingBox[2],
				boundingBox[3]);
		// END wandering monster object

		final PageSection pageSection = new PageSection();

		// loop through each board, generating every two boards
		for (int column = 0; column < quest.getWidth(); column++) {
			for (int row = 0; row < quest.getHeight(); row++) {
				final Quest.Board board = quest.getBoard(column, row);

				if (pageSection.isTopSection()) {
					out.println("%%Page: %s %s",
							pageSection.count(),
							pageSection.count());
				}
				final float boardXPosition = calculateBoardXPosition(paperType);
				final float boardYPosition = calculateBoardYPosition(paperType, pageSection);
				out.println("%s %s StartBoard",
						boardXPosition, boardYPosition);

				for (int i = 1; i <= board.getWidth(); i++) {
					for (int j = 1; j <= board.getHeight(); j++) {
						if (objects.getBoard().getCorridors()[i][j]) {
							out.println("%d %d 1 1 Corridor",
									i, board.getHeight() - j + 1);
						}
					}
				}

				for (int i = 1; i <= board.getWidth(); i++) {
					for (int j = 1; j <= board.getHeight(); j++) {
						if (board.isDark(i, j)) {
							out.println("%d %d 1 1 Dark",
									i, board.getHeight() - j + 1);
						}
					}
				}

				out.println("Grid");

				out.println("EndBoard");

				/* Bridges */
				out.println("%s %s StartBoard",
						boardXPosition, boardYPosition);

				if (column < quest.getWidth() - 1) {
					for (int top = 1; top <= board.getHeight(); top++) {
						if (quest.getHorizontalBridge(column, row, top)) {
							out.println("%d HorizontalBridge",
									board.getHeight() - top + 1);
						}
					}
				}

				if (row < quest.getHeight() - 1) {
					for (int left = 1; left <= board.getWidth(); left++) {
						if (quest.getVerticalBridge(column, row, left)) {
							out.println("%d VerticalBridge",
									left);
						}
					}
				}

				out.println("EndBoard");

				/* Objects */
				out.println("%s %s StartBoard",
						boardXPosition, boardYPosition);

				for (Quest.Board.Object object : board.getObjects()) {
					int width, height;
					float x, y, xoffset, yoffset;

					if (object.getRotation().isPair()) {
						width = objects.getObjectById(object.getId()).getWidth();
						height = objects.getObjectById(object.getId()).getHeight();
					} else {
						width = objects.getObjectById(object.getId()).getHeight();
						height = objects.getObjectById(object.getId()).getWidth();
					}

					x = object.getLeft() + width / 2.0f;
					y = object.getTop() + height / 2.0f;

					if (objects.getObjectById(object.getId()).isTrap()) {
						out.println(object.getLeft()
								+ " "
								+ (board.getHeight() - object.getTop() - height + 2)
								+ " "
								+ width
								+ " "
								+ height
								+ " Trap");

					} else if (objects.getObjectById(object.getId()).isDoor()) {
						if (object.getRotation().isPair()) {
							if (object.getTop() == 0)
								y -= objects.getBoard().getBorderDoorsOffset();
							else if (object.getTop() == board.getHeight())
								y += objects.getBoard().getBorderDoorsOffset();
						} else {
							if (object.getLeft() == 0)
								x -= objects.getBoard().getBorderDoorsOffset();
							else if (object.getLeft() == board.getWidth())
								x += objects.getBoard().getBorderDoorsOffset();
						}
					}

					xoffset = objects.getObjectById(object.getId())
							.getIcon(quest.getRegion()).getXoffset();
					yoffset = objects.getObjectById(object.getId())
							.getIcon(quest.getRegion()).getYoffset();

					switch (object.getRotation()) {
						case DOWNWARD:
							x += xoffset;
							y += yoffset;
							break;

						case RIGHTWARD:
							x += yoffset;
							y -= xoffset;
							break;

						case UPWARD:
							x -= xoffset;
							y -= yoffset;
							break;

						case LEFTWARD:
							x -= yoffset;
							y += xoffset;
							break;
					}

					y = objects.getBoard().getHeight() - y + 2;

					out.println("gsave");
					out.println(x + " Unit " + y + " Unit translate");
					out.println((object.getRotation().getNumber() * 90) + " rotate");
					out.println("Icon" + object.getId() + " execform");
					out.println("grestore");
					out.println();
				}

				out.println("EndBoard");

				if (pageSection.isBottomSection()) {
					out.println("sysshowpage");
					out.println("%%EndPage");
				}

				pageSection.increase();
			}
		}

		out.println("end");
		out.println("%%EOF");

		out.close();
	}

	public static void writeMultiPage(PaperType paperType,
									  File file,
									  Quest quest,
									  ObjectList objects) throws Exception {
		final FormatterWriter out = new FormatterWriter(new PrintWriter(new BufferedWriter(new FileWriter(file))));

		out.println("%!PS-Adobe-3.0");
		out.println("%%Creator: %s %s", APPLICATION_NAME, VERSION);
		out.println("%%Title: %s", quest.getName());
		out.println("%%LanguageLevel: 2");
		out.println("%%BoundingBox: 0 0 %s %s",
				Math.round((double) paperType.getWidth()), // 528
				Math.round((double) paperType.getHeight())); // 794
		out.println("%%HiResBoundingBox: 0 0 %s %s",
				paperType.getWidth(),
				paperType.getHeight());  // 528.0 793.6
		out.println("%%Pages: %s",
				quest.getHeight() * quest.getWidth());
		out.println("/adjacentBoardsOffset %s def",
				objects.getBoard().getAdjacentBoardsOffset());

		appendPS(objects.getBoardVectorPath(quest.getRegion()),
				out,
				false);

		out.println("%d %d BoundingBox",
				quest.getWidth(), quest.getHeight());
		out.println("/sysshowpage {systemdict /showpage get exec} def");
		// HSE - output all the postscript definitions to write text
		out.println("/gs /gsave def /gr /grestore def");
		out.println("/np /newpath def /cp /closepath def ");
		out.println("/mt {/moveto} def /rt /rmoveto def ");
		out.println("/li /lineto def /rl /rlineto def ");
		out.println("/ct /curveto def /tr /translate def ");
		out.println("/st /stroke def /set { gs setlinewidth st gr } def ");
		out.println("/gray {gs setgray fill gr} def ");
		out.println("/ro /rotate def /rp /repeat def");
		out.println("/box { np mt rl rl rl cp set }def ");
		out.println("/circle { np arc set }def ");
		out.println("/ph %d def",
				paperType.getHeight());  // 793.6
		out.println("/s /show load def /L { newline } def /n { s L } def");
		out.println("/textbox { /lm 35 def /bm 0 def /rm %d def /tm 35 def lm tm moveto } def",
				paperType.getWidth() - 20);
		out.println("/newline { tm 12 sub /tm exch def lm tm moveto } def");
		out.println("/centre { dup stringwidth pop 2 div rm lm sub 2 div exch sub lm add tm moveto } def");
		out.println("/n { show newline } def /c {centre n } def /s {show } def /L { newline } def");
		// HSE - definitions to handle word wrapping
		out.println("/space ( ) def");
		out.println("/spacecount { 0 exch ( ) { search { pop 3 -1 roll 1 add 3 1 roll } { pop exit } ifelse } loop } def");
		out.println("/toofar? { ( ) search pop dup stringwidth pop currentpoint pop add rm gt } def");
		out.println("/a { tm exch sub TM lm tm moveto } bind def");
		out.println("/LG { /lg exch def } def 12 LG");
		out.println("/S { dup spacecount { toofar? { L s s } { s s } ifelse } repeat pop } bind def");
		out.println("/P { S L } bind def ");

		out.println("2 dict dup dup /showpage {} put /setpagedevice {} put begin");

		final TreeSet<String> set = new TreeSet<>();
		for (int i = 0; i < quest.getWidth(); i++) {
			for (int j = 0; j < quest.getHeight(); j++) {
				for (Quest.Board.Object object : quest.getBoard(i, j).getObjects()) {
					set.add(object.getId());
				}
			}
		}

		for (String id : set) {
			out.println("/Icon%s << /FormType 1 /PaintProc { pop",
					id);

			/* the postscript is divided in "{ } exec" blocks to broaden
			 * compatibility
			 */
			final int[] boundingBox =
					appendPS(
							objects.getObjectVectorPath(id, quest.getRegion()),
							out,
							true);

			out.println(" } bind /Matrix [1 0 0 1 0 0] /BBox [%d %d %d %d] >> def",
					boundingBox[0],
					boundingBox[1],
					boundingBox[2],
					boundingBox[3]);

		}
		// HSE - add wandering monster object
		out.println("/Icon%s << /FormType 1 /PaintProc { pop",
				quest.getWanderingId());

		/* the postscript is divided in "{ } exec" blocks to broaden
		 * compatibility
		 */
		final int[] boundingBox =
				appendPS(
						objects.getObjectVectorPath(quest.getWanderingId(), quest.getRegion()),
						out,
						true);

		out.println(" } bind /Matrix [1 0 0 1 0 0] /BBox [%d %d %d %d] >> def",
				boundingBox[0],
				boundingBox[1],
				boundingBox[2],
				boundingBox[3]);
		// END wandering monster object

		final PageSection pageSection = new PageSection();

		// loop through each board
		for (int column = 0; column < quest.getWidth(); column++) {
			for (int row = 0; row < quest.getHeight(); row++) {
				final Quest.Board board = quest.getBoard(column, row);

				if (pageSection.isTopSection()) {
					out.println("%%Page: %s %s",
							pageSection.count(),
							pageSection.count());
				}
				final float boardXPosition = calculateBoardXPosition(paperType);
				final float boardYPosition = calculateBoardYPosition(paperType, pageSection);
				out.println("%s %s StartBoard",
						boardXPosition, boardYPosition);

				for (int i = 1; i <= board.getWidth(); i++) {
					for (int j = 1; j <= board.getHeight(); j++) {
						if (objects.getBoard().getCorridors()[i][j]) {
							out.println("%d %d 1 1 Corridor",
									i, board.getHeight() - j + 1);
						}
					}
				}

				for (int i = 1; i <= board.getWidth(); i++) {
					for (int j = 1; j <= board.getHeight(); j++) {
						if (board.isDark(i, j)) {
							out.println("%d %d 1 1 Dark",
									i, board.getHeight() - j + 1);
						}
					}
				}

				out.println("Grid");

				out.println("EndBoard");

				/* Bridges */
				out.println("%s %s StartBoard",
						boardXPosition, boardYPosition);

				if (column < quest.getWidth() - 1) {
					for (int top = 1; top <= board.getHeight(); top++) {
						if (quest.getHorizontalBridge(column, row, top)) {
							out.println("%d HorizontalBridge",
									board.getHeight() - top + 1);
						}
					}
				}

				if (row < quest.getHeight() - 1) {
					for (int left = 1; left <= board.getWidth(); left++) {
						if (quest.getVerticalBridge(column, row, left)) {
							out.println("%d VerticalBridge",
									left);
						}
					}
				}

				out.println("EndBoard");

				/* Objects */
				out.println("%s %s StartBoard",
						boardXPosition, boardYPosition);

				for (Quest.Board.Object object : board.getObjects()) {
					int width, height;
					float x, y, xoffset, yoffset;

					if (object.getRotation().isPair()) {
						width = objects.getObjectById(object.getId()).getWidth();
						height = objects.getObjectById(object.getId()).getHeight();
					} else {
						width = objects.getObjectById(object.getId()).getHeight();
						height = objects.getObjectById(object.getId()).getWidth();
					}

					x = object.getLeft() + width / 2.0f;
					y = object.getTop() + height / 2.0f;

					if (objects.getObjectById(object.getId()).isTrap()) {
						out.println(object.getLeft()
								+ " "
								+ (board.getHeight() - object.getTop() - height + 2)
								+ " "
								+ width
								+ " "
								+ height
								+ " Trap");

					} else if (objects.getObjectById(object.getId()).isDoor()) {
						if (object.getRotation().isPair()) {
							if (object.getTop() == 0)
								y -= objects.getBoard().getBorderDoorsOffset();
							else if (object.getTop() == board.getHeight())
								y += objects.getBoard().getBorderDoorsOffset();
						} else {
							if (object.getLeft() == 0)
								x -= objects.getBoard().getBorderDoorsOffset();
							else if (object.getLeft() == board.getWidth())
								x += objects.getBoard().getBorderDoorsOffset();
						}
					}

					xoffset = objects.getObjectById(object.getId())
							.getIcon(quest.getRegion()).getXoffset();
					yoffset = objects.getObjectById(object.getId())
							.getIcon(quest.getRegion()).getYoffset();

					switch (object.getRotation()) {
						case DOWNWARD:
							x += xoffset;
							y += yoffset;
							break;

						case RIGHTWARD:
							x += yoffset;
							y -= xoffset;
							break;

						case UPWARD:
							x -= xoffset;
							y -= yoffset;
							break;

						case LEFTWARD:
							x -= yoffset;
							y += xoffset;
							break;
					}

					y = objects.getBoard().getHeight() - y + 2;

					out.println("gsave");
					out.println(x + " Unit " + y + " Unit translate");
					out.println((object.getRotation().getNumber() * 90) + " rotate");
					out.println("Icon" + object.getId() + " execform");
					out.println("grestore");
					out.println();
				}

				out.println("EndBoard");

				// HSE - output board location if multi board quest
				if (quest.getWidth() > 1 || quest.getHeight() > 1) {
					// HSE - text area
					out.println("/newline { tm 10 sub /tm exch def lm tm moveto } def");
					out.println("/Times-Roman findfont 10 scalefont setfont");

					// HSE - create the text bounding box in PS
					out.println("gsave 0 ph %d sub translate textbox",
							pageSection.isTopSection() ?
									paperType.getHalfHeight()
											+ roundPercentage(120, percentageProportion(paperType))
									: paperType.getHeight()
					);
					out.println("newline newline (   Board Location: \\(%d,%d\\) ) S",
							column,
							row);
					out.println("grestore");
				}

				if (pageSection.isBottomSection()) {
					out.println("sysshowpage");
					out.println("%%EndPage");
				}

				log.info("Page sections: {}", pageSection.count());
				pageSection.increase();
			}
		}

		// HSE - text area
		out.println("/newline { tm 12 sub /tm exch def lm tm moveto } def");
		out.println("/Times-Roman findfont 16 scalefont setfont");

		// HSE - create the text bounding box in PS
		out.println("gsave 0 ph %d sub translate textbox",
				pageSection.isBottomSection() ?
						paperType.getHalfHeight() + 70 :
						roundPercentage(paperType.getHeight(), percentageProportion(paperType))); // 440  2.256f%

		// HSE - output the quest name in dark red
		out.println("0.50 0 0.20 setrgbcolor (%s) c newline",
				sanitize(quest.getName()));

		// HSE - output the quest speech including line feeds
		out.println("/newline { tm 12 sub /tm exch def lm tm moveto } def");
		out.println("/Times-Roman findfont 12 scalefont setfont");
		out.println("0 0 0 setrgbcolor");

		int numberOfLinesInPage = 0;
		final int speechLines = GhostscriptUtils.numberOfLines(quest.getSpeech(), 12);
		log.info("Speech. number of lines: {}", speechLines);
		numberOfLinesInPage += speechLines;
		for (String linefeed : quest.getSpeech().split("\n")) {
			out.println("(%s ) S L",
					sanitize(linefeed));
		}

		// HSE - output the notes in regular black font, smaller line spacing
		out.println("/LG { /lg exch def } def 10 LG");
		out.println("/newline { tm 10 sub /tm exch def lm tm moveto } def");
		out.println("/Times-Roman findfont 10 scalefont setfont");
		out.println("newline (NOTES ) S");
		out.println("newline");

		int pageMaxNumberOfLines = 0;
		for (String note : quest.getNotesForUI()) {
			for (String noteLine : note.split("\n")) {
				final int lines = GhostscriptUtils.numberOfLines(noteLine, 10);
				log.info("number of lines: {}", lines);
				numberOfLinesInPage += lines;
				pageMaxNumberOfLines = paperType.getNumberLinesHalfPage();
				if (pageSection.isTopSection()) {
					pageMaxNumberOfLines = paperType.getNumberLinesFullPage();
				}
				if (numberOfLinesInPage > pageMaxNumberOfLines) {
					log.info("resetting numberOfLinesInPage: {}", numberOfLinesInPage);
					numberOfLinesInPage = 0;
					printWanderingMonster(paperType, quest, objects, out);

					out.println("sysshowpage");
					out.println("%%EndPage");
					out.println("%%Page: %s %s",
							pageSection.count(),
							pageSection.count() + 1);

					out.println("/Times-Roman findfont 10 scalefont setfont");
					out.println("gsave 0 ph %d sub translate textbox",
							70);

					log.info("Page sections: {}", pageSection.count());
					pageSection.increase();
					if (pageMaxNumberOfLines == paperType.getNumberLinesFullPage()) {
						pageSection.increase();
					}
				}

				out.println("newline (%s ) S",
						sanitize(noteLine));
			}
			out.println("newline");
		}

		if (numberOfLinesInPage >= pageMaxNumberOfLines) {
			pageSection.increase();

			out.println("%%EndPage");
			out.println("%%Page: %s %s",
					pageSection.count(),
					pageSection.count() + 1);
		}
		// HSE - output the wandering monster
		printWanderingMonster(paperType, quest, objects, out);

		// HSE - restore the coords
		out.println("grestore");
//		out.println("gsave 10 ph %d sub translate textbox",
//				150);
		out.println("sysshowpage");
		out.println("end");
		out.println("%%EOF");

		out.close();

		System.out.println("Number of sections: " + pageSection.count());
	}

	private static void printWanderingMonster(PaperType paperType, Quest quest, ObjectList objects, FormatterWriter out) {
		out.println("grestore");
		out.println("gsave 20 ph %d sub translate textbox",
				paperType.getHeight());
		final ObjectList.Object wanderingMonster = objects.getObjectById(quest.getWanderingId());
		out.println("/Times-Roman findfont 12 scalefont setfont");
		out.println("(Wandering Monster in this Quest: %s ) c",
				sanitize(wanderingMonster.getName()));

		out.println("%d (%s) stringwidth pop 2 div sub 40 translate",
				paperType.getHalfWidth() - 90,
				sanitize(wanderingMonster.getName()));
		out.println("Icon%s execform",
				wanderingMonster.getId());
	}

	private static float percentageProportion(PaperType paperType) {
		switch (paperType) {
			case A4:
				return 7.9f;
			case LETTER:
				return 8.7f;
		}
		return 0;
	}

	private static float calculateBoardXPosition(PaperType paperType) {
		// TODO calculate dynamically
		switch (paperType) {
			case A4:
				return 0.055f;
			case LETTER:
				return 0.07f;
		}
		return 0;
	}

	private static float calculateBoardYPosition(PaperType paperType, PageSection pageSection) {
		// TODO calculate dynamically
		switch (paperType) {
			case A4:
				if (pageSection.isTopSection()) {
					return 1.07f;
				} else {
					return 0.07f;
				}
			case LETTER:
				return pageSection.isTopSection() ? 1.0f : 0.02f;
		}
		return 0;
	}

	private static long roundPercentage(int number, float percentage) {
		return Math.round(Math.ceil(percentage(number, percentage)));
	}

	private static double percentage(int number, float percentage) {
		return (number * percentage) / 100;
	}

	private static int[] appendPS(Path inPath,
								  PrintWriter out,
								  boolean divideInBlocks) throws Exception {
		final BufferedReader in = new BufferedReader(
				new InputStreamReader(
						new GZIPInputStream(
								new FileInputStream(inPath.toFile()))));

		int[] boundingBox = null;

		String data = in.readLine();
		int lines = 0;

		while (data != null) {
			if (!data.trim().isEmpty() && data.trim().charAt(0) != '%') {
				if (divideInBlocks && lines % LINES_PER_BLOCK == 0) {
					if (lines != 0)
						out.write(" } exec ");
					out.write(" { ");
				}

				lines++;
				out.println(data);
			}

			if (data.trim().startsWith("%%BoundingBox:") && boundingBox == null) {
				final String[] bit = data.split(" ");
				boundingBox = new int[4];

				for (int i = 0; i < 4; i++) {
					boundingBox[i] = Integer.parseInt(bit[bit.length - 4 + i]);
				}
			}

			data = in.readLine();
		}

		if (divideInBlocks && lines > 0)
			out.write(" } exec ");

		in.close();

		return boundingBox;

	}

	private static String sanitize(String input) {
		return input.replace("(", "\\(")
				.replace(")", "\\)");
	}
}
