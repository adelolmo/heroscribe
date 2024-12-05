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

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.lightless.heroscribe.utils.SwingUtils.*;

public class IconPackRemoveModal extends JPanel {

	private final IconPackService iconPackService;
	private final Box box;
	private final JDialog pleaseWaitDialog;

	public IconPackRemoveModal(IconPackService iconPackService) {
		super();
		this.iconPackService = iconPackService;
		pleaseWaitDialog = createButtonlessDialog("Removing Icon Pack(s), please wait...");
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setPreferredSize(new Dimension(300, 300));

		box = new Box(BoxLayout.Y_AXIS);
		final JScrollPane jScrollPane = new JScrollPane(box);
		jScrollPane.setVerticalScrollBar(createVerticalScrollBar(jScrollPane));
		jScrollPane.setPreferredSize(new Dimension(300, 275));

		add(createLabelPanel("Select the Icon Packs to remove:"));
		add(jScrollPane);
	}

	public void showDialog() {
		box.removeAll();

		final List<JCheckBox> iconPackCheckBoxes = new ArrayList<>();
		for (IconPackService.IconPack iconPack : iconPackService.getInstalledIconPackDetails()) {
			final JCheckBox checkBox = new JCheckBox(iconPack.getKindNames());
			iconPackCheckBoxes.add(checkBox);
			box.add(checkBox);
		}

		if (JOptionPane.showOptionDialog(null,
				this,
				"Remove",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE,
				null,
				null,
				null) == JOptionPane.YES_OPTION) {
			for (JCheckBox checkBox : iconPackCheckBoxes) {
				if (!checkBox.isSelected()) {
					continue;
				}

				new Thread(new DialogThread(pleaseWaitDialog))
						.start();

				removePacksBackgroundWorker(checkBox)
						.execute();
			}
		}
	}

	private SwingWorker<Void, String> removePacksBackgroundWorker(JCheckBox checkBox) {
		return new SwingWorker<>() {
			@Override
			protected Void doInBackground() throws IOException {
				for (IconPackService.IconPack iconPackFile :
						getSelectedIconPacks(iconPackService.getInstalledIconPackDetails(), checkBox)) {
					iconPackService.removePack(iconPackFile.getZipFile());
				}
				return null;
			}

			@Override
			protected void done() {
				try {
					final Void unused = get();
					pleaseWaitDialog.setVisible(false);
					pleaseWaitDialog.dispose();
					JOptionPane.showMessageDialog(null,
							"Icon Pack(s) successfully removed",
							"Success",
							JOptionPane.INFORMATION_MESSAGE);
				} catch (InterruptedException | ExecutionException e) {
					JOptionPane.showMessageDialog(null,
							format("Can't remove Icon Pack.\nDetailed Error: %s", e.getMessage()),
							"Error",
							JOptionPane.ERROR_MESSAGE);
				} finally {
					pleaseWaitDialog.setVisible(false);
					pleaseWaitDialog.dispose();
				}
			}
		};
	}

	private List<IconPackService.IconPack> getSelectedIconPacks(List<IconPackService.IconPack> installedIconPacks, JCheckBox checkBox) {
		return installedIconPacks.stream()
				.filter(iconPack -> checkBox.getText().equals(iconPack.getKindNames()))
				.collect(Collectors.toList());
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