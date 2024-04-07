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
import java.util.List;
import java.util.*;

public class SquareDisplayer extends JPanel implements ListSelectionListener, ActionListener {

	private static final long serialVersionUID = 1L;

	private final Gui gui;

	private final JTextField zorder;
	private final JButton set, remove, rotate;

	private final TreeSet<Quest.Board.Object> selected;

	private final JList<Quest.Board.Object> list;

	private int lastColumn, lastRow;

	public SquareDisplayer(Gui gui) {
		super();

		this.gui = gui;

		setLayout(new BorderLayout());

		selected = new TreeSet<>();

		list = new JList<>(new DefaultListModel<>());
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addListSelectionListener(this);

		final JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(2, 2));
		panel.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

		zorder = new JTextField();
		set = new JButton("Set zorder");
		remove = new JButton("Remove");
		rotate = new JButton("Rotate");

		zorder.setEnabled(false);
		set.setEnabled(false);
		remove.setEnabled(false);
		rotate.setEnabled(false);

		set.addActionListener(this);
		remove.addActionListener(this);
		rotate.addActionListener(this);

		panel.add(zorder);
		panel.add(set);
		panel.add(rotate);
		panel.add(remove);

		this.add(new JScrollPane(list));
		this.add(panel, BorderLayout.SOUTH);
	}

	public void clearList() {
		((DefaultListModel<Quest.Board.Object>) list.getModel()).clear();
	}

	public void createList(int column, int row, int left, int top) {
		int width, height;

		lastColumn = column;
		lastRow = row;

		selected.clear();

		final List<Quest.Board.Object> objectList = gui.getQuest()
				.getBoard(column, row)
				.getObjects();
		for (Quest.Board.Object qobj : objectList) {
			ObjectList.Object lobj = gui.getObjectList().getObjectById(qobj.getId());

			if (qobj.getRotation().getNumber() % 2 == 0) {
				width = lobj.getWidth();
				height = lobj.getHeight();
			} else {
				width = lobj.getHeight();
				height = lobj.getWidth();
			}

			if (qobj.getLeft() <= left
					&& left < qobj.getLeft() + width
					&& qobj.getTop() <= top
					&& top < qobj.getTop() + height) {
				selected.add(qobj);
			}

		}

		updateList();
	}

	public void updateList() {
		clearList();

		Iterator<org.lightless.heroscribe.xml.Quest.Board.Object> iterator = selected.iterator();
		DefaultListModel<Quest.Board.Object> listModel =
				(DefaultListModel<Quest.Board.Object>) list.getModel();

		while (iterator.hasNext()) {
			Quest.Board.Object qobj = iterator.next();
			listModel.add(0, qobj);
		}

		if (listModel.size() > 0) {
			list.setSelectedIndex(0);
		}
	}

	@SuppressWarnings("unchecked")
	public void valueChanged(ListSelectionEvent e) {
		JList<Quest.Board.Object> list = (JList<Quest.Board.Object>) e.getSource();

		Quest.Board.Object obj = list.getSelectedValue();

		if (obj != null) {
			zorder.setText(Float.toString(obj.getZorder()));

			zorder.setEnabled(true);
			set.setEnabled(true);
			remove.setEnabled(true);
			rotate.setEnabled(true);
		} else {
			zorder.setText("");

			zorder.setEnabled(false);
			set.setEnabled(false);
			remove.setEnabled(false);
			rotate.setEnabled(false);
		}
	}

	public void actionPerformed(ActionEvent e) {
		Quest.Board.Object obj = list.getSelectedValue();
		JButton button = (JButton) e.getSource();

		if (obj != null) {
			gui.getQuest().getBoard(lastColumn, lastRow).getObjects().remove(obj);
			selected.remove(obj);
		}

		if (button == set) {
			obj.setZorder(Float.parseFloat(zorder.getText()));
			zorder.setText(Float.toString(obj.getZorder()));
		} else if (button == remove) {
			obj = null;
		} else if (button == rotate) {
			final int rotation = (obj.getRotation().getNumber() + 1) % 4;
			obj.setRotation(Rotation.fromNumber(rotation));
		}

		if (obj != null) {
			gui.getQuest().getBoard(lastColumn, lastRow).getObjects().add(obj);
			selected.add(obj);
		}

		gui.updateTitle();
		gui.board.repaint();

		updateList();

		if (obj != null)
			list.setSelectedValue(obj, true);
	}

}