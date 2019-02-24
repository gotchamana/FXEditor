package fxeditor;

import javafx.application.Platform;
import javafx.collections.*;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.*;

import java.util.*;

public class FontChooser {

	private Stage owner, stage;
	private Scene scene;

	private GridPane root, vBoxContainer;
	private VBox fontFamilyContainer, fontStyleContainer, fontSizeContainer;
	private TitledPane sampleContainer;
	private ButtonBar buttonContainer;

	private Label fontFamilyLabel, fontStyleLabel, fontSizeLabel, sampleLabel;
	private TextField fontFamilyTextField, fontStyleTextField, fontSizeTextField;
	private TextArea editor;
	private ListView<String> fontFamily, fontStyle;
	private ListView<Integer> fontSize;
	private Button ok, cancel;

	private Map<String, Set<String>> familyStyleMap;
	private Font currentFont;

	public FontChooser(Stage owner) {
		this.owner = owner;
		familyStyleMap = createFontFamilyStyleMap();

		fontFamilyTextField = new TextField();
		fontFamilyTextField.focusedProperty().addListener((obs, oldValue, newValue) -> {
			Platform.runLater(() -> {
				if (newValue) {
					fontFamilyTextField.selectAll();
				} else {
					if(isFontFamilyValid()) fontFamily.getSelectionModel().select(fontFamilyTextField.getText());
				}
			});
		});
		fontFamilyTextField.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
			if (e.getCode() == KeyCode.ENTER) {
				if(isFontFamilyValid()) fontFamily.getSelectionModel().select(fontFamilyTextField.getText());
			}
		});
		fontFamilyTextField.addEventFilter(KeyEvent.KEY_RELEASED, e -> {
			String text = fontFamilyTextField.getText();

			for (String family : familyStyleMap.keySet()) {
				if (e.getCode().isLetterKey() || e.getCode().isWhitespaceKey() || e.getCode() == KeyCode.MINUS) {
					KeyCombination selectAll = new KeyCodeCombination(KeyCode.A, KeyCombination.SHORTCUT_DOWN);
					if (selectAll.match(e)) {
						fontFamilyTextField.selectAll();
						e.consume();
						break;
					}

					if (!text.isEmpty() && family.startsWith(text)) {
						String rest = family.substring(text.length());

						fontFamilyTextField.appendText(rest);
						fontFamilyTextField.selectRange(family.length(), text.length());
						e.consume();
						break;
					}		
				}
			}
		});

		fontStyleTextField = new TextField();
		fontStyleTextField.focusedProperty().addListener((obs, oldValue, newValue) -> {
			Platform.runLater(() -> {
				if (newValue) {
					fontStyleTextField.selectAll();
				} else {
					if(isFontStyleValid()) fontStyle.getSelectionModel().select(fontStyleTextField.getText());
				}
			});
		});
		fontStyleTextField.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
			if (e.getCode() == KeyCode.ENTER) {
				if(isFontStyleValid()) fontStyle.getSelectionModel().select(fontStyleTextField.getText());
			}
		});

		fontSizeTextField = new TextField();
		fontSizeTextField.focusedProperty().addListener((obs, oldValue, newValue) -> {
			Platform.runLater(() -> {
				if (newValue) {
					fontSizeTextField.selectAll();
				} else {
					if(isFontSizeValid()) fontSize.getSelectionModel().select(Integer.decode(fontSizeTextField.getText()));
				}
			});
		});
		fontSizeTextField.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
			if (e.getCode() == KeyCode.ENTER) {
				if(isFontSizeValid()) fontSize.getSelectionModel().select(Integer.decode(fontSizeTextField.getText()));
			}
		});

		fontFamilyLabel = new Label("字型(_F):");
		fontFamilyLabel.setMnemonicParsing(true);
		fontFamilyLabel.setLabelFor(fontFamilyTextField);

		fontStyleLabel = new Label("字型樣式(_Y):");
		fontStyleLabel.setMnemonicParsing(true);
		fontStyleLabel.setLabelFor(fontStyleTextField);

		fontSizeLabel = new Label("大小(_S):");
		fontSizeLabel.setMnemonicParsing(true);
		fontSizeLabel.setLabelFor(fontSizeTextField);

		fontFamily = new ListView<>();
		fontFamily.setItems(createFontFamilyList());
		fontFamily.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			fontFamilyTextField.setText(newValue);
			fontStyle.setItems(createFontStyleList(newValue));
			sampleLabel.setFont(createFont());
		});

		fontStyle = new ListView<>();
		fontStyle.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue == null) {
				fontStyle.getSelectionModel().select(0);
				newValue = fontStyle.getSelectionModel().getSelectedItem();
			}
			fontStyleTextField.setText(newValue);
			sampleLabel.setFont(createFont());
		});

		fontSize = new ListView<>();
		fontSize.setItems(createFontSizeList());
		fontSize.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			fontSizeTextField.setText(newValue.toString());
			sampleLabel.setFont(createFont());
		});

		fontFamilyContainer = new VBox(fontFamilyLabel, fontFamilyTextField, fontFamily);
		fontFamilyContainer.setPrefHeight(150.0);
		fontFamilyContainer.setMargin(fontFamilyLabel, new Insets(0, 0, 5.0, 0));

		fontStyleContainer = new VBox(fontStyleLabel, fontStyleTextField, fontStyle);
		fontStyleContainer.setPrefHeight(150.0);
		fontStyleContainer.setPrefWidth(150.0);
		fontStyleContainer.setMargin(fontStyleLabel, new Insets(0, 0, 5.0, 0));

		fontSizeContainer = new VBox(fontSizeLabel, fontSizeTextField, fontSize);
		fontSizeContainer.setPrefHeight(150.0);
		fontSizeContainer.setPrefWidth(80.0);
		fontSizeContainer.setMargin(fontSizeLabel, new Insets(0, 0, 5.0, 0));

		ColumnConstraints vBoxColCon1, vBoxColCon2, vBoxColCon3;
		vBoxColCon1 = new ColumnConstraints();
		vBoxColCon1.setHgrow(Priority.ALWAYS);
		vBoxColCon1.setMinWidth(10.0);

		vBoxColCon2 = new ColumnConstraints();
		vBoxColCon2.setHgrow(Priority.NEVER);

		vBoxColCon3 = new ColumnConstraints();
		vBoxColCon3.setHgrow(Priority.NEVER);

		RowConstraints vBoxRowCon = new RowConstraints();
		vBoxRowCon.setVgrow(Priority.SOMETIMES);
		vBoxRowCon.setMinHeight(10.0);

		vBoxContainer = new GridPane();
		vBoxContainer.setHgap(10.0);
		vBoxContainer.getColumnConstraints().addAll(vBoxColCon1, vBoxColCon2, vBoxColCon3);
		vBoxContainer.getRowConstraints().add(vBoxRowCon);
		vBoxContainer.add(fontFamilyContainer, 0, 0);
		vBoxContainer.add(fontStyleContainer, 1, 0);
		vBoxContainer.add(fontSizeContainer, 2, 0);

		sampleLabel = new Label("AaBbCc中文範例");

		sampleContainer = new TitledPane("範例", sampleLabel);
		sampleContainer.setMaxHeight(Double.MAX_VALUE);
		sampleContainer.setPrefHeight(185.0);
		sampleContainer.setPrefWidth(511.0);
		sampleContainer.setCollapsible(false);

		ok = new Button("確定");
		ok.setMaxHeight(Double.MAX_VALUE);
		ok.setMaxWidth(Double.MAX_VALUE);
		ok.setOnAction(e -> {
			currentFont = createFont();
			stage.close();
		});

		cancel = new Button("取消");
		cancel.setMaxHeight(Double.MAX_VALUE);
		cancel.setMaxWidth(Double.MAX_VALUE);
		cancel.setCancelButton(true);
		cancel.setOnAction(e -> {
			stage.close();
		});

		buttonContainer = new ButtonBar();
		buttonContainer.getButtons().addAll(ok, cancel);

		ColumnConstraints rootColCon = new ColumnConstraints();
		rootColCon.setHgrow(Priority.SOMETIMES);
		rootColCon.setMinWidth(10.0);

		RowConstraints rootRowCon1, rootRowCon2, rootRowCon3;
		rootRowCon1 = new RowConstraints();
		rootRowCon1.setVgrow(Priority.SOMETIMES);
		rootRowCon1.setMinHeight(10.0);

		rootRowCon2 = new RowConstraints();
		rootRowCon2.setVgrow(Priority.SOMETIMES);
		rootRowCon2.setMinHeight(10.0);

		rootRowCon3 = new RowConstraints();
		rootRowCon3.setVgrow(Priority.NEVER);
		rootRowCon3.setMinHeight(10.0);

		root = new GridPane();
		root.setVgap(10.0);
		root.setPadding(new Insets(10.0));
		root.add(vBoxContainer, 0, 0);
		root.add(sampleContainer, 0, 1);
		root.add(buttonContainer, 0, 2);
		root.getColumnConstraints().add(rootColCon);
		root.getRowConstraints().addAll(rootRowCon1, rootRowCon2, rootRowCon3);

		scene = new Scene(root);

		stage = new Stage();
		stage.initOwner(owner);
		stage.initStyle(StageStyle.UTILITY);
		stage.setTitle("字型");
		stage.setScene(scene);
		stage.setOnShown(e -> {
			String family = currentFont.getFamily();
			String style = currentFont.getStyle();
			Integer size = (int)currentFont.getSize();

			if(style.equals("Regular")) style = "標準";
			if(style.equals("Bold")) style = "粗體";
			if(style.equals("Italic")) style = "斜體";
			if(style.equals("Bold Italic")) style = "粗斜體";

			fontFamilyTextField.setText(family);
			fontStyleTextField.setText(style);
			fontSizeTextField.setText(size.toString());

			fontFamily.scrollTo(family);
			fontFamily.getSelectionModel().select(family);

			fontStyle.getSelectionModel().select(style);
			fontStyle.scrollTo(style);

			fontSize.getSelectionModel().select(size);
			fontSize.scrollTo(size);

			stage.setX(owner.getX() + owner.getWidth() / 2 - stage.getWidth() / 2);
			stage.setY(owner.getY() + owner.getHeight() / 2 - stage.getHeight() / 2);
		});
	}

	public Font showAndWait(Font currentFont) {
		this.currentFont = currentFont;
		stage.showAndWait();

		return this.currentFont;
	}

	private ObservableList<String> createFontFamilyList() {
		List<String> families = Font.getFamilies();
		return FXCollections.observableArrayList(families);
	}

	private Map<String, Set<String>> createFontFamilyStyleMap() {
		Map<String, Set<String>> familyStyleMap = new TreeMap<>();

		// Iterate each font family
		for (String family : createFontFamilyList()) {
			Set<String> styles = new TreeSet<>();
			Set<String> filter = new HashSet<>(Font.getFontNames(family));

			// Iterate each font name
			for (String name : filter) {
				boolean isBold = false;
				boolean isItalic = false;
				String[] words = name.contains(" ") ? name.toLowerCase().split(" ") : name.toLowerCase().split("-");

				// Iterate each word to detect the font style
				for (String word : words) {
					isBold = word.equals("bold") || isBold;
					isItalic = (word.equals("it") || word.equals("italic") || word.equals("oblique") || isItalic);
				}

				// Detect the font style
				if (isBold && isItalic) {
					styles.add("Bold Italic");
				} else if (!isBold && isItalic) {
					styles.add("Italic");
				} else if (isBold && !isItalic) {
					styles.add("Bold");
				} else {
					styles.add("Regular");
				}
			}

			familyStyleMap.put(family, styles);
		}

		return familyStyleMap;
	}

	private Font createFont() {
		if (fontFamily.getSelectionModel().getSelectedItem() == null ||
			fontSize.getSelectionModel().getSelectedItem() == null ||
			fontStyle.getSelectionModel().getSelectedItem() == null) {
			return null;
		}

		String family = fontFamily.getSelectionModel().getSelectedItem();
		int size = fontSize.getSelectionModel().getSelectedItem();
		String style = fontStyle.getSelectionModel().getSelectedItem();
		String weight = "Normal";
		String posture = "Regular";

		if(style.equals("標準")) posture = "Regular";
		if(style.equals("粗體")) weight = "Bold";
		if(style.equals("斜體")) posture = "Italic";
		if(style.equals("粗斜體")) {
			posture = "Italic";
			weight = "Bold";
		};

		return Font.font(family, FontWeight.findByName(weight), FontPosture.findByName(posture), size);
	}

	private boolean isFontFamilyValid() {
		for (String family : familyStyleMap.keySet()) {
			if (fontFamilyTextField.getText().equals(family)) {
				return true;
			}
		}
		return false;
	}

	private boolean isFontStyleValid() {
		for (String style : fontStyle.getItems()) {
			if (fontStyleTextField.getText().equals(style)) {
				return true;
			}
		}
		return false;
	}

	private boolean isFontSizeValid() {
		try {
			int size = Integer.parseInt(fontSizeTextField.getText());
			return size > 0;
		} catch(NumberFormatException e){
			e.printStackTrace();
			return false;
		}
	}

	private ObservableList<String> createFontStyleList(String family) {
		Set<String> enStyles = familyStyleMap.get(family);
		Set<String> zhStyles = new TreeSet<>();

		// Translate the name
		for (String style : enStyles) {
			if(style.equals("Regular")) style = "標準";
			if(style.equals("Bold")) style = "粗體";
			if(style.equals("Italic")) style = "斜體";
			if(style.equals("Bold Italic")) style = "粗斜體";
			zhStyles.add(style);
		}

		return FXCollections.observableArrayList(zhStyles);
	}

	private ObservableList<Integer> createFontSizeList() {
		List<Integer> sizes = new ArrayList<>();
		for (int i = 12; i <= 30; i = i + 2) {
			sizes.add(i);
		}
		for (int i = 32; i <= 52; i = i + 4) {
			sizes.add(i);
		}
		for (int i = 54; i <= 78; i = i+ 6) {
			sizes.add(i);
		}
		for (int i = 80; i <= 96; i = i+ 8) {
			sizes.add(i);
		}

		return FXCollections.observableArrayList(sizes);
	}
}
