/*
  HeroScribe
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

import org.lightless.heroscribe.list.*;
import org.lightless.heroscribe.quest.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.util.*;

public class PropertiesModal extends JPanel implements AncestorListener {

	private final Quest quest;
	private final JTextField name = new JTextField();
	private final JTextArea speech = new JTextArea(8, 50);
	private final JComboBox<LObject> wandering = new JComboBox<>();

	public PropertiesModal(Gui gui, Quest quest) {
		super();
		this.quest = quest;
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		final JLabel lName = new JLabel("Quest Name:");
		final JLabel lWandering = new JLabel("Wandering Monster:");
		final JLabel lSpeech = new JLabel("Quest Speech:");

		wandering.setLightWeightPopupEnabled(false);
		speech.setWrapStyleWord(true);
		speech.setAutoscrolls(false);
		speech.setLineWrap(true);
		speech.addAncestorListener(this);

		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(3, 0, 0, 3);
		gbc.ipadx = 20;

		final JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		panel.setLayout(new GridBagLayout());
		panel.add(lName, gbc);
		gbc.gridy = 1;
		panel.add(name, gbc);
		gbc.gridy = 2;
		panel.add(lWandering, gbc);
		gbc.gridy = 3;
		panel.add(wandering, gbc);
		gbc.gridy = 4;
		panel.add(lSpeech, gbc);
		gbc.gridy = 5;
		final JScrollPane scrollPane = new JScrollPane(speech);
		scrollPane.setPreferredSize(new Dimension(700, 180));
		panel.add(scrollPane, gbc);

		add(panel, BorderLayout.NORTH);

		// HSE - populate the combo box for the wandering monster list
		LObject defObj = new LObject();

		final Iterator<LObject> iterator = gui.getObjects().objectsIterator();
		while (iterator.hasNext()) {
			LObject obj = iterator.next();

			if (obj.kind.contentEquals("monster")) {
				wandering.addItem(obj);
				//noteData.addElement(obj);
				if (obj.name.contentEquals("Orc")) {
					defObj = obj;
				}
			}
		}
		// HSE - set the default selected monster to Orc
		wandering.setSelectedItem(defObj);

		refreshQuestData(quest);
	}

	public void showDialog() {
		final int option = JOptionPane.showOptionDialog(null,
				this,
				"Properties",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE,
				null,
				null,
				null);
		if (option == JOptionPane.YES_OPTION) {
			if (!name.getText().equals(quest.getName())) {
				quest.setName(name.getText());
			}

			final LObject selectedItem = (LObject) wandering.getSelectedItem();
			if (!selectedItem.id.equals(quest.getWanderingID())) {
				quest.setWandering(selectedItem.name, selectedItem.id);
			}

			if (!speech.getText().equals(quest.getSpeech())) {
				quest.setSpeech(speech.getText());
			}
		}
	}

	public void refreshQuestData(final Quest quest) {
		name.setText(quest.getName());
		speech.setText(quest.getSpeech());

		for (int i = 0; i < wandering.getItemCount(); i++) {
			if (wandering.getItemAt(i).toString().contentEquals(quest.getWandering())) {
				wandering.setSelectedItem(wandering.getItemAt(i));
			}
		}
	}

	@Override
	public void ancestorAdded(AncestorEvent event) {
		SwingUtilities.invokeLater(name::requestFocusInWindow);
	}

	@Override
	public void ancestorRemoved(AncestorEvent event) {
		// noop
	}

	@Override
	public void ancestorMoved(AncestorEvent event) {
		// noop
	}
}