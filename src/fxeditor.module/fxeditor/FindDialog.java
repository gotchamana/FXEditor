package fxeditor;

import javafx.event.Event;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

public class FindDialog {

	private Stage owner, stage;
	private Scene scene;
	private TextArea editor;

	private AnchorPane root;
	private TitledPane radioButtonContainer;
	private HBox textContainer, optionWrapper, optionContainer;
	private VBox buttonContainer;

	private Label label;
	private TextField findTarget;
	private Button findNext, cancel;
	private CheckBox caseSensitive;
	private ToggleGroup directon;
	private RadioButton up, down;

	public FindDialog(Stage owner, TextArea editor) {
		this.owner = owner;
		this.editor = editor;

		findTarget = new TextField();
		findTarget.setPrefHeight(26);
		findTarget.setPrefWidth(259);
		findTarget.textProperty().addListener((obs, oldValue, newValue) -> {
			findNext.setDisable(newValue.isEmpty());
		});

		label = new Label("尋找目標(_N):");
		label.setMnemonicParsing(true);
		label.setLabelFor(findTarget);

		textContainer = new HBox(10, label, findTarget);
		textContainer.setLayoutX(14);
		textContainer.setLayoutY(18);
		textContainer.setAlignment(Pos.CENTER);
		AnchorPane.setLeftAnchor(textContainer, 10.0);
		AnchorPane.setTopAnchor(textContainer, 10.0);

		findNext = new Button("找下一個(_F)");
		findNext.setMaxHeight(Double.MAX_VALUE);
		findNext.setMaxWidth(Double.MAX_VALUE);
		findNext.setDefaultButton(true);
		findNext.setDisable(true);
		findNext.setOnAction(e -> {
			if (up.isSelected()) {
				findNext(Direction.UP, caseSensitive.isSelected());
			} else {
				findNext(Direction.DOWN, caseSensitive.isSelected());
			}
		});

		cancel = new Button("取消");
		cancel.setMaxHeight(Double.MAX_VALUE);
		cancel.setMaxWidth(Double.MAX_VALUE);
		cancel.setCancelButton(true);
		cancel.setOnAction(e -> Event.fireEvent(stage, new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST)));

		buttonContainer = new VBox(10, findNext, cancel);
		buttonContainer.setLayoutX(377);
		buttonContainer.setLayoutY(18);
		buttonContainer.setAlignment(Pos.CENTER);
		AnchorPane.setLeftAnchor(buttonContainer, 370.0);
		AnchorPane.setTopAnchor(buttonContainer, 10.0);

		caseSensitive = new CheckBox("大小寫視為相異(_C)");
		caseSensitive.setMnemonicParsing(true);

		directon = new ToggleGroup();

		up = new RadioButton("向上(_U)");
		up.setMnemonicParsing(true);
		up.setToggleGroup(directon);

		down = new RadioButton("向下(_D)");
		down.setMnemonicParsing(true);
		down.setSelected(true);
		down.setToggleGroup(directon);

		optionWrapper = new HBox(10, up, down);
		optionWrapper.setPrefHeight(39);
		optionWrapper.setPrefWidth(144);
		optionWrapper.setAlignment(Pos.CENTER_LEFT);
		optionWrapper.setPadding(new Insets(10));

		radioButtonContainer = new TitledPane("方向", optionWrapper);
		radioButtonContainer.setCollapsible(false);
		radioButtonContainer.setPrefHeight(66);
		radioButtonContainer.setPrefWidth(192);

		optionContainer = new HBox(10, caseSensitive, radioButtonContainer);
		optionContainer.setLayoutX(23);
		optionContainer.setLayoutY(109);
		optionContainer.setAlignment(Pos.BOTTOM_CENTER);
		AnchorPane.setBottomAnchor(optionContainer, 10.0);
		AnchorPane.setLeftAnchor(optionContainer, 10.0);
		AnchorPane.setTopAnchor(optionContainer, 109.0);

		root = new AnchorPane();
		root.setMaxHeight(Double.MAX_VALUE);
		root.setMaxWidth(Double.MAX_VALUE);
		root.setMinHeight(Region.USE_PREF_SIZE);
		root.setMinWidth(Region.USE_PREF_SIZE);
		root.setPadding(new Insets(10));
		root.getChildren().addAll(textContainer, buttonContainer, optionContainer);

		scene = new Scene(root);

		stage = new Stage();
		stage.initOwner(owner);
		stage.initStyle(StageStyle.UTILITY);
		stage.setTitle("尋找");
		stage.setMinHeight(205);
		stage.setMinWidth(478);
		stage.setScene(scene);
		stage.setOnShowing(e -> {
			findTarget.requestFocus();
			if (!editor.getSelectedText().isEmpty()) {
				findTarget.setText(editor.getSelectedText());
			}
			findTarget.selectAll();
		});
		stage.setOnShown(e -> {
			stage.setX(owner.getX() + owner.getWidth() / 2 - stage.getWidth() / 2);
			stage.setY(owner.getY() + owner.getHeight() / 2 - stage.getHeight() / 2);
		});
	}

	public void findNext(Direction dir, boolean isCaseSensitive) {
		int targetIndex = 0;
		String content = isCaseSensitive ? editor.getText() : editor.getText().toLowerCase();
		String target = isCaseSensitive ? findTarget.getText() : findTarget.getText().toLowerCase();

		if (dir == Direction.UP) {
			targetIndex = content.lastIndexOf(target, editor.getCaretPosition() - 1);
		} else {
			targetIndex = content.indexOf(target, editor.getCaretPosition());
		}

		if (targetIndex != -1) {
			if (dir == Direction.UP) {
				editor.selectRange(targetIndex + target.length(), targetIndex);
			} else {
				editor.selectRange(targetIndex, targetIndex + target.length());
			}
		} else {
			showTargetStringNotFoundDialog();
		}
	}

	public String getLastFindedString() {
		return findTarget.getText();
	}

	private void showTargetStringNotFoundDialog() {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.initOwner(stage);
		alert.initStyle(StageStyle.UTILITY);
		alert.setTitle("找不到");
		alert.setHeaderText(null);
		alert.setContentText("找不到 " + getLastFindedString());
		alert.getDialogPane().setPrefWidth(150);
		alert.showAndWait();
	}

	public void show() {
		stage.show();
	}
}
