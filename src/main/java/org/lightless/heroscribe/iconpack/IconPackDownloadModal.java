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

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.lightless.heroscribe.Constants;
import org.lightless.heroscribe.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.lightless.heroscribe.utils.SwingUtils.createLabelPanel;
import static org.lightless.heroscribe.utils.SwingUtils.createVerticalScrollBar;

public class IconPackDownloadModal extends JPanel {

	private static final Logger log = LoggerFactory.getLogger(IconPackDownloadModal.class);

	private final WebsiteParser websiteParser = new WebsiteParser();
	private final List<JCheckBox> iconPackCheckBoxes = new ArrayList<>();
	private final List<WebsiteParser.IconPackDetails> iconPackDetails = new ArrayList<>();
	private final List<WebsiteParser.IconPackDetails> iconPackDetailsCache = new ArrayList<>();

	private final IconPackService iconPackService;
	private final Box box;

	public IconPackDownloadModal(IconPackService iconPackService) {
		super();
		this.iconPackService = iconPackService;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setPreferredSize(new Dimension(600, 400));

		box = new Box(BoxLayout.Y_AXIS);
		final JScrollPane jScrollPane = new JScrollPane(box);
		jScrollPane.setVerticalScrollBar(createVerticalScrollBar(jScrollPane));
		jScrollPane.setPreferredSize(new Dimension(600, 375));

		add(createLabelPanel("Select the Icon Packs to install from www.heroscribe.org:"));
		add(jScrollPane);
	}

	public void showDialog() {
		iconPackDetails.clear();
		iconPackCheckBoxes.clear();
		box.removeAll();
		try {
			iconPackDetails.addAll(downloadIconPackDetailsAndCache());
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
			worker.execute();
		}
	}

	private List<WebsiteParser.IconPackDetails> downloadIconPackDetailsAndCache() throws IOException {
		if (iconPackDetailsCache.isEmpty()) {
			iconPackDetailsCache.addAll(websiteParser.parse());
		}
		return iconPackDetailsCache;
	}

	private void showReportDialog(List<DownloadReport> downloadReports) {
		final List<String> reportEntries = downloadReports.stream()
				.map(downloadReport -> {
					if (downloadReport.isSuccessful()) {
						return format("\"%s\" installed.",
								downloadReport.getIconPackDetails().getName());
					}
					return format("\"%s\" not installed.\nIcon pack link: %s\n   Reason is: %s",
							downloadReport.getIconPackDetails().getName(),
							downloadReport.getIconPackDetails().getLink(),
							downloadReport.message());
				})
				.collect(Collectors.toList());

		JOptionPane.showMessageDialog(this,
				String.join("\n\n", reportEntries),
				"Download Results",
				JOptionPane.INFORMATION_MESSAGE);
	}

	private DownloadReport downloadAndInstallIconPack(WebsiteParser.IconPackDetails pack) {
		final File iconPackFile =
				new File(Constants.getIconPackDirectory(), pack.getFilename());

		try {
			FileUtils.downloadToFile(pack.getLink(), iconPackFile);
			iconPackService.importIconPack(iconPackFile);
			return DownloadReport.ofSuccess(pack);
		} catch (IOException e) {
			log.warn("Cannot install icon pack: {}", pack.getName(), e);
			org.apache.commons.io.FileUtils.deleteQuietly(iconPackFile);
			return DownloadReport.ofFailure(pack, e);
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

		public static DownloadReport ofFailure(WebsiteParser.IconPackDetails iconPackDetails, IOException exception) {
			if (exception instanceof UnrecognizedPropertyException) {
				return new DownloadReport(false, iconPackDetails,
						format("The pack is not compatible with %s\n   Error: %s",
								Constants.APPLICATION_NAME, exception.getMessage().split("\n")[0]));
			}
			return new DownloadReport(false, iconPackDetails, exception.getMessage());
		}

		public boolean isSuccessful() {
			return successful;
		}

		public WebsiteParser.IconPackDetails getIconPackDetails() {
			return iconPackDetails;
		}

		public String message() {
			return message == null ? "" : message;
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