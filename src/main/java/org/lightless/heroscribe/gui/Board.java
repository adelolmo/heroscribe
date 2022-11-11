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

public class Board extends JPanel implements MouseInputListener {

	private static final long serialVersionUID = 1L;

	private final Gui gui;

	private int lastRow, lastColumn;
	private int lastTop, lastLeft;
	private int rotation;

	private boolean isPaintingDark, isDark, hasAdded;

	public Board(Gui gui) {
		super();

		this.gui = gui;

		isPaintingDark = isDark = hasAdded = false;

		lastRow = lastColumn = -1;
		lastTop = lastLeft = -1;

		setBackground(Color.WHITE);

		setSize();

		/* Is it possible to just addMouseInputListener() ? */
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	public void setSize() {
		setPreferredSize(gui.boardPainter.framePixelSize);
		//setMaximumSize(gui.boardPainter.framePixelSize);

		this.revalidate();
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (!hasAdded && "add".equals(gui.tools.getCommand()) && gui.tools.selectorPanel.getSelectedObject() != null)
			gui.boardPainter.paint(getNewObject(true), lastColumn, lastRow, (Graphics2D) g);
		else
			gui.boardPainter.paint(null, lastColumn, lastRow, (Graphics2D) g);
	}

	private Quest.Board.Object getNewObject(boolean floating) {
		Quest.Board.Object newObject;
		final String id = gui.tools.selectorPanel.getSelectedObject();

		if (floating) {
//			newObject = new QObject(gui.tools.selectorPanel.getSelectedObject(),
//					gui.getObjectList(),
//					0);

			newObject = new Quest.Board.Object();
			newObject.setId(id);
			newObject.setZorder(Integer.MAX_VALUE);
		} else {
//			newObject = new QObject(id, gui.getObjectList());
			newObject = new Quest.Board.Object();
			newObject.setId(gui.tools.selectorPanel.getSelectedObject());

//			final List objects = gui.getObjects();
			final ObjectList.Object obj = gui.getObjectList().getObject(id);

			newObject.setZorder(obj.getZorder());
		}

		newObject.setRotation(Rotation.fromNumber(rotation));
		newObject.setLeft(lastLeft);
		newObject.setTop(lastTop);

		return newObject;
	}

	public void resetRotation() {
		rotation = 0;

		repaint();
	}

	private void updatePosition(MouseEvent e) {
		int row, column;
		int top, left;

		float width, height;
		float x, y;

		width = gui.boardPainter.boardPixelSize.width
				+ gui.getObjectList().getBoard().getAdjacentBoardsOffset()
				* gui.boardPainter.boxEdge;

		height = gui.boardPainter.boardPixelSize.height
				+ gui.getObjectList().getBoard().getAdjacentBoardsOffset()
				* gui.boardPainter.boxEdge;

		x = e.getX() + (gui.getObjectList().getBoard().getAdjacentBoardsOffset()
				* gui.boardPainter.boxEdge)
				/ 2.0f;
		if (x < 0.0f)
			x = 0.0f;
		else if (x > width * gui.getXmlQuest().getWidth())
			x = width * gui.getXmlQuest().getWidth() - 1;

		y = e.getY() + (gui.getObjectList().getBoard().getAdjacentBoardsOffset()
				* gui.boardPainter.boxEdge)
				/ 2.0f;
		if (y < 0.0f)
			y = 0.0f;
		else if (y > height * gui.getXmlQuest().getHeight())
			y = height * gui.getXmlQuest().getHeight() - 1;

		row = (int) (y / height);
		column = (int) (x / width);

		top = (int) ((y % height) / gui.boardPainter.boxEdge - gui.getObjectList().getBoard().getAdjacentBoardsOffset() / 2.0f);
		if (top < 0)
			top = 0;
		else if (top > gui.getObjectList().getBoard().getHeight() + 1)
			top = gui.getObjectList().getBoard().getHeight() + 1;

		left = (int) ((x % width) / gui.boardPainter.boxEdge - gui.getObjectList().getBoard().getAdjacentBoardsOffset() / 2.0f);
		if (left < 0)
			left = 0;
		else if (left > gui.getObjectList().getBoard().getWidth() + 1)
			left = gui.getObjectList().getBoard().getWidth() + 1;

		//TODO
		//System.err.println("row: " + row + "   column: " + column + "   top: " + top + "   left: " + left);

		if (top != lastTop || left != lastLeft || row != lastRow || column != lastColumn) {
			hasAdded = false;

			lastRow = row;
			lastColumn = column;
			lastTop = top;
			lastLeft = left;

			if (isPaintingDark && gui.getXmlQuest()
					.getBoard(lastColumn, lastRow)
					.isDark(lastLeft, lastTop) != isDark)
				gui.getXmlQuest()
						.getBoard(lastColumn, lastRow)
						.toggleDark(lastLeft, lastTop);

			repaint();

			displayStatus();
		} else {
			lastRow = row;
			lastColumn = column;
			lastTop = top;
			lastLeft = left;
		}
	}

	private void displayStatus() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;

		int width, height;

		if (gui.getXmlQuest().getBoard(lastColumn, lastRow).isDark(lastLeft, lastTop)) {
			sb.insert(0, "Dark");

			first = false;
		}

//		Iterator<QObject> iterator =
//				gui.getXmlQuest().getBoard(lastColumn, lastRow).iterator();
//		while (iterator.hasNext()) {
		for (Quest.Board.Object qobj : gui.getXmlQuest().getBoard(lastColumn, lastRow).getObjects()) {

//					QObject qobj = iterator.next();
			final ObjectList.Object lobj = gui.getObjectList().getObject(qobj.getId());

			if (qobj.getRotation().getNumber() % 2 == 0) {
				width = lobj.getWidth();
				height = lobj.getHeight();
			} else {
				width = lobj.getHeight();
				height = lobj.getWidth();
			}

			if (qobj.getLeft() <= lastLeft
					&& lastLeft < qobj.getLeft() + width
					&& qobj.getTop() <= lastTop
					&& lastTop < qobj.getTop() + height) {

				if (first)
					first = false;
				else
					sb.insert(0, ", ");

				sb.insert(0, lobj.getName());
			}
		}

		gui.status.setText(new String(sb));
	}

