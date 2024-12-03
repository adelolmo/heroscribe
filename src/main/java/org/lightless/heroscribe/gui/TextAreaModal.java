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

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.util.Optional;

public class TextAreaModal extends JPanel implements AncestorListener {

	private final String title;
	private final JTextArea textArea;

	public TextAreaModal(String title, String label) {
		this(title, label, 8, 50);
	}

	public TextAreaModal(String title, String label, int rows, int columns) {
		super();
		this.title = title;
		this.textArea = new JTextArea(rows, columns);

		textArea.setWrapStyleWord(true);
		textArea.setAutoscrolls(false);
		textArea.setLineWrap(true);
		textArea.addAncestorListener(this);

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		final JPanel labelPanel = createPanel();
		labelPanel.add(new JLabel(label, SwingConstants.LEFT));
		add(labelPanel);
		add(new JScrollPane(textArea), BorderLayout.PAGE_START);
	}

	public void setInitialText(String text) {
		this.textArea.setText(text);
	}

	public void clear() {
		textArea.setText("");
	}

	public Optional<String> showDialog() {
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

	private static JPanel createPanel() {
		final JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		return panel;
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