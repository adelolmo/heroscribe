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

import org.lightless.heroscribe.Preferences;
import org.lightless.heroscribe.export.PaperType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;

public class SettingsModal extends JPanel {

	private static final Logger log = LoggerFactory.getLogger(SettingsModal.class);
	private static final Dimension TEXT_INPUT_DIMENSION = new Dimension(300, 25);
	private static final Dimension FILE_CHOOSER_DIMENSION = new Dimension(600, 700);

	private final Preferences preferences;

	public SettingsModal(Preferences preferences) {
		super();
		this.preferences = preferences;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		setPreferredSize(new Dimension(600, 240));

		// Section: General
		// Default path
		final JPanel defaultPathPanel = createSectionPanel("General", 1);
		defaultPathPanel.add(new JLabel("Default directory:", SwingConstants.LEFT));
		final JTextField defaultPathTextField = createTextField(preferences.defaultDir);
		defaultPathPanel.add(defaultPathTextField);
		final JButton defaultPathButton = new JButton("Select...");
		defaultPathButton.addActionListener(
				defaultPathActionListener(preferences, defaultPathTextField)
		);
		defaultPathPanel.add(defaultPathButton);

		// Section: Icon Packs
		// Force install
		final JPanel iconPacksPanel = createSectionPanel("Icon Packs", 1);
		final JCheckBox forceInstallCheckBox = new JCheckBox("Force installation of incompatible Icon Packs");
		forceInstallCheckBox.setToolTipText("It might lead to broken functionality e.g. Unable to export");
		forceInstallCheckBox.setSelected(preferences.forceIconPackInstall);
		forceInstallCheckBox.addActionListener(e ->
				preferences.forceIconPackInstall = forceInstallCheckBox.isSelected());
		iconPacksPanel.add(forceInstallCheckBox);

		// Section: Export pdf
		// Ghostscript
		final JPanel exportSectionPanel = createSectionPanel("Export", 2);

		final JPanel ghostscriptPanel = createPanel();
		ghostscriptPanel.add(new JLabel("GhostScript path:", SwingConstants.LEFT));
		final JTextField ghostscriptTextField = createTextField(preferences.ghostscriptExec);
		ghostscriptPanel.add(ghostscriptTextField);

		final JFileChooser ghostscriptChooser = new JFileChooser();
		ghostscriptChooser.setFileFilter(new GhostScriptFileFilter());
		ghostscriptChooser.setPreferredSize(FILE_CHOOSER_DIMENSION);
		final JButton ghostscriptButton = new JButton("Select...");
		ghostscriptButton.addActionListener(e -> {
			ghostscriptChooser.setSelectedFile(preferences.ghostscriptExec);
			if (JFileChooser.APPROVE_OPTION == ghostscriptChooser.showOpenDialog(this)) {
				ghostscriptTextField.setText(ghostscriptChooser.getSelectedFile().getAbsolutePath());
				preferences.ghostscriptExec = ghostscriptChooser.getSelectedFile();
			}
		});
		ghostscriptPanel.add(ghostscriptButton);
		exportSectionPanel.add(ghostscriptPanel);

		// Paper Type
		final JPanel paperPanel = createPanel();
		paperPanel.add(new JLabel("Select paper size:", SwingConstants.LEFT));
		final JComboBox<PaperType> paperTypeComboBox = new JComboBox<>(PaperType.values());
		paperTypeComboBox.setSelectedItem(preferences.getPaperSize());
		paperTypeComboBox.addActionListener(e ->
				preferences.setPaperSize((PaperType) paperTypeComboBox.getSelectedItem()));
		paperPanel.add(paperTypeComboBox);
		exportSectionPanel.add(paperPanel);
	}

	private ActionListener defaultPathActionListener(
			Preferences preferences,
			JTextField defaultPathTextField) {
		return e -> {
			final JFileChooser chooser = new JFileChooser();
			chooser.setPreferredSize(FILE_CHOOSER_DIMENSION);
			chooser.setCurrentDirectory(preferences.defaultDir);
			chooser.setDialogTitle("Default Directory");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setAcceptAllFileFilterUsed(false);
			if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				defaultPathTextField.setText(chooser.getSelectedFile().getAbsolutePath());
				preferences.defaultDir = chooser.getSelectedFile();
			}
		};
	}

	public void showDialog() {
		if (JOptionPane.showOptionDialog(null,
				this,
				"Settings",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE,
				null,
				null,
				null) == JOptionPane.YES_OPTION) {

			try {
				preferences.write();
			} catch (Exception ex) {
				log.error("Error.", ex);
			}
		}
	}

	private static JTextField createTextField(File file) {
		final JTextField textField = new JTextField(file.getAbsolutePath());
		textField.setPreferredSize(TEXT_INPUT_DIMENSION);
		return textField;
	}

	private JPanel createSectionPanel(String title, int numberOfFields) {
		final JPanel panel = createPanel();
		panel.setBorder(createTitledBorder(title));
		panel.setMaximumSize(new Dimension(600, 60 * numberOfFields));
		add(panel);
		return panel;
	}

	private static JPanel createPanel() {
		final JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
//		panel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 1));
		return panel;
	}

	private static TitledBorder createTitledBorder(String title) {
		final TitledBorder border = BorderFactory.createTitledBorder(title);
		border.setTitleJustification(TitledBorder.LEFT);
//		border.setBorder(BorderFactory.createLineBorder(Color.GREEN, 1));
		return border;
	}
}