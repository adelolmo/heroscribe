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

	private static final int LINES_PER_BLOCK = 500;
	private static final Logger log = LoggerFactory.getLogger(ExportEPS.class);
	private static final int HALF_PAGE_MAX_LINES = 25;
	private static final int FULL_PAGE_MAX_LINES = 70;

	private ExportEPS() {
	}

	public static void write(File file,
							 Quest quest,
							 ObjectList objectList) throws Exception {
		final FormatterWriter out = new FormatterWriter(new PrintWriter(new BufferedWriter(new FileWriter(file))));

		// HSE - set the box height to accommodate quest text
		final float bBoxWidth = (quest.getWidth()
				* (quest.getBoard(0, 0).getWidth() + 2)
				+ (quest.getWidth() - 1)
				* objectList.getBoard().getAdjacentBoardsOffset())
				* 19.2f; //612.0f

		final float bBoxHeight = ((quest.getHeight()
				* (quest.getBoard(0, 0).getHeight() + 2)
				+ (quest.getHeight() - 1)
				* objectList.getBoard().getAdjacentBoardsOffset())
				* 19.2f) + 400; //792.0f + (400*(quest.getHeight()-1))

		out.println("%!PS-Adobe-3.0 EPSF-3.0");
		out.println("%%LanguageLevel: 2");
		out.println("%%BoundingBox: 0 0 "
				+ Math.round(Math.ceil(bBoxWidth))
				+ " "
				+ Math.round(Math.ceil(bBoxHeight)));
		out.println("%%HiResBoundingBox: 0 0 "
				+ bBoxWidth
				+ " "
				+ bBoxHeight);

		out.println("/adjacentBoardsOffset "
				+ objectList.getBoard().getAdjacentBoardsOffset()
				+ " def");

		appendPS(objectList.getBoardVectorPath(quest.getRegion()),
				out,
				false);

		out.println(quest.getWidth() + " " + quest.getHeight() + " BoundingBox");
		out.println("2 dict dup dup /showpage {} put /setpagedevice {} put begin");

		final TreeSet<String> set = new TreeSet<>();
		for (int i = 0; i < quest.getWidth(); i++)
			for (int j = 0; j < quest.getHeight(); j++) {
				for (Quest.Board.Object object : quest.getBoard(i, j).getObjects()) {
					set.add(object.getId());
				}
			}

		for (String id : set) {
			out.println("/Icon" + id + " << /FormType 1 /PaintProc { pop");

			/* the postscript is divided in "{ } exec" blocks to broaden
			 * compatibility
			 */
			final int[] boundingBox = appendPS(objectList.getObjectVectorPath(id, quest.getRegion()),
					out,
					true);

			out.println(" } bind /Matrix [1 0 0 1 0 0] /BBox ["
					+ boundingBox[0]
					+ " "
					+ boundingBox[1]
					+ " "
					+ boundingBox[2]
					+ " "
					+ boundingBox[3]
					+ "] >> def");
		}
		// HSE - add wandering monster object
		out.println("/Icon" + quest.getWanderingId() + " << /FormType 1 /PaintProc { pop");

		/* the postscript is divided in "{ } exec" blocks to broaden
		 * compatibility
		 */
		final int[] boundingBox =
				appendPS(objectList
								.getObjectVectorPath(quest.getWanderingId(),
										quest.getRegion()),
						out,
						true);

		out.println(" } bind /Matrix [1 0 0 1 0 0] /BBox ["
				+ boundingBox[0]
				+ " "
				+ boundingBox[1]
				+ " "
				+ boundingBox[2]
				+ " "
				+ boundingBox[3]
				+ "] >> def");
		// END wandering monster object

		for (int column = 0; column < quest.getWidth(); column++)
			for (int row = 0; row < quest.getHeight(); row++) {
				final Quest.Board board = quest.getBoard(column, row);

				out.println(column + " "
						+ (quest.getHeight() - row - 1)
						+ " StartBoard");

				for (int i = 1; i <= board.getWidth(); i++)
					for (int j = 1; j <= board.getHeight(); j++)
						if (objectList.getBoard().getCorridors()[i][j])
							out.println(i + " "
									+ (board.getHeight() - j + 1)
									+ " 1 1 Corridor");

				for (int i = 1; i <= board.getWidth(); i++)
					for (int j = 1; j <= board.getHeight(); j++)
						if (board.isDark(i, j))
							out.println(i + " " + (board.getHeight() - j + 1) + " 1 1 Dark");

				out.println("Grid");

				out.println("EndBoard");
			}

		/* Bridges */
		for (int column = 0; column < quest.getWidth(); column++)
			for (int row = 0; row < quest.getHeight(); row++) {
				final Quest.Board board = quest.getBoard(column, row);

				out.println(column + " " + (quest.getHeight() - row - 1) + " StartBoard");

				if (column < quest.getWidth() - 1)
					for (int top = 1; top <= board.getHeight(); top++)
						if (quest.getHorizontalBridge(column, row, top))
							out.println((board.getHeight() - top + 1) + " HorizontalBridge");

				if (row < quest.getHeight() - 1)
					for (int left = 1; left <= board.getWidth(); left++)
						if (quest.getVerticalBridge(column, row, left))
							out.println(left + " VerticalBridge");

				out.println("EndBoard");
			}

		for (int column = 0; column < quest.getWidth(); column++)
			for (int row = 0; row < quest.getHeight(); row++) {
				final Quest.Board board = quest.getBoard(column, row);

				out.println(column + " " + (quest.getHeight() - row - 1) + " StartBoard");

				for (Quest.Board.Object object : board.getObjects()) {
					int width, height;

					if (object.getRotation().isPair()) {
						width = objectList.getObjectById(object.getId()).getWidth();
						height = objectList.getObjectById(object.getId()).getHeight();
					} else {
						width = objectList.getObjectById(object.getId()).getHeight();
						height = objectList.getObjectById(object.getId()).getWidth();
					}

					float x = object.getLeft() + width / 2.0f;
					float y = object.getTop() + height / 2.0f;

					if (objectList.getObjectById(object.getId()).isTrap()) {
						out.println(object.getLeft()
								+ " "
								+ (board.getHeight() - object.getTop() - height + 2)
								+ " "
								+ width
								+ " "
								+ height
								+ " Trap");
					} else if (objectList.getObjectById(object.getId()).isDoor()) {
						if (object.getRotation().isPair()) {
							if (object.getTop() == 0)
								y -= objectList.getBoard().getBorderDoorsOffset();
							else if (object.getTop() == board.getHeight())
								y += objectList.getBoard().getBorderDoorsOffset();
						} else {
							if (object.getLeft() == 0)
								x -= objectList.getBoard().getBorderDoorsOffset();
							else if (object.getLeft() == board.getWidth())
								x += objectList.getBoard().getBorderDoorsOffset();
						}
					}

					final float xoffset = objectList.getObjectById(object.getId())
							.getIcon(quest.getRegion()).getXoffset();
					final float yoffset = objectList.getObjectById(object.getId())
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

					y = objectList.getBoard().getHeight() - y + 2;

					out.println("gsave");
					out.println(x + " Unit " + y + " Unit translate");
					out.println((object.getRotation().getNumber() * 90) + " rotate");
					out.println("Icon" + object.getId() + " execform");
					out.println("grestore");
					out.println();
				}

				out.println("EndBoard");
			}

		// HSE - output all the postscript definitions to write text
		out.println("/gs /gsave def /gr /grestore def % isolate translation, scaling and colour changes %");
		out.println("/np /newpath def /cp /closepath def % np = new line: cp = enclose polygon %");
		out.println("/mt {/moveto} def /rt /rmoveto def % rt = move relative to previous position %");
		out.println("/li /lineto def /rl /rlineto def % rl = draw line relative to previous position %");
		out.println("/ct /curveto def /tr /translate def % tr moves the 0x 0y co-ordinate to a new position %");
		out.println("/st /stroke def /set { gs setlinewidth st gr } def % use # set %");
		out.println("/gray {gs setgray fill gr} def % use # gray %");
		out.println("/ro /rotate def /rp /repeat def");
		out.println("/box { np mt rl rl rl cp set }def % composite box command: no fill %");
		out.println("/circle { np arc set }def % composite circle command: no fill %");
		out.println("/ph " + bBoxHeight + " def");
		out.println("/s /show load def /L { newline } def /n { s L } def");

		out.println("/textbox { /lm 36 def /bm 0 def /rm 502 def /tm 36 def lm tm moveto } def");
		out.println("/newline { tm 12 sub /tm exch def lm tm moveto } def");
		out.println("/centre { dup stringwidth pop 2 div rm lm sub 2 div exch sub lm add tm moveto } def");

		out.println("/n { show newline } def /c {centre n } def /s {show } def /L { newline } def");
		out.println("/Times-Roman findfont 12 scalefont setfont");

		// HSE - create the text bounding box in PS
		out.println("gsave 0 ph 120 sub translate textbox");

		// HSE - definitions to handle word wrapping
		out.println("/space ( ) def");
		out.println("/spacecount { 0 exch ( ) { search { pop 3 -1 roll 1 add 3 1 roll } { pop exit } ifelse } loop } def");
		out.println("/toofar? { ( ) search pop dup stringwidth pop currentpoint pop add rm gt } def");
		out.println("/a { tm exch sub TM lm tm moveto } bind def");
		out.println("/LG { /lg exch def } def 12 LG");
		out.println("/S { dup spacecount { toofar? { L s s } { s s } ifelse } repeat pop } bind def");
		out.println("/P { S L } bind def % paragraph advance %");

		// HSE - output the quest name in dark red
		out.println("0.50 0 0.20 setrgbcolor (" + sanitize(quest.getName()) + ") c newline");

		// HSE - output the quest speech including line feeds
		out.println("0 0 0 setrgbcolor");
		for (String linefeed : quest.getSpeech().split("\n"))
			out.println("(" + sanitize(linefeed) + " ) S L");

		// HSE - output the notes in regular black font, smaller line spacing
		out.println("/LG { /lg exch def } def 10 LG");
		out.println("/newline { tm 10 sub /tm exch def lm tm moveto } def");
		out.println("/Times-Roman findfont 10 scalefont setfont");
		for (String note : quest.getNotesForUI()) {
			out.println("newline newline (" + sanitize(note) + " ) S");
		}

		// HSE - output the wandering monster
		out.println("grestore");
		out.println("gsave 0 ph %s sub translate textbox", 430);
		final String wanderingMonsterName = objectList.getObjectById(quest.getWanderingId()).getName();
		out.println("(Wandering Monster in this Quest: " + sanitize(wanderingMonsterName) + " ) c");

		out.println("170 (" + sanitize(wanderingMonsterName) + ") stringwidth pop 2 div sub 40 translate");
		out.println("Icon" + quest.getWanderingId() + " execform");

		// HSE - restore the coords
		out.println("grestore");

		out.println("end");
		out.println("%%EOF");

		out.close();
	}

	public static void writeMultiPage(PaperType paperType,
									  File file,
									  Quest quest,
									  ObjectList objects) throws Exception {
		final FormatterWriter out = new FormatterWriter(new PrintWriter(new BufferedWriter(new FileWriter(file))));

		final float boardXPosition = calculateBoardXPosition(paperType);
		final float boardYPosition = calculateBoardYPosition(paperType);

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

		int pageCount = 1;

		// loop through each board, generating a new page for each one
		for (int column = 0; column < quest.getWidth(); column++) {
			for (int row = 0; row < quest.getHeight(); row++) {
				final Quest.Board board = quest.getBoard(column, row);

				int pageMaxNumberOfLines = HALF_PAGE_MAX_LINES;
				out.println("%%Page: %s %s",
						pageCount,
						pageCount);
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

				// HSE - text area
				out.println("/Times-Roman findfont 16 scalefont setfont");

				// HSE - create the text bounding box in PS
				out.println("gsave 0 ph %d sub translate textbox",
						paperType.getHeight() / 2 +
								roundPercentage(paperType.getHeight(), 7.9f)); // 440  2.256f%

				// HSE - output the quest name in dark red
				out.println("0.50 0 0.20 setrgbcolor (%s) c newline",
						sanitize(quest.getName()));

				// HSE - output the quest speech including line feeds
				out.println("/Times-Roman findfont 12 scalefont setfont");
				out.println("0 0 0 setrgbcolor");

				int numberOfLinePage = 0;
				final int speechLines = GhostscriptUtils.numberOfLines(quest.getSpeech(), 12);
				log.info("Speech. number of lines: {}", speechLines);
				numberOfLinePage += speechLines;
				for (String linefeed : quest.getSpeech().split("\n")) {
					out.println("(%s ) S L",
							sanitize(linefeed));
				}

				// HSE - output the notes in regular black font, smaller line spacing
				out.println("/LG { /lg exch def } def 10 LG");
				out.println("/newline { tm 10 sub /tm exch def lm tm moveto } def");
				out.println("/Times-Roman findfont 10 scalefont setfont");
				for (String note : quest.getNotesForUI()) {
					final int lines = GhostscriptUtils.numberOfLines(note, 10);
					log.info("number of lines: {}", lines);
					numberOfLinePage += lines;
					if (numberOfLinePage > pageMaxNumberOfLines) {
						pageMaxNumberOfLines = FULL_PAGE_MAX_LINES;
						numberOfLinePage = 0;
						printWanderingMonster(paperType, quest, objects, out);

//							out.println("grestore");

						out.println("sysshowpage");
						out.println("%%EndPage");

						out.println("%%Page: %s %s",
								++pageCount,
								pageCount);

//							out.println("grestore");
						out.println("/LG { /lg exch def } def 10 LG");
						out.println("/newline { tm 10 sub /tm exch def lm tm moveto } def");
						out.println("/Times-Roman findfont 10 scalefont setfont");

						// HSE - create the text bounding box in PS
//							out.println("gsave 20 ph %d sub translate textbox",
//									paperType.getHeight());
						out.println("gsave 0 ph %d sub translate textbox",
								roundPercentage(paperType.getHeight(), 7.9f)); // 440  2.256f%
					}
					for (String noteLine : note.split("\n")) {


						out.println("newline (%s ) S",
								sanitize(noteLine));
					}
					out.println("newline");
				}
				// HSE - output board location if multi board quest
				if (quest.getWidth() > 1 || quest.getHeight() > 1) {
					out.println("newline newline (Board Location: \\(%d,%d\\) ) S",
							column,
							row);
				}

				// HSE - output the wandering monster
				printWanderingMonster(paperType, quest, objects, out);

				// HSE - restore the coords
				out.println("grestore");

				out.println("sysshowpage");
				out.println("%%EndPage");

				pageCount++;
			}
		}

		out.println("end");
		out.println("%%EOF");

		out.close();
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
				paperType.getWidth() / 2 - 90,
				sanitize(wanderingMonster.getName()));
		out.println("Icon%s execform",
				wanderingMonster.getId());
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

	private static float calculateBoardYPosition(PaperType paperType) {
		// TODO calculate dynamically
		switch (paperType) {
			case A4:
				return 1.07f;
			case LETTER:
				return 1.0f;
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
			if (data.trim().length() > 0 && data.trim().charAt(0) != '%') {
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
