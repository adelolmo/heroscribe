/*
  HeroScribe Enhanced
  Copyright (C) 2011 Jason Allen

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

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.lightless.heroscribe.helper.*;

import java.io.*;

public class ExportIPDF {
	public static void write(File file, BoardPainter boardPainter) throws Exception {
		Document document = new Document(new Rectangle(0, 0, 950, 1100), 50, 50, 50, 50);
		PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
		document.open();
		boardPainter.paintPDF(writer, document);
		document.close();
	}

}