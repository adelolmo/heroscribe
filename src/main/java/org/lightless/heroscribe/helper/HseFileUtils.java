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

package org.lightless.heroscribe.helper;

import org.apache.commons.io.*;
import org.slf4j.*;

import java.io.*;
import java.net.*;

public class HseFileUtils {

	private static final Logger log = LoggerFactory.getLogger(HseFileUtils.class);

	public static void downloadToFile(String sourceUrl, File targetFile) throws IOException {
		log.info("Download {} to {}", sourceUrl, targetFile.getAbsolutePath());

		final OutputStream os = new FileOutputStream(targetFile);
		final InputStream is = new URL(sourceUrl).openStream();

		IOUtils.copy(is, os);
	}
}