package org.lightless.heroscribe.gui;

import org.lightless.heroscribe.*;
import org.lightless.heroscribe.helper.*;

import javax.swing.*;
import java.awt.*;

/**
 * @author Andoni del Olmo
 * @since 07/11/2022
 */
public class ImageLoader extends JWindow {

	private MediaTracker mt;
	private Toolkit tk;

	public ImageLoader() {
		super();
		mt = new MediaTracker(this);
		tk = Toolkit.getDefaultToolkit();
	}

	public Dimension getScreenSize(){
		return tk.getScreenSize();
	}
	public Image addImageAndFlush(String path, int id) {
		final Image image = tk.createImage(ResourceHelper.getResourceUrl(path));
		mt.addImage(image, id);

		try {
			mt.waitForID(id);
		} catch (InterruptedException e) {
			throw new HeroScribeException(e);
		}

		if (mt.isErrorID(id)) {
			throw new HeroScribeException("Can't load images.");
		}

		return image;
	}
	public Image addImage(String path, int id){
		final Image image = tk.createImage(path);
		mt.addImage(image, id);
		return image;
	}

	public void flush(){
		try {
			mt.waitForAll();
		} catch (InterruptedException e) {
			throw new HeroScribeException(e);
		}

		if (mt.isErrorAny()) {
			throw new HeroScribeException("Can't load all PNG icons.");
		}
	}

}