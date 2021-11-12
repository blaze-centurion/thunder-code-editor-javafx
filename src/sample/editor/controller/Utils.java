package sample.editor.controller;

import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    private HashMap<String, String> fileAddresses;
    private HashMap<String, String> dirAddresses;
    private File selectedFile;
    private DropDown dropDown;
    private TreeItem<String> selectTreeItem;
    private TreeView<String> fileTree;
    private Stage window;
    private StyleClassedTextArea codeArea;

    public void removeHighlightedTxt(ArrayList<ArrayList<Integer>> coordinateList, StyleClassedTextArea currCodeArea, AtomicInteger currWordIndex) {
        if (coordinateList.size()!=0) {
            for (ArrayList<Integer> arrayList : coordinateList) {
                currCodeArea.setStyleClass(arrayList.get(0), arrayList.get(1), "removeFind");
            }
            coordinateList.clear();
            currWordIndex.set(0);
        }
    }

    public void removeHighlightedTxtInRange(int start, int end, StyleClassedTextArea currCodeArea) {
        currCodeArea.setStyleClass(start, end, "find");
    }

    public void highlightText(TextField textField, ArrayList<ArrayList<Integer>> coordinateList, AtomicInteger currWordIndex, StyleClassedTextArea currCodeArea) {
        removeHighlightedTxt(coordinateList, currCodeArea, currWordIndex);
        Pattern pattern = Pattern.compile("\\b("+textField.getText()+")\\b");
        Matcher matcher = pattern.matcher(currCodeArea.getText());
        while (matcher.find()) {
            currCodeArea.setStyleClass(matcher.start(), matcher.end(), "find");
            coordinateList.add(new ArrayList<>(Arrays.asList(matcher.start(), matcher.end())));
        }
        if (coordinateList.size()!=0) currCodeArea.getCaretSelectionBind().moveTo(coordinateList.get(0).get(0));
    }

    public void gotoNextWord(ArrayList<ArrayList<Integer>> coordinateList, AtomicInteger currWordIndex, StyleClassedTextArea codeArea) {
        if (currWordIndex.get() >= (coordinateList.size()-1) && coordinateList.size()!=0) return;
        currWordIndex.incrementAndGet();
        int index = currWordIndex.get();
        codeArea.getCaretSelectionBind().moveTo(coordinateList.get(currWordIndex.get()).get(0));
        codeArea.setStyleClass(coordinateList.get(index).get(0), coordinateList.get(index).get(1), "findActive");
        removeHighlightedTxtInRange(coordinateList.get(index-1).get(0), coordinateList.get(index-1).get(1), codeArea);
    }

    public void gotoPrevWord(ArrayList<ArrayList<Integer>> coordinateList, AtomicInteger currWordIndex, StyleClassedTextArea codeArea) {
        if (currWordIndex.get() <= 0 && coordinateList.size()!=0) return;
        currWordIndex.decrementAndGet();
        int index = currWordIndex.get();
        codeArea.setStyleClass(coordinateList.get(index).get(0), coordinateList.get(index).get(1), "findActive");
        codeArea.getCaretSelectionBind().moveTo(coordinateList.get(currWordIndex.get()).get(0));
        removeHighlightedTxtInRange(coordinateList.get(index+1).get(0), coordinateList.get(index+1).get(1), codeArea);
    }

    public String getExtension(String fileName) {
        String [] arr = fileName.split("\\.");
        return arr[arr.length-1];
    }

    public void configureFileTypes(HashMap<String, String> fileTypes) {
        fileTypes.put("txt", "Plain Text");
        fileTypes.put("java", "Java");
        fileTypes.put("py", "Python");
        fileTypes.put("c", "C");
        fileTypes.put("cpp", "C++");
        fileTypes.put("cs", "C Sharp");
        fileTypes.put("html", "Hypertext Markup Language");
        fileTypes.put("htm", "Hypertext Markup Language");
        fileTypes.put("css", "Cascading Style Sheets");
        fileTypes.put("xml", "Extensible Markup Language");
        fileTypes.put("fxml", "FX Markup Language");
        fileTypes.put("js", "Java Script");
        fileTypes.put("ts", "Type Script");
        fileTypes.put("iml", "International Microgravity Laboratory");
        fileTypes.put("json", "JavaScript Object Notation");
        fileTypes.put("md", "MarkDown");
    }

    public DropDown createContextMenuForFileTree(File file, HashMap<String, String> fileAddresses, HashMap<String, String> dirAddresses, TreeItem<String> item, Stage window, StyleClassedTextArea codeArea, TreeView<String> fileTree) {
        this.codeArea = codeArea;
        this.fileTree = fileTree;
        this.dropDown = new DropDown();
        this.selectTreeItem = item;
        this.fileAddresses = fileAddresses;
        this.dirAddresses = dirAddresses;
        this.selectedFile = file;
        this.window = window;

        if (file.isFile()) {
            DropDownItem revealInExplorer = new DropDownItem(this::revealInFileExplorerMet, "Reveal in File Explorer");
            DropDownItem copyFilePath = new DropDownItem(this::copyFilePath, "Copy Path");
            DropDownItem copyRelativeFilePath = new DropDownItem(this::copyRelativeFilePath, "Copy Relative Path");
            DropDownItem separator = new DropDownItem();
            DropDownItem renameFile = new DropDownItem(this::renameFile, "Rename");
            DropDownItem deleteFile = new DropDownItem(this::deleteFile, "Delete");
            dropDown.addAllItems(revealInExplorer, copyFilePath, copyRelativeFilePath, separator, renameFile, deleteFile);
        } else if (file.isDirectory()) {
            DropDownItem newFile = new DropDownItem(this::newFile, "New File");
            DropDownItem newFolder = new DropDownItem(this::newFolder, "New Folder");
            DropDownItem separator1 = new DropDownItem();
            DropDownItem separator2 = new DropDownItem();
            DropDownItem revealInExplorer = new DropDownItem(this::revealInFileExplorerMet, "Reveal in File Explorer");
            DropDownItem copyFilePath = new DropDownItem(this::copyFolderPath, "Copy Path");
            DropDownItem copyRelativeFilePath = new DropDownItem(this::copyRelativeFolderPath, "Copy Relative Path");
            DropDownItem renameFile = new DropDownItem(this::renameFile, "Rename");
            DropDownItem deleteFile = new DropDownItem(this::deleteFile, "Delete");
            dropDown.addAllItems(newFolder, newFile, separator1, revealInExplorer, copyFilePath, copyRelativeFilePath, separator2, renameFile, deleteFile);
        }
        return dropDown;
    }

    private void revealInFileExplorerMet(MouseEvent event) {
        try {
            Runtime.getRuntime().exec("explorer /select, " + selectedFile.getAbsolutePath());
            dropDown.hide();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setContentInClipboard(String txt) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(txt);
        clipboard.setContent(clipboardContent);
    }

    private void copyFilePath(MouseEvent mouseEvent) {
        setContentInClipboard(fileAddresses.get(selectedFile.getName()));
        dropDown.hide();
    }

    private void copyRelativeFilePath(MouseEvent mouseEvent) {
        setContentInClipboard(selectedFile.getName());
        dropDown.hide();
    }

    private void copyFolderPath(MouseEvent mouseEvent) {
        setContentInClipboard(dirAddresses.get(selectedFile.getName()));
        dropDown.hide();
    }

    private void copyRelativeFolderPath(MouseEvent mouseEvent) {
        setContentInClipboard(selectedFile.getName());
        dropDown.hide();
    }

    private void deleteFile(MouseEvent mouseEvent) {
        try {
            if (selectedFile.isDirectory()) {
                FileUtils.deleteDirectory(selectedFile);
                dirAddresses.remove(selectTreeItem.getValue());
            }
            else if (selectedFile.isFile()){
                Files.deleteIfExists(Path.of(selectedFile.getAbsolutePath()));
                fileAddresses.remove(selectTreeItem.getValue());
            }
            fileTree.getRoot().getChildren().remove(selectTreeItem);
            fileTree.refresh();
            fileTree.getSelectionModel().clearSelection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        dropDown.hide();
    }

    private void renameFile(MouseEvent mouseEvent) {
        CommandPalletDialogBox commandPalletDialogBox = new CommandPalletDialogBox(window, selectTreeItem, fileAddresses, dirAddresses);
        commandPalletDialogBox.showRenameBox(window.getX()+(codeArea.getWidth()/2), window.getY()+70, selectedFile);
        dropDown.hide();
    }

    private void newFile(MouseEvent mouseEvent) {
        CommandPalletDialogBox commandPalletDialogBox = new CommandPalletDialogBox(window, selectTreeItem, fileAddresses, dirAddresses);
        commandPalletDialogBox.showNewNameBox(window.getX()+(codeArea.getWidth()/2), window.getY()+70, selectedFile, true);
        dropDown.hide();
    }

    private void newFolder(MouseEvent mouseEvent) {
        CommandPalletDialogBox commandPalletDialogBox = new CommandPalletDialogBox(window, selectTreeItem, fileAddresses, dirAddresses);
        commandPalletDialogBox.showNewNameBox(window.getX()+(codeArea.getWidth()/2), window.getY()+70, selectedFile, false);
        dropDown.hide();
    }
}
