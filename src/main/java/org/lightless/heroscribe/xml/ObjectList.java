package org.lightless.heroscribe.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.*;
import org.lightless.heroscribe.bundle.*;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.*;

public class ObjectList {

	@JacksonXmlProperty(isAttribute = true)
	private String version;
	@JacksonXmlProperty(isAttribute = true)
	private String vectorPrefix;
	@JacksonXmlProperty(isAttribute = true)
	private String vectorSuffix;
	@JacksonXmlProperty(isAttribute = true)
	private String rasterPrefix;
	@JacksonXmlProperty(isAttribute = true)
	private String rasterSuffix;
	@JacksonXmlProperty(isAttribute = true)
	private String samplePrefix;
	@JacksonXmlProperty(isAttribute = true)
	private String sampleSuffix;

	@JacksonXmlElementWrapper(useWrapping = false)
	private List<Kind> kind;

	@JacksonXmlElementWrapper(useWrapping = false)
	private Board board;
	@JacksonXmlElementWrapper(useWrapping = false)
	private List<Object> object;

	public List<String> getKindIds() {
		return kind.stream()
				.map(Kind::getId)
				.collect(Collectors.toList());
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getVectorPrefix() {
		return vectorPrefix;
	}

	public void setVectorPrefix(String vectorPrefix) {
		this.vectorPrefix = vectorPrefix;
	}

	public String getVectorSuffix() {
		return vectorSuffix;
	}

	public void setVectorSuffix(String vectorSuffix) {
		this.vectorSuffix = vectorSuffix;
	}

	public String getRasterPrefix() {
		return rasterPrefix;
	}

	public void setRasterPrefix(String rasterPrefix) {
		this.rasterPrefix = rasterPrefix;
	}

	public String getRasterSuffix() {
		return rasterSuffix;
	}

	public void setRasterSuffix(String rasterSuffix) {
		this.rasterSuffix = rasterSuffix;
	}

	public String getSamplePrefix() {
		return samplePrefix;
	}

	public void setSamplePrefix(String samplePrefix) {
		this.samplePrefix = samplePrefix;
	}

	public String getSampleSuffix() {
		return sampleSuffix;
	}

	public void setSampleSuffix(String sampleSuffix) {
		this.sampleSuffix = sampleSuffix;
	}

	public List<Kind> getKind() {
		return kind;
	}

	public void setKind(List<Kind> kind) {
		this.kind = kind;
	}

	public Board getBoard() {
		return board;
	}

	public void setBoard(Board board) {
		this.board = board;
	}

	public List<Object> getObject() {
		return object;
	}

	public void setObject(List<Object> object) {
		this.object = object;
	}

	public ObjectList.Object getObject(String id) {
		return getOptionalObject(id)
				.orElseThrow(IllegalStateException::new);
	}

	public boolean containsObjectById(String id) {
		return getOptionalObject(id).isPresent();
	}

	private Optional<Object> getOptionalObject(String id) {
		return object.stream()
				.filter(object1 -> object1.getId().equals(id))
				.findFirst();
	}

	public static class Kind {
		@JacksonXmlProperty(isAttribute = true)
		private String id;
		@JacksonXmlProperty(isAttribute = true)
		private String name;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public static class Board {
		@JacksonXmlProperty(isAttribute = true)
		private int width;
		@JacksonXmlProperty(isAttribute = true)
		private int height;
		@JacksonXmlProperty(isAttribute = true)
		private double borderDoorsOffset;
		@JacksonXmlProperty(isAttribute = true)
		private float adjacentBoardsOffset;

		@JacksonXmlElementWrapper(useWrapping = false)
		private List<Icon> icon;

		@JacksonXmlElementWrapper(useWrapping = false)
		private List<Corridor> corridor;

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

		public double getBorderDoorsOffset() {
			return borderDoorsOffset;
		}

		public void setBorderDoorsOffset(double borderDoorsOffset) {
			this.borderDoorsOffset = borderDoorsOffset;
		}

		public float getAdjacentBoardsOffset() {
			return adjacentBoardsOffset;
		}

		public void setAdjacentBoardsOffset(float adjacentBoardsOffset) {
			this.adjacentBoardsOffset = adjacentBoardsOffset;
		}

		public List<Icon> getIcon() {
			return icon;
		}

		public void setIcon(List<Icon> icon) {
			this.icon = icon;
		}

		public List<Corridor> getCorridor() {
			return corridor;
		}

		public void setCorridor(List<Corridor> corridor) {
			this.corridor = corridor;
		}

		public Icon getIcon(String region) {
			return icon.stream()
					.filter(icon1 -> {return icon1.getRegion().equals(region);})
					.findFirst()
					.orElseThrow(IllegalStateException::new);
		}

		public static class Corridor {
			@JacksonXmlProperty(isAttribute = true)
			private int left;
			@JacksonXmlProperty(isAttribute = true)
			private int top;
			@JacksonXmlProperty(isAttribute = true)
			private int width;
			@JacksonXmlProperty(isAttribute = true)
			private int height;

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
	}

	public static class Object {
		@JacksonXmlProperty(isAttribute = true)
		private String name;
		@JacksonXmlProperty(isAttribute = true)
		private String id;
		@JacksonXmlProperty(isAttribute = true)
		private int width;
		@JacksonXmlProperty(isAttribute = true)
		private int height;
		@JacksonXmlProperty(isAttribute = true)
		private String kind;
		@JacksonXmlProperty(isAttribute = true)
		private float zorder;
		@JacksonXmlProperty(isAttribute = true)
		private boolean door;
		@JacksonXmlProperty(isAttribute = true)
		private boolean trap;

		@JacksonXmlElementWrapper(useWrapping = false)
		private List<Icon> icon;

		@JacksonXmlProperty
		private String note;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
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

		public String getKind() {
			return kind;
		}

		public void setKind(String kind) {
			this.kind = kind;
		}

		public float getZorder() {
			return zorder;
		}

		public void setZorder(float zorder) {
			this.zorder = zorder;
		}

		public boolean isDoor() {
			return door;
		}

		public void setDoor(boolean door) {
			this.door = door;
		}

		public boolean isTrap() {
			return trap;
		}

		public void setTrap(boolean trap) {
			this.trap = trap;
		}

		public List<Icon> getIcon() {
			return icon;
		}

		public void setIcon(List<Icon> icon) {
			this.icon = icon;
		}

		public String getNote() {
			return note;
		}

		public void setNote(String note) {
			this.note = note;
		}

		public String getIconPath(String region) {
			final String iconPath = icon.stream()
					.filter(icon1 -> {
						return region.equals(icon1.getRegion());
					})
					.findFirst()
					.orElseThrow(IllegalStateException::new)
					.getPath();
			return  iconPath ;
		}

		public String toString(){
			return name;
		}

		public ObjectList.Icon getIcon(String region) {
			return icon.stream()
					.filter(icon1 -> { return icon1.getRegion().equals(region);})
					.findFirst()
					.orElseThrow(IllegalStateException::new);
		}
	}

	private String getPrefix(IconType iconType) {
		switch (iconType) {
			case VECTOR:
				return vectorPrefix;
			case RASTER:
				return rasterPrefix;
			case SAMPLE:
				return samplePrefix;
		}
		throw new IllegalStateException("Unsupported icon type: " + iconType.getName());
	}
	private String getSuffix(IconType iconType) {
		switch (iconType) {
			case VECTOR:
				return vectorSuffix;
			case RASTER:
				return rasterSuffix;
			case SAMPLE:
				return sampleSuffix;
		}
		throw new IllegalStateException("Unsupported icon type: " + iconType.getName());
	}

	public static class Icon {
		@JacksonXmlProperty(isAttribute = true)
		private String region;
		@JacksonXmlProperty(isAttribute = true)
		private String path;
		@JacksonXmlProperty(isAttribute = true)
		private boolean original;
		@JacksonXmlProperty(isAttribute = true)
		private float yoffset;
		@JacksonXmlProperty(isAttribute = true)
		private float xoffset;
		private Image image;

		public String getRegion() {
			return region;
		}

		public void setRegion(String region) {
			this.region = region;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public boolean isOriginal() {
			return original;
		}

		public void setOriginal(boolean original) {
			this.original = original;
		}

		public float getYoffset() {
			return yoffset;
		}

		public void setYoffset(float yoffset) {
			this.yoffset = yoffset;
		}

		public float getXoffset() {
			return xoffset;
		}

		public void setXoffset(float xoffset) {
			this.xoffset = xoffset;
		}

		public Image getImage() {
			return image;
		}

		public void setImage(Image image) {
			this.image = image;
		}
	}
}