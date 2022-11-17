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
	LETTER("Letter", 612, 792, "8.5\" x 11.0\""),
	A3("A3", 595, 842, "297mm x 420mm"),
	A4("A4", 595, 842, "210mm x 297mm"),
	A5("A5", 420, 595, "148mm x 210mm");

	private final String name;
	private final int width;
	private final int height;
	private final String humanDimension;

	PaperType(String name, int width, int height, String humanDimension) {
		this.name = name;
		this.width = width;
		this.height = height;
		this.humanDimension = humanDimension;
	}

	public String getName() {
		return name;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	@Override
	public String toString() {
		return name + "  " + humanDimension;
	}
}