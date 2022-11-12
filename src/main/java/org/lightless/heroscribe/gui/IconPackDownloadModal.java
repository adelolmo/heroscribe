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

	private final WebsiteParser websiteParser = new WebsiteParser();

	private final List<JCheckBox> iconPackCheckBoxes = new ArrayList<>();
	private final Box box;

	private final List<WebsiteParser.IconPackDetails> iconPackDetails = new ArrayList<>();
	private final IconPackService iconPackService;


	public IconPackDownloadModal(IconPackService iconPackService) {
		super();
		this.iconPackService = iconPackService;
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		final JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(600, 400));
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		box = new Box(BoxLayout.Y_AXIS);
		panel.add(new JLabel("Select the Icon Packs to install"));
		panel.add(box);

		add(panel, BorderLayout.NORTH);

	}

	public void showDialog() {

		try {
			iconPackDetails.addAll(websiteParser.parse());

			for (int i = 0; i < 10; i++) {
				final WebsiteParser.IconPackDetails iconPack = iconPackDetails.get(i);
				final JCheckBox checkBox = new JCheckBox(iconPack.getName());
				iconPackCheckBoxes.add(checkBox);
				checkBox.addItemListener(this);
				box.add(checkBox);
			}

//			for (JCheckBox checkBox : iconPackCheckBoxes) {
//				box.add(checkBox);
//			}


		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		final int option = JOptionPane.showOptionDialog(null,
				this,
				"www.heroscribe.org",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE,
				null,
				null,
				null);
		if (option == JOptionPane.YES_OPTION) {
			for (JCheckBox checkBox : iconPackCheckBoxes) {
				if (!checkBox.isSelected()) {
					continue;
				}

				final List<WebsiteParser.IconPackDetails> packDetails = iconPackDetails.stream()
						.filter(iconPack -> checkBox.getText().equals(iconPack.getName()))
						.collect(Collectors.toList());
				for (WebsiteParser.IconPackDetails pack : packDetails) {
					final File iconPackFile = new File(Constants.getBundleDirectory(), pack.getFilename());

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
			}
		}
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