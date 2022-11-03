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

import org.lightless.heroscribe.list.LObject;
import org.lightless.heroscribe.quest.Quest;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Iterator;

public class ToolsPanel extends JPanel implements ItemListener, KeyListener, ActionListener, ListSelectionListener {

	private static final long serialVersionUID = 1L;

	Gui gui;
	private Quest quest;
	ObjectSelector selectorPanel;
	SquareDisplayer displayerPanel;

	ButtonGroup commands;
	ItemListener listener;
	JToggleButton add, select, dark, none;
	// HSE - new quest description fields
	JTextField name;
	JComboBox<LObject> wandering;
	JLabel lName, lSpeech, lWandering, lNotes;
	TextArea speech;
	JList<String> note;
	DefaultListModel<String> noteData = new DefaultListModel<>();
	JScrollPane scrollPane = new JScrollPane();
	JButton newNote, editNote, delNote;

	JPanel extraPanel;

	String selected;

	public ToolsPanel(Gui gui, Quest quest) {
		this.gui = gui;
		this.quest = quest;
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		selected = null;

		extraPanel = new JPanel();

		// HSE - initialize the quest detail fields
		newNote = new JButton("New");
		editNote = new JButton("Edit");
		delNote = new JButton("Delete");
		lName = new JLabel("Quest Name:");
		lSpeech = new JLabel("Quest Speech:");
		lWandering = new JLabel("Wandering Monster:");
		lNotes = new JLabel("QuestMaster Notes:");
		name = new JTextField();
		speech = new TextArea("", 4, 20, TextArea.SCROLLBARS_VERTICAL_ONLY);
		note = new JList<>(noteData);
		wandering = new JComboBox<>();
		wandering.setLightWeightPopupEnabled(false);
		scrollPane.getViewport().setView(note);
		scrollPane.setPreferredSize(new Dimension(120, 120));

		commands = new ButtonGroup();

		add = new JToggleButton("Add object");
		select = new JToggleButton("Select/Remove object");
		dark = new JToggleButton("Dark/Bridge");
		none = new JToggleButton();

		commands.add(add);
		commands.add(select);
		commands.add(dark);
		commands.add(none);

		// HSE - add the quest detail fields to the layout and set up the layout
		GridBagConstraints cSetting = new GridBagConstraints();
		cSetting.gridx = 0;
		cSetting.gridwidth = 3;
		cSetting.gridy = 0;
		cSetting.fill = GridBagConstraints.HORIZONTAL;
		cSetting.insets = new Insets(3, 0, 0, 3);
		cSetting.ipadx = 20;
		JPanel settingPanel = new JPanel();
		settingPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		settingPanel.setLayout(new GridBagLayout());
		JSeparator sepLine = new JSeparator(SwingConstants.HORIZONTAL);
		settingPanel.add(sepLine, cSetting);
		cSetting.gridy = 1;
		settingPanel.add(Box.createVerticalStrut(10), cSetting);
		cSetting.gridy = 2;
		settingPanel.add(lWandering, cSetting);
		cSetting.gridy = 3;
		settingPanel.add(wandering, cSetting);
		cSetting.gridy = 4;
		settingPanel.add(lName, cSetting);
		cSetting.gridy = 5;
		settingPanel.add(name, cSetting);
		cSetting.gridy = 6;
		settingPanel.add(lSpeech, cSetting);
		cSetting.gridy = 7;
		settingPanel.add(speech, cSetting);
		cSetting.gridy = 8;
		settingPanel.add(lNotes, cSetting);
		cSetting.gridy = 9;
		settingPanel.add(scrollPane, cSetting);
		cSetting.gridwidth = 1;
		cSetting.fill = GridBagConstraints.NONE;
		cSetting.gridy = 10;
		settingPanel.add(newNote, cSetting);
		cSetting.gridx = 1;
		editNote.setEnabled(false);
		settingPanel.add(editNote, cSetting);
		cSetting.gridx = 2;
		settingPanel.add(delNote, cSetting);

		// HSE - populate the combo box for the wandering monster list
		Iterator<LObject> iterator;
		LObject defObj = new LObject();

		iterator = gui.getObjects().objectsIterator();
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

		// set up the layout for the modePanel
		JPanel modePanel = new JPanel();
		modePanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		modePanel.setLayout(new GridLayout(3, 1));

		modePanel.add(add);
		modePanel.add(select);
		modePanel.add(dark);

		selectorPanel = new ObjectSelector(gui);
		selectorPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		displayerPanel = new SquareDisplayer(gui);
		displayerPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		// HSE - added new quest settings panel as a SOUTHern element in the layout
		this.add(modePanel, BorderLayout.NORTH);
		this.add(settingPanel, BorderLayout.SOUTH);
		this.add(extraPanel);

		extraPanel.setLayout(new CardLayout());

		extraPanel.add(new JPanel(), "empty");
		extraPanel.add(selectorPanel, "add");
		extraPanel.add(displayerPanel, "select");

		add.addItemListener(this);
		select.addItemListener(this);
		dark.addItemListener(this);

		name.addKeyListener(this);
		speech.addKeyListener(this);
		wandering.addActionListener(this);

		newNote.addActionListener(this);
		editNote.addActionListener(this);
		delNote.addActionListener(this);

		note.addListSelectionListener(this);
	}

