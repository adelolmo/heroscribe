/*
  Copyright (C) 2002-2004 Flavio Chierichetti and Valerio Chierichetti

  HeroScribe Enhanced (changes are prefixed with HSE in comments)
  Copyright (C) 2011 Jason Allen

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

import org.lightless.heroscribe.Constants;
import org.lightless.heroscribe.Preferences;
import org.lightless.heroscribe.export.ExportEPS;
import org.lightless.heroscribe.export.ExportPDF;
import org.lightless.heroscribe.export.ExportRaster;
import org.lightless.heroscribe.helper.BoardPainter;
import org.lightless.heroscribe.iconpack.IconPackDownloadModal;
import org.lightless.heroscribe.iconpack.IconPackImportFileChooser;
import org.lightless.heroscribe.iconpack.IconPackRemoveModal;
import org.lightless.heroscribe.iconpack.IconPackService;
import org.lightless.heroscribe.utils.OS;
import org.lightless.heroscribe.xml.Kind;
import org.lightless.heroscribe.xml.ObjectList;
import org.lightless.heroscribe.xml.Quest;
import org.lightless.heroscribe.xml.QuestParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static org.lightless.heroscribe.export.ExportRaster.ImageFormat.PNG;

public class Gui extends JFrame implements WindowListener, ItemListener, ActionListener {

	private static final Logger log = LoggerFactory.getLogger(Gui.class);
	private static final Dimension FILE_CHOOSER_DIMENSION = new Dimension(900, 700);
	private static final Dimension DIRECTORY_CHOOSER_DIMENSION = new Dimension(600, 700);

	private final JFileChooser fileChooser = new FileChooser();
	private final JFileChooser ghostscriptChooser = new JFileChooser();
	private final TreeMap<String, FileFilter> filters = new TreeMap<>();

	private final ImageLoader imageLoader;
	private final ObjectList objectList;
	private final QuestParser questParser;
	private final IconPackImportFileChooser iconPackImportFileChooser;
	private final IconPackDownloadModal iconPackDownloadModal;
	private final IconPackRemoveModal iconPackRemoveModal;
	private final Path objectHtmlPath;
	private final Preferences prefs;
	private final Vector<JMenuItem> newSpecialKeys;

	private JRadioButtonMenuItem europeItem, usaItem;
	private JMenuItem newKey;
	private Quest quest;

	ToolsPanel tools;
	Board board;
	JLabel hint, status;
	BoardPainter boardPainter;

	public Gui(ImageLoader imageLoader,
			   IconPackService iconPackService,
			   Preferences preferences,
			   ObjectList objectList,
			   QuestParser questParser,
			   Quest quest,
			   Path objectHtmlPath) {
		super();
		this.imageLoader = imageLoader;
		this.objectList = objectList;
		this.questParser = questParser;
		this.prefs = preferences;
		this.quest = quest;
		this.objectHtmlPath = objectHtmlPath;

		// HSE - set app icon
		setIconImage(imageLoader.addImageAndFlush("HeroScribe.png"));

		ghostscriptChooser.setFileFilter(new GhostScriptFileFilter());
		ghostscriptChooser.setPreferredSize(FILE_CHOOSER_DIMENSION);

		fileChooser.setPreferredSize(FILE_CHOOSER_DIMENSION);
		fileChooser.setCurrentDirectory(prefs.defaultDir);
		filters.put("pdf", new ActualFileFilter("pdf", "PDF files (*.pdf)"));
		filters.put("eps", new ActualFileFilter("eps", "EPS files (*.eps)"));
		filters.put("png", new ActualFileFilter("png", "PNG files (*.png)"));
		filters.put("xml", new ActualFileFilter("xml", "HeroScribe Quests (*.xml)"));

		boardPainter = new BoardPainter(this);
		objectList.addModificationListener(modificationType -> {
			if (ObjectList.Type.OBJECTS.equals(modificationType)) {
				boardPainter.init(objectList);
			}
		});

		tools = new ToolsPanel(this, this.quest);
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

		iconPackImportFileChooser = new IconPackImportFileChooser(prefs.defaultDir, iconPackService);
		iconPackDownloadModal = new IconPackDownloadModal(iconPackService);
		iconPackRemoveModal = new IconPackRemoveModal(iconPackService);

		setVisible(true);
	}

	public void updateTitle() {
		final StringBuilder sb =
				new StringBuilder(Constants.APPLICATION_NAME + " " + Constants.VERSION + Constants.applicationVersionSuffix + " - ");
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
		final JMenu iconPacks = new JMenu("Icon Packs");
		final JMenu region = new JMenu("Region");
		final JMenu help = new JMenu("Help");

		final JMenu newMenu = new JMenu("New");
		final JMenu exportMenu = new JMenu("Export");
		final JMenu prefsMenu = new JMenu("Preferences");

		final JPanel bottom = new JPanel();

		final ButtonGroup regionGroup = new ButtonGroup();

		/* New Menu */
		newKey = new JMenuItem("Quest",
				new ImageIcon(imageLoader.addImageAndFlush("Icons/new.png")));
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

		final JMenuItem exportPdfKey = new JMenuItem("PDF…",
				new ImageIcon(imageLoader.addImageAndFlush("Icons/export.png")));
		// HSE - add menu modifier 'Ctrl-P'
		exportPdfKey.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx(),
				false));
		exportPdfKey.addActionListener(exportPdfKeyActionListener());
		exportMenu.add(exportPdfKey);

		final JMenuItem exportThumbNail = new JMenuItem("PDF Thumbnail…");
		// HSE - add menu modifier 'Ctrl-T'
		exportThumbNail.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx(),
				false));
		exportThumbNail.addActionListener(exportThumbnailActionListener());
		exportMenu.add(exportThumbNail);
		exportMenu.addSeparator();

		final JMenuItem exportEpsKey = new JMenuItem("EPS…");
		// HSE - add menu modifier 'Ctrl-E'
		exportEpsKey.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx(),
				false));
		exportEpsKey.addActionListener(exportEpsKeyActionListener());
		exportMenu.add(exportEpsKey);
		exportMenu.addSeparator();

		final JMenuItem exportPngKey = new JMenuItem("PNG…");
		// HSE - add menu modifier 'Ctrl+G'
		exportPngKey.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx(),
				false));
		exportPngKey.addActionListener(exportPngKeyActionListener());
		exportMenu.add(exportPngKey);

		/* Prefs Menu */
		final JMenuItem ghostscriptKey = new JMenuItem("GhostScript path...",
				new ImageIcon(imageLoader.addImageAndFlush("Icons/prefs.png")));
		ghostscriptKey.addActionListener(ghostscriptKeyActionListener());
		prefsMenu.add(ghostscriptKey);

		final JMenuItem dirKey = new JMenuItem("Default directory...");
		dirKey.addActionListener(dirKeyActionListener());
		prefsMenu.add(dirKey);

		final JMenuItem paperKey = new JMenuItem("Paper size...");
		paperKey.addActionListener(paperKeyActionListener());
		prefsMenu.add(paperKey);

		/* File Menu */
		file.add(newMenu);

		final JMenuItem openKey = new JMenuItem("Open Quest...",
				new ImageIcon(imageLoader.addImageAndFlush("Icons/open.png")));
		// HSE - add menu modifier 'Ctrl+O'
		openKey.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx(),
				false));
		openKey.addActionListener(openKeyActionListener());
		file.add(openKey);
		file.addSeparator();

		final JMenuItem saveKey = new JMenuItem("Save Quest",
				new ImageIcon(imageLoader.addImageAndFlush("Icons/save.png")));
		// HSE - add menu modifier 'Ctrl-S'
		saveKey.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx(),
				false));
		saveKey.addActionListener(saveKeyActionListener());
		file.add(saveKey);
		final JMenuItem saveAsKey = new JMenuItem("Save Quest as...");
		saveAsKey.addActionListener(saveAsKeyActionListener());
		file.add(saveAsKey);
		file.addSeparator();

		file.add(exportMenu);
		file.addSeparator();

		final JMenuItem propertiesKey = new JMenuItem("Properties...");
		propertiesKey.addActionListener(propertiesKeyActionListener());
		file.add(propertiesKey);

		file.add(prefsMenu);
		file.addSeparator();

		JMenuItem quitKey = new JMenuItem("Quit");
		quitKey.addActionListener(quitKeyActionListener());
		file.add(quitKey);

		menu.add(file);

		/* Icon Packs menu */
		final JMenuItem iconPackDownload = new JMenuItem("Download...");
		iconPackDownload.addActionListener(iconPackDownloadActionListener());
		iconPacks.add(iconPackDownload);

		final JMenuItem iconPackImport = new JMenuItem("Import...");
		iconPackImport.addActionListener(iconPackImportActionListener());
		iconPacks.add(iconPackImport);

		final JMenuItem iconPackRemove = new JMenuItem("Remove...");
		iconPackRemove.addActionListener(iconPackRemoveActionListener());
		iconPacks.add(iconPackRemove);

		menu.add(iconPacks);

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
		final JMenuItem listKey = new JMenuItem("Objects...");
		listKey.addActionListener(listKeyActionListener());
		help.add(listKey);

		help.addSeparator();

		final JMenuItem aboutKey = new JMenuItem("About");
		aboutKey.addActionListener(aboutKeyActionListener());
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

	public ObjectList getObjectList() {
		return objectList;
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

		if (newKey == source) {
			if (!quest.isModified()
					|| JOptionPane.showConfirmDialog(this,
					"The current quest has not been saved.\nDo you really want to create a new one?",
					"New Quest",
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {

				final Quest xmlNewQuest = new Quest(objectList.getBoard());

				tools.none.doClick();
				tools.clearQuestForm();
				this.quest = xmlNewQuest;
				// HSE - assign the quest in the tools class to the new quest instance
				tools.refreshQuestData(quest);
				setMenuRegion();

				updateHint();
				updateTitle();

				boardPainter.init(objectList);

				board.setSize();
				board.repaint();
			}
		} else if (newSpecialKeys.contains(source)) {
			SpecialQuestMenuItem menuItem = (SpecialQuestMenuItem) source;

			if (!quest.isModified()
					|| JOptionPane.showConfirmDialog(this,
					"The current quest has not been saved.\nDo you really want to create a new one?",
					"New Quest",
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {

				final Quest newXmlQuest = new Quest(menuItem.getQuestWidth(),
						menuItem.getQuestHeight(),
						objectList.getBoard().getWidth(),
						objectList.getBoard().getHeight());

				tools.none.doClick();
				tools.clearQuestForm();
				quest = newXmlQuest;
				// HSE - assign the quest in the tools class to the new quest instance
				tools.refreshQuestData(quest);
				setMenuRegion();

				updateHint();
				updateTitle();

				boardPainter.init(objectList);

				board.setSize();
				board.repaint();
			}
		}
	}

	private ActionListener openKeyActionListener() {
		return e -> {
			if (!quest.isModified()
					|| JOptionPane.showConfirmDialog(this,
					"The current quest has not been saved.\nDo you really want to open a new one?",
					"Open Quest",
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {

				fileChooser.resetChoosableFileFilters();

				if (fileChooser.getSelectedFile() != null) {
					final String path = fileChooser.getSelectedFile().getAbsolutePath()
							.replaceFirst("[.][^.]*$", ".xml");
					fileChooser.setSelectedFile(new File(path));
				}

				fileChooser.setFileFilter(filters.get("xml"));

				if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
					try {
						final Quest newXmlQuest =
								questParser.parse(fileChooser.getSelectedFile(),
										objectList.getBoard().getWidth(),
										objectList.getBoard().getHeight());

						tools.none.doClick();

						final Set<Kind> unsupportedKinds = findUnsupportedKinds(newXmlQuest, objectList.getKinds());
						if (!unsupportedKinds.isEmpty()) {
							JOptionPane.showMessageDialog(this,
									"The Quest contains objects not supported.\n\n" +
											"Please import the following Icon Pack(s) and try again:\n"
											+ unsupportedKinds.stream()
											.map(Kind::getName)
											.collect(Collectors.joining("\n", "• ", "")),
									"Error",
									JOptionPane.ERROR_MESSAGE);
							return;
						}

						quest = newXmlQuest;
						setMenuRegion();

						updateHint();
						updateTitle();

						tools.refreshQuestData(quest);
						quest.setModified(false);

						boardPainter.init(objectList);

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
		};
	}

	private ActionListener saveKeyActionListener() {
		return e -> askPath("xml")
				.ifPresent(file -> {
					try {
						quest.setFile(file);
						questParser.saveToDisk(objectList, quest, quest.getFile());
						updateTitle();
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(this,
								"Can't save file.",
								"Error",
								JOptionPane.ERROR_MESSAGE);
						log.error("Can't save file.", ex);
					}
				});
	}

	private ActionListener saveAsKeyActionListener() {
		return e -> askPath("xml")
				.ifPresent(file -> {
					try {
						questParser.saveToDisk(objectList, quest, file);
						updateTitle();
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(this,
								"Can't save file.",
								"Error",
								JOptionPane.ERROR_MESSAGE);
						log.error("Can't save file.", ex);
					}
				});
	}

	private ActionListener exportPdfKeyActionListener() {
		return e -> askPath("pdf")
				.ifPresent(file -> {
					try {
						ExportPDF.write(prefs.ghostscriptExec,
								file,
								quest,
								objectList,
								prefs.getPaperSize());
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(this,
								"Can't save file. Check your ghostscript path.  Detailed Error: " + ex.getMessage(),
								"Error",
								JOptionPane.ERROR_MESSAGE);
						log.error("Can't save file. Check your ghostscript path", ex);
					}
				});
	}

	private ActionListener exportThumbnailActionListener() {
		// HSE - export to PDF all boards on one letter sized sheet
		return e -> askPath("pdf")
				.ifPresent(file -> {
					try {
						ExportPDF.writeThumbNail(prefs.ghostscriptExec,
								file,
								quest,
								objectList,
								prefs.getPaperSize());
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(this,
								"Can't save file. Detailed Error: " + ex.getMessage(),
								"Error",
								JOptionPane.ERROR_MESSAGE);
						log.error("Can't save file.", ex);
					}
				});
	}

	private ActionListener exportEpsKeyActionListener() {
		return e -> askPath("eps")
				.ifPresent(file -> {
					try {
						ExportEPS.writeMultiPage(prefs.getPaperSize(),
								file,
								quest,
								objectList
						);
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(this,
								"Can't save file.  Detailed Error: " + ex.getMessage(),
								"Error",
								JOptionPane.ERROR_MESSAGE);
						log.error("Can't save file.", ex);
					}
				});
	}

	private ActionListener exportPngKeyActionListener() {
		return e -> askPath("png")
				.ifPresent(file -> {
					try {
						ExportRaster.write(file, PNG, boardPainter);
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(this,
								"Can't save file.  Detailed Error: " + ex.getMessage(),
								"Error",
								JOptionPane.ERROR_MESSAGE);
						log.error("Can't save file.", ex);
					}
				});
	}

	private ActionListener ghostscriptKeyActionListener() {
		return e -> {
			ghostscriptChooser.setSelectedFile(prefs.ghostscriptExec);

			if (ghostscriptChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				prefs.ghostscriptExec = ghostscriptChooser.getSelectedFile();

				try {
					prefs.write();
				} catch (Exception ex) {
					log.error("Error.", ex);
				}
			}
		};
	}

	private ActionListener dirKeyActionListener() {
		return e -> {
			// HSE - get default directory
			final JFileChooser chooser = new JFileChooser();
			chooser.setPreferredSize(DIRECTORY_CHOOSER_DIMENSION);
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
		};
	}

	private ActionListener paperKeyActionListener() {
		return e -> {
			final PaperSizeModal modal = new PaperSizeModal();
			modal.showDialog(prefs.getPaperSize()).ifPresent(paperType -> {
				prefs.setPaperSize(paperType);
				try {
					prefs.write();
				} catch (Exception ex) {
					log.error("Error.", ex);
				}
			});
		};
	}

	private ActionListener iconPackImportActionListener() {
		return e -> iconPackImportFileChooser.showModal();
	}

	private ActionListener iconPackDownloadActionListener() {
		return e -> iconPackDownloadModal.showDialog();
	}

	private ActionListener iconPackRemoveActionListener() {
		return e -> iconPackRemoveModal.showDialog();
	}

	private ActionListener quitKeyActionListener() {
		return e -> windowClosing(null);
	}

	private ActionListener listKeyActionListener() {
		return e -> {
			final String objectId = tools.selectorPanel.getSelectedObject();

			if ("add".equals(tools.getCommand()) && objectId != null) {
				final String reference = "object_" + objectId;
				new HtmlPanel(this, objectHtmlPath, reference);
			} else {
				new HtmlPanel(this, objectHtmlPath);
			}
		};
	}

	private ActionListener propertiesKeyActionListener() {
		return e -> {
			final PropertiesModal modal = new PropertiesModal(this, quest);
			modal.showDialog();
		};
	}

	private ActionListener aboutKeyActionListener() {
		return e -> JOptionPane.showMessageDialog(this,
				Constants.APPLICATION_NAME + " " + Constants.VERSION + "\n"
						+ Constants.APPLICATION_NAME + Constants.applicationVersionSuffix + " modifications (C) 2023 Andoni del Olmo.\n"
						+ "HeroScribe Enhanced modifications (C) 2011 Jason Allen.\n"
						+ "HeroScribe original program is (C) 2003-2004 Flavio Chierichetti and Valerio Chierichetti.\n"
						+ Constants.APPLICATION_NAME + " is free software, distributed under the terms of the GNU GPL 2.\n"
						+ "HeroQuest and its icons are (C) of Milton Bradley Co.\n",
				"About", JOptionPane.PLAIN_MESSAGE);
	}

	private Set<Kind> findUnsupportedKinds(Quest newXmlQuest, List<Kind> kinds) {
		final List<String> supportedKindIds = kinds.stream()
				.map(Kind::getId)
				.collect(Collectors.toList());
		return newXmlQuest.getKinds().stream()
				.filter(kind -> !supportedKindIds.contains(kind.getId()))
				.collect(Collectors.toSet());
	}

	private Optional<File> askPath(String extension) {
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

			return Optional.of(saveFile);
		} else {
			return Optional.empty();
		}
	}

	public void windowClosing(WindowEvent e) {
		if (!quest.isModified() || JOptionPane.showConfirmDialog(this,
				"The current quest has not been saved.\nDo you really want to quit?",
				"Quit",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {

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