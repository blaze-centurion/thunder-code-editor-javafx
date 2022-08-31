package sample.editor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import sample.editor.controller.EditorController;
import java.util.*;

import java.util.Objects;

public class Editor extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Font.loadFont(String.valueOf(getClass().getResource("css/JetBrainsMono-Regular.ttf")), 17);
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("fxml/Editor.fxml")));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        EditorController editorController = loader.getController();
        editorController.setIsToCheckHis(true);
        editorController.setMainWindow(stage);
        stage.getIcons().add(new Image("images/logo.jpg"));
        stage.setTitle("Thunder - Code Editor");
        stage.show();
    }
}