	public void deselectAll() {
		add.setSelected(false);
		select.setSelected(false);
		dark.setSelected(false);
	}

	public void clearQuestForm() {
		// HSE - clears the quest data fields
		name.setText("");
		speech.setText("");
		int count = wandering.getItemCount();
		for (int i = 0; i < count; i++) {
			if (wandering.getItemAt(i).toString().contentEquals("Orc")) {
				wandering.setSelectedItem(wandering.getItemAt(i));
			}
		}
		noteData.clear();
	}

	public void refreshQuestData(Quest openQuest) {
		// HSE - refreshed quest data fields from current quest object
		this.quest = openQuest;
		name.setText(quest.getName());
		speech.setText(quest.getSpeech());

		int count = wandering.getItemCount();
		for (int i = 0; i < count; i++) {
			if (wandering.getItemAt(i).toString().contentEquals(quest.getWandering())) {
				wandering.setSelectedItem(wandering.getItemAt(i));
			}
		}

		noteData.clear();

		Iterator<String> iterator = quest.notesIterator();
		while (iterator.hasNext()) {
			String obj = iterator.next();
			noteData.addElement(obj);
		}

	}

	public String getCommand() {
		return selected;
	}

	// HSE - Add key listeners for text fields
	public void keyReleased(KeyEvent e) {
		// HSE - JTextField Listener, name field
		if (e.getSource() == name) {
			JTextField textField = (JTextField) e.getSource();
			String text = textField.getText();
			if (text != quest.getName()) {
				quest.setName(text);
			}
		}
		// HSE - TextArea listener, speech field
		else if (e.getSource() == speech) {
			TextArea textArea = (TextArea) e.getSource();
			String text = textArea.getText();
			if (text != quest.getSpeech()) {
				quest.setSpeech(text);
			}

		}
	}

	public void itemStateChanged(ItemEvent e) {
		JToggleButton source = (JToggleButton) e.getSource();

		if (e.getStateChange() == ItemEvent.SELECTED) {
			if (source == add) {
				selected = "add";
				((CardLayout) extraPanel.getLayout()).show(extraPanel, selected);
			} else if (source == select) {
				selected = "select";
				displayerPanel.clearList();
				((CardLayout) extraPanel.getLayout()).show(extraPanel, selected);
			} else if (source == dark) {
				selected = "darken";
			} else if (source == none) {
				selected = null;
			}

			gui.updateHint();
		} else {
			selected = null;
			((CardLayout) extraPanel.getLayout()).show(extraPanel, "empty");
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == wandering) {
			// HSE - listener for changes to the wandering monster dropdown
			@SuppressWarnings("unchecked")
			JComboBox<LObject> source = (JComboBox<LObject>) e.getSource();
			LObject selectedItem = (LObject) source.getSelectedItem();
			quest.setWandering(selectedItem.name, selectedItem.id);

		} else if (e.getSource() == newNote) {
			// HSE - listener for new note click
			NoteModalDialog.showMessageDialog(this, null, new NoteModalDialog.NoteModalListener() {
				@Override
				public void noteChange(String text) {
					noteData.addElement(text);
					quest.addNote(text);
					quest.setModified(true);
				}
			});
		} else if (e.getSource() == editNote) {
			// HSE - listener for edit note click
			NoteModalDialog.showMessageDialog(this, note.getSelectedValue(), new NoteModalDialog.NoteModalListener() {
				@Override
				public void noteChange(String text) {
					noteData.setElementAt(text, note.getLeadSelectionIndex());
					quest.setNote(text, note.getLeadSelectionIndex());
					quest.setModified(true);
				}
			});
		} else if (e.getSource() == delNote) {
			// HSE - listener for del note click
			if (note.getSelectedValue() != null) {
				int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this note?", "Confirm Delete",
						JOptionPane.YES_NO_OPTION);
				if (response == JOptionPane.YES_OPTION) {
					quest.removeNote(note.getSelectedValue().toString());
					noteData.removeElement(note.getSelectedValue());

					quest.setModified(true);
				}
			}
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource() == note) {
			editNote.setEnabled(true);
		}
	}
}
