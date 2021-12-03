package sample.editor.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    private File selectedFile;
    private DropDown dropDown;
    private TreeItem<String> selectTreeItem;
    private TreeView<String> fileTree;
    private Stage window;
    private StyleClassedTextArea codeArea;
    private String themeFile;

    public void removeHighlightedTxt(ArrayList<ArrayList<Integer>> coordinateList, StyleClassedTextArea currCodeArea, AtomicInteger currWordIndex) {
        if (coordinateList.size()!=0) {
            for (ArrayList<Integer> arrayList : coordinateList) {
                currCodeArea.setStyleClass(arrayList.get(0), arrayList.get(1), "");
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
        currCodeArea.requestFollowCaret();
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

    public boolean isImgFile(File file) {
        try {
            String mimetype = Files.probeContentType(file.toPath());
            return mimetype != null && mimetype.split("/")[0].equals("image");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isBinaryFile(File file) {
        /*
        * This block of code is temporary.
        * I will delete this if clause when the bug in Universal Detector is fixed or
        * I got other way to detect binary file.
        * The bug is: Universal Detector is returning null when the given file is empty.
        */

        if (new FileIO().readFile(file.getAbsolutePath()).isEmpty()) {
            return false;
        }
        try {
            return UniversalDetector.detectCharset(file)==null;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getExtension(String fileName) {
        String [] arr = fileName.split("\\.");
        return arr[arr.length-1];
    }

    public void indent(StyleClassedTextArea area) {
        final Pattern whiteSpace = Pattern.compile( "^\\s+" );
        int caretPosition = area.getCaretPosition();
        int currentParagraph = area.getCurrentParagraph();
        Matcher m0 = whiteSpace.matcher( area.getParagraph( currentParagraph-1 ).getSegments().get( 0 ) );
        if ( m0.find() ) area.insertText( caretPosition, m0.group());
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
        fileTypes.put("iml", "IntelliJ IDEA Module");
        fileTypes.put("json", "JavaScript Object Notation");
        fileTypes.put("md", "MarkDown");
    }

    private boolean runCommand(String cmd, TextArea console) {
        AtomicBoolean err = new AtomicBoolean(false);
        console.clear();
        Task<Void> processTask = new Task<>() {
            @Override
            protected Void call() throws IOException {
                Process process = new ProcessBuilder().command("powershell.exe", "/c" ,cmd).redirectErrorStream(true).start();
                BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line=r.readLine())!=null) {
                    final String nextLine = line;
                    Platform.runLater(() -> console.appendText(nextLine + '\n'));
                }
                return null;
            }
        };

        processTask.setOnSucceeded(event -> console.appendText("Finished in [time]"));
        processTask.exceptionProperty().addListener((observableValue, throwable, t1) -> {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            processTask.getException().printStackTrace(pw);
            console.appendText(String.valueOf(sw));
            err.set(true);
        });
        Thread thread = new Thread(processTask);
        thread.setDaemon(true);
        thread.start();
        return err.get();
    }

    public void runFile(File file, TextArea console) {
        String ext = getExtension(file.getName());
        if (ext.equals("py")) {
            runCommand("cd " + file.getParent() +"; python.exe " + file.getAbsolutePath(), console);
        } else if (ext.equals("java")) {
            String path = file.getAbsolutePath();
            runCommand("cd " + file.getParent() +"; javac.exe "  + path + "; java.exe " +path, console);
        }
    }

    public DropDown createContextMenuForFileTree(File file, TreeItem<String> item, Stage window, StyleClassedTextArea codeArea, TreeView<String> fileTree, String themeFile) {
        this.codeArea = codeArea;
        this.fileTree = fileTree;
        this.themeFile = themeFile;
        this.dropDown = new DropDown(themeFile);
        this.selectTreeItem = item;
        this.selectedFile = file;
        this.window = window;

        if (file.isFile()) {
            DropDownItem revealInExplorer = new DropDownItem(this::revealInFileExplorerMet, "Reveal in File Explorer", themeFile);
            DropDownItem copyFilePath = new DropDownItem(this::copyFilePath, "Copy Path", themeFile);
            DropDownItem copyRelativeFilePath = new DropDownItem(this::copyRelativeFilePath, "Copy Relative Path", themeFile);
            DropDownItem separator = new DropDownItem();
            DropDownItem renameFile = new DropDownItem(this::renameFile, "Rename", themeFile);
            DropDownItem deleteFile = new DropDownItem(this::deleteFile, "Delete", themeFile);
            dropDown.addAllItems(revealInExplorer, copyFilePath, copyRelativeFilePath, separator, renameFile, deleteFile);
        } else if (file.isDirectory()) {
            DropDownItem newFile = new DropDownItem(this::newFile, "New File", themeFile);
            DropDownItem newFolder = new DropDownItem(this::newFolder, "New Folder", themeFile);
            DropDownItem separator1 = new DropDownItem();
            DropDownItem separator2 = new DropDownItem();
            DropDownItem revealInExplorer = new DropDownItem(this::revealInFileExplorerMet, "Reveal in File Explorer", themeFile);
            DropDownItem copyFilePath = new DropDownItem(this::copyFolderPath, "Copy Path", themeFile);
            DropDownItem copyRelativeFilePath = new DropDownItem(this::copyRelativeFolderPath, "Copy Relative Path", themeFile);
            DropDownItem renameFile = new DropDownItem(this::renameFile, "Rename", themeFile);
            DropDownItem deleteFile = new DropDownItem(this::deleteFile, "Delete", themeFile);
            dropDown.addAllItems(newFolder, newFile, separator1, revealInExplorer, copyFilePath, copyRelativeFilePath, separator2, renameFile, deleteFile);
        }
        return dropDown;
    }

    private void revealInFileExplorerMet(MouseEvent event) {
        try {
            Runtime.getRuntime().exec("explorer /select, " + selectedFile.getAbsolutePath());
//            Runtime.getRuntime().exec("open -R <file path>"); // for mac
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
        setContentInClipboard(selectedFile.getAbsolutePath());
        dropDown.hide();
    }

    private void copyRelativeFilePath(MouseEvent mouseEvent) {
        setContentInClipboard(selectedFile.getName());
        dropDown.hide();
    }

    private void copyFolderPath(MouseEvent mouseEvent) {
        setContentInClipboard(selectedFile.getAbsolutePath());
        dropDown.hide();
    }

    private void copyRelativeFolderPath(MouseEvent mouseEvent) {
        setContentInClipboard(selectedFile.getName());
        dropDown.hide();
    }

    private void deleteFile(MouseEvent mouseEvent) {
        try {
            if (selectedFile.isDirectory()) FileUtils.forceDelete(selectedFile);
            else if (selectedFile.isFile()) Files.deleteIfExists(Path.of(selectedFile.getAbsolutePath()));
            selectTreeItem.getParent().getChildren().remove(selectTreeItem);
            fileTree.refresh();
            fileTree.getSelectionModel().clearSelection();
        } catch (IOException e) {
            e.printStackTrace();
            /*
            * This alert will be replace in future to my custom alert box.
            */
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Alert");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
        dropDown.hide();
    }

    private void renameFile(MouseEvent mouseEvent) {
        CommandPalletDialogBox commandPalletDialogBox = new CommandPalletDialogBox(window, selectTreeItem, themeFile);
        commandPalletDialogBox.showRenameBox(window.getX()+(codeArea.getWidth()/2), window.getY()+70, selectedFile);
        dropDown.hide();
    }

    private void newFile(MouseEvent mouseEvent) {
        CommandPalletDialogBox commandPalletDialogBox = new CommandPalletDialogBox(window, selectTreeItem, themeFile);
        commandPalletDialogBox.showNewNameBox(window.getX()+(codeArea.getWidth()/2), window.getY()+70, selectedFile, true);
        dropDown.hide();
    }

    private void newFolder(MouseEvent mouseEvent) {
        CommandPalletDialogBox commandPalletDialogBox = new CommandPalletDialogBox(window, selectTreeItem, themeFile);
        commandPalletDialogBox.showNewNameBox(window.getX()+(codeArea.getWidth()/2), window.getY()+70, selectedFile, false);
        dropDown.hide();
    }
}
