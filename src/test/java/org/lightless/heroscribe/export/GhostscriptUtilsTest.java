package org.lightless.heroscribe.export;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GhostscriptUtilsTest {

	private static final String SPEECH = "\"War with the eastern Orcs is brewing and the Emperor needs to unite the lesser kingdoms for the conflict to come. To do this, he must find the ancient Star of the West as worn by the Kings of Legend and by Rogar when he battled with Morcar in the ages past. Anyone who finds the gem witll be given 200 gold coins. The gem lies in Barak Tor, the resting place of the Witch Lord. He was also known as the King of the Dead, a powerful servant of Morcar, and was destroyed by the Spirit Blade long ago. The Spirit Blade is the only weapon that can harm him.\"";
	private static final String NOTE = "G Once the Heroes enter this room, they should not be permitted to leave the room until all the Heroes are in the room. When all the Heroes are in the room, Zargon should read the following:\nTo your horror, the light vanishes as if it never existed. Before your weapons are even drawn, the returning light reveals the Dwarf gasping for air. He has taken a knife in the shoulder. This must end now!\"";
	/*
"War with the eastern Orcs is brewing and the Emperor needs to unite the lesser kingdoms for the conflict to
come. To do this, he must find the ancient Star of the West as worn by the Kings of Legend and by Rogar when
he battled with Morcar in the ages past. Anyone who finds the gem witll be given 200 gold coins. The gem lies
in Barak Tor, the resting place of the Witch Lord. He was also known as the King of the Dead, a powerful
servant of Morcar, and was destroyed by the Spirit Blade long ago. The Spirit Blade is the only weapon that can
harm him."
	*/
/*
G Once the Heroes enter this room, they should not be permitted to leave the room until all the Heroes are in the room. When all the
Heroes are in the room, Zargon should read the following:
To your horror, the light vanishes as if it never existed. Before your weapons are even drawn, the returning light reveals the Dwarf
gasping for air. He has taken a knife in the shoulder. This must end now!"
	*/

	@Test
	public void shouldCalculateLinesForFontSize12() {
		assertThat(String.join("\n", GhostscriptUtils.splitLines(SPEECH, 12)))
				.isEqualTo("\"War with the eastern Orcs is brewing and the Emperor needs to unite the lesser kingdoms for the conflict to\n" +
						"come. To do this, he must find the ancient Star of the West as worn by the Kings of Legend and by Rogar when\n" +
						"he battled with Morcar in the ages past. Anyone who finds the gem witll be given 200 gold coins. The gem lies\n" +
						"in Barak Tor, the resting place of the Witch Lord. He was also known as the King of the Dead, a powerful\n" +
						"servant of Morcar, and was destroyed by the Spirit Blade long ago. The Spirit Blade is the only weapon that can\n" +
						"harm him.\"");
	}

	@Test
	public void shouldCalculateLinesForFontSize10() {
		assertThat(String.join("\n", GhostscriptUtils.splitLines(NOTE, 10)))
				.isEqualTo("G Once the Heroes enter this room, they should not be permitted to leave the room until all the Heroes are in the room. When all the\n" +
						"Heroes are in the room, Zargon should read the following:\n" +
						"To your horror, the light vanishes as if it never existed. Before your weapons are even drawn, the returning light reveals the Dwarf\n" +
						"gasping for air. He has taken a knife in the shoulder. This must end now!\"");
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
	void shouldCalculateLineLengths() {
		assertThat(GhostscriptUtils.maxLineLength(1)).isEqualTo(227);
		assertThat(GhostscriptUtils.maxLineLength(2)).isEqualTo(217);
		assertThat(GhostscriptUtils.maxLineLength(3)).isEqualTo(206);
		assertThat(GhostscriptUtils.maxLineLength(4)).isEqualTo(196);
		assertThat(GhostscriptUtils.maxLineLength(5)).isEqualTo(185);
		assertThat(GhostscriptUtils.maxLineLength(6)).isEqualTo(175);
		assertThat(GhostscriptUtils.maxLineLength(7)).isEqualTo(164);
		assertThat(GhostscriptUtils.maxLineLength(8)).isEqualTo(154);
		assertThat(GhostscriptUtils.maxLineLength(9)).isEqualTo(143);
		assertThat(GhostscriptUtils.maxLineLength(10)).isEqualTo(133);
		assertThat(GhostscriptUtils.maxLineLength(11)).isEqualTo(122);
		assertThat(GhostscriptUtils.maxLineLength(12)).isEqualTo(112);
		assertThat(GhostscriptUtils.maxLineLength(13)).isEqualTo(101);
		assertThat(GhostscriptUtils.maxLineLength(14)).isEqualTo(91);
		assertThat(GhostscriptUtils.maxLineLength(15)).isEqualTo(80);
		assertThat(GhostscriptUtils.maxLineLength(16)).isEqualTo(70);
	}
}