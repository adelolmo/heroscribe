/*
  HeroScribe
  Copyright (C) 2002-2004 Flavio Chierichetti and Valerio Chierichetti
  
  HeroScribe Enhanced (changes are prefixed with HSE in comments)
  Copyright (C) 2011 Jason Allen  
   
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

package org.lightless.heroscribe.helper;

import org.lightless.heroscribe.Constants;
import org.lightless.heroscribe.gui.Gui;
import org.lightless.heroscribe.list.LObject;
import org.lightless.heroscribe.quest.*;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.DefaultFontMapper;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

import java.util.Iterator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;

import java.awt.geom.AffineTransform;
import java.awt.image.ImageObserver;

public class BoardPainter implements ImageObserver {
	private Gui gui;
	public Dimension boardSize, boardPixelSize, framePixelSize;
	public float boxEdge;
	public int adjacentBoardsOffset;

	public BoardPainter(Gui gui) throws Exception {
		this.gui = gui;
		
		init();
	}
	
	public void init() {
		boardSize =
			new Dimension(
				gui.getObjects().getBoard().width,
				gui.getObjects().getBoard().height);

		boardPixelSize =
			new Dimension(getBoardIcon().getWidth(this),
				getBoardIcon().getHeight(this));

		boxEdge =
			(getBoardIcon().getWidth(this) * 1.0f)
				/ (gui.getObjects().getBoard().width + 2);

		adjacentBoardsOffset = 
			Math.round(boxEdge * gui.getObjects().getBoard().adjacentBoardsOffset);

		framePixelSize =
			new Dimension(
				boardPixelSize.width * gui.getQuest().getWidth() +
					adjacentBoardsOffset * (gui.getQuest().getWidth() - 1),
				boardPixelSize.height * gui.getQuest().getHeight() +
					adjacentBoardsOffset * (gui.getQuest().getHeight() - 1));
	}

	private void drawBridge(int column, int row, boolean horizontal, int position, Graphics2D g2d) {
		int x, y;
		int width, height;
		
		if ( horizontal ) {
			x = getIntX(column, gui.getQuest().getBoard(column, row).getWidth() + 1);
			y = getIntY(row, position);
			
			width = getIntX(column + 1, 1) - x + 1;
			height = getIntY(row, position + 1) - y + 1;
		} else {
			x = getIntX(column, position);
			y = getIntY(row, gui.getQuest().getBoard(column, row).getHeight() + 1);

			width = getIntX(column, position + 1) - x + 1;
			height = getIntY(row + 1, 1) - y + 1;
		}
		
		g2d.setColor(Color.BLACK);
		g2d.fillRect(x, y, width, height);

		if (getRegion().equals("Europe"))
			g2d.setColor(org.lightless.heroscribe.Constants.europeCorridorColor);
		else if (getRegion().equals("USA"))
			g2d.setColor(org.lightless.heroscribe.Constants.usaCorridorColor);
			
		g2d.fillRect(x + 1, y + 1, width - 2, height - 2);
	}

	private void drawRectangle(int column, int row,
		float left, float top,
		float width, float height, Graphics2D g2d) {
		
		g2d.fillRect(getIntX(column, left), getIntY(row, top),
			(int) Math.ceil(width * boxEdge),
			(int) Math.ceil(height * boxEdge));
	}

	private float getX(int column, float left) {
		return column * (boardPixelSize.width + adjacentBoardsOffset) + 
			left * boxEdge;
	}

	private float getY(int row, float top) {
		return row * (boardPixelSize.height + adjacentBoardsOffset) +
			top * boxEdge;
	}
	
	private int getIntX(int column, float left) {
		return (int) Math.floor(getX(column, left));
	}

	private int getIntY(int row, float top) {
		return (int) Math.floor(getY(row, top));
	}
	
	public void paint(QObject floating, int column, int row, Graphics2D g2d) {
		g2d.setColor(Color.WHITE);

		g2d.fillRect(0, 0, framePixelSize.width, framePixelSize.height);

		for (int i = 0; i < gui.getQuest().getWidth(); i++)
			for (int j = 0; j < gui.getQuest().getHeight(); j++) {
				QBoard board = gui.getQuest().getBoard(i, j);
				
				
				/* Corridors */
				if (getRegion().equals("Europe"))
					g2d.setColor(Constants.europeCorridorColor);
				else if (getRegion().equals("USA"))
					g2d.setColor(Constants.usaCorridorColor);

				for (int left = 1; left <= board.getWidth(); left++)
					for (int top = 1; top <= board.getHeight(); top++)
						if (gui.getObjects().board.corridors[left][top])
							drawRectangle(i, j, left, top, 1, 1, g2d);

				/* Dark Areas */
				if (getRegion().equals("Europe"))
					g2d.setColor(Constants.europeDarkColor);
				else if (getRegion().equals("USA"))
					g2d.setColor(Constants.usaDarkColor);

				for (int left = 1; left <= board.getWidth(); left++)
					for (int top = 1; top <= board.getHeight(); top++)
						if (board.isDark(left, top))
							drawRectangle(i, j, left, top, 1, 1, g2d);

				/* Board */
				g2d.drawImage(getBoardIcon(), getIntX(i, 0), getIntY(j, 0), this);
			}

		/* Bridges */
		for (int i = 0; i < gui.getQuest().getWidth(); i++)
			for (int j = 0; j < gui.getQuest().getHeight(); j++) {
				QBoard board = gui.getQuest().getBoard(i, j);

				if ( i < gui.getQuest().getWidth() - 1 )
					for (int top = 1 ; top <= board.getHeight() ; top++ )
						if ( gui.getQuest().getHorizontalBridge(i, j, top) )
							drawBridge(i, j, true, top, g2d);	
				
				if ( j < gui.getQuest().getHeight() - 1 )
					for (int left = 1 ; left <= board.getWidth() ; left++ )	
						if ( gui.getQuest().getVerticalBridge(i, j, left) )
							drawBridge(i, j, false, left, g2d);	
			}
			
		for (int i = 0; i < gui.getQuest().getWidth(); i++)
			for (int j = 0; j < gui.getQuest().getHeight(); j++) {
				QBoard board = gui.getQuest().getBoard(i, j);

				/* Objects */
				Iterator iterator = board.iterator();
				while (iterator.hasNext()) {
					QObject obj = (QObject) iterator.next();

					drawIcon(obj, i, j, g2d);
				}
			}

		if (floating != null)
			drawIcon(floating, column, row, g2d);
	}
	
	// HSE - used to draw the board with the quest text included
	public void paintWithText(QObject floating, int column, int row, Graphics2D g2d) {
		g2d.setColor(Color.WHITE);

		g2d.fillRect(0, 0, framePixelSize.width, framePixelSize.height + 400);

		for (int i = 0; i < gui.getQuest().getWidth(); i++)
			for (int j = 0; j < gui.getQuest().getHeight(); j++) {
				QBoard board = gui.getQuest().getBoard(i, j);
				
				
				/* Corridors */
				if (getRegion().equals("Europe"))
					g2d.setColor(Constants.europeCorridorColor);
				else if (getRegion().equals("USA"))
					g2d.setColor(Constants.usaCorridorColor);

				for (int left = 1; left <= board.getWidth(); left++)
					for (int top = 1; top <= board.getHeight(); top++)
						if (gui.getObjects().board.corridors[left][top])
							drawRectangle(i, j, left, top, 1, 1, g2d);

				/* Dark Areas */
				if (getRegion().equals("Europe"))
					g2d.setColor(Constants.europeDarkColor);
				else if (getRegion().equals("USA"))
					g2d.setColor(Constants.usaDarkColor);

				for (int left = 1; left <= board.getWidth(); left++)
					for (int top = 1; top <= board.getHeight(); top++)
						if (board.isDark(left, top))
							drawRectangle(i, j, left, top, 1, 1, g2d);

				/* Board */
				g2d.drawImage(getBoardIcon(), getIntX(i, 0), getIntY(j, 0), this);
			}

		/* Bridges */
		for (int i = 0; i < gui.getQuest().getWidth(); i++)
			for (int j = 0; j < gui.getQuest().getHeight(); j++) {
				QBoard board = gui.getQuest().getBoard(i, j);

				if ( i < gui.getQuest().getWidth() - 1 )
					for (int top = 1 ; top <= board.getHeight() ; top++ )
						if ( gui.getQuest().getHorizontalBridge(i, j, top) )
							drawBridge(i, j, true, top, g2d);	
				
				if ( j < gui.getQuest().getHeight() - 1 )
					for (int left = 1 ; left <= board.getWidth() ; left++ )	
						if ( gui.getQuest().getVerticalBridge(i, j, left) )
							drawBridge(i, j, false, left, g2d);	
			}
			
		for (int i = 0; i < gui.getQuest().getWidth(); i++)
			for (int j = 0; j < gui.getQuest().getHeight(); j++) {
				QBoard board = gui.getQuest().getBoard(i, j);

				/* Objects */
				Iterator iterator = board.iterator();
				while (iterator.hasNext()) {
					QObject obj = (QObject) iterator.next();

					drawIcon(obj, i, j, g2d);
				}
			}

		if (floating != null)
			drawIcon(floating, column, row, g2d);
		
		// HSE - write quest name
		Font font = new Font("Times New Roman", Font.PLAIN, 14);
		FontMetrics metrics = g2d.getFontMetrics(font);
		float textWidth;
		float xPos, margin, yPos;
		
		margin = 40;
		
		textWidth = metrics.stringWidth(gui.getQuest().getName());
		xPos = (framePixelSize.width /2) - (textWidth / 2);
		yPos = (gui.getQuest().getHeight()*700)+20;
		
		g2d.setColor(new Color(127, 0, 21, 255));
		g2d.setFont(font);
		g2d.drawString(gui.getQuest().getName(), xPos, yPos);
		
		// HSE - write quest speech
		g2d.setColor(new Color(0, 0, 0, 255));
		font = new Font("Times New Roman", Font.ITALIC, 12);
		g2d.setFont(font);
		metrics = g2d.getFontMetrics(font);
		xPos = margin;
		yPos += (metrics.getHeight()*2);
		
		// HSE - break out speech by line
		String linefeeds[] = gui.getQuest().getSpeech().split("\n");
		String words[];
		String output = new String("");
		
		// HSE - loop for each line
		for (int i = 0; i < linefeeds.length; i++) {
			words = linefeeds[i].split(" ");
			// HSE - loop for each word in the line
			for (int j = 0; j < words.length; j++) {
				textWidth = metrics.stringWidth(words[j] + " ");
				if (xPos + textWidth > (framePixelSize.width-(margin*3))) {
					// new line, print current output
					g2d.drawString(output, margin, yPos);
					output = "";

					xPos = margin;
					yPos += metrics.getHeight();
				}
				
				// add to output buffer
				output += words[j] + " "; 
				xPos += textWidth;
							
				// check for last word
				if (j == words.length-1) {
					g2d.drawString(output, margin, yPos);
					output="";
				}
			}
			
			// new line
			yPos += metrics.getHeight();
		}
		
		// HSE - write quest master notes
		font = new Font("Times New Roman", Font.PLAIN, 10);
		metrics = g2d.getFontMetrics(font);
		g2d.setFont(font);
		xPos = margin;
		yPos += (metrics.getHeight()*3);
		
		Iterator iterator = gui.getQuest().notesIterator();
		while (iterator.hasNext()) {
			String note = (String) iterator.next();
			g2d.drawString(note, margin, yPos);
			yPos += (metrics.getHeight()*1.5);
		}
		
		// HSE - write wandering monster
		metrics = g2d.getFontMetrics(font);
		textWidth = metrics.stringWidth("Wandering Monster in this Quest: " + gui.getQuest().getWandering());
		
		xPos = (framePixelSize.width /2) - (textWidth / 2);
		yPos = framePixelSize.height +400 - 40;
		g2d.drawString("Wandering Monster in this Quest: " + gui.getQuest().getWandering(), xPos, yPos);
		Image icon = getObjectIconByName(gui.getQuest().getWandering());
		if (icon != null) {
			g2d.drawImage(icon, Math.round(xPos-icon.getWidth(null)-5), Math.round(yPos-3-(icon.getHeight(null)/2)), this);
		}
	}
	
	// HSE - used to draw the board with the quest text included to a PDF
	public void paintPDF(QObject floating, int column, int row, PdfWriter writer, Document document) {
		
		PdfContentByte cb = writer.getDirectContent();
		PdfTemplate tp[][] = new PdfTemplate[gui.getQuest().getWidth()][gui.getQuest().getHeight()];
		Graphics2D g2d;

		// reset the frame size so we can print one page per board
		Dimension tmpFrame = framePixelSize;
		this.framePixelSize = new Dimension(boardPixelSize.width+ adjacentBoardsOffset,boardPixelSize.height+adjacentBoardsOffset);

		
		for (int i = 0; i < gui.getQuest().getWidth(); i++)
			for (int j = 0; j < gui.getQuest().getHeight(); j++) {
							
				tp[i][j]=cb.createTemplate(this.framePixelSize.width, this.framePixelSize.height+400);
				g2d = tp[i][j].createGraphics(this.framePixelSize.width, this.framePixelSize.height+400, new DefaultFontMapper());
				
				g2d.setColor(Color.WHITE);
				g2d.fillRect(0, 0, framePixelSize.width, framePixelSize.height + 400);
			
				QBoard board = gui.getQuest().getBoard(i, j);		
				
				/* Corridors */
				if (getRegion().equals("Europe"))
					g2d.setColor(Constants.europeCorridorColor);
				else if (getRegion().equals("USA"))
					g2d.setColor(Constants.usaCorridorColor);

				for (int left = 1; left <= board.getWidth(); left++)
					for (int top = 1; top <= board.getHeight(); top++)
						if (gui.getObjects().board.corridors[left][top])
							drawRectangle(i, j, left, top, 1, 1, g2d);

				/* Dark Areas */
				if (getRegion().equals("Europe"))
					g2d.setColor(Constants.europeDarkColor);
				else if (getRegion().equals("USA"))
					g2d.setColor(Constants.usaDarkColor);

				for (int left = 1; left <= board.getWidth(); left++)
					for (int top = 1; top <= board.getHeight(); top++)
						if (board.isDark(left, top))
							drawRectangle(i, j, left, top, 1, 1, g2d);

				/* Board */
				g2d.drawImage(getBoardIcon(), getIntX(0, 0), getIntY(0, 0), this);

				/* Bridges */
				if ( i < gui.getQuest().getWidth() - 1 )
					for (int top = 1 ; top <= board.getHeight() ; top++ )
						if ( gui.getQuest().getHorizontalBridge(i, j, top) )
							drawBridge(0, 0, true, top, g2d);	
				
				if ( j < gui.getQuest().getHeight() - 1 )
					for (int left = 1 ; left <= board.getWidth() ; left++ )	
						if ( gui.getQuest().getVerticalBridge(i, j, left) )
							drawBridge(0, 0, false, left, g2d);	
	
				/* Objects */
				Iterator iterator = board.iterator();
				while (iterator.hasNext()) {
					QObject obj = (QObject) iterator.next();
	
					drawIcon(obj, 0, 0, g2d);
				}
				
	
				// HSE - write quest name
				Font font = new Font("Times New Roman", Font.PLAIN, 14);
				FontMetrics metrics = g2d.getFontMetrics(font);
				float textWidth;
				float xPos, margin, yPos;
				
				margin = 40;
				
				textWidth = metrics.stringWidth(gui.getQuest().getName());
				xPos = (framePixelSize.width /2) - (textWidth / 2);
				yPos = 720;
				
				g2d.setColor(new Color(127, 0, 21, 255));
				g2d.setFont(font);
				g2d.drawString(gui.getQuest().getName(), xPos, yPos);
				
				// HSE - write quest speech
				g2d.setColor(new Color(0, 0, 0, 255));
				font = new Font("Times New Roman", Font.ITALIC, 12);
				g2d.setFont(font);
				metrics = g2d.getFontMetrics(font);
				xPos = margin;
				yPos += (metrics.getHeight()*2);
				
				// HSE - break out speech by line
				String linefeeds[] = gui.getQuest().getSpeech().split("\n");
				String words[];
				String output = new String("");
				
				// HSE - loop for each line
				for (int i1 = 0; i1 < linefeeds.length; i1++) {
					words = linefeeds[i1].split(" ");
					// HSE - loop for each word in the line
					for (int j1 = 0; j1 < words.length; j1++) {
						textWidth = metrics.stringWidth(words[j1] + " ");
						if (xPos + textWidth > (framePixelSize.width-(margin*3))) {
							// new line, print current output
							g2d.drawString(output, margin, yPos);
							output = "";
		
							xPos = margin;
							yPos += metrics.getHeight();
						}
						
						// add to output buffer
						output += words[j1] + " "; 
						xPos += textWidth;
									
						// check for last word
						if (j1 == words.length-1) {
							g2d.drawString(output, margin, yPos);
							output="";
						}
					}
					
					// new line
					yPos += metrics.getHeight();
				}
				
				// HSE - write quest master notes
				font = new Font("Times New Roman", Font.PLAIN, 10);
				metrics = g2d.getFontMetrics(font);
				g2d.setFont(font);
				xPos = margin;
				yPos += (metrics.getHeight()*3);
				
				iterator = gui.getQuest().notesIterator();
				while (iterator.hasNext()) {
					String note = (String) iterator.next();
					g2d.drawString(note, margin, yPos);
					yPos += (metrics.getHeight()*1.5);
				}
				
				// HSE - write map coords if multi map
				if (gui.getQuest().getHeight()> 1 || gui.getQuest().getWidth() > 1) {
					String note = "Board Location: (" + i + "," + j + ")";
					g2d.drawString(note, margin, yPos);
					yPos += (metrics.getHeight()*1.5);
				}
				
				// HSE - write wandering monster
				metrics = g2d.getFontMetrics(font);
				textWidth = metrics.stringWidth("Wandering Monster in this Quest: " + gui.getQuest().getWandering());
				
				xPos = (framePixelSize.width /2) - (textWidth / 2);
				yPos = framePixelSize.height +400 - 40;
				g2d.drawString("Wandering Monster in this Quest: " + gui.getQuest().getWandering(), xPos, yPos);
				Image icon = getObjectIconByName(gui.getQuest().getWandering());
				if (icon != null) {
					g2d.drawImage(icon, Math.round(xPos-icon.getWidth(null)-5), Math.round(yPos-3-(icon.getHeight(null)/2)), this);
				}
				
				cb.addTemplate(tp[i][j], 0, 0);
				document.newPage();
				
				g2d.dispose();
			}
		
			this.framePixelSize=tmpFrame;
	}
	
	private void drawIcon(QObject piece, int column, int row, Graphics2D g2d) {
		AffineTransform original = null;
		float x, y, xoffset, yoffset;
		int width, height;
		LObject obj = gui.getObjects().getObject(piece.id);

		if (!isWellPositioned(piece))
			return;

		if (piece.rotation % 2 == 0) {
			width = obj.width;
			height = obj.height;
		} else {
			width = obj.height;
			height = obj.width;
		}

		if (obj.trap) {
			if (getRegion().equals("Europe"))
				g2d.setColor(Constants.europeTrapColor);
			else if (getRegion().equals("USA"))
				g2d.setColor(Constants.usaTrapColor);

			drawRectangle(0, 0, piece.left, piece.top, width, height, g2d);
		}

		x = piece.left + width / 2.0f;
		y = piece.top + height / 2.0f;

		if (obj.door) {
			if (piece.rotation % 2 == 0) {
				if (piece.top == 0)
					y -= gui.getObjects().getBoard().borderDoorsOffset;
				else if (piece.top == boardSize.height)
					y += gui.getObjects().getBoard().borderDoorsOffset;
			} else {
				if (piece.left == 0)
					x -= gui.getObjects().getBoard().borderDoorsOffset;
				else if (piece.left == boardSize.width)
					x += gui.getObjects().getBoard().borderDoorsOffset;
			}
		}

		xoffset = obj.getIcon(getRegion()).xoffset;
		yoffset = obj.getIcon(getRegion()).yoffset;

		switch (piece.rotation) {
			case 0 :
				x += xoffset;
				y += yoffset;
				break;

			case 1 :
				x += yoffset;
				y -= xoffset;
				break;

			case 2 :
				x -= xoffset;
				y -= yoffset;
				break;

			case 3 :
				x -= yoffset;
				y += xoffset;
				break;
		}

		x = getX(column, x);
		y = getY(row, y);

		if (piece.rotation != 0) {
			AffineTransform rotated;

			original = g2d.getTransform();
			rotated = (AffineTransform) (original.clone());

			rotated.rotate((-Math.PI / 2) * piece.rotation, x, y);

			g2d.setTransform(rotated);
		}

		x -= getObjectIcon(obj.id).getWidth(this) / 2.0f;
		y -= getObjectIcon(obj.id).getHeight(this) / 2.0f;

		g2d.drawImage(getObjectIcon(obj.id), Math.round(x), Math.round(y), this);

		if (piece.rotation != 0)
			g2d.setTransform(original);
	}

	private Image getObjectIcon(String id) {
		return gui.getObjects().getObject(id).getIcon(getRegion()).image;
	}
	
	
	// HSE - get an object by its name
	private Image getObjectIconByName(String name) {
		return gui.getObjects().getObjectByName(name).getIcon(getRegion()).image;
	}

	private Image getBoardIcon() {
		return gui.getObjects().getBoard().getIcon(getRegion()).image;
	}

	private String getRegion() {
		return gui.getQuest().getRegion();
	}

	public boolean isWellPositioned(QObject piece) {
		LObject obj = gui.getObjects().getObject(piece.id);

		int width, height;

		if (piece.rotation % 2 == 0) {
			width = obj.width;
			height = obj.height;
		} else {
			width = obj.height;
			height = obj.width;
		}

		if (obj.door) {
			if (piece.left < 0
				|| piece.top < 0
				|| piece.left + width - 1 > boardSize.width + 1
				|| piece.top + height - 1 > boardSize.height + 1
				|| (piece.rotation % 2 == 0
					&& (piece.left == 0 || piece.left == boardSize.width + 1))
				|| (piece.rotation % 2 == 1
					&& (piece.top == 0 || piece.top == boardSize.height + 1)))
				return false;
		} else {
			if (piece.left < 1
				|| piece.top < 1
				|| piece.left + width - 1 > boardSize.width
				|| piece.top + height - 1 > boardSize.height)
				return false;
		}

		return true;
	}

	public boolean imageUpdate(
		Image img,
		int infoflags,
		int x,
		int y,
		int width,
		int height) {
		return false;
	}
}
