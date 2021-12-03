package sample.editor.controller;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class DialogBox {
    private Stage confirmStage;
    private Button yesBtn, noBtn, cancelBtn;
    private Label titleLabel, descLabel;
    private HBox btnContainer;

    public int getBtnCode() {
        return btnCode;
    }

    private int btnCode = -1;

    public DialogBox(String stageTitle, String title, String desc) {
        confirmStage = new Stage();
        AnchorPane anchorPane = createUi();
        Scene scene = new Scene(anchorPane);
        titleLabel.setText(title);
        descLabel.setText(desc);
        confirmStage.initModality(Modality.APPLICATION_MODAL);
        confirmStage.setTitle(stageTitle);
        confirmStage.setScene(scene);
        confirmStage.setResizable(false);
        confirmStage.setOnCloseRequest(windowEvent -> btnCode = 0);
    }

    public void showAndWait() {
        confirmStage.showAndWait();
    }

    private AnchorPane createUi() {
        AnchorPane parent = new AnchorPane();
        parent.getStylesheets().add(String.valueOf(getClass().getResource("../css/dracula.css")));
        parent.setPrefWidth(397);
        parent.setPrefHeight(166);
        ImageView imageView = new ImageView(new Image("images/attention.png"));
        imageView.setLayoutX(24);
        imageView.setLayoutY(20);
        imageView.setFitWidth(48);
        imageView.setFitHeight(48);

        titleLabel = new Label();
        titleLabel.setLayoutX(85);
        titleLabel.setLayoutY(16);
        titleLabel.setPrefWidth(267);
        titleLabel.setPrefHeight(60);
        titleLabel.setStyle("-fx-text-fill: #0078d7; -fx-font-size: 17px");
        titleLabel.setWrapText(true);

        descLabel = new Label();
        descLabel.setLayoutX(85);
        descLabel.setLayoutY(82);
        descLabel.setPrefWidth(277);
        descLabel.setPrefHeight(20);

        btnContainer = new HBox();
        btnContainer.setLayoutX(0);
        btnContainer.setLayoutY(118);
        btnContainer.setPrefHeight(48);
        btnContainer.setPrefWidth(397);
        btnContainer.setAlignment(Pos.CENTER_RIGHT);
        btnContainer.setSpacing(13);
        btnContainer.setStyle("-fx-padding: 0 22px 0 0; -fx-background-color: #F0F0F0;");

        yesBtn = new Button("Save");
        yesBtn.getStyleClass().add("window_btn");
        noBtn = new Button("Don't Save");
        noBtn.getStyleClass().add("window_btn");
        cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("window_btn");

        yesBtn.setOnAction(event -> {
            btnCode = 1;
            confirmStage.close();
        });
        noBtn.setOnAction(event -> {
            btnCode = -1;
            confirmStage.close();

        });
        cancelBtn.setOnAction(event -> {
            btnCode = 0;
            confirmStage.close();

        });

        btnContainer.getChildren().addAll(yesBtn, noBtn, cancelBtn);
        parent.getChildren().addAll(imageView, titleLabel, descLabel, btnContainer);
        return parent;
    }
}
