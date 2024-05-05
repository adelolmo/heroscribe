package org.lightless.heroscribe.export;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GhostscriptUtilsTest {

	private static final String SPEECH = "\"War with the eastern Orcs is brewing and the Emperor needs to unite the lesser kingdoms for the conflict to come. To do this, he must find the ancient Star of the West as worn by the Kings of Legend and by Rogar when he battled with Morcar in the ages past. Anyone who finds the gem witll be given 200 gold coins. The gem lies in Barak Tor, the resting place of the Witch Lord. He was also known as the King of the Dead, a powerful servant of Morcar, and was destroyed by the Spirit Blade long ago. The Spirit Blade is the only weapon that can harm him.\"";
	private static final String NOTE = "D The tomb of the Witch Lord. The Witch Lord will be released from his imprisonment when the players enter the room. Place the Witch Lord where shown. Read the bold text below to the players.\n\"You have broken the magic seal that kept the Witch Lord imprisoned. Now he has awoken and you must run. Only the Spirit Blade can harm him.\"";
	/*
"War with the eastern Orcs is brewing and the Emperor needs to unite the lesser kingdoms for the conflict to
come. To do this, he must find the ancient Star of the West as worn by the Kings of Legend and by Rogar when
he battled with Morcar in the ages past. Anyone who finds the gem witll be given 200 gold coins. The gem lies
in Barak Tor, the resting place of the Witch Lord. He was also known as the King of the Dead, a powerful
servant of Morcar, and was destroyed by the Spirit Blade long ago. The Spirit Blade is the only weapon that can
harm him."
	*/
/*
D The tomb of the Witch Lord. The Witch Lord will be released from his imprisonment when the players enter the room. Place the
Witch Lord where shown. Read the bold text below to the players.
"You have broken the magic seal that kept the Witch Lord imprisoned. Now he has awoken and you must run. Only the Spirit Blade
can harm him."
	*/

	@Test
	public void shouldCalculateLinesForFontSize10() {
		assertThat(GhostscriptUtils.numberOfLines(NOTE, 10))
				.isEqualTo(4);
	}

	@Test
	public void shouldCalculateLinesForFontSize12() {
		assertThat(GhostscriptUtils.numberOfLines(SPEECH, 12))
				.isEqualTo(6);
	}

	@Test
	void shouldCalculateWordLengthStartBlank() {
		assertThat(GhostscriptUtils.nextWord(" word two".toCharArray()))
				.isEqualTo("");
	}

	@Test
	void shouldCalculateWordLength() {
		assertThat(GhostscriptUtils.nextWord(SPEECH.toCharArray()))
				.isEqualTo("\"War");
	}

	@Test
	void shouldCalculateLineLengths(){
		assertThat(GhostscriptUtils.maxLineLength(1)).isEqualTo(178);
		assertThat(GhostscriptUtils.maxLineLength(2)).isEqualTo(172);
		assertThat(GhostscriptUtils.maxLineLength(3)).isEqualTo(166);
		assertThat(GhostscriptUtils.maxLineLength(4)).isEqualTo(160);
		assertThat(GhostscriptUtils.maxLineLength(5)).isEqualTo(154);
		assertThat(GhostscriptUtils.maxLineLength(6)).isEqualTo(148);
		assertThat(GhostscriptUtils.maxLineLength(7)).isEqualTo(142);
		assertThat(GhostscriptUtils.maxLineLength(8)).isEqualTo(136);
		assertThat(GhostscriptUtils.maxLineLength(9)).isEqualTo(130);
		assertThat(GhostscriptUtils.maxLineLength(10)).isEqualTo(124);
		assertThat(GhostscriptUtils.maxLineLength(11)).isEqualTo(118);
		assertThat(GhostscriptUtils.maxLineLength(12)).isEqualTo(112);
		assertThat(GhostscriptUtils.maxLineLength(13)).isEqualTo(106);
		assertThat(GhostscriptUtils.maxLineLength(14)).isEqualTo(100);
		assertThat(GhostscriptUtils.maxLineLength(15)).isEqualTo(94);
		assertThat(GhostscriptUtils.maxLineLength(16)).isEqualTo(88);
	}
}