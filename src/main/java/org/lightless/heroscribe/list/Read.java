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

package org.lightless.heroscribe.list;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.lightless.heroscribe.HeroScribeException;
import org.lightless.heroscribe.helper.ResourceHelper;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class Read extends DefaultHandler {
	private List objects;

	private boolean onBoard;
	private LObject piece;
	private LBoard board;
	private StringBuffer content;

	public Read(File file) {
		super();

		objects = new List();

		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(true);

		SAXParser saxParser;
		try {
			saxParser = factory.newSAXParser();
			saxParser.parse(file, this);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new HeroScribeException(e);
		}
	}

	public List getObjects() {
		return objects;
	}

	/* --- */

	public InputSource resolveEntity(String publicId, String systemId) {
		if (publicId.equals("-//org.lightless//HeroScribe Object List 1.5//EN")) {
			InputStream is = ResourceHelper.getResourceAsStream("DtdXsd/objectList-1.5.dtd");
			return new InputSource(is);
		} else
			return null;
	}

	public void error(SAXParseException e) throws SAXException {
		throw new SAXException(e);
	}

	public void startDocument() {
		content = new StringBuffer();
		onBoard = false;
	}

	public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
		content = new StringBuffer();

		if (qName == "objectList") {
			objects.version = attrs.getValue("version");

			if (!objects.version.equals(org.lightless.heroscribe.Constants.objectVersion))
				throw new SAXException("HeroScribe's objectVersion and Objects.xml's version numbers don't match.");

			objects.vectorPrefix = attrs.getValue("vectorPrefix");
			objects.vectorSuffix = attrs.getValue("vectorSuffix");

			objects.rasterPrefix = attrs.getValue("rasterPrefix");
			objects.rasterSuffix = attrs.getValue("rasterSuffix");

			objects.samplePrefix = attrs.getValue("samplePrefix");
			objects.sampleSuffix = attrs.getValue("sampleSuffix");
		} else if (qName == "kind") {
			objects.kinds.add(new Kind(attrs.getValue("id"), attrs.getValue("name")));
		} else if (qName == "board") {
			board = new LBoard(Integer.parseInt(attrs.getValue("width")), Integer.parseInt(attrs.getValue("height")));

			board.borderDoorsOffset = Float.parseFloat(attrs.getValue("borderDoorsOffset"));

			board.adjacentBoardsOffset = Float.parseFloat(attrs.getValue("adjacentBoardsOffset"));

			onBoard = true;
		} else if (qName == "object") {
			piece = new LObject();

			piece.id = attrs.getValue("id");
			piece.name = attrs.getValue("name");

			piece.kind = attrs.getValue("kind");

			piece.door = Boolean.valueOf(attrs.getValue("door")).booleanValue();
			piece.trap = Boolean.valueOf(attrs.getValue("trap")).booleanValue();

			piece.width = Integer.parseInt(attrs.getValue("width"));
			piece.height = Integer.parseInt(attrs.getValue("height"));

			piece.zorder = Float.parseFloat(attrs.getValue("zorder"));

			piece.note = null;
		} else if (qName == "icon") {
			Icon icon = new Icon();

			icon.path = attrs.getValue("path");
			icon.xoffset = Float.parseFloat(attrs.getValue("xoffset"));
			icon.yoffset = Float.parseFloat(attrs.getValue("yoffset"));

			icon.original = Boolean.valueOf(attrs.getValue("original")).booleanValue();

			if (onBoard)
				board.putIcon(icon, attrs.getValue("region"));
			else
				piece.putIcon(icon, attrs.getValue("region"));
		} else if (qName == "corridor") {
			if (onBoard) {
				int width, height;
				int left, top;

				width = Integer.parseInt(attrs.getValue("width"));
				height = Integer.parseInt(attrs.getValue("height"));
				left = Integer.parseInt(attrs.getValue("left"));
				top = Integer.parseInt(attrs.getValue("top"));

				if (left + width - 1 > board.width || left < 1 || top + height - 1 > board.height || top < 1)
					throw new SAXException("Corridors: out of border");

				for (int i = 0; i < width; i++)
					for (int j = 0; j < height; j++)
						board.corridors[i + left][j + top] = true;

			}
		}
	}

	public void characters(char[] ch, int start, int length) {
		content.append(ch, start, length);
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName == "board") {
			if (!board.region.containsKey("Europe") || !board.region.containsKey("USA"))
				throw new SAXException("There should be both icons for each board.");

			objects.board = board;

			onBoard = false;
		} else if (qName == "object") {
			if (!piece.region.containsKey("Europe") || !piece.region.containsKey("USA"))
				throw new SAXException("There should be both icons for each object.");

			objects.list.put(piece.id, piece);

		} else if (qName == "note") {
			piece.note = new String(content);
		}
	}

	public void endDocument() {
		content = null;
		piece = null;
	}
}
