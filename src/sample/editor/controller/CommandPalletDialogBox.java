package sample.editor.controller;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CommandPalletDialogBox extends Popup {
    private Stage window;
    private TreeItem<String> selectedTreeItem;
    private String themeFile;
    private BorderPane borderPane;
    private Function exitFunction;

    public CommandPalletDialogBox(Stage window, TreeItem<String> selectedTreeItem, String themeFile) {
        this.window = window;
        this.selectedTreeItem = selectedTreeItem;
        this.themeFile = themeFile;
        setAutoHide(true);
        setHideOnEscape(true);
    }

    public CommandPalletDialogBox(Stage window, String themeFile) {
        this.window = window;
        this.themeFile = themeFile;
        setAutoHide(true);
        setHideOnEscape(true);
    }

    public void showChangeThemeBox(double x, double y, BorderPane borderPane, HashMap<String, String> themeList, Function exitFunction) {
        this.borderPane = borderPane;
        this.exitFunction = exitFunction;
        createThemeBoxUi(themeList);
        show(window, x,y);
    }

    public void showRenameBox(double x, double y, File file) {
        createRenameUi(file);
        show(window, x,y);
    }

    public void showNewNameBox(double x, double y, File file, boolean isFile) {
        createNewNameUi(file, isFile);
        show(window, x,y);
    }

    private void createThemeBoxUi(HashMap<String, String> themeList) {
        VBox container = new VBox();
        container.getStylesheets().add(String.valueOf(getClass().getResource(themeFile)));
        container.getStyleClass().add("popup-container");
        container.setPrefWidth(500);

        HBox fieldBox = new HBox();
        fieldBox.setAlignment(Pos.CENTER);
        fieldBox.setStyle("-fx-padding: 10px;");

        TextField field = new TextField();
        field.setPrefWidth(500);
        field.getStyleClass().add("rename_field");
        fieldBox.getChildren().add(field);

        VBox listContainer = new VBox();
        listContainer.getStyleClass().add("theme-list-container");
        for (Map.Entry<String, String> i : themeList.entrySet()) {
            Label item = new Label(i.getKey());
            item.setId(i.getValue());
            item.getStyleClass().add("theme-list-item");
            item.setOnMouseClicked(event -> {
                changeTheme(i.getValue());
                hide();
            });
            item.setPrefWidth(500);
            listContainer.getChildren().add(item);
        }

        container.getChildren().add(fieldBox);
        container.getChildren().add(listContainer);
        getContent().add(container);
    }

    private void changeTheme(String val) {
        new FileIO().changeTheme(val);
        exitFunction.execute();
        this.window.close();
        Stage stage = new Stage();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/Editor.fxml"));
            Parent root = loader.load();
            EditorController editorController = loader.getController();
            editorController.setIsToCheckHis(true);
            editorController.setIsThemeChanging(true);
            editorController.setMainWindow(stage);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createRenameUi(File file) {
        VBox container = new VBox();
        container.getStylesheets().add(String.valueOf(getClass().getResource(themeFile)));
        container.getStyleClass().add("popup-container");
        container.setPrefWidth(500);
        container.getStylesheets().add(String.valueOf(getClass().getResource(themeFile)));

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
        container.getStylesheets().add(String.valueOf(getClass().getResource(themeFile)));
        container.getStyleClass().add("popup-container");
        container.setPrefWidth(500);
        container.getStylesheets().add(String.valueOf(getClass().getResource(themeFile)));

        HBox fieldBox = new HBox();
        fieldBox.setAlignment(Pos.CENTER);
        fieldBox.setStyle("-fx-padding: 10px;");

        TextField field = new TextField();
        field.setPrefWidth(500);
        field.getStyleClass().add("rename_field");
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
