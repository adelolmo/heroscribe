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

package org.lightless.heroscribe.export;

/*
Paper Size                      Dimension (in points)
   ------------------              ---------------------
   Comm #10 Envelope               297 x 684
   C5 Envelope                     461 x 648
   DL Envelope                     312 x 624
   Folio                           595 x 935
   Executive                       522 x 756
   Letter                          612 x 792
   Legal                           612 x 1008
   Ledger                          1224 x 792
   Tabloid                         792 x 1224
   A0                              2384 x 3370
   A1                              1684 x 2384
   A2                              1191 x 1684
   A3                              842 x 1191
   A4                              595 x 842
   A5                              420 x 595
   A6                              297 x 420
   A7                              210 x 297
   A8                              148 x 210
   A9                              105 x 148
   B0                              2920 x 4127
   B1                              2064 x 2920
   B2                              1460 x 2064
   B3                              1032 x 1460
   B4                              729 x 1032
   B5                              516 x 729
   B6                              363 x 516
   B7                              258 x 363
   B8                              181 x 258
   B9                              127 x 181
   B10                             91 x 127
 */
public enum PaperType {
	LETTER("letter",
			"Letter",
			612,
			792,
			"8.5\" x 11.0\"",
			55,
			21),
	//	LEGAL("legal", "Legal", 612, 1008, "8.5\" x 14.0\""),
//	A3("a3", "A3", 595, 842, "297mm x 420mm"),
	A4("a4",
			"A4",
			595,
			842,
			"210mm x 297mm",
			58,
			24),
//	A5("a5", "A5", 420, 595, "148mm x 210mm")
	;

	private final String id;
	private final String name;
	private final int width;
	private final int height;
	private final String humanDimension;
	private final int numberLinesFullPage;
	private final int numberLinesHalfPage;

	PaperType(String id,
			  String name,
			  int width,
			  int height,
			  String humanDimension,
			  int numberLinesFullPage,
			  int numberLinesHalfPage) {
		this.id = id;
		this.name = name;
		this.width = width;
		this.height = height;
		this.humanDimension = humanDimension;
		this.numberLinesFullPage = numberLinesFullPage;
		this.numberLinesHalfPage = numberLinesHalfPage;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getHalfWidth() {
		return width / 2;
	}

	public int getWidth() {
		return width;
	}

	public int getHalfHeight() {
		return height / 2;
	}

	public int getHeight() {
		return height;
	}

	public int getNumberLinesFullPage() {
		return numberLinesFullPage;
	}

	public int getNumberLinesHalfPage() {
		return numberLinesHalfPage;
	}

	@Override
	public String toString() {
		return name + "  " + humanDimension;
	}
}