package sample.editor.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Popup;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class FindAndReplaceDialogController {
    @FXML
    private TextField findInput, replaceInput;
    @FXML
    private Label currIndexLabel, totalIndexLabel;
    @FXML
    private HBox findBox, replaceBox, container;

    private StyleClassedTextArea codeArea;
    private Popup popup;
    private Utils utils = new Utils();
    ArrayList<ArrayList<Integer>> coordinateList = new ArrayList<>();
    AtomicInteger currWordIndex = new AtomicInteger(0);

    public void setCodeArea(StyleClassedTextArea codeArea) {
        this.codeArea = codeArea;
    }

    public void setPopup(Popup popup) {
        this.popup = popup;
        popup.setOnHiding(windowEvent -> {
            utils.removeHighlightedTxt(coordinateList, codeArea, currWordIndex);
            /*
            * After closing the popup dialog the color of keywords is also changed.
            * So in order to re-highlight the syntax I am appending and deleting the text to fire plaintext change event.
            * I think, there are some better way, but until that I am going with this one.
            */
            codeArea.appendText(" ");
            int len = codeArea.getText().length();
            codeArea.deleteText(len-1, len);
        });
    }

    @FXML
    void find() {
        if (findInput.getText().isEmpty()) return;
        utils.highlightText(findInput, coordinateList, currWordIndex, codeArea);
        if (coordinateList.size()==0) return;
        totalIndexLabel.setText(String.valueOf(coordinateList.size()));
        currIndexLabel.setText(String.valueOf(currWordIndex.get()+1));
    }

    @FXML
    void close() {
        popup.hide();
    }

    @FXML
    void nextWord() {
        if (coordinateList.size()==0) return;
        utils.gotoNextWord(coordinateList, currWordIndex, codeArea);
        currIndexLabel.setText(String.valueOf(currWordIndex.get()+1));
    }

    @FXML
    void prevWord() {
        if (coordinateList.size()==0) return;
        utils.gotoPrevWord(coordinateList, currWordIndex, codeArea);
        currIndexLabel.setText(String.valueOf(currWordIndex.get()+1));
    }

    @FXML
    void replace() {
        if (coordinateList.size()==0) return;
        codeArea.replaceText(coordinateList.get(currWordIndex.get()).get(0), coordinateList.get(currWordIndex.get()).get(1), replaceInput.getText());
        utils.highlightText(findInput, coordinateList, currWordIndex, codeArea);
    }

    @FXML
    void replaceAll() {
        if (coordinateList.size()==0) return;
        codeArea.replaceText(codeArea.getText().replaceAll("\\b(" + findInput.getText() + ")\\b", replaceInput.getText()));
    }

    @FXML
    void toggleReplace() {
        if (container.getPrefHeight()<=47) {
            container.setPrefHeight(79);
            findBox.setTranslateY(0);
            replaceBox.setTranslateY(0);
        } else {
            container.setPrefHeight(41);
            findBox.setTranslateY(14);
            replaceBox.setTranslateY(42);
        }
        replaceBox.setVisible(!replaceBox.isVisible());
    }

    public void showReplaceBox() {
        container.setPrefHeight(79);
        findBox.setTranslateY(0);
        replaceBox.setTranslateY(0);
        replaceBox.setVisible(true);
    }
}