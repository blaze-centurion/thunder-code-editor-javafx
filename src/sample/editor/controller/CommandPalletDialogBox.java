package sample.editor.controller;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class CommandPalletDialogBox extends Popup {
    Stage window;
    TreeItem<String> selectedTreeItem;

    public CommandPalletDialogBox(Stage window, TreeItem<String> selectedTreeItem) {
        this.window = window;
        this.selectedTreeItem = selectedTreeItem;
        setAutoHide(true);
        setHideOnEscape(true);
    }

    public void showRenameBox(double x, double y, File file) {
        createRenameUi(file);
        show(window, x,y);
    }

    public void showNewNameBox(double x, double y, File file, boolean isFile) {
        createNewNameUi(file, isFile);
        show(window, x,y);
    }

    private void createRenameUi(File file) {
        VBox container = new VBox();
        container.setStyle("-fx-background-color: #21222C; -fx-padding: 0;");
        container.setPrefWidth(500);
        container.getStylesheets().add(String.valueOf(getClass().getResource("../css/style.css")));

        HBox renameFieldBox = new HBox();
        renameFieldBox.setAlignment(Pos.CENTER);
        renameFieldBox.setStyle("-fx-padding: 10px;");

        TextField renameField = new TextField();
        renameField.setPrefWidth(500);
        renameField.getStyleClass().add("rename_field");
        renameField.setText(file.getName());
        renameField.setOnAction(event -> renameFile(file, renameField));
        renameFieldBox.getChildren().add(renameField);

        container.getChildren().add(renameFieldBox);
        getContent().add(container);
    }

    private void createNewNameUi(File file, boolean isFile) {
        VBox container = new VBox();
        container.setStyle("-fx-background-color: #21222C; -fx-padding: 0;");
        container.setPrefWidth(500);
        container.getStylesheets().add(String.valueOf(getClass().getResource("../css/style.css")));

        HBox fieldBox = new HBox();
        fieldBox.setAlignment(Pos.CENTER);
        fieldBox.setStyle("-fx-padding: 10px;");

        TextField field = new TextField();
        field.setPrefWidth(500);
        field.getStyleClass().add("rename_field");
        field.setText(file.getName());
        if (isFile) field.setOnAction(event -> newFile(file, field));
        else field.setOnAction(event -> newFolder(file, field));
        fieldBox.getChildren().add(field);

        container.getChildren().add(fieldBox);
        getContent().add(container);
    }


    private void renameFile(File file, TextField renameField) {
        String path = file.getAbsolutePath();
        File newFile = new File(path.substring(0, path.length()-file.getName().length()) + renameField.getText());
        if (file.renameTo(newFile)) {
            selectedTreeItem.setValue(newFile.getName());
            selectedTreeItem.getGraphic().setId(newFile.getAbsolutePath());
            hide();
        } else {
            System.out.println("Files already exists.");
        }
    }

    private void newFile(File dir, TextField field) {
        File file = new File(dir.getAbsolutePath() + "\\" + field.getText());
        try {
            if (file.createNewFile()) {
                TreeItem<String> item = new TreeItem<>(file.getName());
                Label label = new Label();
                label.setId(file.getAbsolutePath());
                item.setGraphic(label);
                selectedTreeItem.getChildren().add(item);
                hide();
            } else {
                System.out.println("Files already exists");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void newFolder(File dir, TextField field) {
        File file = new File(dir.getAbsolutePath() + "\\" + field.getText());
        if (file.mkdir()) {
            TreeItem<String> item = new TreeItem<>(file.getName());
            Label label = new Label();
            label.setId(file.getAbsolutePath());
            item.setGraphic(label);
            selectedTreeItem.getChildren().add(item);
            hide();
        } else {
            System.out.println("Files already exists");
        }
    }
}
