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

package org.lightless.heroscribe.utils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class SwingUtils {

	public static JPanel createPanel() {
		final JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
//		panel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 1));
		return panel;
	}

	public static JPanel createLabelPanel(String text){
		final JPanel panel = createPanel();
		panel.add(new JLabel(text, SwingConstants.LEFT));
//		panel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 1));
		return panel;
	}

	public static TitledBorder createTitledBorder(String title) {
		final TitledBorder border = BorderFactory.createTitledBorder(title);
		border.setTitleJustification(TitledBorder.LEFT);
//		border.setBorder(BorderFactory.createLineBorder(Color.GREEN, 1));
		return border;
	}

	public static JScrollBar createVerticalScrollBar(JScrollPane jScrollPane) {
		final JScrollBar scrollBar = jScrollPane.createVerticalScrollBar();
		scrollBar.setUnitIncrement(16);
		return scrollBar;
	}

	public static JDialog createButtonlessDialog(String message) {
		final JDialog dialog = new JDialog();
		dialog.setModal(true);
		dialog.setResizable(false);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setSize(new Dimension(290, 126));  // used only to center the dialog
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(false);
		dialog.setContentPane(new JOptionPane(
				message,
				JOptionPane.INFORMATION_MESSAGE,
				JOptionPane.DEFAULT_OPTION,
				null,
				new Object[]{},
				null));
		return dialog;
	}
}