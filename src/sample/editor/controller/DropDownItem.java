package sample.editor.controller;

import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

public class DropDownItem extends HBox {
    EventHandler<MouseEvent> eventEventHandler;

    public DropDownItem(EventHandler<MouseEvent> eventEventHandler, String text, String themeFile) {
        this.eventEventHandler = eventEventHandler;
        Label label = new Label(text);
        label.getStyleClass().add("custom_cmenu_label");
        label.getStylesheets().add(String.valueOf(getClass().getResource(themeFile)));
        label.setPrefWidth(320);
        getChildren().add(label);
        setOnMouseClicked(this.eventEventHandler);
    }

    public DropDownItem() {
        HBox separator = new HBox();
        separator.setStyle("-fx-border-color: #656770;");
        separator.setPrefWidth(290);
        setStyle("-fx-padding: 10px;");
        setAlignment(Pos.CENTER);
        getChildren().add(separator);
        setPrefWidth(320);
    }
}
