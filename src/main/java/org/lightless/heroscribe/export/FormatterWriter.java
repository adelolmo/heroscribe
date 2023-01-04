/*
  HeroScribe Enhanced Skull
  Copyright (C) 2023 Andoni del Olmo

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

package org.lightless.heroscribe.export;

import java.io.*;

public class FormatterWriter extends PrintWriter {

	public FormatterWriter(PrintWriter printWriter) {
		super(printWriter);
	}

	public void println(String x, Object... args) {
		final String escapedFormat = x.startsWith("%%") ?
				x.replaceAll("%%", "%%%%") : x;
		super.println(String.format(escapedFormat, args));
	}
}