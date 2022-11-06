/*
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

package org.lightless.heroscribe.gui;

import org.lightless.heroscribe.*;
import org.lightless.heroscribe.export.*;
import org.lightless.heroscribe.helper.*;
import org.lightless.heroscribe.list.List;
import org.lightless.heroscribe.quest.Read;
import org.lightless.heroscribe.quest.*;
import org.slf4j.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class Gui extends JFrame implements WindowListener, ItemListener, ActionListener {

	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(Gui.class);

	private final List objects;
	private Quest quest;
	private final Preferences prefs;

	ToolsPanel tools;
	Board board;

	BoardPainter boardPainter;

	private final JFileChooser fileChooser = new FileChooser();
	private final JFileChooser ghostscriptChooser = new JFileChooser();

	TreeMap<String, FileFilter> filters = new TreeMap<>();

	JRadioButtonMenuItem europeItem, usaItem;
	private JMenuItem newKey, openKey, saveKey, saveAsKey, exportPdfKey, exportEpsKey, exportPngKey, ghostscriptKey,
			quitKey, listKey, aboutKey, dirKey, readMeKey, exportPdf2Key, exportThumbNail, propertiesKey;

	Vector<JMenuItem> newSpecialKeys;

	JLabel hint, status;

	public Gui(Preferences preferences, List objects, Quest quest) {
		super();
		// HSE - set app icon
		setIconImage(Toolkit.getDefaultToolkit().getImage("HeroScribe.png"));
		this.prefs = preferences;
		this.objects = objects;
		this.quest = quest;

		ghostscriptChooser.setFileFilter(new GhostScriptFileFilter());

		fileChooser.setCurrentDirectory(prefs.defaultDir);
		filters.put("pdf", new ActualFileFilter("pdf", "PDF files (*.pdf)"));
		filters.put("eps", new ActualFileFilter("eps", "EPS files (*.eps)"));
		filters.put("png", new ActualFileFilter("png", "PNG files (*.png)"));
		filters.put("xml", new ActualFileFilter("xml", "HeroScribe Quests (*.xml)"));

		boardPainter = new BoardPainter(this);

		tools = new ToolsPanel(this, quest);
		board = new Board(this);

		newSpecialKeys = new Vector<>();
		newSpecialKeys.add(new SpecialQuestMenuItem(1, 2));
		newSpecialKeys.add(new SpecialQuestMenuItem(2, 1));
		newSpecialKeys.add(new SpecialQuestMenuItem(2, 2));
		newSpecialKeys.add(new SpecialQuestMenuItem(2, 3));
		newSpecialKeys.add(new SpecialQuestMenuItem(3, 2));
		newSpecialKeys.add(new SpecialQuestMenuItem(3, 3));

		populateFrame();

		setMenuRegion();
		updateHint();
		updateTitle();

		addWindowListener(this);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		Toolkit tk = Toolkit.getDefaultToolkit();

		setLocation((tk.getScreenSize().width - this.getWidth()) / 2,
				(tk.getScreenSize().height - this.getHeight()) / 2);

		setVisible(true);
	}

	public void updateTitle() {
		final StringBuilder sb =
				new StringBuilder(Constants.applicationName + " " + Constants.VERSION + Constants.applicationVersionSuffix + " - ");

		if (quest.getFile() == null) {
			sb.append("Untitled");
		} else {
			sb.append(quest.getFile().getName());
		}

		if (quest.isModified()) {
			sb.append("*");
		}

		setTitle(new String(sb));
	}

	private void populateFrame() {
		Container content;
		final JMenuBar menu = new JMenuBar();
		final JMenu file = new JMenu("File");
		final JMenu region = new JMenu("Region");
		final JMenu help = new JMenu("Help");

		final JMenu newMenu = new JMenu("New");
		final JMenu exportMenu = new JMenu("Export");
		final JMenu prefsMenu = new JMenu("Preferences");

		final JPanel bottom = new JPanel();

		final ButtonGroup regionGroup = new ButtonGroup();

		/* New Menu */
		newKey = new JMenuItem("Quest",
				new ImageIcon(ResourceHelper.getResourceUrl("Icons/new.png").getFile()));
		// HSE - add menu modifier 'Ctrl+N'
		newKey.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx(),
				false));
		newKey.addActionListener(this);
		newMenu.add(newKey);

		newMenu.addSeparator();

		for (JMenuItem newSpecialKey : newSpecialKeys) {
			SpecialQuestMenuItem menuItem = (SpecialQuestMenuItem) newSpecialKey;

			menuItem.addActionListener(this);
			newMenu.add(menuItem);
		}

		/* Export Menu */
		exportPdfKey = new JMenuItem("PDF (high quality, requires GhostScript) ...",
				new ImageIcon(ResourceHelper.getResourceUrl("Icons/export.png").getFile()));
		// HSE - add menu modifier 'Ctrl-P'
		exportPdfKey.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx(),
				false));
		exportPdfKey.addActionListener(this);
		exportMenu.add(exportPdfKey);

		exportPdf2Key = new JMenuItem("PDF (low quality, no GhostScript required) ...");
		// HSE - add menu modifier 'Ctrl+Shift-P'
		exportPdf2Key.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
				InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK,
				false));
		exportPdf2Key.addActionListener(this);
		exportMenu.add(exportPdf2Key);

		exportThumbNail = new JMenuItem("PDF Thumbnail (high quality, requires GhostScript) ...");
		// HSE - add menu modifier 'Ctrl-T'
		exportThumbNail.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx(),
				false));
		exportThumbNail.addActionListener(this);
		exportMenu.add(exportThumbNail);
		exportMenu.addSeparator();

		exportEpsKey = new JMenuItem("EPS (high quality) ...");
		// HSE - add menu modifier 'Ctrl-E'
		exportEpsKey.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx(),
				false));
		exportEpsKey.addActionListener(this);
		exportMenu.add(exportEpsKey);
		exportMenu.addSeparator();

		exportPngKey = new JMenuItem("PNG (low quality) ...");
		// HSE - add menu modifier 'Ctrl+G'
		exportPngKey.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx(),
				false));
		exportPngKey.addActionListener(this);
		exportMenu.add(exportPngKey);

		/* Prefs Menu */
		ghostscriptKey = new JMenuItem("GhostScript path...",
				new ImageIcon(ResourceHelper.getResourceUrl("Icons/prefs.png").getFile()));
		ghostscriptKey.addActionListener(this);
		prefsMenu.add(ghostscriptKey);

		dirKey = new JMenuItem("Default directory...");
		dirKey.addActionListener(this);
		prefsMenu.add(dirKey);

		/* File Menu */
		file.add(newMenu);

		openKey = new JMenuItem("Open Quest...",
				new ImageIcon(ResourceHelper.getResourceUrl("Icons/open.png").getFile()));
		// HSE - add menu modifier 'Ctrl+O'
		openKey.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx(),
				false));
		openKey.addActionListener(this);
		file.add(openKey);
		file.addSeparator();

		saveKey = new JMenuItem("Save Quest",
				new ImageIcon(ResourceHelper.getResourceUrl("Icons/save.png").getFile()));
		// HSE - add menu modifier 'Ctrl-S'
		saveKey.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx(),
				false));
		saveKey.addActionListener(this);
		file.add(saveKey);
		saveAsKey = new JMenuItem("Save Quest as...");
		saveAsKey.addActionListener(this);
		file.add(saveAsKey);
		file.addSeparator();

		file.add(exportMenu);
		file.addSeparator();

		propertiesKey = new JMenuItem("Properties...");
		propertiesKey.addActionListener(this);
		file.add(propertiesKey);

		file.add(prefsMenu);
		file.addSeparator();

		quitKey = new JMenuItem("Quit");
		quitKey.addActionListener(this);
		file.add(quitKey);

		menu.add(file);

		/* Region menu */
		europeItem = new JRadioButtonMenuItem("Europe layout");
		europeItem.addItemListener(this);
		regionGroup.add(europeItem);
		region.add(europeItem);

		usaItem = new JRadioButtonMenuItem("USA layout");
		usaItem.addItemListener(this);
		regionGroup.add(usaItem);
		region.add(usaItem);

		menu.add(region);

		/* Help menu */
		listKey = new JMenuItem("Objects...");
		listKey.addActionListener(this);
		help.add(listKey);

		readMeKey = new JMenuItem("Read Me...");
		readMeKey.addActionListener(this);
		help.add(readMeKey);

		help.addSeparator();

		aboutKey = new JMenuItem("About");
		aboutKey.addActionListener(this);
		help.add(aboutKey);

		menu.add(help);

		menu.setBorderPainted(false);

		setJMenuBar(menu);

		content = getContentPane();

		content.setLayout(new BorderLayout());

		tools.setPreferredSize(tools.getMinimumSize());

		content.add(new JScrollPane(board));
		content.add(tools, BorderLayout.WEST);

		bottom.setLayout(new BorderLayout());

		hint = new JLabel();
		hint.setHorizontalAlignment(SwingConstants.LEFT);
		bottom.add(hint, BorderLayout.WEST);

		status = new JLabel();
		status.setHorizontalAlignment(SwingConstants.RIGHT);
		bottom.add(status, BorderLayout.EAST);

		bottom.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

		/* MacOSX: bar on top; other os: bar on bottom */
		if (OS.isMacOsX()) {
			content.add(bottom, BorderLayout.NORTH);
		} else {
			content.add(bottom, BorderLayout.SOUTH);
		}

		// HSE - Updated default size
		setSize(1200, 800);
	}

	private void setMenuRegion() {
		if (quest.getRegion().equals("Europe")) {
			europeItem.setSelected(true);
		} else if (quest.getRegion().equals("USA")) {
			usaItem.setSelected(true);
		}
	}

	public List getObjects() {
		return objects;
	}

	public Quest getQuest() {
		return quest;
	}

	public void itemStateChanged(ItemEvent e) {
		JRadioButtonMenuItem source = (JRadioButtonMenuItem) e.getSource();

		if (e.getStateChange() == ItemEvent.SELECTED) {
			if (source == europeItem) {
				quest.setRegion("Europe");
			} else if (source == usaItem) {
				quest.setRegion("USA");
			}

			updateTitle();
			board.repaint();
		}
	}

	public void updateHint() {
		if ("add".equals(tools.getCommand())) {
			if (tools.selectorPanel.getSelectedObject() == null) {
				hint.setText("Select an object.");
			} else {
				hint.setText("Click on a square to add. Right Click or CTRL Click to turn.");
			}
		} else if ("select".equals(tools.getCommand())) {
			hint.setText("Click on a square to select it.");
		} else if ("darken".equals(tools.getCommand())) {
			hint.setText("Click to darken a square or to add a bridge. Right Click or CTRL Click to clear.");

		} else if (tools.getCommand() == null) {
			hint.setText("Select a command.");
		} else {
			hint.setText("!! COMMAND WITHOUT HINTS !!");
		}
	}

	public void actionPerformed(ActionEvent e) {
		JMenuItem source = (JMenuItem) e.getSource();

		if (source == newKey) {
			if (!quest.isModified()
					|| JOptionPane.showConfirmDialog(this,
					"The current quest has not been saved.\nDo you really want to create a new one?",
					"New Quest",
					JOptionPane.WARNING_MESSAGE,
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

				final Quest newQuest = new Quest(1, 1, objects.getBoard(), null);

				tools.none.doClick();
				tools.clearQuestForm();
				quest = newQuest;
				// HSE - assign the quest in the tools class to the new quest instance
				tools.refreshQuestData(quest);
				setMenuRegion();

				updateHint();
				updateTitle();

				boardPainter.init();

				board.setSize();
				board.repaint();
			}
		} else if (newSpecialKeys.contains(source)) {
			SpecialQuestMenuItem menuItem = (SpecialQuestMenuItem) source;

			if (!quest.isModified()
					|| JOptionPane.showConfirmDialog(this,
					"The current quest has not been saved.\nDo you really want to create a new one?",
					"New Quest",
					JOptionPane.WARNING_MESSAGE,
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

				final Quest newQuest = new Quest(menuItem.getQuestWidth(),
						menuItem.getQuestHeight(),
						objects.getBoard(),
						null);

				tools.none.doClick();
				tools.clearQuestForm();
				quest = newQuest;
				// HSE - assign the quest in the tools class to the new quest instance
				tools.refreshQuestData(quest);
				setMenuRegion();

				updateHint();
				updateTitle();

				boardPainter.init();

				board.setSize();
				board.repaint();
			}

		} else if (source == openKey) {
			if (!quest.isModified()
					|| JOptionPane.showConfirmDialog(this,
					"The current quest has not been saved.\nDo you really want to open a new one?",
					"Open Quest",
					JOptionPane.WARNING_MESSAGE,
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

				fileChooser.resetChoosableFileFilters();

				if (fileChooser.getSelectedFile() != null) {
					final String path = fileChooser.getSelectedFile().getAbsolutePath()
							.replaceFirst("[.][^.]*$", ".xml");
					fileChooser.setSelectedFile(new File(path));
				}

				fileChooser.setFileFilter(filters.get("xml"));

				if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
					try {
						final Quest newQuest = new Read(fileChooser.getSelectedFile(), objects).getQuest();

						tools.none.doClick();
						quest = newQuest;
						setMenuRegion();

						updateHint();
						updateTitle();

						tools.refreshQuestData(quest);
						quest.setModified(false);

						boardPainter.init();

						board.setSize();
						board.repaint();
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(this,
								"Can't open file.",
								"Error",
								JOptionPane.ERROR_MESSAGE);
						log.error("Can't open file.", ex);
					}
				}
			}
		} else if (source == saveKey) {
			File file = null;
			if (quest.getFile() != null || (file = askPath("xml")) != null) {
				try {
					if (file != null) {
						quest.setFile(file);
					}

					quest.save();
					updateTitle();
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(this,
							"Can't save file.",
							"Error",
							JOptionPane.ERROR_MESSAGE);
					log.error("Can't save file.", ex);
				}
			}
		} else if (source == saveAsKey) {
			File file;

			if ((file = askPath("xml")) != null) {
				try {
					quest.setFile(file);
					quest.save();
					updateTitle();
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(this,
							"Can't save file.",
							"Error",
							JOptionPane.ERROR_MESSAGE);
					log.error("Can't save file.", ex);
				}
			}
		} else if (source == exportPdfKey) {
			File file;
			if ((file = askPath("pdf")) != null) {
				try {
					ExportPDF.write(prefs.ghostscriptExec,
							file,
							quest,
							objects,
							true);
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(this,
							"Can't save file. Check your ghostscript path.  Detailed Error: " + ex.getMessage(),
							"Error",
							JOptionPane.ERROR_MESSAGE);
					log.error("Can't save file. Check your ghostscript path", ex);
				}
			}
		} else if (source == exportPdf2Key) {
			// HSE - export to PDF without using Ghostscript or EPS
			File file;
			if ((file = askPath("pdf")) != null) {
				try {
					ExportIPDF.write(file, boardPainter);
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(this,
							"Can't save file. Detailed Error: " + ex.getMessage(),
							"Error",
							JOptionPane.ERROR_MESSAGE);
					log.error("Can't save file.", ex);
				}
			}
		} else if (source == exportThumbNail) {
			// HSE - export to PDF all boards on one letter sized sheet
			File file;
			if ((file = askPath("pdf")) != null) {
				try {
					ExportPDF.write(prefs.ghostscriptExec,
							file,
							quest,
							objects,
							false);
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(this,
							"Can't save file. Detailed Error: " + ex.getMessage(),
							"Error",
							JOptionPane.ERROR_MESSAGE);
					log.error("Can't save file.", ex);
				}
			}
		} else if (source == exportEpsKey) {
			File file;
			if ((file = askPath("eps")) != null) {
				try {
					ExportEPS.writeMultiPage(file, quest, objects);
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(this,
							"Can't save file.  Detailed Error: " + ex.getMessage(),
							"Error",
							JOptionPane.ERROR_MESSAGE);
					log.error("Can't save file.", ex);
				}
			}
		} else if (source == exportPngKey) {
			File file;
			if ((file = askPath("png")) != null) {
				try {
					ExportRaster.write(file, "png", boardPainter);
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(this,
							"Can't save file.  Detailed Error: " + ex.getMessage(),
							"Error",
							JOptionPane.ERROR_MESSAGE);
					log.error("Can't save file.", ex);
				}
			}
		} else if (source == ghostscriptKey) {
			ghostscriptChooser.setSelectedFile(prefs.ghostscriptExec);

			if (ghostscriptChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				prefs.ghostscriptExec = ghostscriptChooser.getSelectedFile();

				try {
					prefs.write();
				} catch (Exception ex) {
					log.error("Error.", ex);
				}
			}
		} else if (source == dirKey) {
			// HSE - get default directory
			final JFileChooser chooser = new JFileChooser();
			chooser.setCurrentDirectory(new File("."));
			chooser.setDialogTitle("Default Directory");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			// disable the "All files" option.
			chooser.setAcceptAllFileFilterUsed(false);

			if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				prefs.defaultDir = chooser.getSelectedFile();
				try {
					prefs.write();
				} catch (Exception ex) {
					log.error("Error.", ex);
				}
			}
		} else if (source == quitKey) {
			windowClosing(null);
		} else if (source == listKey) {
			String object = tools.selectorPanel.getSelectedObject();

			if ("add".equals(tools.getCommand()) && object != null) {
				OS.openURL(new File("Objects.html"), "object_" + object);
			} else {
				OS.openURL(new File("Objects.html"), null);
			}

		} else if (source == readMeKey) {
			OS.openURL(new File("Readme.html"), null);

		} else if (source == propertiesKey) {
			final PropertiesModal modal = new PropertiesModal(this, quest);
			modal.showDialog();

		} else if (source == aboutKey) {
			JOptionPane.showMessageDialog(this,
					Constants.applicationName + " " + Constants.VERSION + Constants.applicationVersionSuffix + "\n"
							+ "HeroScribe Enhanced modifications (C) 2011 Jason Allen.\n"
							+ "HeroScribe original program is (C) 2003-2004 Flavio Chierichetti and Valerio Chierichetti.\n"
							+ Constants.applicationName + " is free software, distributed under the terms of the GNU GPL 2.\n"
							+ "HeroQuest and its icons are (C) of Milton Bradley Co.\n",
					"About", JOptionPane.PLAIN_MESSAGE);
		}
	}

	private File askPath(String extension) {
		fileChooser.resetChoosableFileFilters();

		if (fileChooser.getSelectedFile() != null) {
			final String path = fileChooser.getSelectedFile().getAbsolutePath()
					.replaceFirst("[.][^.]*$", "." + extension);
			fileChooser.setSelectedFile(new File(path));
		}

		fileChooser.setFileFilter(filters.get(extension));

		if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			File saveFile = fileChooser.getSelectedFile();

			if (!saveFile.getName().toLowerCase().endsWith("." + extension)) {
				saveFile = new File(saveFile + "." + extension);
				fileChooser.setSelectedFile(saveFile);
			}

			return saveFile;
		} else {
			return null;
		}
	}

	public void windowClosing(WindowEvent e) {
		if (!quest.isModified() || JOptionPane.showConfirmDialog(this,
				"The current quest has not been saved.\nDo you really want to quit?",
				"Quit",
				JOptionPane.WARNING_MESSAGE,
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

			try {
				prefs.write();
			} catch (Exception ex) {
				log.error("Error.", ex);
			}

			System.exit(0);

		}
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}
}

