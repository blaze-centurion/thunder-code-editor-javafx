package sample.editor.controller;

import javafx.scene.layout.VBox;
import javafx.stage.Popup;

public class DropDown extends Popup {
    VBox container;

    public DropDown() {
        container = new VBox();
        container.setStyle("-fx-background-color: #343746; -fx-padding: 10px 0;");
        container.setPrefWidth(320);
        getContent().add(container);
        setAutoHide(true);
        setHideOnEscape(true);
    }

    public void addAllItems(DropDownItem ...args) {
        for (DropDownItem item : args) {
            container.getChildren().add(item);
        }
    }

    public void addItem(DropDownItem item) {
        container.getChildren().add(item);
    }
}
