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

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.util.*;

public class TextAreaModal extends JPanel implements AncestorListener {

	private final String title;
	private final String label;
	private final JTextArea textArea;

	public TextAreaModal(String title, String label) {
		this(title, label, 8, 50);
	}

	public TextAreaModal(String title, String label, int rows, int columns) {
		super();
		this.title = title;
		this.label = label;
		this.textArea = new JTextArea(rows, columns);
	}

	public void setInitialText(String text) {
		this.textArea.setText(text);
	}

	public Optional<String> showDialog() {
		textArea.setWrapStyleWord(true);
		textArea.setAutoscrolls(false);
		textArea.setLineWrap(true);
		textArea.addAncestorListener(this);

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(new Label(label));
		add(new JScrollPane(textArea), BorderLayout.PAGE_START);

		if (JOptionPane.showOptionDialog(null,
				this,
				title,
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE,
				null,
				null,
				null) == JOptionPane.YES_OPTION) {
			return Optional.ofNullable(textArea.getText());
		}

		return Optional.empty();
	}

	@Override
	public void ancestorAdded(AncestorEvent event) {
		SwingUtilities.invokeLater(event.getComponent()::requestFocusInWindow);
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