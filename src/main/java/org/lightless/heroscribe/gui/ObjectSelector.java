/*
  HeroScribe
  Copyright (C) 2002-2004 Flavio Chierichetti and Valerio Chierichetti

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

import org.lightless.heroscribe.xml.Kind;
import org.lightless.heroscribe.xml.ObjectList;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.TreeMap;

public class ObjectSelector extends JPanel implements ItemListener, ListSelectionListener {

	private static final long serialVersionUID = 1L;

	private final Gui gui;

	private final JPanel objectsPanel;
	private final CardLayout cardLayout;
	private final TreeMap<String, JList<ObjectList.Object>> kindList;

	private String selectedObject;

	private final JComboBox<Kind> kindsComboBox;

	public ObjectSelector(Gui gui) {
		super();

		this.gui = gui;

		objectsPanel = new JPanel();
		cardLayout = new CardLayout();
		kindsComboBox = new JComboBox<>();
		kindList = new TreeMap<>();

		selectedObject = null;

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		objectsPanel.setLayout(cardLayout);

		add(kindsComboBox);
		add(objectsPanel);

		gui.getObjectList().addModificationListener(modificationType -> refresh());

		refresh();
		kindsComboBox.addItemListener(this);
	}

	public void refresh() {
		final ObjectList objectList = gui.getObjectList();
		kindList.clear();
		kindsComboBox.removeAllItems();
		objectList.getKinds().forEach(
				kind -> {
					JList<ObjectList.Object> list = new JList<>(new DefaultListModel<>());

					list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

					kindList.put(kind.getId(), list);

					kindsComboBox.addItem(kind);

					objectsPanel.add(new JScrollPane(list), kind.getId());

					list.addListSelectionListener(this);
				}
		);

		objectList.getObjects().forEach(object ->
				kindList.computeIfPresent(object.getKind(), (String s, JList<ObjectList.Object> objectJList) -> {
					final DefaultListModel<ObjectList.Object> listModel = (DefaultListModel<ObjectList.Object>) objectJList.getModel();
					listModel.addElement(object);
					return objectJList;
				}));
	}

	public String getSelectedObject() {
		return selectedObject;
	}

	private void setSelectedObject(ObjectList.Object obj) {
		if (obj != null) {
			selectedObject = obj.getId();
		} else {
			selectedObject = null;
		}

		gui.board.resetRotation();
	}

	@SuppressWarnings("unchecked")
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			Kind selected;
			JList<ObjectList.Object> list;

			selected = (Kind) ((JComboBox<Kind>) e.getSource()).getSelectedItem();
			cardLayout.show(objectsPanel, selected.getId());
			list = kindList.get(selected.getId());

			setSelectedObject(list.getSelectedValue());

			gui.updateHint();
		}
	}

	@SuppressWarnings("unchecked")
	public void valueChanged(ListSelectionEvent e) {
		JList<ObjectList.Object> list = (JList<ObjectList.Object>) e.getSource();
		setSelectedObject(list.getSelectedValue());
		gui.updateHint();
	}
}
