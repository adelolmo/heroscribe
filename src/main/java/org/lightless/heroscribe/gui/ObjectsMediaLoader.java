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

package org.lightless.heroscribe.gui;

import org.lightless.heroscribe.xml.*;
import org.slf4j.*;

public class ObjectsMediaLoader {
	private static final Logger log = LoggerFactory.getLogger(ObjectsMediaLoader.class);

	private final ImageLoader imageLoader;

	public ObjectsMediaLoader(ImageLoader imageLoader) {
		this.imageLoader = imageLoader;
	}

	public void loadIcons(ObjectList objectList) {
		long start, end;
		start = System.currentTimeMillis();

		/* Board */
		objectList.getBoard()
				.getIcon("Europe")
				.setImage(imageLoader
						.addImage(objectList.getBoardRasterPath("Europe").toString(), 10));

		objectList.getBoard()
				.getIcon("USA")
				.setImage(imageLoader
						.addImage(objectList.getBoardRasterPath("USA").toString(), 10));

		objectList.getObjects().forEach(object -> {
			/* Icons */
			objectList.getObject(object.getId())
					.getIcon("Europe")
					.setImage(imageLoader
							.addImage(objectList.getObjectRasterPath(object.getId(), "Europe").toString(), 20));

			objectList.getObject(object.getId())
					.getIcon("USA")
					.setImage(imageLoader
							.addImage(objectList.getObjectRasterPath(object.getId(), "USA").toString(), 20));
		});


		imageLoader.flush();

		end = System.currentTimeMillis();

		log.info("PNGs loaded (" + (end - start) + "ms).");
	}
}