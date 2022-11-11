package org.lightless.heroscribe.xml;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.dataformat.xml.annotation.*;

import java.io.*;
import java.util.*;
import java.util.stream.*;

@JsonRootName("quest")
public class Quest {

	private static final String WANDERING_MONSTER_NOTE_MESSAGE =
			"Wandering Monster in this quest: ";

	@JacksonXmlProperty(isAttribute = true)
	private String name;
	@JacksonXmlProperty(isAttribute = true)
	private String region = "Europe";
	@JacksonXmlProperty(isAttribute = true)
	private String version;
	@JacksonXmlProperty(isAttribute = true)
	private int width = 1;
	@JacksonXmlProperty(isAttribute = true)
	private int height = 1;
	@JacksonXmlElementWrapper(useWrapping = false)
	private List<Board> board = new ArrayList<>();
	@JacksonXmlProperty
	private String speech;

	@JacksonXmlElementWrapper(useWrapping = false)
	private List<String> note = new ArrayList<>();

	@JsonIgnore
	private File file;
	@JsonIgnore
	private boolean modified;
	@JsonIgnore
	private boolean[][][] horizontalBridges, verticalBridges;

	public Quest() {
		this(1, 1, 1, 1);
	}

	/*public static Quest emptyQuest(org.lightless.heroscribe.xml.ObjectList.Board board) {
		// new Quest(1, 1, objects.getBoard(), null)
		final Quest quest = new Quest();
		quest.setWidth(1);
		quest.setHeight(1);
		final Board board1 = new Board();
		boards = new QBoard[width][height];

		quest.setBoard(board1);
		return quest;
	}*/

	public Quest(ObjectList.Board board) {
		this(1, 1, board.getWidth(), board.getHeight());
	}

	public Quest(int width, int height, int boardWidth, int boardHeight) {
		setWidth(width);
		setHeight(height);
		updateDimensions(boardWidth, boardHeight);

//		wandering = "Orc";
//		wanderingID = "Orc";
	}

	public void updateDimensions(int boardWidth, int boardHeight) {
//		setWidth(boardWidth);
//		setHeight(boardHeight);
/*		boards = new Quest.Board[this.width][this.height];

		for (int i = 0; i < this.width; i++) {
			for (int j = 0; j < this.height; j++) {
				final QBoard qBoard =
						new QBoard(boardWidth, boardHeight, this);
				boards[i][j] = qBoard;
			}
		}*/

		horizontalBridges = new boolean[width - 1][height][boardHeight];
		verticalBridges = new boolean[width][height - 1][boardWidth];
//		region = "Europe";

		board.add(new Board());
		for (Board b : board) {
			b.setWidth(boardWidth);
			b.setHeight(boardHeight);
		}
	}

	public void setWanderingMonster(String name) {
		note.add(WANDERING_MONSTER_NOTE_MESSAGE + name);
	}

	public static Quest of(int questWidth, int questHeight, int width, int height) {
		return null;
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

	public List<Board> getBoard() {
		return board;
	}

	public void setBoard(List<Board> board) {
		this.board = board;
	}

	public String getSpeech() {
		return speech;
	}

	public void setSpeech(String speech) {
		this.speech = speech;
	}

	public List<String> getNote() {
		return note.stream()
				.filter(n -> !n.startsWith(WANDERING_MONSTER_NOTE_MESSAGE))
				.collect(Collectors.toList());
	}

	public void setNote(List<String> note) {
		this.note = note;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public void setModified(boolean modified) {
		this.modified = modified;
	}

	public boolean isModified() {
		return this.modified;
	}

	public Quest.Board getBoard(int column, int row) {
		int rowIndex = 0;
		for (int i = 0; i < board.size(); i++) {
			if (rowIndex > width) {
				rowIndex = 0;
			}
			if (column + row == i) {
				return board.get(i);
			}
			rowIndex++;
		}
		throw new IllegalStateException("Can't find the board on col: " + column + " and row: " + row);
//		return boards[column][row];
	}

	@JsonIgnore
	public String getWanderingId() {
		return findWanderingMonsterNote()
				.orElseThrow(IllegalStateException::new)
				.substring(WANDERING_MONSTER_NOTE_MESSAGE.length());
	}

	public void setWandering(String name, String id) {
		final Optional<String> wanderingMonsterNote = findWanderingMonsterNote();
		if (wanderingMonsterNote.isEmpty()) {
			note.add(WANDERING_MONSTER_NOTE_MESSAGE + id);
		} else {

			for (int i = 0; i < note.size(); i++) {
				final String s = note.get(i);
				if (s.startsWith(WANDERING_MONSTER_NOTE_MESSAGE)) {
					note.set(i, WANDERING_MONSTER_NOTE_MESSAGE + id);
				}
			}

		}
	}

	private Optional<String> findWanderingMonsterNote() {
		return note.stream()
				.filter(n -> n.startsWith(WANDERING_MONSTER_NOTE_MESSAGE))
				.findFirst();
	}

	public static class Board {
		@JacksonXmlElementWrapper(useWrapping = false)
		private List<Dark> dark = new ArrayList<>();
		@JacksonXmlElementWrapper(useWrapping = false)
		private List<Object> object = new ArrayList<>();
		@JsonIgnore
		private int width;
		@JsonIgnore
		private int height;

		public List<Dark> getDark() {
			return dark;
		}

		public void setDark(List<Dark> dark) {
			this.dark = dark;
		}

		public List<Object> getObject() {
			return object;
		}

		public void setObject(List<Object> object) {
			this.object = object;
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
				dark.remove(optionalDark.get());
			} else {
				dark.add(Dark.of(left, top));
			}

//			quest.setModified(true); TODO implement

		}

		private Optional<Dark> findDark(int left, int top) {
			for (Dark d : dark) {
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

		public boolean addObject(Quest.Board.Object newObj) {
			for (Object obj : object) {
				if (obj.left == newObj.left
						&& obj.top == newObj.top
						&& obj.rotation == newObj.rotation
						&& obj.id.equals(newObj.id))
					return false;
			}

			object.add(newObj);
			//setModified(true); TODO imlement

			return true;
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
		}
	}

}