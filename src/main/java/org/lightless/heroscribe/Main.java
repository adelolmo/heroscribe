package org.lightless.heroscribe;

import org.apache.xmlgraphics.java2d.ps.*;
import org.slf4j.*;

import java.io.*;

/**
 * @author Andoni del Olmo
 * @since 12/11/2022
 */
public class Main {

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) throws IOException {
		try (FileOutputStream out = new FileOutputStream("/tmp/test.ps")) {

//		final PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(new File("/tmp/test.ps"))));

			EPSDocumentGraphics2D g2d = new EPSDocumentGraphics2D(false);
			g2d.setGraphicContext(new org.apache.xmlgraphics.java2d.GraphicContext());

//Set up the document size
			g2d.setupDocument(out, 400, 200); //400pt x 200pt
//out is the OutputStream to write the EPS to

			g2d.drawRect(10, 10, 50, 50); //paint a rectangle using normal Java2D calls

			g2d.finish(); //Wrap up and finalize the EPS file
		}

	}

}