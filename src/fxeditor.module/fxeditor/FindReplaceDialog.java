package fxeditor;

import javafx.beans.value.ChangeListener;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Scene;
import javafx.stage.*;

public class FindReplaceDialog {

	private Stage owner, stage;
	private Scene scene;

	private AnchorPane root;
	private GridPane textContainer;
	private VBox buttonContainer;
	
	private Label targetLabel, replaceAsLabel;
	private TextField findTarget, replaceAs;
	private Button findNext, replace, replaceAll, cancel;
	private CheckBox caseSensitive;
	private TextArea editor;

	public FindReplaceDialog(Stage owner, TextArea editor) {
		this.owner = owner;
		this.editor = editor;

		findTarget = new TextField();
		findTarget.setMaxWidth(Double.MAX_VALUE);
		findTarget.textProperty().addListener((obs, oldValue, newValue) -> {
			findNext.setDisable(newValue.isEmpty());
			replaceAll.setDisable(newValue.isEmpty());
		});

		replaceAs = new TextField();
		replaceAs.setMaxWidth(Double.MAX_VALUE);

		targetLabel = new Label("尋找目標(_N):");
		targetLabel.setMnemonicParsing(true);
		targetLabel.setLabelFor(findTarget);

		replaceAsLabel = new Label("取代為(_P):");
		replaceAsLabel.setMnemonicParsing(true);
		replaceAsLabel.setLabelFor(replaceAs);

		ColumnConstraints colcon1 = new ColumnConstraints();
		colcon1.setMinWidth(10.0);
		colcon1.setHalignment(HPos.LEFT);
		colcon1.setHgrow(Priority.SOMETIMES);

		ColumnConstraints colcon2 = new ColumnConstraints();
		colcon2.setMinWidth(10.0);
		colcon2.setHgrow(Priority.ALWAYS);

		RowConstraints rowcon1 = new RowConstraints();
		rowcon1.setMinHeight(10.0);
		rowcon1.setVgrow(Priority.SOMETIMES);

		RowConstraints rowcon2 = new RowConstraints();
		rowcon2.setMinHeight(10.0);
		rowcon2.setVgrow(Priority.SOMETIMES);

		textContainer = new GridPane();
		textContainer.setLayoutX(14.0);
		textContainer.setLayoutY(29.0);
		textContainer.setMaxHeight(Double.MAX_VALUE);
		textContainer.setMaxWidth(Double.MAX_VALUE);
		textContainer.setPrefHeight(52.0);
		textContainer.setPrefWidth(318.0);
		textContainer.setHgap(20.0);
		textContainer.setVgap(15.0);
		textContainer.add(targetLabel, 0, 0);
		textContainer.add(replaceAsLabel, 0, 1);
		textContainer.add(findTarget, 1, 0);
		textContainer.add(replaceAs, 1, 1);
		textContainer.getColumnConstraints().addAll(colcon1, colcon2);
		textContainer.getRowConstraints().addAll(rowcon1, rowcon2);
		AnchorPane.setLeftAnchor(textContainer, 10.0);
		AnchorPane.setRightAnchor(textContainer, 138.0);

		findNext = new Button("找下一個(_F)");
		findNext.setMaxHeight(Double.MAX_VALUE);
		findNext.setMaxWidth(Double.MAX_VALUE);
		findNext.setDefaultButton(true);
		findNext.setDisable(true);
		findNext.setOnAction(e -> {
			if (!findNext(caseSensitive.isSelected())) {
				showInfoDialog("找不到", null, "找不到 " + getLastFindedString());
			}
			replace.setDisable(editor.getSelectedText().isEmpty());
		});

		replace = new Button("取代(_R)");
		replace.setMaxHeight(Double.MAX_VALUE);
		replace.setMaxWidth(Double.MAX_VALUE);
		replace.setDisable(true);
		replace.setOnAction(e -> {
			replace();
			replace.setDisable(true);
		});

		replaceAll = new Button("全部取代(_A)");
		replaceAll.setMaxHeight(Double.MAX_VALUE);
		replaceAll.setMaxWidth(Double.MAX_VALUE);
		replaceAll.setDisable(true);
		replaceAll.setOnAction(e -> {
			int times = 0;
			editor.positionCaret(0);
			while (findNext(caseSensitive.isSelected())) {
				replace();
				times++;
			}
			showInfoDialog("完成", null, "一共取代 " + times + " 個");
		});

		cancel = new Button("取消");
		cancel.setMaxHeight(Double.MAX_VALUE);
		cancel.setMaxWidth(Double.MAX_VALUE);
		cancel.setCancelButton(true);
		cancel.setOnAction(e -> stage.close());

		buttonContainer = new VBox(10, findNext, replace, replaceAll, cancel);
		buttonContainer.setLayoutX(359.0);
		buttonContainer.setLayoutY(29.0);
		AnchorPane.setRightAnchor(buttonContainer, 10.0);
		AnchorPane.setTopAnchor(buttonContainer, 10.0);

		caseSensitive = new CheckBox("大小寫視為相異(_C)");
		caseSensitive.setLayoutX(14);
		caseSensitive.setLayoutY(199);
		AnchorPane.setBottomAnchor(caseSensitive, 10.0);
		AnchorPane.setLeftAnchor(caseSensitive, 10.0);
		AnchorPane.setTopAnchor(caseSensitive, 199.0);

		root = new AnchorPane(textContainer, buttonContainer, caseSensitive);
		root.setPadding(new Insets(10));

		scene = new Scene(root);

		ChangeListener<IndexRange> selectionProperty = (obs, oldValue, newValue) -> replace.setDisable(true);

		stage = new Stage();
		stage.initOwner(owner);
		stage.initStyle(StageStyle.UTILITY);
		stage.setTitle("取代");
		stage.setScene(scene);
		stage.setMinWidth(486);
		stage.setMinHeight(247);
		stage.setOnShown(e -> {
			editor.selectionProperty().addListener(selectionProperty);

			findTarget.requestFocus();
			findTarget.selectAll();

			stage.setX(owner.getX() + owner.getWidth() / 2 - stage.getWidth() / 2);
			stage.setY(owner.getY() + owner.getHeight() / 2 - stage.getHeight() / 2);
		});
		stage.setOnHidden(e -> editor.selectionProperty().removeListener(selectionProperty));
	}

	public boolean findNext(boolean isCaseSensitive) {
		String content = isCaseSensitive ? editor.getText() : editor.getText().toLowerCase();
		String target = isCaseSensitive ? findTarget.getText() : findTarget.getText().toLowerCase();
		int targetIndex = content.indexOf(target, editor.getCaretPosition());

		if (targetIndex != -1) {
			editor.selectRange(targetIndex, targetIndex + target.length());
			return true;
		} else {
			return false;
		}
	}

	private void replace() {
		editor.replaceText(editor.getSelection(), replaceAs.getText());
	}

	public String getLastFindedString() {
		return findTarget.getText();
	}

	private void showInfoDialog(String title, String header, String content) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.initOwner(stage);
		alert.setTitle(title);
		alert.initStyle(StageStyle.UTILITY);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.getDialogPane().setPrefWidth(150);
		alert.showAndWait();
	}

	public void show() {
		stage.show();
	}
}
