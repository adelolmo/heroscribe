package org.lightless.heroscribe.quest;

import java.util.*;

public class QBoard {
	private final boolean[][] dark;

	private final TreeSet<QObject> objects;

	private final org.lightless.heroscribe.xml.Quest quest;

	private final int width, height;

	public QBoard(int width, int height, org.lightless.heroscribe.xml.Quest quest) {
		this.quest = quest;
		this.width = width;
		this.height = height;

		objects = new TreeSet<>();

		dark = new boolean[width][height];
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public boolean isDark(int left, int top) {
		if (left == 0 || left == width + 1 || top == 0 || top == height + 1)
			return false;
		else
			return dark[left - 1][top - 1];
	}

	public void toggleDark(int left, int top) {
		if (left == 0 || left == width + 1 || top == 0 || top == height + 1)
			return;

		dark[left - 1][top - 1] = !dark[left - 1][top - 1];

		quest.setModified(true);
	}

	public boolean addObject(QObject newObj) {
		Iterator<QObject> iterator = iterator();

		while (iterator.hasNext()) {
			QObject obj = iterator.next();

			if (obj.left == newObj.left && obj.top == newObj.top && obj.rotation == newObj.rotation && obj.id.equals(newObj.id))
				return false;
		}

		objects.add(newObj);

		quest.setModified(true);

		return true;
	}

	public boolean removeObject(QObject obj) {
		if (objects.remove(obj)) {
			quest.setModified(true);

			return true;
		} else
			return false;
	}

	public Iterator<QObject> iterator() {
		return objects.iterator();
	}

	public Set<QObject> getObjects() {
		return objects;
	}
}