	public void mouseDragged(MouseEvent e) {
		updatePosition(e);
	}

	public void mouseMoved(MouseEvent e) {
		updatePosition(e);
	}

	public void mouseExited(MouseEvent e) {
		lastRow = lastColumn = -1;
		lastTop = lastLeft = -1;

		repaint();
	}

	public void mousePressed(MouseEvent e) {
		updatePosition(e);

		if ("add".equals(gui.tools.getCommand())) {
			if (SwingUtilities.isRightMouseButton(e) || e.isControlDown()) {
				/* right click or (ctrl + click) (for mac's single button mice) */
				rotation = (rotation + 1) % 4;
			} else {
				/* left click */
				Quest.Board.Object obj = getNewObject(false);

				if (isWellPositioned(obj))
					if (gui.getXmlQuest().getBoard(lastColumn, lastRow).addObject(obj))
						hasAdded = true;
			}
		} else if ("select".equals(gui.tools.getCommand())) {
			gui.tools.displayerPanel.createList(lastColumn, lastRow, lastLeft, lastTop);

		} else if ("darken".equals(gui.tools.getCommand())) {
//			QBoard board = gui.getQuest().getBoard(lastColumn, lastRow);
			final Quest.Board board = gui.getXmlQuest().getBoard(lastColumn, lastRow);

			if (1 <= lastLeft && lastLeft <= board.getWidth()
					&& 1 <= lastTop && lastTop <= board.getHeight()) {
				/* Darken/Clear */

				if (SwingUtilities.isRightMouseButton(e) || e.isControlDown()) {
					/* right click or (ctrl + click) (for mac's single button mice) */
					isDark = false;
				} else {
					/* left click */
					isDark = true;
				}

				if (gui.getXmlQuest().getBoard(lastColumn, lastRow)
						.isDark(lastLeft, lastTop)
						!= isDark)
					gui.getXmlQuest().getBoard(lastColumn, lastRow)
							.toggleDark(lastLeft, lastTop);

				isPaintingDark = true;
			} else if ((1 <= lastLeft && lastLeft <= board.getWidth())
					|| (1 <= lastTop && lastTop <= board.getHeight())) {
				/* Bridge */

				boolean value;
				int column = lastColumn;
				int row = lastRow;

				if (lastLeft == 0)
					column--;
				if (lastTop == 0)
					row--;

				if (SwingUtilities.isRightMouseButton(e) || e.isControlDown()) {
					/* right click or (ctrl + click) (for mac's single button mice) */

					value = false;
				} else {
					/* left click */
					value = true;
				}

				if (lastLeft < 1 || lastLeft > board.getWidth()) {
					gui.getXmlQuest().setHorizontalBridge(value, column, row, lastTop);
				} else {
					gui.getXmlQuest().setVerticalBridge(value, column, row, lastLeft);
				}
			}
		}

		repaint();

		gui.updateTitle();
		displayStatus();
	}

	public void mouseReleased(MouseEvent e) {
		isPaintingDark = false;
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public boolean isWellPositioned(Quest.Board.Object piece) {
		final ObjectList.Object obj = gui.getObjectList().getObject(piece.getId());

		int width, height;

		if (piece.getRotation().getNumber() % 2 == 0) {
			width = obj.getWidth();
			height = obj.getHeight();
		} else {
			width = obj.getHeight();
			height = obj.getWidth();
		}

		if (obj.isDoor()) {
			if (piece.getLeft() < 0 || piece.getTop() < 0 || piece.getLeft() + width - 1 > gui.boardPainter.boardSize.width + 1
					|| piece.getTop() + height - 1 > gui.boardPainter.boardSize.height + 1
					|| (piece.getRotation().getNumber() % 2 == 0 && (piece.getLeft() == 0 || piece.getLeft() == gui.boardPainter.boardSize.width + 1))
					|| (piece.getRotation().getNumber() % 2 == 1 && (piece.getTop() == 0 || piece.getTop() == gui.boardPainter.boardSize.height + 1)))
				return false;
		} else {
			if (piece.getLeft() < 1 || piece.getTop() < 1 || piece.getLeft() + width - 1 > gui.boardPainter.boardSize.width
					|| piece.getTop() + height - 1 > gui.boardPainter.boardSize.height)
				return false;
		}

		return true;
	}
}
