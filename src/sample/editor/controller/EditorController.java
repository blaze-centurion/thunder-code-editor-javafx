package sample.editor.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TabPane.TabDragPolicy;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.robot.Robot;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.NavigationActions;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.richtext.model.RichTextChange;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import sample.editor.Highlighter.JavaKeywordHighlighting;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditorController implements Initializable {
    @FXML
    private BorderPane borderPane;
    @FXML
    private SplitPane splitPane;
    @FXML
    private TabPane tabPane;
    @FXML
    private Label lineNoLabel, colNoLabel, selectTextLabel, tabSizeLabel, fileTypeLabel;

    private StyleClassedTextArea codeArea;
    private VirtualizedScrollPane virtualizedScrollPane;
    private TreeView<String> fileTree;
    private HashMap<String, String> fileAddresses;
    private HashMap<String, String> dirAddresses;
    private HashMap<String, String> fileTypes;
    private FileIO fileIO = new FileIO();
    private HashMap<String, String> fileOpened;
    private boolean isToCheckHis;
    private boolean isRightClick;
    private Utils utils = new Utils();
    private Stage mainWindow;
    private String currFolder = "";
    private Popup autoCompletionPopup;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        selectTextLabel.setVisible(false);
        autoCompletionPopup = new Popup();
        autoCompletionPopup.setAutoHide(true);
        autoCompletionPopup.setHideOnEscape(true);
        fileTree = new TreeView<>();
        fileAddresses = new HashMap<>();
        dirAddresses = new HashMap<>();
        fileTypes = new HashMap<>();
        fileOpened = new HashMap<>();
        splitPane.getItems().add(0, fileTree);
        splitPane.setDividerPositions(0.2);
        SplitPane.setResizableWithParent(fileTree, false);
        splitPane.setStyle("-fx-padding: 0;");
        utils.configureFileTypes(fileTypes);

//        fileTree.setCellFactory(stringTreeView -> new TreeCellWithMenu());

        fileTree.getSelectionModel().selectedItemProperty().addListener((observableValue, oldVal, newVal) -> {
            if (newVal == null) return;
            File file;
            String path = fileAddresses.get(newVal.getValue());
            file = new File(path != null ? path : dirAddresses.get(newVal.getValue()));

            if (isRightClick) {
                DropDown contextMenu = utils.createContextMenuForFileTree(file, fileAddresses, dirAddresses, newVal, mainWindow, getCurrCodeArea(), fileTree);
                Robot robot = new Robot();
                contextMenu.show(mainWindow, robot.getMouseX(), robot.getMouseY());

                isRightClick = false;
                return;
            } else if (file.isDirectory()) return;

            if (fileOpened.containsValue(file.getAbsolutePath())) return;
            Tab tab = createNewTabWithCodeArea(file, true);
            StyleClassedTextArea area = (StyleClassedTextArea) ((VirtualizedScrollPane) tab.getContent()).getContent();
            area.appendText(fileIO.readFile(file.getAbsolutePath()));
            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);
        });

        fileTree.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (event.isSecondaryButtonDown()) {
                isRightClick = true;
            }
        });

    }

    public void setIsToCheckHis(boolean b) {
        isToCheckHis = b;
    }

    public void setMainWindow(Stage mainWindow) {
        this.mainWindow = mainWindow;
        this.mainWindow.setOnCloseRequest(windowEvent -> closeWindow());
        configureEditor();
    }


    private void configureEditor() {
        if (isToCheckHis) checkHisFiles();
        tabPane.setTabDragPolicy(TabDragPolicy.REORDER);
        tabPane.setTabClosingPolicy(TabClosingPolicy.ALL_TABS);
        tabPane.getSelectionModel().selectedItemProperty().addListener((observableValue, tab, t1) -> setFileType(t1 != null ? t1.getText() : ""));
        if (currFolder.length()!=0) {
            openFolderWithoutChooser();
        }
    }

    private void checkHisFiles() {
        /*
        * Read history.json file, and open previously opened folder and files (it they exist).
        */

        JSONParser jsonParser = new JSONParser();
        try (Reader reader = new FileReader("history.json")) {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
            currFolder = (String) jsonObject.get("opened folder");
            JSONArray opened_file = (JSONArray) jsonObject.get("opened file");

            for (Object obj: opened_file) {
                JSONObject fileObj = (JSONObject) obj;
                String filePath = (String) fileObj.get("filePath");
                File file = new File(filePath != null ? filePath : "");
                if (!file.exists() && filePath!=null) continue;
                String fileName = (String) fileObj.get("fileName");
                String opened = (String) fileObj.get("opened");
                String data = (String) fileObj.get("data");
                boolean saved = (boolean) fileObj.get("saved");
                tabPane.getTabs().add(createNewTabForHis(fileName, filePath, opened, filePath == null ? data : fileIO.readFile(filePath), saved));
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private StyleClassedTextArea getCurrCodeArea() {
        return (StyleClassedTextArea) ((VirtualizedScrollPane) tabPane.getSelectionModel().getSelectedItem().getContent()).getContent();
    }

    private void autoIndent() {
        final Pattern whiteSpace = Pattern.compile( "^\\s+" );
        codeArea.addEventHandler( KeyEvent.KEY_PRESSED, KE ->
        {
            if ( KE.getCode() == KeyCode.ENTER ) {
                int caretPosition = codeArea.getCaretPosition();
                int currentParagraph = codeArea.getCurrentParagraph();
                Matcher m0 = whiteSpace.matcher( codeArea.getParagraph( currentParagraph-1 ).getSegments().get( 0 ) );
                if ( m0.find() ) codeArea.insertText( caretPosition, m0.group());
            }
        });
    }

    private void autoCompleteBrackets(StyleClassedTextArea area) {
        area.setOnKeyTyped(keyEvent -> {
            String str = keyEvent.getCharacter().equals("{") ? "}" :
                    keyEvent.getCharacter().equals("[") ? "]" :
                            keyEvent.getCharacter().equals("(") ? ")" :
                                    keyEvent.getCharacter().equals("\"") ? "\"" :
                                            keyEvent.getCharacter().equals("'") ? "'" : null;
            if (str==null) return;
            codeArea.insertText(codeArea.getCaretPosition(), str);
            codeArea.getCaretSelectionBind().moveTo(codeArea.getCaretPosition()-1);
        });
    }

    private void autoCompletion(String toSearch, StyleClassedTextArea currCodeArea) {
        System.out.println(toSearch);
        if (toSearch.length()==0) return;
        List<String> words = List.of(
                "abstract", "assert", "boolean", "break", "byte",
                "case", "catch", "char", "class", "const",
                "continue", "default", "do", "double", "else",
                "enum", "extends", "final", "finally", "float",
                "for", "goto", "if", "implements", "import",
                "instanceof", "int", "interface", "long", "native",
                "new", "package", "private", "protected", "public",
                "return", "short", "static", "strictfp", "super",
                "switch", "synchronized", "this", "throw", "throws",
                "transient", "try", "void", "volatile", "while",
                "true", "false", "String"
        );
        AutoCompletion completion = new AutoCompletion(words);
        showCompletion(completion.suggest(toSearch), currCodeArea, toSearch.length());
    }

    public void insertCompletion(String s, StyleClassedTextArea currCodeArea, int wordLen) {
        currCodeArea.replaceText(currCodeArea.getCaretPosition()-wordLen, currCodeArea.getCaretPosition(), s);
        autoCompletionPopup.hide();
    }

    private void showCompletion(List<String> list, StyleClassedTextArea currCodeArea, int wordLen) {
        if (autoCompletionPopup!=null) autoCompletionPopup.hide();
        if (list.size()>0) {
            ListView<String> listView = new ListView<>();
            listView.getItems().addAll(list);

            // set listeners
            listView.setOnKeyPressed(keyEvent -> {
                if (keyEvent.getCode().equals(KeyCode.ENTER)) insertCompletion(listView.getSelectionModel().getSelectedItem(), currCodeArea, wordLen);
            });
            listView.setOnMouseClicked(event -> {
                if (listView.getSelectionModel().getSelectedItem()!=null) insertCompletion(listView.getSelectionModel().getSelectedItem(), currCodeArea, wordLen);
            });

            // replacing old data with new one.
            autoCompletionPopup.getContent().clear();
            autoCompletionPopup.getContent().add(listView);
            autoCompletionPopup.show(mainWindow, currCodeArea.getCaretBounds().get().getMaxX(), currCodeArea.getCaretBounds().get().getMaxY());
        } else {
            if (autoCompletionPopup !=null) {
                autoCompletionPopup.getContent().clear();
                autoCompletionPopup.hide();
            }
        }
    }

    private String getCurrWord(StyleClassedTextArea currCodeArea) {
        StringBuilder curr = new StringBuilder(); // this contain the word in reverse order.
        StringBuilder currFinal = new StringBuilder(); // this contain word in correct order.

        Set<Character> a = new HashSet<>();
        a.add('(');
        a.add(')');
        a.add('[');
        a.add(']');
        a.add('{');
        a.add('}');

        for (int i = currCodeArea.getAnchor(); i > 0; i--) {
            if (currCodeArea.getText().charAt(i) == '\n' || currCodeArea.getText().charAt(i) == ' ' || a.contains(currCodeArea.getText().charAt(i))) {
                break;
            } else {
                curr.append(currCodeArea.getText().charAt(i));
            }
        }

        for (int i = curr.length()-1; i >= 0; i--) {
            currFinal.append(curr.charAt(i));
        }

        return currFinal.toString();
    }

    private void configureAllShortcuts() {
        final KeyCombination copyComb = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN);
        codeArea.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (copyComb.match(event)) {
                copy();
            }
        });

    }

    private void createCodeAreaWithLineNo() {
        /*
        * Create new textArea without any preloaded data.
        */
        codeArea = new StyleClassedTextArea();
        virtualizedScrollPane = new VirtualizedScrollPane(codeArea);
        IntFunction<Node> noFactory = LineNumberFactory.get(codeArea);
        IntFunction<Node> graphicFactory = line -> {
            HBox lineBox = new HBox(noFactory.apply(line));
            lineBox.setStyle("-fx-background-color: #282A36;");
            lineBox.setAlignment(Pos.CENTER_LEFT);
            return lineBox;
        };

        autoIndent();  // auto-indent: insert previous line's indents on enter
        autoCompleteBrackets(codeArea);
        configureAllShortcuts();
//        codeArea.richChanges().subscribe(x -> autoCompletion(getCurrWord(codeArea), codeArea));

        setSelectionListener(codeArea);
        codeArea.caretPositionProperty().addListener((observableValue, oldPos, newPos) -> updateCaret());
        JavaKeywordHighlighting javaKeywordHighlighting = new JavaKeywordHighlighting();
        javaKeywordHighlighting.start(codeArea);
        codeArea.setParagraphGraphicFactory(graphicFactory);
        codeArea.requestFocus();
    }

    private void createCodeAreaWithLineNo(String data) {
        /*
        * Create a textArea with preloaded data. This is mainly for opening the last opened files.
        */
        codeArea = new StyleClassedTextArea();
        codeArea.appendText(data);
        virtualizedScrollPane = new VirtualizedScrollPane(codeArea);
        IntFunction<Node> noFactory = LineNumberFactory.get(codeArea);
        IntFunction<Node> graphicFactory = line -> {
            HBox lineBox = new HBox(noFactory.apply(line));
            lineBox.setStyle("-fx-background-color: #282A36; -fx-padding: 0 0 0 15px;");
            lineBox.setAlignment(Pos.CENTER_LEFT);
            return lineBox;
        };

        // auto-indent: insert previous line's indents on enter
        autoIndent();
        autoCompleteBrackets(codeArea);
        configureAllShortcuts();
//        codeArea.richChanges().subscribe(x -> autoCompletion(getCurrWord(codeArea), codeArea));

        codeArea.caretPositionProperty().addListener((observableValue, oldPos, newPos) -> updateCaret());
        setSelectionListener(codeArea);
        JavaKeywordHighlighting javaKeywordHighlighting = new JavaKeywordHighlighting();
        javaKeywordHighlighting.start(codeArea);
        codeArea.setParagraphGraphicFactory(graphicFactory);
    }

    private void updateCaret() {
        lineNoLabel.setText(String.valueOf(getCurrCodeArea().getCaretSelectionBind().getParagraphIndex()+1));
        colNoLabel.setText(String.valueOf(getCurrCodeArea().getCaretSelectionBind().getColumnPosition()+1));
    }

    private void setSelectionListener(StyleClassedTextArea currCodeArea) {
        currCodeArea.selectedTextProperty().addListener((observableValue, s, t1) -> {
            selectTextLabel.setVisible(true);
            selectTextLabel.setText("(" + t1.length() +" selected)");
            if (t1.length()==0) selectTextLabel.setVisible(false);
        });
    }

    private Tab createNewTabWithCodeArea(boolean isFileOpen) {
        /*
        * Create new tab with new file.
        */

        Tab tab = new Tab();
        createCodeAreaWithLineNo();
        tab.setText("Untitled 1.txt");
        tab.setContent(virtualizedScrollPane);
        virtualizedScrollPane.setId(String.valueOf(isFileOpen));
        return tab;
    }

    private Tab createNewTabWithCodeArea(File file, boolean isFileOpen) {
        /*
        * Create new tab with existing file.
        */

        String fileName = file.getName();
        String filePath = file.getAbsolutePath();
        Tab tab = new Tab();
        createCodeAreaWithLineNo();
        tab.setText(fileName);
        setFileType(fileName);
        virtualizedScrollPane.setId(String.valueOf(isFileOpen));
        tab.setContent(virtualizedScrollPane);
        tab.setId(filePath);
        fileOpened.put(fileName, filePath);
        tab.setOnCloseRequest(event -> fileOpened.remove(fileName, filePath));
        return tab;
    }

    private Tab createNewTabForHis(String fileName, String filePath, String isFileOpen, String data, boolean saved) {
        /*
        * Create new tab for the recent closed file.
        */

        Tab tab = new Tab();
        createCodeAreaWithLineNo(data);
        tab.setText(fileName);
        virtualizedScrollPane.setId(isFileOpen);
        tab.setContent(virtualizedScrollPane);
        tab.setId(filePath);
        setFileType(fileName);
        fileOpened.put(fileName, filePath);
        tab.setOnCloseRequest(event -> fileOpened.remove(fileName, filePath));
        return tab;
    }

    private void setFileType(String s) {
        String f = fileTypes.get(utils.getExtension(s));
        fileTypeLabel.setText(f!=null ? f : "Plain Text");
    }

    private void newFile(File file, boolean isFileOpen) {
        Tab tab = createNewTabWithCodeArea(file, isFileOpen);
        setFileType(file.getName());
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
    }

    private TreeItem<String>[] getDirContent(File dir) {
        int dirLen = Objects.requireNonNull(dir.listFiles()).length;
        TreeItem[] RootNodes;

        if (dirLen==0) {
            RootNodes = new TreeItem[1];
            RootNodes[0] = new TreeItem<>("No Files");
        } else {
            RootNodes = new TreeItem[dirLen];
            int i = 0;
            for (File file : Objects.requireNonNull(dir.listFiles())) {
                if (file.isDirectory()) {
                    RootNodes[i] = new TreeItem<>(file.getName());
                    RootNodes[i].getChildren().addAll(getDirContent(file));
                    dirAddresses.put(file.getName(), file.getAbsolutePath());
                } else {
                    RootNodes[i] = new TreeItem<>();
                    RootNodes[i].setValue(file.getName());
                    fileAddresses.put(file.getName(), file.getAbsolutePath());
                }
                i++;
            }
        }

        return RootNodes;
    }

    private void setFileOpened(File file, Tab tab, String oldKey) {
        fileOpened.remove(oldKey);
        fileOpened.put(file.getName(), file.getAbsolutePath());
        tab.setOnCloseRequest(event -> fileOpened.remove(file.getName(), file.getAbsolutePath()));
    }

    @FXML
    void closeEditor() {
        for (int i=0; i<tabPane.getTabs().size(); i++) {
            Tab tab = tabPane.getTabs().get(i);
            StyleClassedTextArea editor = getCurrCodeArea();
            if (!fileIO.isContentSame(tab.getId(), editor.getText())) {
                if (!askAndSaveFile(tab)) return;
            }
        }
        tabPane.getTabs().clear();
    }

    @FXML
    void closeFolder() {
        fileTree.setRoot(null);
    }

    @FXML
    void closeWindow() {
        JSONObject obj = new JSONObject();  // main obj in which all data will be stored.
        JSONArray list = new JSONArray();  // it contains the data of opened file.
        JSONObject fileObj;  // contains data of single opened file.
        boolean saved;

        for (Tab tab : tabPane.getTabs()) {
            StyleClassedTextArea currCodeArea = (StyleClassedTextArea) ((VirtualizedScrollPane) tab.getContent()).getContent();
            saved = fileIO.isContentSame(tab.getId(), currCodeArea.getText());
            fileObj = new JSONObject();
            fileObj.put("fileName", tab.getText());
            fileObj.put("filePath", tab.getId());
            fileObj.put("data", currCodeArea.getText());
            fileObj.put("saved", saved);
            fileObj.put("opened", (tab.getContent()).getId());
            list.add(fileObj);
        }
        obj.put("opened folder", currFolder);
        obj.put("opened file", list);

        try (FileWriter file = new FileWriter("history.json")) {
            file.write(obj.toJSONString());  // writing in history.json.
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void copy() {
        StyleClassedTextArea c = getCurrCodeArea();
        if (c.getSelectedText().length()==0) {
            c.selectLine();
            c.copy();
            return;
        }
        c.copy();

    }

    @FXML
    void copyLineDown() {
        StyleClassedTextArea currCodeArea = getCurrCodeArea();
        IndexRange range = currCodeArea.getCaretSelectionBind().getRange();
        int oldStart = range.getStart();
        if (currCodeArea.getSelectedText().length()==0) {
            currCodeArea.selectLine();
            String str = currCodeArea.getSelectedText();
            currCodeArea.insertText(range.getEnd(), "\n"+str);
            return;
        }

        currCodeArea.getCaretSelectionBind().moveTo(range.getEnd());
        currCodeArea.selectLine();
        range = currCodeArea.getCaretSelectionBind().getRange();
        int end = range.getEnd();

        currCodeArea.getCaretSelectionBind().moveTo(oldStart);
        currCodeArea.selectLine();
        range = currCodeArea.getCaretSelectionBind().getRange();
        int start = range.getStart();
        currCodeArea.getCaretSelectionBind().selectRange(start, end);
        currCodeArea.insertText(end, '\n' + codeArea.getSelectedText());
    }

    @FXML
    void copyLineUp() {
        StyleClassedTextArea currCodeArea = getCurrCodeArea();
        IndexRange range = currCodeArea.getCaretSelectionBind().getRange();
        int oldStart = range.getStart();
        if (currCodeArea.getSelectedText().length()==0) {
            currCodeArea.selectLine();
            String str = currCodeArea.getSelectedText();
            currCodeArea.insertText(range.getEnd(), "\n"+str);
            return;
        }

        currCodeArea.getCaretSelectionBind().moveTo(range.getEnd());
        currCodeArea.selectLine();
        range = currCodeArea.getCaretSelectionBind().getRange();
        int end = range.getEnd();

        currCodeArea.getCaretSelectionBind().moveTo(oldStart);
        currCodeArea.selectLine();
        range = currCodeArea.getCaretSelectionBind().getRange();
        int start = range.getStart();
        currCodeArea.getCaretSelectionBind().selectRange(start, end);
        currCodeArea.insertText(start-1, '\n' + codeArea.getSelectedText());
    }

    @FXML
    void cut() {
        StyleClassedTextArea c = getCurrCodeArea();
        if (c.getSelectedText().length()==0) c.selectLine();
        c.cut();
    }

    @FXML
    void duplicateSelection() {
        StyleClassedTextArea c = getCurrCodeArea();
        if (c.getSelectedText().length()==0) return;
        c.insertText(c.getCaretPosition(), c.getSelectedText());
    }

    @FXML
    void exit() {
        closeWindow();
        System.exit(-1);
    }

    @FXML
    void find() {
        try {
            StyleClassedTextArea currCodeArea = getCurrCodeArea();
            Popup popup = new Popup();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/FindAndReplaceDialog.fxml"));
            HBox container = loader.load();
            FindAndReplaceDialogController findAndReplaceDialogController = loader.getController();
            findAndReplaceDialogController.setCodeArea(getCurrCodeArea());
            findAndReplaceDialogController.setPopup(popup);
            popup.getContent().add(container);
            Bounds bounds = currCodeArea.localToScene(currCodeArea.getBoundsInLocal());
            popup.show(mainWindow, bounds.getMaxX(), bounds.getMinY());
            popup.setAutoHide(false);
            popup.setHideOnEscape(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void moveLineDown() {
    }

    @FXML
    void moveLineUp() {

    }

    @FXML
    void newFile() {
        Tab tab = createNewTabWithCodeArea(false);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
    }

    @FXML
    void newWindow() {
        Stage stage = new Stage();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/Editor.fxml"));
            Parent root = loader.load();
            EditorController editorController = loader.getController();
            editorController.setIsToCheckHis(false);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openFile() {
        FileChooser fileChooser = fileIO.createFileChooser();
        File file = fileChooser.showOpenDialog(null);
        if (file==null) return;
        if (Files.isExecutable(Path.of(file.getAbsolutePath()))) return;
        if (fileOpened.containsValue(file.getAbsolutePath())) return;
        newFile(file, true);
        codeArea.appendText(fileIO.readFile(file.getAbsolutePath()));
    }

    @FXML
    void openFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select a folder.");
        File dir = directoryChooser.showDialog(null);
        if (dir==null) return;
        currFolder = dir.getAbsolutePath();
        TreeItem<String> rootItem = new TreeItem<>(dir.getName());
        fileTree.setRoot(rootItem);
        rootItem.getChildren().addAll(getDirContent(dir));
    }

    void openFolderWithoutChooser() {
        File dir = new File(currFolder);
        if (!dir.exists()) return;
        TreeItem<String> rootItem = new TreeItem<>(dir.getName());
        fileTree.setRoot(rootItem);
        rootItem.getChildren().addAll(getDirContent(dir));
    }

    @FXML
    void paste() {
        getCurrCodeArea().paste();
    }

    @FXML
    void redo() {
        getCurrCodeArea().redo();
    }

    @FXML
    void replace() {

    }

    @FXML
    void saveAsFile() {
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        String codeAreaText = getCurrCodeArea().getText();
        File file = fileIO.saveFileAs(tab, codeAreaText);
        setFileOpened(file, tab, tab.getId());
    }

    @FXML
    void saveFile() {
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        VirtualizedScrollPane content = (VirtualizedScrollPane) tab.getContent();
        String codeAreaText = getCurrCodeArea().getText();
        if (content.getId().equals("true")) {
            new FileIO().saveFile(tab.getId(), codeAreaText);
        } else {
            File file = fileIO.saveFileAs(tab, codeAreaText);
            setFileOpened(file, tab, tab.getId());
        }
    }

    private void saveFile(Tab tab) {
        VirtualizedScrollPane content = (VirtualizedScrollPane) tab.getContent();
        String codeAreaText = getCurrCodeArea().getText();
        if (content.getId().equals("true")) {
            new FileIO().saveFile(tab.getId(), codeAreaText);
        } else {
            File file = fileIO.saveFileAs(tab, codeAreaText);
            if (file!=null) setFileOpened(file, tab, tab.getId());
        }
    }

    private boolean askAndSaveFile(Tab tab) {
        DialogBox dialogBox = new DialogBox("Text Editor Name", "Do you want to save the changes you made to " + tab.getText() + " ?", "Your changes will be lost if don't save them.");
        dialogBox.showAndWait();

        if (dialogBox.getBtnCode() == 0) return false;
        if (dialogBox.getBtnCode() == 1) saveFile(tab);
        return true;
    }

    @FXML
    void selectAll() {
        getCurrCodeArea().selectAll();
    }

    @FXML
    void undo() {
        getCurrCodeArea().undo();
    }
}
