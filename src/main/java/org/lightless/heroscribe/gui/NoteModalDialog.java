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

import org.lightless.heroscribe.helper.ResourceHelper;
import org.lightless.heroscribe.helper.ScreenSize;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class NoteModalDialog {
	private static final int MODAL_WIDTH = 500;
	private static final int MODAL_HEIGHT = 400;
	private static final Dimension BUTTON_DIMENSION = new Dimension(120, 40);
	private static final int BORDER_SIZE = 8;

	public static void showMessageDialog(JPanel parent, String noteContent, NoteModalListener listener) {

		final JFrame windowAncestor = (JFrame) SwingUtilities.getWindowAncestor(parent);
		final JDialog dialog = new JDialog(windowAncestor, "Enter Note");
		final Label label = new Label("Enter the QuestMaster Note:");
		final JTextArea textArea = new JTextArea(10, 50);
		final JButton okButton = createButton("OK", "Icons/ok.png");
		final JButton cancelButton = createButton("Cancel", "Icons/no.png");

		textArea.setText(noteContent);

		okButton.addActionListener(e -> {
			listener.noteChange(textArea.getText());
			dialog.setVisible(false);
		});
		cancelButton.addActionListener(e -> {
			dialog.setVisible(false);
		});

		final JPanel contentPanel = (JPanel) dialog.getContentPane();
		contentPanel.setBorder(BorderFactory.createEmptyBorder(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE));
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.add(label);
		contentPanel.add(new JScrollPane(textArea));
		contentPanel.add(createSeparator());
		contentPanel.add(createButtonsSection(cancelButton, okButton));

		dialog.setBounds(ScreenSize.getWidth() / 2 - MODAL_WIDTH / 2, ScreenSize.getHeight() / 2 - MODAL_HEIGHT / 2, MODAL_WIDTH, MODAL_HEIGHT);
		dialog.addWindowListener(closeWindow);
		dialog.setVisible(true);
		dialog.pack();
	}

	private static JButton createButton(String text, String icon) {
		final ImageIcon imageIcon = new ImageIcon(ResourceHelper.getResourceUrl(icon).getFile());
		final JButton button = new JButton(text, imageIcon);
		button.setPreferredSize(BUTTON_DIMENSION);
		button.setMnemonic(text.charAt(0));
		return button;
	}

	private static JSeparator createSeparator() {
		final JSeparator separator = new JSeparator();
		separator.setBorder(BorderFactory.createEmptyBorder(BORDER_SIZE, 0, BORDER_SIZE, 0));
		return separator;
	}

	private static JPanel createButtonsSection(JButton... buttons) {
		final JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.RIGHT, BORDER_SIZE, BORDER_SIZE));
		panel.setSize(MODAL_WIDTH, MODAL_HEIGHT);
		for (JButton button : buttons) {
			panel.add(button);
		}
		return panel;
	}

	private static final WindowListener closeWindow = new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
			e.getWindow().dispose();
		}
	};

	public interface NoteModalListener {
		void noteChange(String text);
	}
}
