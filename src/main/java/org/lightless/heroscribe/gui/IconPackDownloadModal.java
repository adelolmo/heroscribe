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
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;
import java.util.*;
import java.util.stream.*;

import static java.lang.String.*;

public class IconPackDownloadModal extends JPanel implements AncestorListener, ItemListener {

	private static final Logger log = LoggerFactory.getLogger(IconPackDownloadModal.class);
	private static final int ELEMENTS_PER_PAGE = 15;
	private static final int ELEMENTS_PER_COLUMN = 10;

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

		panel.add(new JLabel("Select the Icon Packs to install"));
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
			checkBox.addItemListener(this);
			box.add(checkBox);
		}

		if (JOptionPane.showOptionDialog(null,
				this,
				"www.heroscribe.org",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE,
				null,
				null,
				null) == JOptionPane.YES_OPTION) {
			for (JCheckBox checkBox : iconPackCheckBoxes) {
				if (!checkBox.isSelected()) {
					continue;
				}

				for (WebsiteParser.IconPackDetails pack : getSelectedIconPacks(checkBox)) {
					downloadAndInstallIconPack(pack);
				}
			}
		}
	}

	private void downloadAndInstallIconPack(WebsiteParser.IconPackDetails pack) {
		final File iconPackFile =
				new File(Constants.getIconPackDirectory(), pack.getFilename());

		try {
			HseFileUtils.downloadToFile(pack.getLink(), iconPackFile);
			iconPackService.importIconPack(iconPackFile);

		} catch (IOException e) {
			JOptionPane.showMessageDialog(this,
					format("Can't install Icon Pack %s.\nDetailed Error: %s",
							pack.getFilename(), e.getMessage()),
					"Error",
					JOptionPane.ERROR_MESSAGE);
			log.warn("Cannot install icon pack: {}", pack.getName(), e);
			FileUtils.deleteQuietly(iconPackFile);
		}
	}

	private List<WebsiteParser.IconPackDetails> getSelectedIconPacks(JCheckBox checkBox) {
		return iconPackDetails.stream()
				.filter(iconPack -> checkBox.getText().equals(iconPack.getName()))
				.collect(Collectors.toList());
	}


	@Override
	public void ancestorAdded(AncestorEvent event) {

	}

	@Override
	public void ancestorRemoved(AncestorEvent event) {

	}

	@Override
	public void ancestorMoved(AncestorEvent event) {

	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		System.out.println(e.getSource());
	}


}