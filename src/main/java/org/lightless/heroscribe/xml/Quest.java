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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.lightless.heroscribe.utils.Modifiable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@JsonRootName("quest")
public class Quest extends Modifiable<Quest.Type> {

	private static final String WANDERING_MONSTER_NOTE_MESSAGE =
			"Wandering Monster in this quest: ";
	private static final String DEFAULT_WANDERING_MONSTER = "Orc";

	@JacksonXmlProperty(isAttribute = true)
	private String name = "Untitled";
	@JacksonXmlProperty(isAttribute = true)
	private String region = "Europe";
	@JacksonXmlProperty(isAttribute = true)
	private String version;
	@JacksonXmlProperty(isAttribute = true)
	private int width = 1;
	@JacksonXmlProperty(isAttribute = true)
	private int height = 1;
	@JsonProperty("board")
	@JacksonXmlElementWrapper(useWrapping = false)
	private List<Board> boards = new ArrayList<>();
	@JacksonXmlProperty
	private String speech = "";

	@JsonProperty("note")
	@JacksonXmlElementWrapper(useWrapping = false)
	private List<String> notes = new ArrayList<>();

	@JsonProperty("kind")
	@JacksonXmlElementWrapper(useWrapping = false)
	private Set<Kind> kinds = new HashSet<>();

	@JsonIgnore
	private File file;
	@JsonIgnore
	private boolean modified;
	@JsonIgnore
	private boolean[][][] horizontalBridges, verticalBridges;

	public Quest() {
		this(1, 1, 1, 1);
	}

	public Quest(ObjectList.Board board) {
		this(1, 1, board.getWidth(), board.getHeight());
	}

	public Quest(int width, int height, int boardWidth, int boardHeight) {
		setWidth(width);
		setHeight(height);
		for (int i = 0; i < width * height; i++) {
			boards.add(new Board());
		}
		setWanderingMonster("Orc");
		updateDimensions(boardWidth, boardHeight);
	}

	public void updateDimensions(int boardWidth, int boardHeight) {
		horizontalBridges = new boolean[width - 1][height][boardHeight];
		verticalBridges = new boolean[width][height - 1][boardWidth];
		for (Board b : boards) {
			b.setWidth(boardWidth);
			b.setHeight(boardHeight);
		}
	}

	public void setWanderingMonster(String name) {
		notes.add(WANDERING_MONSTER_NOTE_MESSAGE + name);
	}

	public void setHorizontalBridge(boolean value, int column, int row, int top) {
		if (0 <= column && column < width - 1)
			horizontalBridges[column][row][top - 1] = value;
	}

	public boolean getHorizontalBridge(int column, int row, int top) {
		return horizontalBridges[column][row][top - 1];
	}

	public void setVerticalBridge(boolean value, int column, int row, int left) {
		if (0 <= row && row < height - 1)
			verticalBridges[column][row][left - 1] = value;
	}

