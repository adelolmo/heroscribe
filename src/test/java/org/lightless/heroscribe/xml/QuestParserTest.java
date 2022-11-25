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
package org.lightless.heroscribe.xml;

import com.fasterxml.jackson.dataformat.xml.*;
import org.junit.jupiter.api.*;

import java.io.*;

import static org.assertj.core.api.Assertions.*;
import static org.lightless.heroscribe.helper.ResourceHelper.*;

public class QuestParserTest {

	private final QuestParser parser = new QuestParser(new XmlMapper());

	@Test
	void shouldParseQuestXmlFile() throws IOException {
		final Quest quest = parser.parse(
				getResourceAsFile("HQBase-01-TheTrial_US.xml"), 26, 19);

		assertThat(quest)
				.extracting("name", "region", "version", "width", "height")
				.containsExactly("The Trial", "USA", "1.0", 1, 2);
	}

	@Test
	void shouldFindTwoBoards() throws IOException {
		final Quest quest = parser.parse(
				getResourceAsFile("HQBase-01-TheTrial_US.xml"), 26, 19);

		assertThat(quest.getBoards())
				.hasSize(2);
	}

	@Test
	void shouldNotFindBoard() throws IOException {
		final Quest quest = parser.parse(
				getResourceAsFile("HQBase-01-TheTrial_US.xml"), 26, 19);

		assertThatThrownBy(() -> {
			quest.getBoard(3, 3);
		}).isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("Can't find the board on col: 3 and row: 3");
	}

	@Test
	void shouldFindBoard() throws IOException {
		final Quest quest = parser.parse(
				getResourceAsFile("HQBase-01-TheTrial_US.xml"), 26, 19);

		assertThat(quest.getBoard(0, 0))
				.isNotNull();
	}

	@Test
	void shouldBeDark() throws IOException {
		final Quest quest = parser.parse(
				getResourceAsFile("HQBase-01-TheTrial_US.xml"), 26, 19);

		assertThat(quest.getBoard(0, 0)
				.isDark(1, 1))
				.isTrue();
	}

	@Test
	void shouldNotBeDark() throws IOException {
		final Quest quest = parser.parse(
				getResourceAsFile("HQBase-01-TheTrial_US.xml"), 26, 19);

		assertThat(quest.getBoard(1, 0)
				.isDark(1, 1))
				.isFalse();
	}

	@Test
	void shouldTurnDark() throws IOException {
		final Quest quest = parser.parse(
				getResourceAsFile("HQBase-01-TheTrial_US.xml"), 26, 19);

		quest.getBoard(1, 0)
				.toggleDark(3, 3);

		assertThat(quest.getBoard(1, 0)
				.isDark(3, 3))
				.isTrue();
	}

	@Test
	void shouldTurnBright() throws IOException {
		final Quest quest = parser.parse(
				getResourceAsFile("HQBase-01-TheTrial_US.xml"), 26, 19);

		quest.getBoard(0, 0)
				.toggleDark(6, 1);

		assertThat(quest.getBoard(0, 0)
				.isDark(6, 1))
				.isFalse();
	}
}
