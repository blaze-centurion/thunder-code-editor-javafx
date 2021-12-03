package sample.editor.controller;

import com.sun.source.tree.UsesTree;
import javafx.scene.control.Tab;
import javafx.stage.FileChooser;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.io.*;
import java.util.Properties;

public class FileIO {

    public void saveFile(String fileToSave, String contentToSave) {
        try {
            FileWriter fileWriter = new FileWriter(fileToSave);
            fileWriter.write(contentToSave);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public File saveFileAs(Tab tab, String contentToSave) {
        FileChooser fileChooser = createFileChooser();
        fileChooser.setInitialFileName("Untitled.txt");
        File file = fileChooser.showSaveDialog(null);
        if (file==null) return null;
        String fileToSave = file.getAbsolutePath();
        saveFile(fileToSave, contentToSave);
        tab.setId(fileToSave);
        tab.setText(file.getName());
        tab.getContent().setId("true");
        return file;
    }

    public void fileModified(Tab tab, StyleClassedTextArea codeArea) {
        if (tab.getId() == null) {
            if (codeArea.getText().length()!=0) {
                if (!tab.getStyleClass().contains("modified")) tab.getStyleClass().add("modified");
            } else tab.getStyleClass().remove("modified");
        } else {
            if (!isContentSame(tab.getId(), codeArea.getText())) {
                if (!tab.getStyleClass().contains("modified")) tab.getStyleClass().add("modified");
            } else tab.getStyleClass().remove("modified");
        }
    }

    public String readFile(String fileToRead) {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader bf = new BufferedReader(new FileReader(fileToRead));
            String txt;
            while ((txt = bf.readLine()) !=null) {
                sb.append(txt).append("\n");
            }
            if (sb.length()!=0) sb.deleteCharAt(sb.length()-1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public FileChooser createFileChooser() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All Files", "*.*"));
        fc.setInitialFileName("*.txt");
        return fc;
    }

    public boolean isContentSame(String fileForCheck, String textToCheck) {
        if (fileForCheck==null) return false;
        return readFile(fileForCheck).equals(textToCheck);
    }

    public void changeTheme(String theme) {
        try {
            File file = new File("settings.properties");
            if (!file.exists()) file.createNewFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            Properties settings = new Properties();
            settings.setProperty("theme", theme);
            settings.store(bw, null);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readTheme() {
        try {
            File file = new File("settings.properties");
            Properties settings = new Properties();
            BufferedReader br = new BufferedReader(new FileReader(file));
            settings.load(br);
            String theme = settings.getProperty("theme");
            br.close();
            if (theme.isEmpty()) return "../css/dracula.css";
            else return theme;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
