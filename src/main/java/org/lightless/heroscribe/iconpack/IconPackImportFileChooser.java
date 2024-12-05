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

import org.apache.commons.io.FileUtils;
import org.lightless.heroscribe.Constants;
import org.lightless.heroscribe.xml.HeroScribeParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ExecutionException;

import static java.lang.String.format;
import static org.lightless.heroscribe.Constants.APPLICATION_NAME;
import static org.lightless.heroscribe.utils.SwingUtils.createLabelPanel;

public class IconPackImportFileChooser extends JFileChooser {

	private static final Logger log = LoggerFactory.getLogger(IconPackImportFileChooser.class);
	private static final Dimension FILE_CHOOSER_DIMENSION = new Dimension(900, 700);

	private final IconPackService iconPackService;
	private final JDialog pleaseWaitDialog;

	public IconPackImportFileChooser(File defaultDir, IconPackService iconPackService) {
		super(defaultDir);
		this.iconPackService = iconPackService;
		pleaseWaitDialog = createPleaseWaitDialog();
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

				new Thread(new DialogThread(pleaseWaitDialog))
						.start();

				installPackBackgroundWorker(importedIconPackFile, pleaseWaitDialog)
						.execute();

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

						new Thread(new DialogThread(pleaseWaitDialog))
								.start();

						installPackBackgroundWorker(importedIconPackFile, pleaseWaitDialog)
								.execute();

					} catch (IOException e) {
						handleException(importedIconPackFile, e);
					}
				}
			} catch (IOException e) {
				handleException(importedIconPackFile, e);
			}
		}
	}

	private SwingWorker<Void, String> installPackBackgroundWorker(
			File importedIconPackFile,
			JDialog pleaseWaitDialog) {
		return new SwingWorker<>() {
			@Override
			protected Void doInBackground() throws HeroScribeParseException {
				iconPackService.importIconPack(importedIconPackFile);
				return null;
			}

			@Override
			protected void done() {
				try {
					final Void unused = get();
					pleaseWaitDialog.setVisible(false);
					pleaseWaitDialog.dispose();
					JOptionPane.showMessageDialog(null,
							"Icon Pack successfully imported",
							"Import",
							JOptionPane.INFORMATION_MESSAGE);
				} catch (InterruptedException | ExecutionException e) {
					if (e.getCause() instanceof HeroScribeParseException) {
						final HeroScribeParseException exception = (HeroScribeParseException) e.getCause();
						handleException(importedIconPackFile, exception);
					}

				} finally {
					pleaseWaitDialog.setVisible(false);
					pleaseWaitDialog.dispose();
				}
			}
		};
	}

	private void handleException(File iconPackFile, IOException e) {
		log.error(e.getMessage(), e);
		FileUtils.deleteQuietly(iconPackFile);
		JOptionPane.showMessageDialog(this,
				errorMessage(e),
				"Error",
				JOptionPane.ERROR_MESSAGE);
	}

	private JDialog createPleaseWaitDialog() {
		final JDialog dialog = new JDialog();
		dialog.setModal(true);
		dialog.setResizable(false);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setSize(new Dimension(290, 126));  // used only to center the dialog
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(false);
		dialog.setContentPane(new JOptionPane(
				"Installing Icon Pack, please wait...",
				JOptionPane.INFORMATION_MESSAGE,
				JOptionPane.DEFAULT_OPTION,
				null,
				new Object[]{},
				null));
		return dialog;
	}

	private static JPanel errorMessage(IOException e) {
		final JTextArea textArea = new JTextArea(4, 80);
		textArea.setWrapStyleWord(true);
		textArea.setAutoscrolls(false);
		textArea.setLineWrap(true);
		textArea.setText(e.getMessage());
		if (e.getCause() != null) {
			textArea.setText(e.getMessage() + "\n" + e.getCause().getMessage());
		}
		textArea.setCaretPosition(0);  // Scroll to top

		final JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(createLabelPanel(format("The Icon Pack is not compatible with %s.", APPLICATION_NAME)));
		panel.add(createLabelPanel("Technical reason:"));
		panel.add(new JScrollPane(textArea), BorderLayout.PAGE_START);
		return panel;
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

	private static class DialogThread implements Runnable {
		private final JDialog dialog;

		public DialogThread(JDialog dialog) {
			this.dialog = dialog;
		}

		@Override
		public void run() {
			dialog.pack();
			dialog.setVisible(true);
		}
	}
}