	public boolean getVerticalBridge(int column, int row, int left) {
		return verticalBridges[column][row][left - 1];
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public List<Board> getBoards() {
		return boards;
	}

	public void setBoards(List<Board> boards) {
		this.boards = boards;
	}

	public String getSpeech() {
		return speech;
	}

	public void setSpeech(String speech) {
		this.speech = speech;
	}

	public Set<Kind> getKinds() {
		return kinds;
	}

	public void setKinds(Set<Kind> kinds) {
		this.kinds = kinds;
	}

	@JsonIgnore
	public List<String> getNotesForUI() {
		return notes.stream()
				.filter(n -> !n.startsWith(WANDERING_MONSTER_NOTE_MESSAGE))
				.collect(Collectors.toList());
	}

	public void addNote(String text) {
		// Ensures that the wandering monster note is always the last one
		int wanderingMonsterNoteIndex = -1;
		for (int i = 0; i < notes.size(); i++) {
			final String note = notes.get(i);
			if (!note.startsWith(WANDERING_MONSTER_NOTE_MESSAGE)) {
				continue;
			}
			wanderingMonsterNoteIndex = i;
		}
		if (wanderingMonsterNoteIndex == -1) {
			notes.add(text);
		} else {
			final String wanderingMonsterNote = notes.get(wanderingMonsterNoteIndex);
			notes.set(wanderingMonsterNoteIndex, text);
			notes.add(wanderingMonsterNote);
		}
	}

	public void setNote(int index, String text) {
		notes.set(index, text);
	}

	public List<String> getNotes() {
		return notes;
	}

	public void setNotes(List<String> notes) {
		this.notes = notes;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public void setModified(boolean modified) {
		this.modified = modified;
		notifyMutation(Type.NOTES);
	}

	public boolean isModified() {
		return this.modified;
	}

	public boolean hasFile() {
		return file != null;
	}

	@JsonIgnore
	public Quest.Board getBoard(int column, int row) {
		int rowIndex = 0;
		for (int i = 0; i < boards.size(); i++) {
			if (rowIndex > width) {
				rowIndex = 0;
			}
			if (column + row == i) {
				return boards.get(i);
			}
			rowIndex++;
		}
		throw new IllegalStateException("Can't find the board on col: " + column + " and row: " + row);
	}

	@JsonIgnore
	public String getWanderingId() {
		return findWanderingMonsterNote()
				.map(note -> note.substring(WANDERING_MONSTER_NOTE_MESSAGE.length()))
				.orElse(DEFAULT_WANDERING_MONSTER);
	}

	public void setWandering(String id) {
		final Optional<String> wanderingMonsterNote = findWanderingMonsterNote();
		if (wanderingMonsterNote.isEmpty()) {
			notes.add(WANDERING_MONSTER_NOTE_MESSAGE + id);
		} else {

			for (int i = 0; i < notes.size(); i++) {
				final String s = notes.get(i);
				if (s.startsWith(WANDERING_MONSTER_NOTE_MESSAGE)) {
					notes.set(i, WANDERING_MONSTER_NOTE_MESSAGE + id);
				}
			}

		}
	}

	public boolean hasWanderingMonster() {
		return findWanderingMonsterNote().isPresent();
	}

	private Optional<String> findWanderingMonsterNote() {
		return notes.stream()
				.filter(n -> n.startsWith(WANDERING_MONSTER_NOTE_MESSAGE))
				.findFirst();
	}

	public static class Board {
		@JsonProperty("dark")
		@JacksonXmlElementWrapper(useWrapping = false)
		private List<Dark> darks = new ArrayList<>();
		@JsonProperty("object")
		@JacksonXmlElementWrapper(useWrapping = false)
		private List<Object> objects = new ArrayList<>();
		@JsonIgnore
		private int width;
		@JsonIgnore
		private int height;

		public List<Dark> getDarks() {
			return darks;
		}

		public void setDarks(List<Dark> darks) {
			this.darks = darks;
		}

		public List<Object> getObjects() {
			return Collections.unmodifiableList(objects);
		}

		public void setObjects(List<Object> objects) {
			this.objects = objects;
		}

		public boolean isDark(int left, int top) {

			if (left == 0 || left == width + 1 || top == 0 || top == height + 1) {
				return false;
			} else {
				return findDark(left, top)
						.isPresent();
			}
		}

		public void toggleDark(int left, int top) {
			if (left == 0 || left == width + 1 || top == 0 || top == height + 1) {
				return;
			}

//			dark[left - 1][top - 1] = !dark[left - 1][top - 1];

			final Optional<Dark> optionalDark = findDark(left, top);
			if (optionalDark.isPresent()) {
				darks.remove(optionalDark.get());
			} else {
				darks.add(Dark.of(left, top));
			}
		}

		private Optional<Dark> findDark(int left, int top) {
			for (Dark d : darks) {
				if (d.getLeft() != left) {
					continue;
				}
				if (d.getTop() != top) {
					continue;
				}
				return Optional.of(d);
			}
			return Optional.empty();
		}

		public boolean addObjectIfNotPresentInCell(Quest.Board.Object newObj) {
			for (Object obj : objects) {
				if (obj.left == newObj.left
						&& obj.top == newObj.top
						&& obj.rotation == newObj.rotation
						&& obj.id.equals(newObj.id))
					return false;
			}

			addObject(newObj);

			return true;
		}

		public void addObject(Object object) {
			objects.add(object);
		}

		public int getWidth() {
			return width;
		}

		public void setWidth(int width) {
			this.width = width;
		}

		public int getHeight() {
			return height;
		}

		public void setHeight(int height) {
			this.height = height;
		}

		public void removeObject(Object object) {
			objects.remove(object);
		}

		private static class Dark {
			@JacksonXmlProperty(isAttribute = true)
			private int left;
			@JacksonXmlProperty(isAttribute = true)
			private int top;
			@JacksonXmlProperty(isAttribute = true)
			private int width;
			@JacksonXmlProperty(isAttribute = true)
			private int height;

			public static Dark of(int left, int top) {
				final Dark d = new Dark();
				d.setLeft(left);
				d.setTop(top);
				d.setWidth(1);
				d.setHeight(1);
				return d;
			}

			public int getLeft() {
				return left;
			}

			public void setLeft(int left) {
				this.left = left;
			}

			public int getTop() {
				return top;
			}

			public void setTop(int top) {
				this.top = top;
			}

			public int getWidth() {
				return width;
			}

			public void setWidth(int width) {
				this.width = width;
			}

			public int getHeight() {
				return height;
			}

			public void setHeight(int height) {
				this.height = height;
			}
		}

		public static class Object implements Comparable<Object> {

			@JacksonXmlProperty(isAttribute = true)
			private String id;

			@JacksonXmlProperty(isAttribute = true)
			private float left;

			@JacksonXmlProperty(isAttribute = true)
			private float top;

			@JacksonXmlProperty(isAttribute = true)
			private Rotation rotation;

			@JacksonXmlProperty(isAttribute = true)
			private float zorder;

			@JacksonXmlProperty(isAttribute = true)
			@JsonSerialize(using = KindToIdString.class)
			@JsonDeserialize(using = KindIdToObject.class)
			private Kind kind;

			@JsonIgnore
			private int order;
			@JsonIgnore
			private static int count = 0;

			public Object() {
				order = getOrder();
			}

			synchronized private static int getOrder() {
				return ++count;
			}

			public String getId() {
				return id;
			}

			public void setId(String id) {
				this.id = id;
			}

			public float getLeft() {
				return left;
			}

			public void setLeft(float left) {
				this.left = left;
			}

			public float getTop() {
				return top;
			}

			public void setTop(float top) {
				this.top = top;
			}

			public Rotation getRotation() {
				return rotation;
			}

			public void setRotation(Rotation rotation) {
				this.rotation = rotation;
			}

			public float getZorder() {
				return zorder;
			}

			public void setZorder(float zorder) {
				this.zorder = zorder;
			}

			public Kind getKind() {
				return kind;
			}

			public void setKind(Kind kind) {
				this.kind = kind;
			}

			@Override
			public int compareTo(Object o) {
				if (this.zorder < o.zorder)
					return -1;
				else if (this.zorder > o.zorder)
					return 1;
				else if (this.order < o.order)
					return -1;
				else if (this.order > o.order)
					return 1;

				return 0;
			}

			@Override
			public String toString() {
				return id;
			}

			public static class KindToIdString extends JsonSerializer<Kind> {

				@Override
				public void serialize(Kind value, JsonGenerator gen, SerializerProvider provider) throws IOException {
					gen.writeString(value.getId());
				}
			}

			public static class KindIdToObject extends JsonDeserializer<Kind> {
				@Override
				public Kind deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
					return new Kind() {{
						setId(((TextNode) p.readValueAsTree()).asText());
					}};
				}
			}
		}
	}

	public enum Type {
		NOTES
	}
}