/*
  HeroScribe
  Copyright (C) 2002-2004 Flavio Chierichetti and Valerio Chierichetti

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

package org.lightless.heroscribe;

import org.lightless.heroscribe.export.*;
import org.lightless.heroscribe.utils.OS;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import javax.xml.parsers.*;
import java.io.*;

public class Preferences extends DefaultHandler {
	public File ghostscriptExec;
	public File defaultDir;
	public boolean forceIconPackInstall;
	public PaperType paperType;

	public Preferences() {
		super();

		ghostscriptExec = new File("");
		defaultDir = new File("");
		paperType = PaperType.A4;

		if (OS.isWindows()) {
			File base = new File("c:\\gs\\");

			if (base.isDirectory()) {
				File[] files = base.listFiles();

				for (File file : files) {
					if (file.isDirectory() && new File(file, "bin\\gswin32c.exe").isFile()) {
						ghostscriptExec = new File(file, "bin\\gswin32c.exe");

						break;
					}
				}
			}
		} else {
			if (new File("/usr/bin/gs").isFile())
				ghostscriptExec = new File("/usr/bin/gs");
		}
	}

	public Preferences(File file) {
		this();

		if (file.isFile()) {
			try {
				SAXParserFactory factory = SAXParserFactory.newInstance();

				SAXParser saxParser = factory.newSAXParser();
				saxParser.parse(file, this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/* Read XML */

	public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
		if ("ghostscript".equals(qName)) {
			File file = new File(attrs.getValue("path"));

			if (file.isFile()) {
				ghostscriptExec = file;
			}
		}
		if ("defaultDir".equals(qName)) {
			File file = new File(attrs.getValue("path"));
			if (file.isDirectory()) {
				defaultDir = file;
			}
		}
		if ("paperSize".equals(qName)) {
			paperType = PaperType.valueOf(attrs.getValue("type"));
		}
		if ("forceIconPackInstall".equals(qName)) {
			forceIconPackInstall = Boolean.parseBoolean(attrs.getValue("enabled"));
		}
	}

	public PaperType getPaperSize() {
		return paperType;
	}

	public void setPaperSize(PaperType paperType) {
		this.paperType = paperType;
	}

	/* Write XML */

	public void write() throws IOException {
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(Constants.PREFERENCES_FILE)));

		out.println("<?xml version=\"1.0\"?>");
		out.println("<preferences>");

		out.println("<ghostscript path=\"" + ghostscriptExec.getAbsoluteFile().toString().replaceAll("\"", "&quot;") + "\"/>");
		out.println("<defaultDir path=\"" + defaultDir.getAbsolutePath().replaceAll("\"", "&quot;") + "\"/>");
		out.println("<paperSize type=\"" + paperType.name() + "\"/>");
		out.println("<forceIconPackInstall enabled=\"" + forceIconPackInstall + "\"/>");
		out.println("</preferences>");

		out.close();
	}
}