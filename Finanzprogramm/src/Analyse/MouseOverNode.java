package Analyse;

import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class MouseOverNode extends StackPane {

	public MouseOverNode(String wert) {
		this.setPrefSize(15, 15);

		final Label label = new Label(wert);
		label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
		label.getStyleClass().addAll("default-color0", "chart-line-symbol", "chart-series-line");
		label.setStyle("-fx-font-size: 15;");
		this.setOnMouseEntered(mouseEvent -> {
			MouseOverNode.this.getChildren().setAll(label);
			MouseOverNode.this.setCursor(Cursor.NONE);
			this.toFront();
		});
		this.setOnMouseExited(mouseEvent -> {
			this.getChildren().clear();
			this.setCursor(Cursor.HAND);
		});
	}
}
