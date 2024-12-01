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

package org.lightless.heroscribe.iconpack;

import org.apache.commons.compress.archivers.*;
import org.apache.commons.io.*;
import org.lightless.heroscribe.HeroScribeException;

import java.io.*;
import java.nio.file.*;

public class ZipExtractor {

	protected void extract(final File zipFile, final Path targetDir) throws HeroScribeException {
		final Path zipFilePath = zipFile.toPath();
		final ArchiveStreamFactory archiveStreamFactory = new ArchiveStreamFactory();

		try (InputStream inputStream = Files.newInputStream(zipFilePath);
			 ArchiveInputStream archiveInputStream = archiveStreamFactory
					 .createArchiveInputStream(ArchiveStreamFactory.ZIP, inputStream)) {

			ArchiveEntry archiveEntry = null;
			while ((archiveEntry = archiveInputStream.getNextEntry()) != null) {
				Path path = Paths.get(targetDir.toString(), archiveEntry.getName());
				File file = path.toFile();
				if (archiveEntry.isDirectory()) {
					if (!file.isDirectory()) {
						file.mkdirs();
					}
				} else {
					File parent = file.getParentFile();
					if (!parent.isDirectory()) {
						parent.mkdirs();
					}
					try (OutputStream outputStream = Files.newOutputStream(path)) {
						IOUtils.copy(archiveInputStream, outputStream);
					}
				}
			}
		} catch (Exception e) {
			throw new HeroScribeException(e);
		}
	}

}