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

import org.lightless.heroscribe.export.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.util.*;

public class PaperSizeModal extends JPanel implements AncestorListener {

	public Optional<PaperType> showDialog(PaperType selectedItem) {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(new Label("Select paper size:"));
		final JComboBox<PaperType> paperTypeComboBox = new JComboBox<>();
		for (PaperType paperType : PaperType.values()) {
			paperTypeComboBox.addItem(paperType);
		}
		paperTypeComboBox.setSelectedItem(selectedItem);
		add(paperTypeComboBox);

		if (JOptionPane.showOptionDialog(null,
				this,
				"Paper Size",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE,
				null,
				null,
				null) == JOptionPane.YES_OPTION) {
			return Optional.ofNullable((PaperType) paperTypeComboBox.getSelectedItem());
		}

		return Optional.empty();

	}

	@Override
	public void ancestorAdded(AncestorEvent event) {
		SwingUtilities.invokeLater(event.getComponent()::requestFocusInWindow);
	}

	@Override
	public void ancestorRemoved(AncestorEvent event) {

	}

	@Override
	public void ancestorMoved(AncestorEvent event) {

	}
}