class GhostScriptFileFilter extends FileFilter {
	public GhostScriptFileFilter() {
		super();
	}

	public boolean accept(File f) {
		if (f.isDirectory())
			return true;

		if (OS.isWindows() && (f.getName().equalsIgnoreCase("gswin32c.exe") || f.getName().equalsIgnoreCase("gswin64c.exe")))
			return true;

		if (!OS.isWindows() && f.getName().equalsIgnoreCase("gs"))
			return true;

		return false;
	}

	public String getDescription() {
		if (OS.isWindows())
			return "Ghostscript Shell (gswin32c.exe, gswin64c.exe)";
		else
			return "Ghostscript Shell (gs)";
	}
}

class ActualFileFilter extends FileFilter {
	String extension, description;

	public ActualFileFilter(String extension, String description) {
		super();
		this.extension = extension;
		this.description = description;
	}

	public boolean accept(File f) {
		return f.isDirectory() || f.getName().toLowerCase().endsWith("." + extension);
	}

	public String getDescription() {
		return description;
	}
}

class SpecialQuestMenuItem extends JMenuItem {
	private static final long serialVersionUID = 1L;
	private final int questWidth, questHeight;

	public SpecialQuestMenuItem(int questWidth, int questHeight) {
		super("Quest " + questWidth + "x" + questHeight);

		this.questWidth = questWidth;
		this.questHeight = questHeight;
	}

	public int getQuestWidth() {
		return questWidth;
	}

	public int getQuestHeight() {
		return questHeight;
	}
}