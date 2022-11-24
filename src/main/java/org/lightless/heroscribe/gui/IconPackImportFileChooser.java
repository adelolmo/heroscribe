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

package org.lightless.heroscribe.gui;

import org.apache.commons.io.*;
import org.lightless.heroscribe.*;
import org.lightless.heroscribe.iconpack.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.*;
import java.nio.file.*;

public class IconPackImportFileChooser extends JFileChooser {
	private static final Dimension FILE_CHOOSER_DIMENSION = new Dimension(900, 700);
	private final IconPackService iconPackService;


	public IconPackImportFileChooser(File defaultDir, IconPackService iconPackService) {
		super(defaultDir);
		this.iconPackService = iconPackService;
		setPreferredSize(FILE_CHOOSER_DIMENSION);
		setDialogTitle("Import");
		setFileSelectionMode(JFileChooser.FILES_ONLY);
		setFileFilter(new ZipFileFilter());
		setAcceptAllFileFilterUsed(false);
	}

	public void showModal() {
		if (showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			final File importedIconPackFile = new File(Constants.getIconPackDirectory(),
					getSelectedFile().getName());
			try {
				Files.copy(getSelectedFile().toPath(),
						importedIconPackFile.toPath());
				iconPackService.importIconPack(importedIconPackFile);

				JOptionPane.showMessageDialog(this,
						"Icon Pack successfully imported",
						"Import",
						JOptionPane.INFORMATION_MESSAGE);
			} catch (FileAlreadyExistsException ex) {
				if (JOptionPane.showConfirmDialog(this,
						"The Icon Pack already exists.\nDo you want to replace it?",
						"Import",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
					try {
						Files.copy(getSelectedFile().toPath(),
								importedIconPackFile.toPath(),
								StandardCopyOption.REPLACE_EXISTING);
						iconPackService.importIconPack(importedIconPackFile);

						JOptionPane.showMessageDialog(this,
								"Icon Pack successfully imported",
								"Import",
								JOptionPane.INFORMATION_MESSAGE);

					} catch (IOException e) {
						handleException(importedIconPackFile, e);
					}
				}
			} catch (IOException e) {
				handleException(importedIconPackFile, e);
			}
		}
	}

	private void handleException(File importedIconPackFile, IOException e) {
		JOptionPane.showMessageDialog(this,
				"Can't import Icon Pack\nReason is: " + e.getMessage(),
				"Error",
				JOptionPane.ERROR_MESSAGE);
		FileUtils.deleteQuietly(importedIconPackFile);
	}

	static class ZipFileFilter extends FileFilter {
		@Override
		public boolean accept(File f) {
			if (f.isDirectory()) {
				return true;
			}
			if (f.getName().endsWith(".zip")) {
				return true;
			}
			return false;
		}

		@Override
		public String getDescription() {
			return null;
		}
	}
}