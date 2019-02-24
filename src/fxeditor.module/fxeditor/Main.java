package fxeditor;

import javafx.application.*;
import javafx.event.Event;
import javafx.geometry.*;
import javafx.print.PrinterJob;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.*;

import java.io.*;
import java.text.DateFormat;
import java.util.*;

public class Main extends Application {

	private Stage stage;
	private Scene scene;

	private ImageView imageView;
	private Image icon;

	private HBox statusBar;
	private BorderPane root;
	private TextArea editor;
	private Label status;

	private MenuBar menuBar;
	private Menu file, edit, format, view, help;
	private MenuItem newFile, openFile, saveFile, saveAs, separator1, print, separator2, exit;
	private MenuItem undo, separator3, cut, copy, paste, delete, separator4, find, findNext, replace, goTo, separator5, selectAll, timeDate;
	private MenuItem font, getHelp, separator6, about;
	private CheckMenuItem autoWrap, toggleStatusBar;

	private File textFile;
	private Clipboard clipboard;
	private FindDialog findDialog;
	private FindReplaceDialog findReplaceDialog;
	private FontChooser fontChooser;
	private String lastOpenDir = "";
	private String lastSavedText = "";

	@Override
	public void init() {
		Locale.setDefault(Locale.TAIWAN);
		Platform.setImplicitExit(false);

		// Initialize the menus
		file = new Menu("檔案(_F)");
		edit = new Menu("編輯(_E)");
		edit.setOnShown(e -> {
			paste.setDisable(!clipboard.hasString());
			findNext.setDisable(findDialog.getLastFindedString().isEmpty());
		});
		format = new Menu("格式(_O)");
		view = new Menu("檢視(_V)");
		help = new Menu("說明(_H)");

		// Initialize the menu bar
		menuBar = new MenuBar();
		menuBar.getMenus().addAll(file, edit, format, view, help);

		// Initialize the menu items
		// File menu
		newFile = new MenuItem("開新檔案(_N)");
		newFile.setAccelerator(KeyCombination.valueOf("Shortcut+N"));
		newFile.setOnAction(e -> newFile());

		openFile = new MenuItem("開啟舊檔(_O)...");
		openFile.setAccelerator(KeyCombination.valueOf("Shortcut+O"));
		openFile.setOnAction(e -> loadFile());

		saveFile = new MenuItem("儲存檔案(_S)");
		saveFile.setAccelerator(KeyCombination.valueOf("Shortcut+S"));
		saveFile.setOnAction(e -> saveFile());

		saveAs = new MenuItem("另存新檔(_A)");
		saveAs.setAccelerator(KeyCombination.valueOf("Shortcut+Shift+S"));
		saveAs.setOnAction(e -> saveAsNewFile());

		separator1 = new SeparatorMenuItem();

		print = new MenuItem("列印(_P)...");
		print.setAccelerator(KeyCombination.valueOf("Shortcut+P"));
		print.setOnAction(e -> {
			PrinterJob job = PrinterJob.createPrinterJob();
			if (job != null) {
				job.showPrintDialog(stage);
			} else {
				Alert alert = createPrinterErrorDialog();
				alert.show();
			}
		});

		separator2 = new SeparatorMenuItem();

		exit = new MenuItem("結束(_X)");
		exit.setOnAction(e -> Platform.exit());

		file.getItems().addAll(newFile, openFile, saveFile, saveAs, separator1, print, separator2, exit);

		// Edit menu
		undo = new MenuItem("復原(_U)");
		undo.setAccelerator(KeyCombination.valueOf("Shortcut+Z"));
		undo.setDisable(true);
		undo.setOnAction(e -> editor.undo());

		separator3 = new SeparatorMenuItem();

		cut = new MenuItem("剪下(_T)");
		cut.setAccelerator(KeyCombination.valueOf("Shortcut+X"));
		cut.setDisable(true);
		cut.setOnAction(e -> editor.cut());

		copy = new MenuItem("複製(_C)");
		copy.setAccelerator(KeyCombination.valueOf("Shortcut+C"));
		copy.setDisable(true);
		copy.setOnAction(e -> editor.copy());

		paste = new MenuItem("貼上(_P)");
		paste.setAccelerator(KeyCombination.valueOf("Shortcut+V"));
		paste.setOnAction(e -> editor.paste());

		delete = new MenuItem("刪除(_L)");
		delete.setAccelerator(KeyCombination.valueOf("Del"));
		delete.setDisable(true);
		delete.setOnAction(e -> editor.deleteText(editor.getSelection()));

		separator4 = new SeparatorMenuItem();

		find = new MenuItem("尋找(_F)...");
		find.setAccelerator(KeyCombination.valueOf("Shortcut+F"));
		find.setOnAction(e -> findDialog.show());

		findNext = new MenuItem("找下一個(_N)");
		findNext.setAccelerator(KeyCombination.valueOf("F3"));
		findNext.setOnAction(e -> {
			if (!findDialog.getLastFindedString().isEmpty()) {
				findDialog.findNext(Direction.DOWN, false);
			}
		});

		replace = new MenuItem("取代(_R)...");
		replace.setAccelerator(KeyCombination.valueOf("Shortcut+H"));
		replace.setOnAction(e -> findReplaceDialog.show());

		goTo = new MenuItem("移至(_G)...");
		goTo.setAccelerator(KeyCombination.valueOf("Shortcut+G"));
		goTo.setOnAction(e -> {
			String[] totalLines = getTotalLines();
			TextInputDialog goToLineDialog = createGoToLineDialog();
			Optional<String> result = goToLineDialog.showAndWait();
			if (result.isPresent()) {
				try {
					int lineNumber = Integer.parseInt(result.get());
					if (lineNumber >= 1 && lineNumber <= totalLines.length) {
						// Calculate the length from beginning to the target line
						int position = 0;
						for (int i = 0; i < lineNumber - 1; i++) {
							position += totalLines[i].length();
						}

						// Move the caret to the target line
						editor.positionCaret(position);
					}
				} catch(NumberFormatException ex){
					throw new NumberFormatException("Invalid input");
				}
			}
		});


		separator5 = new SeparatorMenuItem();

		selectAll = new MenuItem("全選(_A)");
		selectAll.setAccelerator(KeyCombination.valueOf("Shortcut+A"));
		selectAll.setOnAction(e -> editor.selectAll());

		timeDate = new MenuItem("時間/日期(_D)");
		timeDate.setAccelerator(KeyCombination.valueOf("F5"));
		timeDate.setOnAction(e -> {
			DateFormat dateFormat = DateFormat.getDateTimeInstance();
			editor.insertText(editor.getCaretPosition(), dateFormat.format(new Date()));
		});

		edit.getItems().addAll(undo, separator3, cut, copy, paste, delete, separator4, find, findNext, replace, goTo, separator5, selectAll, timeDate);

		// Format menu
		autoWrap = new CheckMenuItem("自動換行(_W)");
		autoWrap.setOnAction(e -> {
			if (autoWrap.isSelected()) {
				goTo.setDisable(true);

				toggleStatusBar.setDisable(true);
				toggleStatusBar.setSelected(false);

				statusBar.setVisible(false);
				statusBar.setManaged(false);

				editor.setWrapText(true);
			} else {
				goTo.setDisable(false);
				toggleStatusBar.setDisable(false);
				editor.setWrapText(false);
			}
		});

		font = new MenuItem("字型(_F)...");
		font.setOnAction(e -> {
			Font newFont = fontChooser.showAndWait(editor.getFont());
			editor.setFont(newFont);
		});

		format.getItems().addAll(autoWrap, font);

		// View menu
		toggleStatusBar = new CheckMenuItem("狀態列(_S)");
		toggleStatusBar.setSelected(true);
		toggleStatusBar.setOnAction(e -> {
			if (toggleStatusBar.isSelected()) {
				statusBar.setManaged(true);
				statusBar.setVisible(true);
			} else {
				statusBar.setManaged(false);
				statusBar.setVisible(false);
			}
		});
		view.getItems().add(toggleStatusBar);

		// Help menu
		getHelp = new MenuItem("檢視說明(_H)");
		getHelp.setOnAction(e -> {
			getHostServices().showDocument("https://www.google.com.tw/search?source=hp&ei=MzpsXKmKFYKX8gXll7DYCA&q=%E4%B8%8D%E6%9C%83%E7%94%A8%E8%A8%98%E4%BA%8B%E6%9C%AC%E8%A9%B2%E5%90%83%E4%BB%80%E9%BA%BC%E8%97%A5");
		});

		separator6 = new SeparatorMenuItem();

		about = new MenuItem("關於FXEditor(_A)");
		about.setOnAction(e -> {
			Alert aboutDialog = createAboutDialog();
			aboutDialog.show();
		});

		help.getItems().addAll(getHelp, separator6, about);

		// Initialize the text area
		editor = new TextArea();
		editor.undoableProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue) {
				undo.setDisable(false);
			} else {
				undo.setDisable(true);
			}
		});
		editor.caretPositionProperty().addListener((obs, oldValue, newValue) -> {
			String[] totalLines = getTotalLines();
			int row = 0;
			int col = 0;
			int length = 0;

			for (int i = 0; i < totalLines.length; i++) {
				length += totalLines[i].length();
				row = i + 1;

				if (i == totalLines.length - 1) {
					length -= totalLines[i].length();
				}

				if (length > newValue.intValue()) {
					length -= totalLines[i].length();
					break;
				}
			}
			col = newValue.intValue() - length + 1;
			status.setText("第" + row + "列，第" + col + "行");
		});
		editor.selectedTextProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue.isEmpty()) {
				copy.setDisable(true);
				cut.setDisable(true);
				delete.setDisable(true);
			} else {
				copy.setDisable(false);
				cut.setDisable(false);
				delete.setDisable(false);
			}
		});
		editor.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
			if (e.getCode() == KeyCode.H && e.isShortcutDown()) {
				replace.fire();
				e.consume();
			}
		});

		// Initialize the label
		status = new Label("第" + 1 + "列，第" + 1 + "行");

		// Set the status bar
		statusBar = new HBox();
		statusBar.setPadding(new Insets(5));
		statusBar.setAlignment(Pos.CENTER_RIGHT);
		statusBar.getChildren().add(status);

		// Set the root layout
		root = new BorderPane();
		root.setTop(menuBar);
		root.setCenter(editor);
		root.setBottom(statusBar);

		// Add the root into scene
		scene = new Scene(root, 600, 400);
		scene.getStylesheets().add(getClass().getResource("/resources/css/style.css").toExternalForm());

		// Load icon
		icon = new Image(getClass().getResourceAsStream("/resources/icon/notes.png"));
		imageView = new ImageView(icon);
	}

	@Override
	public void start(Stage stage) {
		clipboard = Clipboard.getSystemClipboard();
		findDialog = new FindDialog(stage, editor);
		findReplaceDialog = new FindReplaceDialog(stage, editor);
		fontChooser = new FontChooser(stage);

		this.stage = stage;
		stage.setTitle("FXEditor");
		stage.setScene(scene);
		stage.getIcons().add(icon);
		stage.setOnCloseRequest(e -> {
			if (isFileModified()) {

				Optional<ButtonType> result = showSaveConfirmationDialog();
				switch (result.get().getButtonData()) {
					case YES: 
						saveFile();
					case NO:
						// Do nothing
						break;

					case CANCEL_CLOSE:
					default:
						e.consume();
						return;
				}
			}
			Platform.exit();
		});
		stage.show();
	}

	private FileChooser createFileChooser(String mode) {
		FileChooser fileChooser = new FileChooser();
		lastOpenDir = lastOpenDir.isEmpty() ? System.getProperty("user.home"): lastOpenDir;
		fileChooser.setInitialDirectory(new File(lastOpenDir));

		if (mode.equalsIgnoreCase("open")) {
			fileChooser.setTitle("開啟檔案");
			fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("純文字檔", "*.txt"));
		} else if (mode.equalsIgnoreCase("save")) {
			fileChooser.setTitle("儲存檔案");
			fileChooser.setInitialFileName("未命名.txt");
		} else {
			throw new IllegalArgumentException("Invalid mode: " + mode);
		}

		return fileChooser;
	}

	private TextInputDialog createGoToLineDialog() {
		TextInputDialog textInputDialog = new TextInputDialog("1");
		textInputDialog.initOwner(stage);
		textInputDialog.initStyle(StageStyle.UTILITY);
		textInputDialog.setTitle("跳至行");
		textInputDialog.setGraphic(null);
		textInputDialog.setHeaderText("行號:");

		return textInputDialog;
	}

	private Alert createPrinterErrorDialog() {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.initOwner(stage);
		alert.initStyle(StageStyle.UTILITY);
		alert.getDialogPane().setPrefWidth(300);
		alert.setTitle("印表機錯誤");
		alert.setHeaderText(null);
		alert.setContentText("找不到印表機，請檢查印表機設定");

		return alert;
	}

	private Alert createAboutDialog() {
		Alert aboutDialog = new Alert(Alert.AlertType.NONE);

		Label label1 = new Label("Icons made by");
		Label label2 = new Label("from");
		Label label3 = new Label("is licensed by");

		Hyperlink link1 = new Hyperlink("Prosymbols");
		link1.setOnAction(e -> getHostServices().showDocument("https://www.flaticon.com/authors/prosymbols"));

		Hyperlink link2 = new Hyperlink("Flaticon");
		link2.setOnAction(e -> getHostServices().showDocument("https://www.flaticon.com/"));

		Hyperlink link3 = new Hyperlink("CC 3.0 BY");
		link3.setOnAction(e -> getHostServices().showDocument("http://creativecommons.org/licenses/by/3.0/"));

		FlowPane flowPane = new FlowPane(label1, link1, label2, link2, label3, link3);
		flowPane.setAlignment(Pos.BOTTOM_CENTER);

		DialogPane dialogPane = new DialogPane();
		dialogPane.setMaxHeight(Region.USE_PREF_SIZE);
		dialogPane.setMaxWidth(Region.USE_PREF_SIZE);
		dialogPane.setMinHeight(Region.USE_PREF_SIZE);
		dialogPane.setMinWidth(Region.USE_PREF_SIZE);
		dialogPane.setPadding(new Insets(10));
		dialogPane.setGraphic(imageView);
		dialogPane.setHeaderText("一個用來練習JavaFX的程式作品(在字體設定上有些Bug)");
		dialogPane.getButtonTypes().add(ButtonType.OK);
		dialogPane.setContent(flowPane);

		aboutDialog.initOwner(stage);
		aboutDialog.initStyle(StageStyle.UTILITY);
		aboutDialog.setTitle("關於");
		aboutDialog.setDialogPane(dialogPane);

		return aboutDialog;
	}

	private boolean isFileModified() {
		return !lastSavedText.equals(editor.getText());
	}

	private Optional<ButtonType> showSaveConfirmationDialog() {
		String fileName = (textFile == null) ? "未命名" : textFile.getName();
		ButtonType save = new ButtonType("儲存", ButtonBar.ButtonData.YES);
		ButtonType notSave = new ButtonType("不要儲存", ButtonBar.ButtonData.NO);
		ButtonType cancel = ButtonType.CANCEL;

		Alert alert = new Alert(Alert.AlertType.NONE, "是否要儲存對 " + fileName +" 所做的變更?", save, notSave, cancel);
		alert.setTitle("FXEditor");
		alert.initOwner(stage);
		alert.initStyle(StageStyle.UTILITY);

		return alert.showAndWait();
	}

	private void newFile() {
		if (isFileModified()) {
			Optional<ButtonType> result = showSaveConfirmationDialog();
			switch (result.get().getButtonData()) {
				case YES: 
					saveFile();
				case NO:
					// Do nothing
					break;

				case CANCEL_CLOSE:
				default:
					return;
			}
		}
		textFile = null;
		lastSavedText = "";
		editor.clear();
	}

	private void loadFile() {
		if (isFileModified()) {
			Optional<ButtonType> result = showSaveConfirmationDialog();
			switch (result.get().getButtonData()) {
				case YES: 
					saveFile();
				case NO:
					// Do nothing
					break;

				case CANCEL_CLOSE:
				default:
					return;
			}
		}

		FileChooser fileChooser = createFileChooser("Open");
		textFile = fileChooser.showOpenDialog(stage);

		if (textFile != null) {
			lastOpenDir = textFile.getParent();
			try(Scanner scanner = new Scanner(textFile)) {
				StringBuilder text = new StringBuilder();
				while (scanner.hasNextLine()) {
					text.append(scanner.nextLine() + "\n");
				}
				lastSavedText = text.toString();
				editor.setText(lastSavedText);
			} catch(FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	private void saveFile() {
		// The file hasn't been saved
		if (textFile == null) {
			saveAsNewFile();
		} else {
			try(PrintWriter writer = new PrintWriter(textFile)) {
				lastSavedText = editor.getText();
				writer.write(lastSavedText);
			} catch(FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	private void saveAsNewFile() {
		FileChooser fileChooser = createFileChooser("Save");
		textFile = fileChooser.showSaveDialog(stage);

		if (textFile != null) {
			lastOpenDir = textFile.getParent();
			try(PrintWriter writer = new PrintWriter(textFile)) {
				lastSavedText = editor.getText();
				writer.write(lastSavedText);
			} catch(FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	private String[] getTotalLines() {
		String text = editor.getText();
		List<String> result = new ArrayList<>();

		int beginIndex = 0;
		int newLineIndex = text.indexOf("\n");
		while (newLineIndex != -1) {
			result.add(text.substring(beginIndex, newLineIndex + 1));
			beginIndex = newLineIndex + 1;
			newLineIndex = text.indexOf("\n", beginIndex);
		}
		result.add(text.substring(beginIndex, text.length()));

		return result.toArray(new String[]{});
	}

	public static void main(String[] args) {
		launch(args);

	} 
}
