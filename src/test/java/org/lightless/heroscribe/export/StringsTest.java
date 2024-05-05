package org.lightless.heroscribe.export;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StringsTest {

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
		assertThat(Strings.numberOfLines(NOTE, 10))
				.isEqualTo(4);
	}

	@Test
	public void shouldCalculateLinesForFontSize12() {
		assertThat(Strings.numberOfLines(SPEECH, 12))
				.isEqualTo(6);
	}

	@Test
	void shouldCalculateWordLengthStartBlank() {
		assertThat(Strings.nextWord(" word two".toCharArray()))
				.isEqualTo("");
	}

	@Test
	void shouldCalculateWordLength() {
		assertThat(Strings.nextWord(SPEECH.toCharArray()))
				.isEqualTo("\"War");
	}
}