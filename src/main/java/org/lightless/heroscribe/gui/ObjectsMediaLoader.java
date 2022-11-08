/*
  HeroScribe
  Copyright (C) 2002-2004 Flavio Chierichetti and Valerio Chierichetti

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

package org.lightless.heroscribe.gui;

import org.lightless.heroscribe.list.List;
import org.lightless.heroscribe.list.*;
import org.lightless.heroscribe.xml.*;
import org.slf4j.*;

import java.awt.*;
import java.util.*;

public class ObjectsMediaLoader {
	private static final Logger log = LoggerFactory.getLogger(ObjectsMediaLoader.class);

	private final ImageLoader imageLoader;

	public ObjectsMediaLoader(ImageLoader imageLoader) {
		this.imageLoader = imageLoader;
	}

	public void loadIcons(List objects, ObjectList objectList) {
		long start, end;
		start = System.currentTimeMillis();

		/* Board */
		final Image europe = imageLoader.addImage(objects.getRasterPath("Europe").toString(), 10);
		objects.getBoard().getIcon("Europe").image = europe;
		objectList.getBoard().getIcon("Europe").setImage(europe);

		final Image usa = imageLoader.addImage(objects.getRasterPath("USA").toString(), 10);
		objects.getBoard().getIcon("USA").image = usa;
		objectList.getBoard().getIcon("USA").setImage(usa);

		final Iterator<LObject> iterator = objects.objectsIterator();
		while (iterator.hasNext()) {
			final String id = iterator.next().id;

			/* Icons */
			final Image eu = imageLoader.addImage(objects.getRasterPath(id, "Europe").toString(), 20);
			objects.getObject(id).getIcon("Europe").image = eu;
			objectList.getObject(id).getIcon("Europe").setImage(eu);

			final Image usa1 = imageLoader.addImage(objects.getRasterPath(id, "USA").toString(), 20);
			objects.getObject(id).getIcon("USA").image =usa1;
			objectList.getObject(id).getIcon("USA").setImage(usa1);
		}

		imageLoader.flush();

		end = System.currentTimeMillis();

		log.info("PNGs loaded (" + (end - start) + "ms).");
	}
}