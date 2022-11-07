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

import org.lightless.heroscribe.quest.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class ToolsPanel extends JPanel implements ItemListener, KeyListener, ActionListener, ListSelectionListener {

	private static final long serialVersionUID = 1L;

	private final Gui gui;
	private Quest quest;
	ObjectSelector selectorPanel;
	SquareDisplayer displayerPanel;

	ButtonGroup commands;
	JToggleButton add, select, dark, none;
	// HSE - new quest description fields
	JLabel lNotes;
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
		lNotes = new JLabel("QuestMaster Notes:");
		note = new JList<>(noteData);
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

		final GridBagConstraints cSetting = new GridBagConstraints();
		cSetting.gridx = 0;
		cSetting.gridwidth = 3;
		cSetting.gridy = 0;
		cSetting.fill = GridBagConstraints.HORIZONTAL;
		cSetting.insets = new Insets(3, 0, 0, 3);
		cSetting.ipadx = 20;
		final JPanel settingPanel = new JPanel();
		settingPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		settingPanel.setLayout(new GridBagLayout());
		final JSeparator sepLine = new JSeparator(SwingConstants.HORIZONTAL);
		settingPanel.add(sepLine, cSetting);
		cSetting.gridy = 1;
		settingPanel.add(Box.createVerticalStrut(10), cSetting);
		cSetting.gridy = 2;
		settingPanel.add(lNotes, cSetting);
		cSetting.gridy = 3;
		settingPanel.add(scrollPane, cSetting);
		cSetting.gridwidth = 1;
		cSetting.fill = GridBagConstraints.NONE;
		cSetting.gridy = 4;
		settingPanel.add(newNote, cSetting);
		cSetting.gridx = 1;
		editNote.setEnabled(false);
		settingPanel.add(editNote, cSetting);
		cSetting.gridx = 2;
		settingPanel.add(delNote, cSetting);

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
		noteData.clear();
	}

	public void refreshQuestData(Quest openQuest) {
		// HSE - refreshed quest data fields from current quest object
		this.quest = openQuest;
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
		if (newNote == e.getSource()) {
			// HSE - listener for new note click
			final TextAreaModal modal = new TextAreaModal("Enter Note", "Enter the QuestMaster Note:");
			modal.showDialog().ifPresent(text -> {
				noteData.addElement(text);
				quest.addNote(text);
				quest.setModified(true);
			});
		} else if (editNote == e.getSource()) {
			// HSE - listener for edit note click
			final TextAreaModal modal = new TextAreaModal("Enter Note", "Enter the QuestMaster Note:");
			modal.setInitialText(note.getSelectedValue());
			modal.showDialog().ifPresent(text -> {
				noteData.setElementAt(text, note.getLeadSelectionIndex());
				quest.setNote(text, note.getLeadSelectionIndex());
				quest.setModified(true);
			});
		} else if (delNote == e.getSource()) {
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
