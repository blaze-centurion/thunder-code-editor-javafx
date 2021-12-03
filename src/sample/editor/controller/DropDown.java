package sample.editor.controller;

import javafx.scene.layout.VBox;
import javafx.stage.Popup;

public class DropDown extends Popup {
    VBox container;

    public DropDown(String themeFile) {
        container = new VBox();
        container.getStyleClass().add("custom-cmenu-container");
        container.getStylesheets().add(String.valueOf(getClass().getResource(themeFile)));
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
