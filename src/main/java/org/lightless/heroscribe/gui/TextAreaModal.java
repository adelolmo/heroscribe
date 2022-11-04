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
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.util.Optional;

public class TextAreaModal {

	private final String title;
	private final String label;
	private final int rows;
	private final int columns;

	public TextAreaModal(String title, String label) {
		this(title, label, 10, 50);
	}

	public TextAreaModal(String title, String label, int rows, int columns) {
		this.title = title;
		this.label = label;
		this.rows = rows;
		this.columns = columns;
	}

	public Optional<String> showDialog() {
		final JPanel panel = new JPanel();
		final JTextArea textArea = new JTextArea(rows, columns);

		textArea.addAncestorListener(new RequestFocusListener());

		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(new Label(label));
		panel.add(textArea);

		final int option = JOptionPane.showOptionDialog(null, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null,
				null);
		if (option == JOptionPane.YES_OPTION) {
			return Optional.ofNullable(textArea.getText());
		}

		return Optional.empty();
	}

	private static class RequestFocusListener implements AncestorListener {

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

}