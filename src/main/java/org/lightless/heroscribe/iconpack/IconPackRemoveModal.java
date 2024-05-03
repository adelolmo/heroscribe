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
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class IconPackRemoveModal extends JPanel {

	private final IconPackService iconPackService;
	private final Box box;

	public IconPackRemoveModal(IconPackService iconPackService) {
		super();
		this.iconPackService = iconPackService;
		setLayout(new BorderLayout());

		final JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(300, 300));
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		box = new Box(BoxLayout.Y_AXIS);
		box.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		final JScrollPane jScrollPane = new JScrollPane(box);
		jScrollPane.setVerticalScrollBar(createVerticalScrollBar(jScrollPane));

		panel.add(new JLabel("Select the Icon Packs to remove:"));
		panel.add(jScrollPane, BorderLayout.PAGE_START);

		add(panel, BorderLayout.NORTH);
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

				for (IconPackService.IconPack iconPackFile :
						getSelectedIconPacks(iconPackService.getInstalledIconPackDetails(), checkBox)) {
					try {
						iconPackService.removePack(iconPackFile.getZipFile());
					} catch (IOException e) {
						JOptionPane.showMessageDialog(this,
								format("Can't remove Icon Pack %s.\nDetailed Error: %s",
										iconPackFile.getZipFile().getName(), e.getMessage()),
								"Error",
								JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
			}
			JOptionPane.showMessageDialog(this,
					"Icon Pack(s) successfully removed",
					"Success",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private List<IconPackService.IconPack> getSelectedIconPacks(List<IconPackService.IconPack> installedIconPacks, JCheckBox checkBox) {
		return installedIconPacks.stream()
				.filter(iconPack -> checkBox.getText().equals(iconPack.getKindNames()))
				.collect(Collectors.toList());
	}

	private static JScrollBar createVerticalScrollBar(JScrollPane jScrollPane) {
		final JScrollBar scrollBar = jScrollPane.createVerticalScrollBar();
		scrollBar.setUnitIncrement(16);
		return scrollBar;
	}
}