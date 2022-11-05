/*
  HeroScribe
  Copyright (C) 2002-2004 Flavio Chierichetti and Valerio Chierichetti

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

import org.lightless.heroscribe.list.List;
import org.lightless.heroscribe.quest.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;

public class ExportEPS {

	private static final int linesPerBlock = 500;

	private ExportEPS() {
	}

	private static int[] appendPS(String inPath, PrintWriter out, boolean printComments, boolean divideInBlocks) throws Exception {

		BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(inPath))));

		int[] boundingBox = null;

		String data = in.readLine();
		int lines = 0;

		while (data != null) {
			if (printComments || (data.trim().length() > 0 && data.trim().charAt(0) != '%')) {
				if (divideInBlocks && lines % linesPerBlock == 0) {
					if (lines != 0)
						out.write(" } exec ");
					out.write(" { ");
				}

				lines++;
				out.println(data);
			}

			if (data.trim().startsWith("%%BoundingBox:") && boundingBox == null) {
				String[] bit = data.split(" ");
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

	public static void write(File file, Quest quest, List objects) throws Exception {
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));


		float bBoxWidth, bBoxHeight;

		// HSE - set the box height to accommodate quest text
		bBoxWidth = (quest.getWidth() * (quest.getBoard(0, 0).getWidth() + 2) + (quest.getWidth() - 1) * objects.getBoard().adjacentBoardsOffset)
				* 19.2f; //612.0f

		bBoxHeight = ((quest.getHeight() * (quest.getBoard(0, 0).getHeight() + 2) + (quest.getHeight() - 1) * objects.getBoard().adjacentBoardsOffset)
				* 19.2f) + 400; //792.0f + (400*(quest.getHeight()-1))

		out.println("%!PS-Adobe-3.0 EPSF-3.0");
		out.println("%%LanguageLevel: 2");
		out.println("%%BoundingBox: 0 0 " + Math.round(Math.ceil(bBoxWidth)) + " " + Math.round(Math.ceil(bBoxHeight)));
		out.println("%%HiResBoundingBox: 0 0 " + bBoxWidth + " " + bBoxHeight);

		out.println("/adjacentBoardsOffset " + objects.getBoard().adjacentBoardsOffset + " def");

		appendPS(objects.getVectorPath(quest.getRegion()), out, false, false);

		out.println(quest.getWidth() + " " + quest.getHeight() + " BoundingBox");
		out.println("2 dict dup dup /showpage {} put /setpagedevice {} put begin");

		TreeSet<String> set = new TreeSet<>();
		for (int i = 0; i < quest.getWidth(); i++)
			for (int j = 0; j < quest.getHeight(); j++) {
				Iterator<QObject> boardIterator = quest.getBoard(i, j).iterator();

				while (boardIterator.hasNext()) {
					var qObject = (QObject) boardIterator.next();
					set.add(qObject.id);
				}
			}

		Iterator<String> iterator;
		iterator = set.iterator();
		while (iterator.hasNext()) {
			String id = (String) iterator.next();
			int[] boundingBox;

			out.println("/Icon" + id + " << /FormType 1 /PaintProc { pop");

			/* the postscript is divided in "{ } exec" blocks to broaden
			 * compatibility
			 */
			boundingBox = appendPS(objects.getVectorPath(id, quest.getRegion()), out, false, true);

			out.println(" } bind /Matrix [1 0 0 1 0 0] /BBox [" + boundingBox[0] + " " + boundingBox[1] + " " + boundingBox[2] + " " + boundingBox[3]
					+ "] >> def");
		}
		// HSE - add wandering monster object
		String id = quest.getWanderingID();
		int[] boundingBox;

		out.println("/Icon" + id + " << /FormType 1 /PaintProc { pop");

		/* the postscript is divided in "{ } exec" blocks to broaden
		 * compatibility
		 */
		boundingBox = appendPS(objects.getVectorPath(id, quest.getRegion()), out, false, true);

		out.println(" } bind /Matrix [1 0 0 1 0 0] /BBox [" + boundingBox[0] + " " + boundingBox[1] + " " + boundingBox[2] + " " + boundingBox[3]
				+ "] >> def");
		// END wandering monster object

		for (int column = 0; column < quest.getWidth(); column++)
			for (int row = 0; row < quest.getHeight(); row++) {
				QBoard board = quest.getBoard(column, row);

				out.println(column + " " + (quest.getHeight() - row - 1) + " StartBoard");

				for (int i = 1; i <= board.getWidth(); i++)
					for (int j = 1; j <= board.getHeight(); j++)
						if (objects.board.corridors[i][j])
							out.println(i + " " + (board.getHeight() - j + 1) + " 1 1 Corridor");

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
				QBoard board = quest.getBoard(column, row);

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
				QBoard board = quest.getBoard(column, row);

				out.println(column + " " + (quest.getHeight() - row - 1) + " StartBoard");

				Iterator<QObject> boardIterator = board.iterator();
				while (iterator.hasNext()) {
					QObject obj = boardIterator.next();
					int width, height;
					float x, y, xoffset, yoffset;

					if (obj.rotation % 2 == 0) {
						width = objects.getObject(obj.id).width;
						height = objects.getObject(obj.id).height;
					} else {
						width = objects.getObject(obj.id).height;
						height = objects.getObject(obj.id).width;
					}

					x = obj.left + width / 2.0f;
					y = obj.top + height / 2.0f;

					if (objects.getObject(obj.id).trap) {
						out.println(obj.left + " " + (board.getHeight() - obj.top - height + 2) + " " + width + " " + height + " Trap");
					} else if (objects.getObject(obj.id).door) {
						if (obj.rotation % 2 == 0) {
							if (obj.top == 0)
								y -= objects.getBoard().borderDoorsOffset;
							else if (obj.top == board.getHeight())
								y += objects.getBoard().borderDoorsOffset;
						} else {
							if (obj.left == 0)
								x -= objects.getBoard().borderDoorsOffset;
							else if (obj.left == board.getWidth())
								x += objects.getBoard().borderDoorsOffset;
						}
					}

					xoffset = objects.getObject(obj.id).getIcon(quest.getRegion()).xoffset;
					yoffset = objects.getObject(obj.id).getIcon(quest.getRegion()).yoffset;

					switch (obj.rotation) {
						case 0:
							x += xoffset;
							y += yoffset;
							break;

						case 1:
							x += yoffset;
							y -= xoffset;
							break;

						case 2:
							x -= xoffset;
							y -= yoffset;
							break;

						case 3:
							x -= yoffset;
							y += xoffset;
							break;
					}

					y = objects.getBoard().height - y + 2;

					out.println("gsave");
					out.println(x + " Unit " + y + " Unit translate");
					out.println((obj.rotation * 90) + " rotate");
					out.println("Icon" + obj.id + " execform");
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
		out.println("0.50 0 0.20 setrgbcolor (" + quest.getName().replace("(", "\\(").replace(")", "\\)") + ") c newline");

		// HSE - output the quest speech including line feeds
		out.println("0 0 0 setrgbcolor");
		String[] linefeeds = quest.getSpeech().split("\n");
		for (String linefeed : linefeeds)
			out.println("(" + linefeed.replace("(", "\\(").replace(")", "\\)") + " ) S L");

		// HSE - output the notes in regular black font, smaller line spacing
		out.println("/LG { /lg exch def } def 10 LG");
		out.println("/newline { tm 10 sub /tm exch def lm tm moveto } def");
		out.println("/Times-Roman findfont 10 scalefont setfont");
		iterator = quest.notesIterator();
		while (iterator.hasNext()) {
			String note = (String) iterator.next();
			out.println("newline newline (" + note.replace("(", "\\(").replace(")", "\\)") + " ) S");
		}

		// HSE - output the wandering monster
		//QObject obj =  quest.getWandering();

		out.println("grestore");
		out.println("gsave 0 ph 430 sub translate textbox");
		out.println("(Wandering Monster in this Quest: " + quest.getWandering().replace("(", "\\(").replace(")", "\\)") + " ) c");

		out.println("170 (" + quest.getWandering().replace("(", "\\(").replace(")", "\\)") + ") stringwidth pop 2 div sub 40 translate");
		out.println("Icon" + quest.getWanderingID() + " execform");

		// HSE - restore the coords
		out.println("grestore");

		out.println("end");
		out.println("%%EOF");

		out.close();
	}

	public static void writeMultiPage(File file, Quest quest, List objects) throws Exception {
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));

		float bBoxWidth, bBoxHeight;

		// HSE - set the box height to accommodate quest text
		bBoxWidth = (quest.getBoard(0, 0).getWidth() + 2 + objects.getBoard().adjacentBoardsOffset) * 19.2f;

		bBoxHeight = ((quest.getBoard(0, 0).getHeight() + 2 + objects.getBoard().adjacentBoardsOffset) * 19.2f) + 400;

		out.println("%!PS-Adobe-3.0");
		out.println("%%LanguageLevel: 2");
		out.println("%%BoundingBox: 0 0 " + Math.round(Math.ceil(bBoxWidth)) + " " + Math.round(Math.ceil(bBoxHeight)));
		out.println("%%HiResBoundingBox: 0 0 " + bBoxWidth + " " + bBoxHeight);
		out.println("%%Pages: " + (quest.getHeight() * quest.getWidth()));
		out.println("/adjacentBoardsOffset " + objects.getBoard().adjacentBoardsOffset + " def");

		appendPS(objects.getVectorPath(quest.getRegion()), out, false, false);

		out.println(quest.getWidth() + " " + quest.getHeight() + " BoundingBox");
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
		out.println("/ph " + bBoxHeight + " def");
		out.println("/s /show load def /L { newline } def /n { s L } def");
		out.println("/textbox { /lm 36 def /bm 0 def /rm 502 def /tm 36 def lm tm moveto } def");
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

		TreeSet<String> set = new TreeSet<>();
		for (int i = 0; i < quest.getWidth(); i++)
			for (int j = 0; j < quest.getHeight(); j++) {
				Iterator<QObject> boardIterator = quest.getBoard(i, j).iterator();

				while (boardIterator.hasNext())
					set.add(boardIterator.next().id);
			}


		for (String id : set) {
			int[] boundingBox;

			out.println("/Icon" + id + " << /FormType 1 /PaintProc { pop");

			/* the postscript is divided in "{ } exec" blocks to broaden
			 * compatibility
			 */
			boundingBox = appendPS(objects.getVectorPath(id, quest.getRegion()), out, false, true);

			out.println(" } bind /Matrix [1 0 0 1 0 0] /BBox [" + boundingBox[0] + " " + boundingBox[1] + " " + boundingBox[2] + " " + boundingBox[3]
					+ "] >> def");
		}
		// HSE - add wandering monster object
		String id = quest.getWanderingID();
		int[] boundingBox;

		out.println("/Icon" + id + " << /FormType 1 /PaintProc { pop");

		/* the postscript is divided in "{ } exec" blocks to broaden
		 * compatibility
		 */
		boundingBox = appendPS(objects.getVectorPath(id, quest.getRegion()), out, false, true);

		out.println(" } bind /Matrix [1 0 0 1 0 0] /BBox [" + boundingBox[0] + " " + boundingBox[1] + " " + boundingBox[2] + " " + boundingBox[3]
				+ "] >> def");
		// END wandering monster object

		int PageCount = 1;

		// loop through each board, generating a new page for each one
		for (int column = 0; column < quest.getWidth(); column++)
			for (int row = 0; row < quest.getHeight(); row++) {
				QBoard board = quest.getBoard(column, row);

				out.println("%%Page: " + PageCount + " " + PageCount);
				out.println("0 0 StartBoard");

				for (int i = 1; i <= board.getWidth(); i++)
					for (int j = 1; j <= board.getHeight(); j++)
						if (objects.board.corridors[i][j])
							out.println(i + " " + (board.getHeight() - j + 1) + " 1 1 Corridor");

				for (int i = 1; i <= board.getWidth(); i++)
					for (int j = 1; j <= board.getHeight(); j++)
						if (board.isDark(i, j))
							out.println(i + " " + (board.getHeight() - j + 1) + " 1 1 Dark");

				out.println("Grid");

				out.println("EndBoard");

				/* Bridges */
				out.println("0 0 StartBoard");

				if (column < quest.getWidth() - 1)
					for (int top = 1; top <= board.getHeight(); top++)
						if (quest.getHorizontalBridge(column, row, top))
							out.println((board.getHeight() - top + 1) + " HorizontalBridge");

				if (row < quest.getHeight() - 1)
					for (int left = 1; left <= board.getWidth(); left++)
						if (quest.getVerticalBridge(column, row, left))
							out.println(left + " VerticalBridge");

				out.println("EndBoard");

				/* Objects */
				out.println("0 0 StartBoard");

				Iterator<QObject> boardIterator = board.iterator();
				while (boardIterator.hasNext()) {
					QObject obj = boardIterator.next();
					int width, height;
					float x, y, xoffset, yoffset;

					if (obj.rotation % 2 == 0) {
						width = objects.getObject(obj.id).width;
						height = objects.getObject(obj.id).height;
					} else {
						width = objects.getObject(obj.id).height;
						height = objects.getObject(obj.id).width;
					}

					x = obj.left + width / 2.0f;
					y = obj.top + height / 2.0f;

					if (objects.getObject(obj.id).trap) {
						out.println(obj.left + " " + (board.getHeight() - obj.top - height + 2) + " " + width + " " + height + " Trap");
					} else if (objects.getObject(obj.id).door) {
						if (obj.rotation % 2 == 0) {
							if (obj.top == 0)
								y -= objects.getBoard().borderDoorsOffset;
							else if (obj.top == board.getHeight())
								y += objects.getBoard().borderDoorsOffset;
						} else {
							if (obj.left == 0)
								x -= objects.getBoard().borderDoorsOffset;
							else if (obj.left == board.getWidth())
								x += objects.getBoard().borderDoorsOffset;
						}
					}

					xoffset = objects.getObject(obj.id).getIcon(quest.getRegion()).xoffset;
					yoffset = objects.getObject(obj.id).getIcon(quest.getRegion()).yoffset;

					switch (obj.rotation) {
						case 0:
							x += xoffset;
							y += yoffset;
							break;

						case 1:
							x += yoffset;
							y -= xoffset;
							break;

						case 2:
							x -= xoffset;
							y -= yoffset;
							break;

						case 3:
							x -= yoffset;
							y += xoffset;
							break;
					}

					y = objects.getBoard().height - y + 2;

					out.println("gsave");
					out.println(x + " Unit " + y + " Unit translate");
					out.println((obj.rotation * 90) + " rotate");
					out.println("Icon" + obj.id + " execform");
					out.println("grestore");
					out.println();

				}

				out.println("EndBoard");

				// HSE - text area
				out.println("/Times-Roman findfont 12 scalefont setfont");

				// HSE - create the text bounding box in PS
				out.println("gsave 0 ph 120 sub translate textbox");

				// HSE - output the quest name in dark red
				out.println("0.50 0 0.20 setrgbcolor (" + quest.getName().replace("(", "\\(").replace(")", "\\)") + ") c newline");

				// HSE - output the quest speech including line feeds
				out.println("0 0 0 setrgbcolor");
				String[] linefeeds = quest.getSpeech().split("\n");
				for (String linefeed : linefeeds)
					out.println("(" + linefeed.replace("(", "\\(").replace(")", "\\)") + " ) S L");

				// HSE - output the notes in regular black font, smaller line spacing
				out.println("/LG { /lg exch def } def 10 LG");
				out.println("/newline { tm 10 sub /tm exch def lm tm moveto } def");
				out.println("/Times-Roman findfont 10 scalefont setfont");
				Iterator<String> iterator = quest.notesIterator();
				while (iterator.hasNext()) {
					String note = iterator.next();
					out.println("newline newline (" + note.replace("(", "\\(").replace(")", "\\)") + " ) S");
				}
				// HSE - output board location if multi board quest
				if (quest.getWidth() > 1 || quest.getHeight() > 1) {
					String note = "Board Location: \\(" + column + "," + row + "\\)";
					out.println("newline newline (" + note + " ) S");
				}

				// HSE - output the wandering monster
				//QObject obj =  quest.getWandering();

				out.println("grestore");
				out.println("gsave 0 ph 430 sub translate textbox");
				out.println("(Wandering Monster in this Quest: " + quest.getWandering().replace("(", "\\(").replace(")", "\\)") + " ) c");

				out.println("170 (" + quest.getWandering().replace("(", "\\(").replace(")", "\\)") + ") stringwidth pop 2 div sub 40 translate");
				out.println("Icon" + quest.getWanderingID() + " execform");

				// HSE - restore the coords
				out.println("grestore");

				out.println("sysshowpage");
				out.println("%%EndPage");

				PageCount++;
			}

		out.println("end");
		out.println("%%EOF");

		out.close();
	}
}
