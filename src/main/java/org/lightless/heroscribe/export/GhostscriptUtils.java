package org.lightless.heroscribe.export;

import java.util.Arrays;

public class GhostscriptUtils {

	private static final int LINE_LEN_WITH_FONT_1 = 178;
	private static final int CHAR_INCREASE = 6;

	public static int numberOfLines(String note, int fontSize) {
		final int maxLineLength = maxLineLength(fontSize);
		int lines = 1;
		char[] charArray = note.toCharArray();
		int linePosition = 0;
		for (int i = 0, charArrayLength = charArray.length; i < charArrayLength; i++) {

			if (charArray[i] == '\n') {
				linePosition = 0;
				lines++;
			}

			if (i == 0 || charArray[i] == ' ') {
				System.out.print(" ");
				linePosition++;

				String nextWord = nextWord(Arrays.copyOfRange(charArray, ++i, charArray.length));
				final int nextWordLength = nextWord.length();
				System.out.print(nextWord);
				linePosition += nextWordLength;
				if (nextWordLength + linePosition > maxLineLength) {
					System.out.print('\n');
					linePosition = 0;
					lines++;
				}
			}
		}
		System.out.print('\n');
		return lines;
	}

	public static int maxLineLength(int fontSize) {
		int minimumFontSize = 1;
		int lineLen = LINE_LEN_WITH_FONT_1;

		while (fontSize != minimumFontSize) {
			minimumFontSize++;
			lineLen -= CHAR_INCREASE;
		}

		return lineLen;
	}

	public static String nextWord(char[] chars) {
		if (chars.length == 0) {
			return "";
		}
		int size = 0;
		while (chars[size] != ' ') {
			size++;
			if (chars.length <= size) {
				break;
			}
		}
		return new String(Arrays.copyOfRange(chars, 0, size));
	}
}