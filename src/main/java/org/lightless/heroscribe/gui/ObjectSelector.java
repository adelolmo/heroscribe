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

import org.lightless.heroscribe.xml.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class ObjectSelector extends JPanel implements ItemListener, ListSelectionListener {

	private static final long serialVersionUID = 1L;

	private final Gui gui;

	private final JPanel objectsPanel;
	private final CardLayout cardLayout;
	private final TreeMap<String, JList<ObjectList.Object>> kindList;

	private String selectedObject;
	private int objectRotation;

	private final JComboBox<ObjectList.Kind> kindsComboBox;

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


		refresh();
/*		Iterator<LObject> objIterator = gui.getObjects().objectsIterator();
		while (objIterator.hasNext()) {
			LObject obj = objIterator.next();

			JList<LObject> list = kindList.get(obj.kind);
			DefaultListModel<LObject> listModel = (DefaultListModel<LObject>) list.getModel();

			listModel.addElement(obj);
		}*/

		kindsComboBox.addItemListener(this);
	}

	public void refresh() {
		final ObjectList objectList = gui.getObjectList();
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
	/*	Iterator<Kind> kindIterator = gui.getObjects().kindsIterator();
		while (kindIterator.hasNext()) {
			Kind kind = kindIterator.next();

			JList<LObject> list = new JList<>(new DefaultListModel<>());

			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			kindList.put(kind.id, list);

			kindsComboBox.addItem(kind);

			objectsPanel.add(new JScrollPane(list), kind.id);

			list.addListSelectionListener(this);
		}*/

		objectList.getObjects().forEach(object -> {
//			LObject obj = objIterator.next();

			JList<ObjectList.Object> list = kindList.get(object.getKind());
			DefaultListModel<ObjectList.Object> listModel = (DefaultListModel<ObjectList.Object>) list.getModel();

			listModel.addElement(object);
		});
	}

	public String getSelectedObject() {
		return selectedObject;
	}

	public int getSelectedObjectRotation() {
		return objectRotation;
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
			ObjectList.Kind selected;
			JList<ObjectList.Object> list;

			selected = (ObjectList.Kind) ((JComboBox<ObjectList.Kind>) e.getSource()).getSelectedItem();
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
