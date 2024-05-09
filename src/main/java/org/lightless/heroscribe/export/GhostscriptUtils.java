package org.lightless.heroscribe.export;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GhostscriptUtils {

	private static final double LINE_LEN_WITH_FONT_1 = 227.5;
	private static final double CHAR_INCREASE = 10.5;

	public static int numberOfLines(String note, int fontSize) {
		return splitLines(note, fontSize).size();
	}

	public static List<String> splitLines(String text, int fontSize) {
		final List<String> textLines = new ArrayList<>();
		final int maxLineLength = maxLineLength(fontSize);
		System.out.printf("maxLineLength: %d%n", maxLineLength);
		final StringBuilder line = new StringBuilder();
		char[] charArray = text.toCharArray();
		String nextWord = nextWord(Arrays.copyOfRange(charArray, 0, charArray.length));
		System.out.printf("%s(%d)[%d] ", nextWord, nextWord.length(), nextWord.length() + line.length());
		line.append(nextWord);
		for (int i = line.length(), charArrayLength = charArray.length; i < charArrayLength; i++) {

			if (charArray[i] == '\n') {
				i++;
				nextWord = nextWord(Arrays.copyOfRange(charArray, i, charArray.length));
				textLines.add(line.toString());
				line.delete(0, line.length());
				line.append(nextWord);
			}

			if (charArray[i] == ' ') {
				i++;

				nextWord = nextWord(Arrays.copyOfRange(charArray, i, charArray.length));
				if (nextWord.length() + line.length() + 1 >= maxLineLength) {
					System.out.print('\n');
					textLines.add(line.toString());
					line.delete(0, line.length());
					line.append(nextWord);

				} else {
					line.append(" ").append(nextWord);
				}
				System.out.printf("%s(%d)[%d] ", nextWord, nextWord.length(), nextWord.length() + line.length());
			}
		}
		textLines.add(line.toString());
		return textLines;
	}

	public static int maxLineLength(int fontSize) {
		int minimumFontSize = 1;
		double lineLen = LINE_LEN_WITH_FONT_1;

		while (fontSize != minimumFontSize) {
			minimumFontSize++;
			lineLen -= CHAR_INCREASE;
		}

		return (int) lineLen;
	}

	public static String nextWord(char[] chars) {
		if (chars.length == 0) {
			return "";
		}
		int size = 0;
		while (chars[size] != ' ' && chars[size] != '\n') {
			size++;
			if (chars.length <= size) {
				break;
			}
		}
		return new String(Arrays.copyOfRange(chars, 0, size));
	}
}