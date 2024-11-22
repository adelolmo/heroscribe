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

import org.apache.commons.io.IOUtils;
import org.lightless.heroscribe.xml.ObjectList;
import org.lightless.heroscribe.xml.Quest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static java.lang.String.format;

public class ExportPDF {

	private static final Logger log = LoggerFactory.getLogger(ExportPDF.class);

	public static void writeThumbNail(File ghostscript,
									  File file,
									  Quest quest,
									  ObjectList objects,
									  PaperType paperType) throws Exception {
		final File eps = File.createTempFile("hsb", ".ps");
		final File pdf = File.createTempFile("hsb", ".pdf");
		ExportEPS.write(paperType, eps, quest, objects);
		final Process process = Runtime.getRuntime()
				.exec(ghostscriptCommand(ghostscript, paperType, pdf, eps));

		executeAndEvaluateProcessOutput(process, file, eps, pdf);
	}

	public static void write(File ghostscript,
							 File file,
							 Quest quest,
							 ObjectList objects,
							 PaperType paperType) throws Exception {
		final File eps = File.createTempFile("hsb", ".ps");
		final File pdf = File.createTempFile("hsb", ".pdf");
		ExportEPS.writeMultiPage(paperType, eps, quest, objects);
		final Process process = Runtime.getRuntime()
				.exec(ghostscriptCommand(ghostscript, paperType, pdf, eps));

		executeAndEvaluateProcessOutput(process, file, eps, pdf);
	}

	private static String[] ghostscriptCommand(File ghostscript, PaperType paperType, File pdf, File eps) {
		final String[] command = {
				ghostscript.getAbsoluteFile().toString(),
				"-dBATCH",
				"-dNOPAUSE",
				"-sDEVICE=pdfwrite",
				"-dFIXEDMEDIA",
				"-sDEFAULTPAPERSIZE=" + paperType.getId(),
				"-sPAPERSIZE=" + paperType.getId(),
				"-sOutputFile=" + pdf.getAbsolutePath(),
				eps.getAbsoluteFile().toString()
		};
		log.info("Ghostscript command: {}", String.join(" ", command));
		return command;
	}

	private static void executeAndEvaluateProcessOutput(Process process, File file, File eps, File pdf) throws Exception {
		log.info(IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8));
		int exitValue = process.waitFor();
		Files.delete(eps.toPath());

		if (exitValue == 0) {
			Files.move(pdf.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} else {

			Files.delete(pdf.toPath());
			final String errorMessage =
					IOUtils.toString(process.getErrorStream(), StandardCharsets.UTF_8);

			throw new Exception(format("Process execution failed! exit value: %d\n%s",
					exitValue, errorMessage));
		}
	}
}