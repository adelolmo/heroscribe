package org.lightless.heroscribe.export;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GhostscriptUtils {

	private static final int LINE_LEN_WITH_FONT_1 = 178;
	private static final int CHAR_INCREASE = 6;

	public static int numberOfLines(String note, int fontSize) {
		final int maxLineLength = maxLineLength(fontSize);
		System.out.printf("maxLineLength: %d%n", maxLineLength);
		int lines = 1;
		char[] charArray = note.toCharArray();
		String nextWord;
		nextWord = nextWord(Arrays.copyOfRange(charArray, 0, charArray.length));
		int linePosition = 1;
		linePosition = nextWord.length() + 1;
		System.out.printf("%s(%d)[%d]", nextWord, nextWord.length(), linePosition);
		for (int i = nextWord.length(), charArrayLength = charArray.length; i < charArrayLength; i++) {

			if (charArray[i] == '\n') {
				linePosition = 1;
				lines++;
				i++;
			}

			if (charArray[i] == ' ') {
				System.out.print(" ");
				linePosition++;
				i++;

				nextWord = nextWord(Arrays.copyOfRange(charArray, i, charArray.length));
				final int nextWordLength = nextWord.length();
//				System.out.print(nextWord);
//				linePosition += nextWordLength;
				System.out.printf("%s(%d)[%d]", nextWord, nextWordLength, nextWordLength + linePosition);
				if (nextWordLength + linePosition > maxLineLength) {
					System.out.printf("{%s}", nextWord);
					System.out.print('\n');
					linePosition = 1;
					lines++;
				} else {
					linePosition += nextWordLength;
				}
			}
		}
		System.out.print('\n');
		return lines;
	}

	public static List<String> splitLines(String text, int fontSize){
		final List<String> textLines = new ArrayList<>();
		final int maxLineLength = maxLineLength(fontSize);
		System.out.printf("maxLineLength: %d%n", maxLineLength);
		final StringBuilder line = new StringBuilder();
		char[] charArray = text.toCharArray();
		String nextWord;
		nextWord = nextWord(Arrays.copyOfRange(charArray, 0, charArray.length));
		System.out.printf("%s(%d)[%d] ", nextWord, nextWord.length(), nextWord.length() + line.length());
		line.append(nextWord);
		for (int i = line.length(), charArrayLength = charArray.length; i < charArrayLength; i++) {

			if (charArray[i] == '\n') {
				i++;
				nextWord = nextWord(Arrays.copyOfRange(charArray, i, charArray.length));
				textLines.add(line.toString());
				line.delete(0, line.length());
				line.append(nextWord);
//				textLines.add("");
			}

			if (charArray[i] == ' ') {
				i++;

				nextWord = nextWord(Arrays.copyOfRange(charArray, i, charArray.length));
				if (nextWord.length() + line.length() +1  >= maxLineLength) {
					System.out.print('\n');
//					System.out.printf("{%s} ", nextWord);
					textLines.add(line.toString());
					line.delete(0, line.length());
					line.append(nextWord);

				} else {
//					System.out.printf(" ");
					line.append(" ").append(nextWord);
				}
				System.out.printf("%s(%d)[%d] ", nextWord, nextWord.length(),  nextWord.length() + line.length());
			}
		}
		textLines.add(line.toString());
		return textLines;
	}

	public static int maxLineLength(int fontSize) {
		switch (fontSize){
			case 10:
				return 133;
			case 12 :
				return 112;
			default:
				throw new IllegalStateException("fontSize not supported");

		}
/*		int minimumFontSize = 1;
		int lineLen = LINE_LEN_WITH_FONT_1;

		while (fontSize != minimumFontSize) {
			minimumFontSize++;
			lineLen -= CHAR_INCREASE;
		}

		return lineLen;*/
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