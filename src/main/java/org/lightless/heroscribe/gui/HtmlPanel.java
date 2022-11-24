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

import org.slf4j.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;

import static java.lang.String.*;

public class HtmlPanel extends JPanel implements HyperlinkListener {
	private static final Logger log = LoggerFactory.getLogger(HtmlPanel.class);

	private final JEditorPane jEditorPane = new JEditorPane();

	public HtmlPanel(JFrame frame, Path htmlPath) {
		this(frame, htmlPath, null);
	}

	public HtmlPanel(JFrame frame, Path path, String reference) {
		setLayout(new FlowLayout());

		jEditorPane.addHyperlinkListener(this);
		jEditorPane.setContentType("text/html");
		jEditorPane.setEditable(false);
		try {
			jEditorPane.setPage(getHtmlUrl(path, reference));
		} catch (IOException e) {
			throw new IllegalStateException(format("Cannot render html file: %s", path));
		}
		final JScrollPane jScrollPane = new JScrollPane(jEditorPane);
		jScrollPane.setPreferredSize(new Dimension(1000, 700));

		add(jScrollPane);

		JOptionPane.showMessageDialog(frame,
				this,
				"Objects",
				JOptionPane.PLAIN_MESSAGE);
	}

	private static URL getHtmlUrl(Path path, String reference) throws MalformedURLException {
		if (reference == null) {
			return path.toUri().toURL();
		} else {
			return new URL(path.toUri().toURL() + "#" + reference);
		}
	}

	@Override
	public void hyperlinkUpdate(HyperlinkEvent event) {
		if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			try {
				jEditorPane.setPage(event.getURL());
			} catch (IOException e) {
				log.error("Cannot follow hyperlink", e);
			}
		}
	}
}