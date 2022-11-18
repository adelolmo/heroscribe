/*
  Copyright (C) 2002-2004 Flavio Chierichetti and Valerio Chierichetti

  HeroScribe Enhanced (changes are prefixed with HSE in comments)
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

package org.lightless.heroscribe.gui;

import org.apache.commons.io.*;
import org.lightless.heroscribe.*;
import org.lightless.heroscribe.helper.*;
import org.lightless.heroscribe.iconpack.*;
import org.slf4j.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.*;
import java.util.stream.*;

import static java.lang.String.*;

public class IconPackDownloadModal extends JPanel {

	private static final Logger log = LoggerFactory.getLogger(IconPackDownloadModal.class);

	private final WebsiteParser websiteParser = new WebsiteParser();

	private final List<JCheckBox> iconPackCheckBoxes = new ArrayList<>();
	private final Box box;

	private final List<WebsiteParser.IconPackDetails> iconPackDetails = new ArrayList<>();
	private final IconPackService iconPackService;


	public IconPackDownloadModal(IconPackService iconPackService) {
		super();
		this.iconPackService = iconPackService;
		setLayout(new BorderLayout());

		final JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(600, 400));
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		box = new Box(BoxLayout.Y_AXIS);
		box.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		final JScrollPane jScrollPane = new JScrollPane(box);

		panel.add(new JLabel("Select the Icon Packs to install from www.heroscribe.org:"));
		panel.add(jScrollPane, BorderLayout.PAGE_START);

		add(panel, BorderLayout.NORTH);
	}

	public void showDialog() {

		try {
			iconPackDetails.addAll(websiteParser.parse());
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this,
					format("Can't read Icon Packs remote content.\nDetailed Error: %s",
							e.getMessage()),
					"Error",
					JOptionPane.ERROR_MESSAGE);
			log.warn("Cannot parse website for icon pack", e);
		}

		for (final WebsiteParser.IconPackDetails iconPack : iconPackDetails) {
			final JCheckBox checkBox = new JCheckBox(iconPack.getName());
			iconPackCheckBoxes.add(checkBox);
			box.add(checkBox);
		}

		if (JOptionPane.showOptionDialog(null,
				this,
				"Download",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE,
				null,
				null,
				null) == JOptionPane.YES_OPTION) {
			final List<DownloadReport> downloadReports = new ArrayList<>();

			new Thread(new DialogThread(this))
					.start();

			final SwingWorker<Void, String> worker = new SwingWorker<>() {
				@Override
				protected Void doInBackground() throws Exception {
					for (JCheckBox checkBox : iconPackCheckBoxes) {
						if (!checkBox.isSelected()) {
							continue;
						}

						for (WebsiteParser.IconPackDetails pack : getSelectedIconPacks(checkBox)) {
							downloadReports.add(downloadAndInstallIconPack(pack));
						}
					}
					return null;
				}

				@Override
				protected void process(List<String> chunks) {
					super.process(chunks);
				}

				@Override
				protected void done() {
					super.done();
					for (Window window : Window.getWindows()) {
						if (window instanceof JDialog) {
							JDialog dialog = (JDialog) window;
							dialog.dispose();
						}
					}

					showReportDialog(downloadReports);

				}
			};
			worker.addPropertyChangeListener(evt -> {
				System.out.println(evt.getNewValue());
			});
			worker.execute();
		}
	}

	private void showReportDialog(List<DownloadReport> downloadReports) {
		final List<String> reportEntries = downloadReports.stream()
				.map(downloadReport -> {
					if (downloadReport.isSuccessful()) {
						return format("\"%s\" installed.",
								downloadReport.getIconPackDetails().getName());
					}
					return format("\"%s\" not installed.\n   Reason is: %s",
							downloadReport.getIconPackDetails().getName(),
							downloadReport.getMessage().substring(0, 100));
				}).collect(Collectors.toList());

		JOptionPane.showMessageDialog(this,
				String.join("\n", reportEntries),
				"Download Results",
				JOptionPane.INFORMATION_MESSAGE);
	}

	private DownloadReport downloadAndInstallIconPack(WebsiteParser.IconPackDetails pack) {
		final File iconPackFile =
				new File(Constants.getIconPackDirectory(), pack.getFilename());

		try {
			HseFileUtils.downloadToFile(pack.getLink(), iconPackFile);
			iconPackService.importIconPack(iconPackFile);
			return DownloadReport.ofSuccess(pack);
		} catch (IOException e) {
			log.warn("Cannot install icon pack: {}", pack.getName(), e);
			FileUtils.deleteQuietly(iconPackFile);
			return DownloadReport.ofFailure(pack, e.getMessage());
		}
	}

	private List<WebsiteParser.IconPackDetails> getSelectedIconPacks(JCheckBox checkBox) {
		return iconPackDetails.stream()
				.filter(iconPack -> checkBox.getText().equals(iconPack.getName()))
				.collect(Collectors.toList());
	}

	private static class DownloadReport {
		private final boolean successful;
		private final WebsiteParser.IconPackDetails iconPackDetails;
		private final String message;

		private DownloadReport(boolean successful,
							   WebsiteParser.IconPackDetails iconPackDetails,
							   String message) {
			this.successful = successful;
			this.iconPackDetails = iconPackDetails;
			this.message = message;
		}

		private static DownloadReport ofSuccess(WebsiteParser.IconPackDetails iconPackDetails) {
			return new DownloadReport(true, iconPackDetails, "");
		}

		public static DownloadReport ofFailure(WebsiteParser.IconPackDetails iconPackDetails,
											   String message) {
			return new DownloadReport(false, iconPackDetails, message);
		}

		public boolean isSuccessful() {
			return successful;
		}

		public WebsiteParser.IconPackDetails getIconPackDetails() {
			return iconPackDetails;
		}

		public String getMessage() {
			return message;
		}
	}

	private static class DialogThread implements Runnable {
		private final IconPackDownloadModal modal;

		public DialogThread(IconPackDownloadModal iconPackDownloadModal) {
			this.modal = iconPackDownloadModal;
		}

		@Override
		public void run() {
			JOptionPane.showConfirmDialog(modal,
					"Downloading Icon Packs, please wait",
					null,
					JOptionPane.DEFAULT_OPTION,
					JOptionPane.PLAIN_MESSAGE);
		}
	}
}