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

import org.lightless.heroscribe.iconpack.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;
import java.util.*;
import java.util.stream.*;

import static java.lang.String.*;

public class IconPackRemoveModal extends JPanel implements ItemListener {


	private final IconPackService iconPackService;
	private final List<JCheckBox> iconPackCheckBoxes = new ArrayList<>();
	private final Box box;
	private final List<IconPackService.IconPack> installedIconPacks = new ArrayList<>();

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

		panel.add(new JLabel("Select the Icon Packs to remove:"));
		panel.add(jScrollPane, BorderLayout.PAGE_START);

		add(panel, BorderLayout.NORTH);
	}

	public void showDialog() {
		installedIconPacks.addAll(iconPackService.getInstalledIconPackDetails());
		for (IconPackService.IconPack iconPack : installedIconPacks) {

			final JCheckBox checkBox = new JCheckBox(iconPack.getKindNames());
			iconPackCheckBoxes.add(checkBox);
			checkBox.addItemListener(this);
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

				for (IconPackService.IconPack iconPackFile : getSelectedIconPacks(checkBox)) {
					deleteIconPack(iconPackFile.getZipFile());
				}
			}
			JOptionPane.showMessageDialog(this,
					"Icon Pack(s) successfully removed",
					"Success",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private void deleteIconPack(File iconPackFile) {
		try {
			iconPackService.removePack(iconPackFile);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this,
					format("Can't remove Icon Pack %s.\nDetailed Error: %s",
							iconPackFile.getName(), e.getMessage()),
					"Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private List<IconPackService.IconPack> getSelectedIconPacks(JCheckBox checkBox) {
		return installedIconPacks.stream()
				.filter(iconPack -> checkBox.getText().equals(iconPack.getKindNames()))
				.collect(Collectors.toList());
	}

	@Override
	public void itemStateChanged(ItemEvent e) {

	}
}