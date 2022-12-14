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

import org.lightless.heroscribe.helper.*;

import javax.imageio.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;

public class ExportRaster {
	public static void write(File file, ImageFormat format, BoardPainter boardPainter) throws Exception {
		BufferedImage image = new BufferedImage(
				boardPainter.framePixelSize.width,
				boardPainter.framePixelSize.height,
				BufferedImage.TYPE_INT_RGB);

		Graphics2D g = image.createGraphics();

		boardPainter.paintWithText(null, 0, 0, g);

		ImageIO.write(image, format.name(), file);
	}

	public enum ImageFormat {
		JPEG,
		PNG
	}
}